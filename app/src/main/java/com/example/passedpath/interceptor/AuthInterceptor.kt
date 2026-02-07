package com.example.passedpath.interceptor

import android.content.Context
import android.util.Log
import com.example.passedpath.data.datastore.TokenDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // datastore에서 access token 조회 (동기 허용 범위)
        val accessToken = runBlocking {
            TokenDataStore.getAccessToken(context)
        }

        // Authorization 헤더 추가
        val authRequest = originalRequest.newBuilder().apply {
            if (!accessToken.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $accessToken")
            }
        }.build()

        val response = chain.proceed(authRequest)

        // 401은 "감지만" 한다
        if (response.code == 401) {
            Log.w("AUTH", "401 Unauthorized detected")
        }

        return response
    }
}
