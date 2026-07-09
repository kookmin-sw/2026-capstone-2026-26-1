package com.example.passedpath.feature.fcm.service

import android.util.Log
import com.example.passedpath.app.appContainer
import com.example.passedpath.data.datastore.AuthSessionStorage
import com.example.passedpath.feature.locationtracking.domain.policy.TrackingUploadMode
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GilbutFirebaseMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val appContainer = applicationContext.appContainer
        serviceScope.launch {
            val sessionStorage = appContainer.authSessionStorage
            if (!sessionStorage.isLoggedIn()) {
                Log.d(TAG, "Skip FCM token registration: 로그인 상태가 아님")
                return@launch
            }

            try {
                appContainer.fcmTokenRepository.registerFcmToken(token)
                Log.i(TAG, "FCM 토큰 갱신 등록 성공")
            } catch (throwable: Throwable) {
                Log.e(TAG, "FCM 토큰 갱신 등록 실패", throwable)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val type = message.data["type"]
        if (type != MESSAGE_TYPE_TRACKING_INTERVAL_CHANGE) {
            Log.d(TAG, "알 수 없는 FCM data message type=$type")
            return
        }

        val watching = message.data["watching"].toBoolean()
        Log.i(TAG, "TRACKING_INTERVAL_CHANGE 수신 watching=$watching")
        applicationContext.appContainer.trackingUploadModeWriter.update(
            if (watching) TrackingUploadMode.IMMEDIATE else TrackingUploadMode.NORMAL
        )
    }

    private companion object {
        const val TAG = "GilbutFcm"
        const val MESSAGE_TYPE_TRACKING_INTERVAL_CHANGE = "TRACKING_INTERVAL_CHANGE"
    }
}

private suspend fun AuthSessionStorage.isLoggedIn(): Boolean {
    return !getAccessToken().isNullOrBlank()
}
