package com.example.passedpath.feature.auth.data.manager

import android.util.Log
import com.example.passedpath.data.datastore.AuthSessionStorage
import com.example.passedpath.feature.auth.data.remote.api.AuthApi
import com.example.passedpath.feature.auth.presentation.state.AuthEvent
import com.example.passedpath.feature.auth.presentation.state.LogoutEvent
import retrofit2.HttpException

class AuthTokenManager(
    private val authApi: AuthApi,
    private val sessionStorage: AuthSessionStorage
) {

    private val logTag = "AuthFlow"
    private val sessionExpiredMessage = "로그인 인증이 만료되어 자동으로 로그아웃되었어요"

    suspend fun refreshAccessToken(): Boolean {
        val refreshToken = sessionStorage.getRefreshToken()
            ?: run {
                Log.e(logTag, "refresh aborted because no refresh token is stored")
                return handleExpiredSession("missing_refresh_token")
            }

        Log.d(
            logTag,
            "refresh start refreshToken=${refreshToken.toTokenPreview()}"
        )

        return try {
            val response = authApi.refreshToken(refreshToken)

            sessionStorage.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )

            Log.d(
                logTag,
                "refresh success newAccessToken=${response.accessToken.toTokenPreview()} newRefreshToken=${response.refreshToken.toTokenPreview()}"
            )
            true

        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(
                logTag,
                "refresh http failure code=${e.code()} body=$errorBody",
                e
            )
            handleExpiredSession("refresh_http_${e.code()}")
        } catch (e: Exception) {
            Log.e(
                logTag,
                "refresh unexpected failure type=${e::class.java.simpleName} message=${e.message}",
                e
            )
            handleExpiredSession("refresh_exception_${e::class.java.simpleName}")
        }
    }

    suspend fun logout() {
        sessionStorage.clear()
    }

    suspend fun logoutDueToExpiredSession(): Boolean {
        return handleExpiredSession("final_401_after_retry")
    }

    private suspend fun handleExpiredSession(reason: String): Boolean {
        Log.e(logTag, "clearing session and emitting logout event reason=$reason")
        sessionStorage.clear()
        AuthEvent.logoutEvent.emit(
            LogoutEvent(message = sessionExpiredMessage)
        )
        return false
    }
}

private fun String?.toTokenPreview(): String {
    if (this.isNullOrBlank()) return "none"
    return if (length <= 12) "***" else "${take(8)}...${takeLast(4)}"
}
