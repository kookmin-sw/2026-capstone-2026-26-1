package com.example.passedpath.interceptor

import android.content.Context
import android.util.Log
import com.example.passedpath.data.datastore.TokenDataStore
import com.example.passedpath.data.network.api.AuthApi
import com.example.passedpath.feature.auth.AuthEvent
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor(
    private val context: Context,
    private val authApi: AuthApi
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val accessToken = runBlocking {
            TokenDataStore.getAccessToken(context)
        }

        // accessToken 헤더 추가
        val authRequest = originalRequest.newBuilder().apply {
            if (accessToken != null) {
                addHeader("Authorization", "Bearer $accessToken")
            }
        }.build()

        val response = chain.proceed(authRequest)

        // 401 발생 시 refresh 시도
        if (response.code == 401) {
            val errorBody = response.peekBody(Long.MAX_VALUE).string()
            Log.e("AUTH", "401 응답 바디: $errorBody")

            response.close()

            return runBlocking {
                handleRefresh(chain, originalRequest)
            }
        }

        return response
    }

    private suspend fun handleRefresh(
        chain: Interceptor.Chain,
        originalRequest: Request
    ): Response {

        val refreshToken = TokenDataStore.getRefreshToken(context)
            ?: run {
                Log.d("AUTH", "refreshToken 없음 → 재시도 불가")
                return chain.proceed(originalRequest)
            }

        return try {
            // refreshToken은 Header로 전달
            val refreshResponse = authApi.refreshToken(refreshToken)

            Log.d("AUTH", "토큰 재발급 성공")

            // 새 토큰 저장
            TokenDataStore.saveTokens(
                context = context,
                accessToken = refreshResponse.accessToken,
                refreshToken = refreshResponse.refreshToken
            )

            // 기존 요청 재시도
            val newRequest = originalRequest.newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization", "Bearer ${refreshResponse.accessToken}")
                .build()

            chain.proceed(newRequest)

        } catch (e: Exception) {
            Log.e("AUTH", "토큰 재발급 실패", e)
            // TODO: refresh 실패 시 강제 로그아웃
            // 토큰 삭제
            TokenDataStore.clear(context)

            // 로그아웃 이벤트 발생
            AuthEvent.logoutEvent.emit(Unit)

            chain.proceed(originalRequest)
        }
    }
}