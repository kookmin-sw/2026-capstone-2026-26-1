package com.example.passedpath.interceptor

import android.util.Log
import com.example.passedpath.data.datastore.AuthSessionStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionStorage: AuthSessionStorage
) : Interceptor {

    private fun shouldSkipAuthorization(path: String): Boolean {
        return path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/signup") ||
            path.startsWith("/api/auth/refresh")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        if (shouldSkipAuthorization(path)) {
            return chain.proceed(originalRequest)
        }

        val accessToken = runBlocking {
            sessionStorage.getAccessToken()
        }

        val authRequest = originalRequest.newBuilder().apply {
            if (!accessToken.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $accessToken")
            }
        }.build()

        val response = chain.proceed(authRequest)

        if (response.code == 401) {
            Log.w("AUTH", "401 Unauthorized detected")
        }

        return response
    }
}
