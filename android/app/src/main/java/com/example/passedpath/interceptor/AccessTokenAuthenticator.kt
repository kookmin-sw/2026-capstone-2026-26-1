package com.example.passedpath.interceptor

import android.util.Log
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

    private val logTag = "AuthFlow"

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (path.startsWith("/api/auth/refresh")) {
            Log.w(logTag, "refresh request itself returned 401 path=$path")
            return null
        }
        val requestAccessToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()
            .orEmpty()
        if (requestAccessToken.isEmpty()) {
            Log.w(logTag, "cannot recover 401 because request had no Authorization header path=$path")
            return null
        }
        Log.d(
            logTag,
            "authenticate path=$path responseCount=${responseCount(response)} requestToken=${requestAccessToken.toTokenPreview()}"
        )
        if (responseCount(response) >= 2) {
            Log.e(
                logTag,
                "retry also failed with 401 path=$path body=${response.peekBody(1024).string()}"
            )
            runBlocking {
                tokenManager.logoutDueToExpiredSession()
            }
            return null
        }

        synchronized(this) {
            val latestAccessToken = runBlocking { sessionStorage.getAccessToken() }
            if (!latestAccessToken.isNullOrBlank() && latestAccessToken != requestAccessToken) {
                Log.d(
                    logTag,
                    "token already updated by another request path=$path latestToken=${latestAccessToken.toTokenPreview()}"
                )
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $latestAccessToken")
                    .build()
            }

            Log.d(logTag, "attempting refresh for path=$path")
            val refreshSucceeded = runBlocking {
                tokenManager.refreshAccessToken()
            }
            if (!refreshSucceeded) {
                Log.e(logTag, "refresh failed for path=$path")
                return null
            }

            val refreshedAccessToken = runBlocking { sessionStorage.getAccessToken() }
                ?: run {
                    Log.e(logTag, "refresh reported success but no access token was stored path=$path")
                    return null
                }

            Log.d(
                logTag,
                "retrying request with refreshed token path=$path token=${refreshedAccessToken.toTokenPreview()}"
            )

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

private fun String?.toTokenPreview(): String {
    if (this.isNullOrBlank()) return "none"
    return if (length <= 12) "***" else "${take(8)}...${takeLast(4)}"
}
