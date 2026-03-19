package com.example.passedpath.feature.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.main.presentation.state.DailyPathUiState
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.permission.data.manager.LocationPermissionChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val permissionChecker: LocationPermissionChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainUiState(
            todayPath = DailyPathUiState(dateKey = todayDateKey())
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshPermissionState()
    }

    fun refreshPermissionState() {
        val permissionState = when {
            permissionChecker.isBackgroundAlwaysGranted() -> LocationPermissionUiState.ALWAYS
            permissionChecker.isForegroundGranted() -> LocationPermissionUiState.FOREGROUND_ONLY
            else -> LocationPermissionUiState.DENIED
        }

        _uiState.value = _uiState.value.copy(permissionState = permissionState)
    }

    fun updateCurrentLocation(location: MainCoordinateUiState) {
        val currentState = _uiState.value
        val updatedPoints = currentState.todayPath.points + location

        _uiState.value = currentState.copy(
            currentLocation = location,
            todayPath = currentState.todayPath.copy(points = updatedPoints)
        )
    }

    fun markInitialCameraCentered() {
        _uiState.value = _uiState.value.copy(hasCenteredOnCurrentLocation = true)
    }

    fun resetTodayPath(dateKey: String = todayDateKey()) {
        _uiState.value = _uiState.value.copy(
            todayPath = DailyPathUiState(dateKey = dateKey),
            hasCenteredOnCurrentLocation = false
        )
    }

    private fun todayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
    }
}

class MainViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                permissionChecker = appContainer.permissionChecker
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
