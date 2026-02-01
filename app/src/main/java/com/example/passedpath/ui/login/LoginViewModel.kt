package com.example.passedpath.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passedpath.auth.KakaoAuthManager
import com.example.passedpath.datastore.TokenDataStore
import com.example.passedpath.network.RetrofitClient
import com.example.passedpath.repository.AuthRepository
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

                    TokenDataStore.saveAccessToken(
                        context = context,
                        token = response.accessToken
                    )

                    // 로그인 성공 콜백
                    onLoginSuccess()
                }
            },
            onError = {
                // TODO: 로그인 실패 처리
            }
        )
    }

}