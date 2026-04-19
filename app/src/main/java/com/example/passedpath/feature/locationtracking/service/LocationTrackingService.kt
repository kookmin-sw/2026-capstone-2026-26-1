package com.example.passedpath.feature.locationtracking.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.passedpath.app.appContainer
import com.example.passedpath.debug.TrackingDiagnosticsLogger
import com.example.passedpath.feature.locationtracking.domain.policy.LocationRequestPolicy
import com.example.passedpath.feature.locationtracking.domain.policy.TrackingDateKeyResolver
import com.example.passedpath.feature.locationtracking.domain.policy.LocationUploadPolicy
import com.example.passedpath.feature.locationtracking.domain.tracker.LocationTrackingSession
import com.example.passedpath.feature.locationtracking.data.manager.LocationTrackingServiceStateWriter
import com.example.passedpath.feature.locationtracking.presentation.notification.TrackingNotificationFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LocationTrackingService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val uploadMutex = Mutex()

    private lateinit var notificationFactory: TrackingNotificationFactory
    private lateinit var dateKeyResolver: TrackingDateKeyResolver
    private lateinit var serviceStateWriter: LocationTrackingServiceStateWriter
    private lateinit var diagnosticsLogger: TrackingDiagnosticsLogger
    private var trackingSession: LocationTrackingSession? = null
    private var periodicUploadJob: Job? = null
    private var preBoundaryUploadJob: Job? = null
    private var lastLocationCallbackAtEpochMillis: Long? = null

    override fun onCreate() {
        super.onCreate()
        notificationFactory = TrackingNotificationFactory(this)
        dateKeyResolver = applicationContext.appContainer.trackingDateKeyResolver
        serviceStateWriter = applicationContext.appContainer.locationTrackingServiceStateWriter
        diagnosticsLogger = applicationContext.appContainer.trackingDiagnosticsLogger
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopTrackingAndSelf()
            ACTION_START, null -> startTrackingIfNeeded()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        stopTracking()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTrackingIfNeeded() {
        if (trackingSession != null) return

        Log.i(TAG, "Starting location tracking service")
        serviceScope.launch {
            diagnosticsLogger.log(
                category = TrackingDiagnosticsLogger.CATEGORY_SERVICE,
                message = "start_tracking_service"
            )
        }
        notificationFactory.ensureTrackingChannel()
        startForeground(
            TrackingNotificationFactory.NOTIFICATION_ID,
            notificationFactory.createTrackingNotification()
        )

        val appContainer = applicationContext.appContainer
        serviceScope.launch {
            appContainer.cleanupTrackingLocalDataUseCase()
        }
        startPeriodicUploadLoop()
        startPreBoundaryUploadLoop()
        trackingSession = appContainer.trackingLocationTracker.startLocationUpdates { trackedLocation ->
            serviceScope.launch {
                val dateKey = dateKeyResolver.resolveDateKey(trackedLocation.recordedAtEpochMillis)
                lastLocationCallbackAtEpochMillis = System.currentTimeMillis()
                diagnosticsLogger.logLocationCallback(dateKey, trackedLocation)
                appContainer.locationTrackingRepository.saveRawLocation(trackedLocation)
                Log.d(TAG, "Saved location for dateKey=$dateKey recordedAt=${trackedLocation.recordedAtEpochMillis}")

                val pendingCount = appContainer.locationTrackingRepository
                    .getPendingUploadLocationCount(dateKey)
                Log.d(TAG, "Pending upload count for dateKey=$dateKey is $pendingCount")
                if (pendingCount >= LocationUploadPolicy.BATCH_SIZE) {
                    val didUpload = uploadPendingPoints(dateKey)
                    if (didUpload) {
                        Log.i(TAG, "Immediate upload succeeded for dateKey=$dateKey after reaching batch size")
                        resetPeriodicUploadLoop()
                    }
                }
            }
        }
        serviceStateWriter.update(isTracking = true)
    }

    private fun stopTrackingAndSelf() {
        flushPendingPointsOnStop()
        stopTracking()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopTracking() {
        trackingSession?.stop()
        trackingSession = null
        periodicUploadJob?.cancel()
        periodicUploadJob = null
        preBoundaryUploadJob?.cancel()
        preBoundaryUploadJob = null
        serviceStateWriter.update(isTracking = false)
        serviceScope.launch {
            diagnosticsLogger.log(
                category = TrackingDiagnosticsLogger.CATEGORY_SERVICE,
                message = "stop_tracking_service"
            )
        }
        Log.i(TAG, "Stopped location tracking service")
    }

    private fun startPeriodicUploadLoop() {
        periodicUploadJob?.cancel()
        periodicUploadJob = serviceScope.launch {
            while (true) {
                delay(LocationUploadPolicy.UPLOAD_INTERVAL_MS)
                val currentDateKey = dateKeyResolver.resolveCurrentDateKey()
                val previousDateKey = dateKeyResolver.resolvePreviousDateKey()
                Log.d(TAG, "Periodic upload tick currentDateKey=$currentDateKey previousDateKey=$previousDateKey")
                val lastCallbackAtEpochMillis = lastLocationCallbackAtEpochMillis
                if (lastCallbackAtEpochMillis == null) {
                    diagnosticsLogger.log(
                        category = TrackingDiagnosticsLogger.CATEGORY_CALLBACK,
                        message = "gap_no_callback_since_service_start",
                        dateKey = currentDateKey
                    )
                } else {
                    val silenceMillis = System.currentTimeMillis() - lastCallbackAtEpochMillis
                    if (silenceMillis >= LocationRequestPolicy.CALLBACK_SILENCE_THRESHOLD_MS) {
                        diagnosticsLogger.log(
                            category = TrackingDiagnosticsLogger.CATEGORY_CALLBACK,
                            message = "gap_since_last_callback_ms=$silenceMillis",
                            dateKey = currentDateKey
                        )
                    }
                }

                val didUploadPrevious = uploadPendingPoints(previousDateKey)
                val didUploadCurrent = uploadPendingPoints(currentDateKey)
                if (didUploadPrevious || didUploadCurrent) {
                    Log.i(TAG, "Periodic upload succeeded previous=$didUploadPrevious current=$didUploadCurrent")
                    resetPeriodicUploadLoop()
                    return@launch
                }
            }
        }
    }

    private fun startPreBoundaryUploadLoop() {
        preBoundaryUploadJob?.cancel()
        preBoundaryUploadJob = serviceScope.launch {
            while (true) {
                val delayMillis = dateKeyResolver.millisUntilPreBoundaryFlush(
                    leadTimeMillis = LocationUploadPolicy.PRE_BOUNDARY_UPLOAD_LEAD_TIME_MS
                )
                Log.d(TAG, "Scheduling pre-boundary upload in ${delayMillis}ms")
                delay(delayMillis)

                val activeDateKey = dateKeyResolver.resolveCurrentDateKey()
                val didUpload = uploadPendingPoints(activeDateKey)
                if (didUpload) {
                    Log.i(TAG, "Pre-boundary upload succeeded for dateKey=$activeDateKey")
                    resetPeriodicUploadLoop()
                } else {
                    Log.d(TAG, "Pre-boundary upload skipped for dateKey=$activeDateKey because there were no pending points")
                }
            }
        }
    }

    private fun resetPeriodicUploadLoop() {
        startPeriodicUploadLoop()
    }

    private fun flushPendingPointsOnStop() {
        runBlocking {
            val currentDateKey = dateKeyResolver.resolveCurrentDateKey()
            val previousDateKey = dateKeyResolver.resolvePreviousDateKey()
            Log.i(
                TAG,
                "Flushing pending points on stop currentDateKey=$currentDateKey previousDateKey=$previousDateKey"
            )

            val didUploadPrevious = uploadPendingPoints(previousDateKey)
            val didUploadCurrent = uploadPendingPoints(currentDateKey)
            Log.i(
                TAG,
                "Stop flush completed previous=$didUploadPrevious current=$didUploadCurrent"
            )
        }
    }

    private suspend fun uploadPendingPoints(dateKey: String): Boolean {
        return uploadMutex.withLock {
            try {
                val didUpload = applicationContext.appContainer.uploadGpsPointsBatchUseCase(dateKey)
                if (!didUpload) {
                    Log.d(TAG, "No pending points to upload for dateKey=$dateKey")
                }
                didUpload
            } catch (throwable: Throwable) {
                Log.e(TAG, "Upload failed for dateKey=$dateKey", throwable)
                diagnosticsLogger.log(
                    category = TrackingDiagnosticsLogger.CATEGORY_UPLOAD,
                    message = "failure cause=${throwable::class.java.simpleName}: ${throwable.message}",
                    dateKey = dateKey
                )
                false
            }
        }
    }

    companion object {
        private const val TAG = "LocationTracking"
        private const val ACTION_START =
            "com.example.passedpath.feature.locationtracking.action.START"
        private const val ACTION_STOP =
            "com.example.passedpath.feature.locationtracking.action.STOP"

        fun createStartIntent(context: Context): Intent {
            return Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START
            }
        }

        fun createStopIntent(context: Context): Intent {
            return Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP
            }
        }

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, createStartIntent(context))
        }

        fun stop(context: Context) {
            context.startService(createStopIntent(context))
        }
    }
}


