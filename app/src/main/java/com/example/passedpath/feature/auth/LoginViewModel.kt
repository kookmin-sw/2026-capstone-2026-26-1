package com.example.passedpath.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passedpath.data.datastore.TokenDataStore
import com.example.passedpath.data.network.RetrofitClient
import com.example.passedpath.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    fun kakaoLogin(
        context: Context,
        onLoginSuccess: () -> Unit
    ) {
        val repository = AuthRepository(
            authApi = RetrofitClient.createAuthApi(context)
        )

        KakaoAuthManager.login(
            context = context,
            onSuccess = { kakaoAccessToken ->
                viewModelScope.launch {
                    val response = repository.loginWithKakao(kakaoAccessToken)

                    // accessToken + refreshToken 저장
                    TokenDataStore.saveTokens(
                        context = context,
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken
                    )

                    onLoginSuccess()
                }
            },
            onError = {
                // TODO: 로그인 실패 처리
            }
        )
    }

}