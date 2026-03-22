package com.example.passedpath.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.BuildConfig
import com.example.passedpath.app.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface AppEntryState {
    data object Loading : AppEntryState
    data class Ready(val destination: String) : AppEntryState
}

class AppEntryViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val _state = MutableStateFlow<AppEntryState>(AppEntryState.Loading)
    val state: StateFlow<AppEntryState> = _state

    init {
        viewModelScope.launch {
            val shouldSkipLogin = BuildConfig.DEV_SKIP_LOGIN
            val token = if (shouldSkipLogin) {
                "debug-skip-login"
            } else {
                appContainer.authSessionStorage.getAccessToken()
            }

            val destination = when {
                token == null -> NavRoute.LOGIN
                appContainer.permissionChecker.isBackgroundAlwaysGranted() -> NavRoute.MAIN
                else -> NavRoute.PERMISSION_INTRO
            }

            _state.value = AppEntryState.Ready(destination)
        }
    }
}

class AppEntryViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppEntryViewModel(appContainer) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
