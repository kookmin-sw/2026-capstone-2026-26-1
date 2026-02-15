package com.example.passedpath.feature.auth.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passedpath.data.datastore.TokenDataStore
import com.example.passedpath.data.network.RetrofitClient
import com.example.passedpath.feature.auth.data.AuthRepository
import com.example.passedpath.feature.auth.data.AuthTokenManager
import com.example.passedpath.feature.auth.data.KakaoAuthManager
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    fun kakaoLogin(
        context: Context,
        onLoginSuccess: () -> Unit,
        onLoginError: (String) -> Unit
    ) {
        val authApi = RetrofitClient.authApi(context)

        val tokenManager = AuthTokenManager(
            context = context,
            authApi = authApi
        )

        val repository = AuthRepository(
            authApi = authApi,
            tokenManager = tokenManager
        )

        KakaoAuthManager.login(
            context = context,
            onSuccess = { kakaoAccessToken ->
                viewModelScope.launch {
                    try {
                        val loginResponse = repository.loginWithKakao(kakaoAccessToken)

                        TokenDataStore.saveTokens(
                            context = context,
                            accessToken = loginResponse.accessToken,
                            refreshToken = loginResponse.refreshToken
                        )

                        onLoginSuccess()

                    } catch (e: Exception) {
                        Log.e("LOGIN", "Server login failed", e)
                        onLoginError("Server connection failed. Please try again.")
                    }
                }
            },
            onError = { error ->
                Log.e("LOGIN", "Kakao login failed", error)
                onLoginError("Kakao login failed.")
            }
        )
    }
}
