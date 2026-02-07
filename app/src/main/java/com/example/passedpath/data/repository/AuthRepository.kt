package com.example.passedpath.data.repository

import com.example.passedpath.data.auth.AuthTokenManager
import com.example.passedpath.data.network.api.AuthApi
import com.example.passedpath.data.network.dto.ErrorResponse
import com.example.passedpath.data.network.dto.KakaoLoginRequest
import com.example.passedpath.data.network.dto.KakaoLoginResponse
import com.google.gson.Gson
import retrofit2.HttpException


class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: AuthTokenManager
) {

    // 카카오 로그인 → 서버 로그인
    suspend fun loginWithKakao(kakaoAccessToken: String): KakaoLoginResponse {
        return try {
            requestLogin(kakaoAccessToken)

        } catch (e: HttpException) {
            if (e.code() == 401) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = parseError(errorBody)

                if (
                    errorResponse?.code == "ACCESS_TOKEN_EXPIRED" &&
                    tokenManager.refreshAccessToken()
                ) {
                    return requestLogin(kakaoAccessToken)
                } else {
                    // 만료 토큰이 아님 → 세션 무효
                    tokenManager.logout()
                    throw e
                }
            } else {
                // TODO: 상위(ViewModel)에서 로그아웃/에러 처리
                throw e
            }
        }
    }

    // 실제 서버 로그인 요청
    private suspend fun requestLogin(kakaoAccessToken: String): KakaoLoginResponse {
        val request = KakaoLoginRequest(kakaoAccessToken)
        return authApi.loginWithKakao(request)
    }

    // 401 error시에 requestBody 파싱 함수
    private fun parseError(body: String?): ErrorResponse? {
        return try {
            Gson().fromJson(body, ErrorResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

}