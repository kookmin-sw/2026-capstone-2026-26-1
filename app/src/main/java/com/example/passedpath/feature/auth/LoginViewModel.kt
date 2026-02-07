package com.example.passedpath.feature.auth

import android.content.Context
import android.util.Log
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
                        // 서버 로그인 + 토큰 처리
                        repository.loginWithKakao(kakaoAccessToken)

                        // 서버 로그인 성공 시에만 진입
                        onLoginSuccess()

                    } catch (e: Exception) {
                        // TODO: 서버 OFF / 네트워크 오류 / 인증 실패 에러 타입별 메세지 분기
                        Log.e("LOGIN", "서버 로그인 실패", e)
                        onLoginError("서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.")
                    }
                }
            },
            onError = { error ->
                // 카카오 로그인 실패 처리
                Log.e("LOGIN", "카카오 로그인 실패", error)
                onLoginError("카카오 로그인에 실패했습니다.")
            }
        )
    }
}
