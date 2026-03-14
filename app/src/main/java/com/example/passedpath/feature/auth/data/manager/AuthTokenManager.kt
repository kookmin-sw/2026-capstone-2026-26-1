package com.example.passedpath.feature.auth.data.manager

import android.util.Log
import com.example.passedpath.data.datastore.AuthSessionStorage
import com.example.passedpath.feature.auth.data.remote.api.AuthApi

class AuthTokenManager(
    private val authApi: AuthApi,
    private val sessionStorage: AuthSessionStorage
) {

    suspend fun refreshAccessToken(): Boolean {
        val refreshToken = sessionStorage.getRefreshToken()
            ?: return false

        return try {
            val response = authApi.refreshToken(refreshToken)

            sessionStorage.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )

            Log.d("AUTH", "Token refresh success")
            true

        } catch (e: Exception) {
            Log.e("AUTH", "Token refresh failed", e)
            sessionStorage.clear()
            false
        }
    }

    suspend fun logout() {
        sessionStorage.clear()
    }
}
