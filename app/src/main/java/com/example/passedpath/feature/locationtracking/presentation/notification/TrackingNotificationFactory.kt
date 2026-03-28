package com.example.passedpath.feature.locationtracking.presentation.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.passedpath.MainActivity
import com.example.passedpath.R
import com.example.passedpath.feature.locationtracking.service.LocationTrackingService

class TrackingNotificationFactory(
    private val context: Context
) {
    private val notificationManager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    fun ensureTrackingChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.tracking_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.tracking_notification_channel_description)
            setShowBadge(false)
        }

        notificationManager.createNotificationChannel(channel)
    }

    fun createTrackingNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_CONTENT,
            MainActivity.createIntent(context),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            context,
            REQUEST_CODE_STOP,
            LocationTrackingService.createStopIntent(context),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(context.getString(R.string.tracking_notification_title))
            .setContentText(context.getString(R.string.tracking_notification_text))
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(
                0,
                context.getString(R.string.tracking_notification_stop),
                stopIntent
            )
            .build()
    }

    companion object {
        const val CHANNEL_ID = "location_tracking"
        const val NOTIFICATION_ID = 1001

        private const val REQUEST_CODE_CONTENT = 100
        private const val REQUEST_CODE_STOP = 101
    }
}
