package com.example.passedpath.data.repository

import com.example.passedpath.data.network.api.AuthApi
import com.example.passedpath.data.network.dto.KakaoLoginRequest
import com.example.passedpath.data.network.dto.KakaoLoginResponse

class AuthRepository(
    private val authApi: AuthApi
) {

    suspend fun loginWithKakao(kakaoAccessToken: String): KakaoLoginResponse {
        val request = KakaoLoginRequest(kakaoAccessToken)
        return authApi.loginWithKakao(request)
    }
}