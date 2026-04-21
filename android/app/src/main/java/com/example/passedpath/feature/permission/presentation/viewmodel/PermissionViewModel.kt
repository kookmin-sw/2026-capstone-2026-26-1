package com.example.passedpath.feature.permission.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.passedpath.app.AppContainer
import com.example.passedpath.debug.AppDebugLogger
import com.example.passedpath.debug.DebugLogTag
import com.example.passedpath.feature.permission.data.manager.LocationPermissionStatusReader
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
    private val locationPermissionStatusReader: LocationPermissionStatusReader
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<PermissionEffect>()
    val effect = _effect.asSharedFlow()

    suspend fun onContinueClick() {
        val hasBackgroundAlways = locationPermissionStatusReader.isBackgroundAlwaysGranted()
        val hasForeground = locationPermissionStatusReader.isForegroundGranted()
        AppDebugLogger.debug(
            DebugLogTag.PERMISSION,
            "permission continue click backgroundAlways=$hasBackgroundAlways foreground=$hasForeground"
        )
        when {
            hasBackgroundAlways -> {
                AppDebugLogger.debug(
                    DebugLogTag.PERMISSION,
                    "permission continue result=navigate-next"
                )
                _effect.emit(PermissionEffect.NavigateNext)
            }

            !hasForeground -> {
                AppDebugLogger.debug(
                    DebugLogTag.PERMISSION,
                    "permission continue result=request-foreground"
                )
                _effect.emit(PermissionEffect.RequestForegroundPermission)
            }

            else -> {
                AppDebugLogger.debug(
                    DebugLogTag.PERMISSION,
                    "permission continue result=show-settings-dialog"
                )
                _uiState.update { it.copy(showSettingsDialog = true) }
            }
        }
    }

    suspend fun onForegroundPermissionResult() {
        val hasBackgroundAlways = locationPermissionStatusReader.isBackgroundAlwaysGranted()
        AppDebugLogger.debug(
            DebugLogTag.PERMISSION,
            "foreground permission result backgroundAlways=$hasBackgroundAlways"
        )
        if (hasBackgroundAlways) {
            AppDebugLogger.debug(
                DebugLogTag.PERMISSION,
                "foreground permission resolved=navigate-next"
            )
            _effect.emit(PermissionEffect.NavigateNext)
        } else {
            AppDebugLogger.debug(
                DebugLogTag.PERMISSION,
                "foreground permission resolved=show-settings-dialog"
            )
            _uiState.update { it.copy(showSettingsDialog = true) }
        }
    }

    suspend fun onSettingsConfirm() {
        AppDebugLogger.debug(
            DebugLogTag.PERMISSION,
            "permission settings dialog confirm"
        )
        _uiState.update { it.copy(showSettingsDialog = false) }
        _effect.emit(PermissionEffect.OpenAppSettings)
    }

    suspend fun onSettingsDismiss() {
        AppDebugLogger.debug(
            DebugLogTag.PERMISSION,
            "permission settings dialog dismiss navigate-next"
        )
        _uiState.update { it.copy(showSettingsDialog = false) }
        _effect.emit(PermissionEffect.NavigateNext)
    }

    suspend fun onReturnedFromSettings() {
        AppDebugLogger.debug(
            DebugLogTag.PERMISSION,
            "returned from app settings navigate-next"
        )
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
                locationPermissionStatusReader = appContainer.locationPermissionStatusReader
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
