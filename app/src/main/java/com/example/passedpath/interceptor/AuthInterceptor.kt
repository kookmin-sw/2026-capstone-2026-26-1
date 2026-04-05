package com.example.passedpath.interceptor

import com.example.passedpath.data.datastore.AuthSessionStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionStorage: AuthSessionStorage,
    private val attachAuthorizationToRefreshRequest: Boolean = false
) : Interceptor {

    private val logTag = "AuthFlow"

    private fun shouldSkipAuthorization(path: String): Boolean {
        return path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/signup") ||
            (path.startsWith("/api/auth/refresh") && !attachAuthorizationToRefreshRequest)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        if (shouldSkipAuthorization(path)) {
            android.util.Log.d(logTag, "skip auth header path=$path method=${originalRequest.method}")
            return chain.proceed(originalRequest)
        }

        val accessToken = runBlocking {
            sessionStorage.getAccessToken()
        }

        android.util.Log.d(
            logTag,
            "request path=$path method=${originalRequest.method} hasAccessToken=${!accessToken.isNullOrBlank()} token=${accessToken.toTokenPreview()}"
        )

        val authRequest = originalRequest.newBuilder().apply {
            if (!accessToken.isNullOrEmpty()) {
                header("Authorization", "Bearer $accessToken")
            }
        }.build()

        val response = chain.proceed(authRequest)

        if (response.code == 401) {
            android.util.Log.w(
                logTag,
                "received 401 path=$path method=${originalRequest.method} body=${response.peekBody(1024).string()}"
            )
        }

        return response
    }
}

private fun String?.toTokenPreview(): String {
    if (this.isNullOrBlank()) return "none"
    return if (length <= 12) "***" else "${take(8)}...${takeLast(4)}"
}
