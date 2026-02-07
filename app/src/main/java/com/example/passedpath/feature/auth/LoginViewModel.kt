package com.example.passedpath.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passedpath.data.auth.AuthTokenManager
import com.example.passedpath.data.auth.KakaoAuthManager
import com.example.passedpath.data.network.RetrofitClient
import com.example.passedpath.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    fun kakaoLogin(
        context: Context,
        onLoginSuccess: () -> Unit
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
                    // 서버 로그인 + 토큰 처리까지 Repository에서 완료
                    repository.loginWithKakao(kakaoAccessToken)
                    onLoginSuccess()
                }
            },
            onError = {
                // TODO: 카카오 로그인 실패 처리
            }
        )
    }
}
