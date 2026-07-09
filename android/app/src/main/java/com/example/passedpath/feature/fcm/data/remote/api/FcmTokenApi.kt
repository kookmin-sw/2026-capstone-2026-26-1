package com.example.passedpath.feature.fcm.data.remote.api

import com.example.passedpath.feature.fcm.data.remote.dto.FcmTokenUpdateRequest
import com.example.passedpath.feature.fcm.data.remote.dto.FcmTokenUpdateResponse
import retrofit2.http.Body
import retrofit2.http.PATCH

interface FcmTokenApi {

    @PATCH("/api/users/me/fcm-token")
    suspend fun updateFcmToken(
        @Body request: FcmTokenUpdateRequest
    ): FcmTokenUpdateResponse
}
