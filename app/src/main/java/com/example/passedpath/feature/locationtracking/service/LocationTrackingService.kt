package com.example.passedpath.feature.locationtracking.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.locationtracking.domain.tracker.LocationTrackingSession
import com.example.passedpath.feature.locationtracking.presentation.notification.TrackingNotificationFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var notificationFactory: TrackingNotificationFactory
    private var trackingSession: LocationTrackingSession? = null

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
        trackingSession = appContainer.locationTracker.startLocationUpdates { trackedLocation ->
            serviceScope.launch {
                appContainer.locationTrackingRepository.saveRawLocation(trackedLocation)
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
    }

    companion object {
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
