package com.example.passedpath.feature.auth.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.R
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.auth.data.manager.KakaoAuthManager
import com.example.passedpath.feature.auth.data.repository.AuthRepository
import com.example.passedpath.feature.permission.data.manager.LocationPermissionStatusReader
import kotlinx.coroutines.launch

enum class LoginDestination {
    MAIN,
    PERMISSION_INTRO
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val locationPermissionStatusReader: LocationPermissionStatusReader
) : ViewModel() {

    fun kakaoLogin(
        context: Context,
        onLoginSuccess: (LoginDestination) -> Unit,
        onLoginError: (Int) -> Unit
    ) {
        KakaoAuthManager.login(
            context = context,
            onSuccess = { kakaoAccessToken ->
                viewModelScope.launch {
                    try {
                        val response = authRepository.loginWithKakao(kakaoAccessToken)
                        Log.d(
                            "LOGIN",
                            "Kakao profile received: nickname=${response.nickname}, profileImageUrl=${response.profileImageUrl}"
                        )

                        val destination =
                            if (locationPermissionStatusReader.isBackgroundAlwaysGranted()) {
                                LoginDestination.MAIN
                            } else {
                                LoginDestination.PERMISSION_INTRO
                            }

                        onLoginSuccess(destination)
                    } catch (e: Exception) {
                        Log.e("LOGIN", "Server login failed", e)
                        onLoginError(R.string.login_error_server_connection)
                    }
                }
            },
            onError = { error ->
                Log.e("LOGIN", "Kakao login failed", error)
                onLoginError(R.string.login_error_kakao_failed)
            }
        )
    }
}

class LoginViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(
                authRepository = appContainer.authRepository,
                locationPermissionStatusReader = appContainer.locationPermissionStatusReader
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
