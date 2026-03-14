package com.example.passedpath.feature.auth.data.repository

import com.example.passedpath.data.datastore.AuthSessionStorage
import com.example.passedpath.feature.auth.data.manager.AuthTokenManager
import com.example.passedpath.feature.auth.data.remote.api.AuthApi
import com.example.passedpath.feature.auth.data.remote.dto.ErrorResponse
import com.example.passedpath.feature.auth.data.remote.dto.KakaoLoginRequest
import com.example.passedpath.feature.auth.data.remote.dto.KakaoLoginResponse
import com.google.gson.Gson
import retrofit2.HttpException

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: AuthTokenManager,
    private val sessionStorage: AuthSessionStorage
) {

    suspend fun loginWithKakao(kakaoAccessToken: String): KakaoLoginResponse {
        return try {
            requestLogin(kakaoAccessToken).also { response ->
                sessionStorage.saveTokens(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken
                )
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
                        sessionStorage.saveTokens(
                            accessToken = response.accessToken,
                            refreshToken = response.refreshToken
                        )
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
