package com.example.passedpath.feature.permission.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.permission.data.manager.LocationPermissionChecker
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PermissionUiState(
    val showSettingsDialog: Boolean = false
)

sealed interface PermissionEffect {
    data object RequestForegroundPermission : PermissionEffect
    data object OpenAppSettings : PermissionEffect
    data object NavigateNext : PermissionEffect
}

class PermissionViewModel(
    private val permissionChecker: LocationPermissionChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<PermissionEffect>()
    val effect = _effect.asSharedFlow()

    suspend fun onContinueClick() {
        when {
            permissionChecker.isBackgroundAlwaysGranted() -> {
                _effect.emit(PermissionEffect.NavigateNext)
            }

            !permissionChecker.isForegroundGranted() -> {
                _effect.emit(PermissionEffect.RequestForegroundPermission)
            }

            else -> {
                _uiState.update { it.copy(showSettingsDialog = true) }
            }
        }
    }

    suspend fun onForegroundPermissionResult() {
        if (permissionChecker.isBackgroundAlwaysGranted()) {
            _effect.emit(PermissionEffect.NavigateNext)
        } else {
            _uiState.update { it.copy(showSettingsDialog = true) }
        }
    }

    suspend fun onSettingsConfirm() {
        _uiState.update { it.copy(showSettingsDialog = false) }
        _effect.emit(PermissionEffect.OpenAppSettings)
    }

    suspend fun onSettingsDismiss() {
        _uiState.update { it.copy(showSettingsDialog = false) }
        _effect.emit(PermissionEffect.NavigateNext)
    }

    suspend fun onReturnedFromSettings() {
        _effect.emit(PermissionEffect.NavigateNext)
    }
}

class PermissionViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PermissionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PermissionViewModel(
                permissionChecker = appContainer.permissionChecker
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
