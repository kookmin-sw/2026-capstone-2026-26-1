package com.example.passedpath.feature.auth.data.manager

import android.content.Context
import android.util.Log
import com.example.passedpath.data.datastore.TokenDataStore
import com.example.passedpath.feature.auth.data.remote.api.AuthApi


class AuthTokenManager(
    private val context: Context,
    private val authApi: AuthApi
) {

    /*
    * refresh token을 사용해 access token 재발급
    * @return 재발급 성공 여부
    */
    suspend fun refreshAccessToken(): Boolean {
        val refreshToken = TokenDataStore.getRefreshToken(context)
            ?: return false

        return try {
            val response = authApi.refreshToken(refreshToken)

            TokenDataStore.saveTokens(
                context = context,
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )

            Log.d("AUTH", "토큰 재발급 성공")
            true

        } catch (e: Exception) {
            Log.e("AUTH", "토큰 재발급 실패", e)
            TokenDataStore.clear(context)
            false
        }
    }

    // 로그아웃 처리 (토큰 제거)
    suspend fun logout() {
        TokenDataStore.clear(context)
    }
}
