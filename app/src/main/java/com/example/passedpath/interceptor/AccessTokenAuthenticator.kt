package com.example.passedpath.interceptor

import com.example.passedpath.data.datastore.AuthSessionStorage
import com.example.passedpath.feature.auth.data.manager.AuthTokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AccessTokenAuthenticator(
    private val sessionStorage: AuthSessionStorage,
    private val tokenManager: AuthTokenManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (path.startsWith("/api/auth/refresh")) return null
        if (responseCount(response) >= 2) return null

        val requestAccessToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()
            .orEmpty()
        if (requestAccessToken.isEmpty()) return null

        synchronized(this) {
            val latestAccessToken = runBlocking { sessionStorage.getAccessToken() }
            if (!latestAccessToken.isNullOrBlank() && latestAccessToken != requestAccessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $latestAccessToken")
                    .build()
            }

            val refreshSucceeded = runBlocking {
                tokenManager.refreshAccessToken()
            }
            if (!refreshSucceeded) return null

            val refreshedAccessToken = runBlocking { sessionStorage.getAccessToken() }
                ?: return null

            return response.request.newBuilder()
                .header("Authorization", "Bearer $refreshedAccessToken")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }
}
