package com.example.passedpath.data.repository

import com.example.passedpath.data.auth.AuthTokenManager
import com.example.passedpath.data.network.api.AuthApi
import com.example.passedpath.data.network.dto.KakaoLoginRequest
import com.example.passedpath.data.network.dto.KakaoLoginResponse
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
            // 401일 때만 refresh 시도
            if (e.code() == 401 && tokenManager.refreshAccessToken()) {
                // refresh 성공 → 1회 재시도
                requestLogin(kakaoAccessToken)
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
}