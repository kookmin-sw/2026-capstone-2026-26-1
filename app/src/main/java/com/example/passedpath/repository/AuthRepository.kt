package com.example.passedpath.repository

import android.util.Log
import com.example.passedpath.network.RetrofitClient
import com.example.passedpath.network.api.AuthApi
import com.example.passedpath.network.dto.KakaoLoginRequest
import com.example.passedpath.network.dto.KakaoLoginResponse

class AuthRepository(
    private val authApi: AuthApi
) {

    suspend fun loginWithKakao(kakaoAccessToken: String): KakaoLoginResponse {
        val request = KakaoLoginRequest(kakaoAccessToken)
        return authApi.loginWithKakao(request)
    }
}