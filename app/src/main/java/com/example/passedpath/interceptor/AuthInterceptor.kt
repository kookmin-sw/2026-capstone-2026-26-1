package com.example.passedpath.interceptor

import android.content.Context
import com.example.passedpath.datastore.TokenDataStore
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val accessToken = runBlocking {
            TokenDataStore.getAccessToken(context)
        }

        val newRequest = if (!accessToken.isNullOrBlank()) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            request
        }

        return chain.proceed(newRequest)
    }
}