package com.example.passedpath.feature.locationtracking.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.locationtracking.data.local.mapper.epochMillisToDateKey
import com.example.passedpath.feature.locationtracking.domain.policy.LocationTrackingPolicy
import com.example.passedpath.feature.locationtracking.domain.tracker.LocationTrackingSession
import com.example.passedpath.feature.locationtracking.presentation.notification.TrackingNotificationFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LocationTrackingService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val uploadMutex = Mutex()

    private lateinit var notificationFactory: TrackingNotificationFactory
    private var trackingSession: LocationTrackingSession? = null
    private var periodicUploadJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        notificationFactory = TrackingNotificationFactory(this)
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

        notificationFactory.ensureTrackingChannel()
        startForeground(
            TrackingNotificationFactory.NOTIFICATION_ID,
            notificationFactory.createTrackingNotification()
        )

        val appContainer = applicationContext.appContainer
        startPeriodicUploadLoop()
        trackingSession = appContainer.locationTracker.startLocationUpdates { trackedLocation ->
            serviceScope.launch {
                val dateKey = epochMillisToDateKey(trackedLocation.recordedAtEpochMillis)
                appContainer.locationTrackingRepository.saveRawLocation(trackedLocation)

                val pendingCount = appContainer.locationTrackingRepository
                    .getPendingUploadLocationCount(dateKey)
                if (pendingCount >= LocationTrackingPolicy.UPLOAD_BATCH_SIZE) {
                    val didUpload = uploadPendingPoints(dateKey)
                    if (didUpload) {
                        resetPeriodicUploadLoop()
                    }
                }
            }
        }
    }

    private fun stopTrackingAndSelf() {
        stopTracking()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopTracking() {
        trackingSession?.stop()
        trackingSession = null
        periodicUploadJob?.cancel()
        periodicUploadJob = null
    }

    private fun startPeriodicUploadLoop() {
        periodicUploadJob?.cancel()
        periodicUploadJob = serviceScope.launch {
            while (true) {
                delay(PERIODIC_UPLOAD_INTERVAL_MS)
                val didUpload = uploadPendingPoints(todayDateKey())
                if (didUpload) {
                    resetPeriodicUploadLoop()
                    return@launch
                }
            }
        }
    }

    private fun resetPeriodicUploadLoop() {
        startPeriodicUploadLoop()
    }

    private suspend fun uploadPendingPoints(dateKey: String): Boolean {
        return uploadMutex.withLock {
            applicationContext.appContainer.uploadGpsPointsBatchUseCase(dateKey)
        }
    }

    private fun todayDateKey(): String {
        return epochMillisToDateKey(System.currentTimeMillis())
    }

    companion object {
        private const val PERIODIC_UPLOAD_INTERVAL_MS = 3 * 60 * 1000L
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
