package com.example.passedpath.feature.auth.data.repository

import android.util.Log
import com.example.passedpath.data.datastore.AuthSessionStorage
import com.example.passedpath.feature.auth.data.manager.AuthTokenManager
import com.example.passedpath.feature.auth.data.remote.api.AuthApi
import com.example.passedpath.feature.auth.data.remote.dto.ErrorResponse
import com.example.passedpath.feature.auth.data.remote.dto.KakaoLoginRequest
import com.example.passedpath.feature.auth.data.remote.dto.KakaoLoginResponse
import com.example.passedpath.feature.fcm.data.repository.FcmTokenRepository
import com.google.gson.Gson
import retrofit2.HttpException

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: AuthTokenManager,
    private val sessionStorage: AuthSessionStorage,
    private val fcmTokenRepository: FcmTokenRepository
) {

    suspend fun loginWithKakao(kakaoAccessToken: String): KakaoLoginResponse {
        return try {
            requestLogin(kakaoAccessToken).also { response ->
                onLoginSucceeded(response)
            }
        } catch (e: HttpException) {
            if (e.code() == 401) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = parseError(errorBody)

                if (
                    errorResponse?.code == "ACCESS_TOKEN_EXPIRED" &&
                    tokenManager.refreshAccessToken()
                ) {
                    return requestLogin(kakaoAccessToken).also { response ->
                        onLoginSucceeded(response)
                    }
                } else {
                    tokenManager.logout()
                    throw e
                }
            } else {
                throw e
            }
        }
    }

    private suspend fun onLoginSucceeded(response: KakaoLoginResponse) {
        sessionStorage.saveTokens(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken
        )
        sessionStorage.saveUserProfile(
            userId = response.userId,
            nickname = response.nickname,
            profileImageUrl = response.profileImageUrl
        )

        // FCM 토큰 등록 실패는 부가 기능(위치 즉시 업로드 트리거)의 오류일 뿐이므로 로그인 흐름을 막지 않는다
        try {
            fcmTokenRepository.registerCurrentDeviceToken()
        } catch (e: Exception) {
            Log.e("LOGIN", "FCM 토큰 등록 실패", e)
        }
    }

    private suspend fun requestLogin(kakaoAccessToken: String): KakaoLoginResponse {
        val request = KakaoLoginRequest(kakaoAccessToken)
        return authApi.loginWithKakao(request)
    }

    private fun parseError(body: String?): ErrorResponse? {
        return try {
            Gson().fromJson(body, ErrorResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
