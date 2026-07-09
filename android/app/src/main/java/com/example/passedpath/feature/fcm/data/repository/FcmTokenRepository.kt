package com.example.passedpath.feature.fcm.data.repository

import com.example.passedpath.feature.fcm.data.remote.api.FcmTokenApi
import com.example.passedpath.feature.fcm.data.remote.dto.FcmTokenUpdateRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class FcmTokenRepository(
    private val fcmTokenApi: FcmTokenApi
) {
    suspend fun registerFcmToken(fcmToken: String) {
        fcmTokenApi.updateFcmToken(FcmTokenUpdateRequest(fcmToken))
    }

    suspend fun registerCurrentDeviceToken() {
        registerFcmToken(fetchCurrentToken())
    }

    private suspend fun fetchCurrentToken(): String = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> continuation.resume(token) }
            .addOnFailureListener { throwable -> continuation.resumeWithException(throwable) }
    }
}
