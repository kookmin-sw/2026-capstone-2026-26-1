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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

enum class LoginDestination {
    MAIN,
    PERMISSION_INTRO
}

sealed interface LoginEffect {
    data class Navigate(val destination: LoginDestination) : LoginEffect
    data class ShowError(val messageResId: Int) : LoginEffect
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val locationPermissionStatusReader: LocationPermissionStatusReader
) : ViewModel() {

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect = _effect.asSharedFlow()

    fun kakaoLogin(context: Context) {
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

                        _effect.emit(LoginEffect.Navigate(destination))
                    } catch (e: Exception) {
                        Log.e("LOGIN", "Server login failed", e)
                        _effect.emit(LoginEffect.ShowError(R.string.login_error_server_connection))
                    }
                }
            },
            onError = { error ->
                viewModelScope.launch {
                    Log.e("LOGIN", "Kakao login failed", error)
                    _effect.emit(LoginEffect.ShowError(R.string.login_error_kakao_failed))
                }
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
