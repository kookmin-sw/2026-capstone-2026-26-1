package com.example.passedpath.feature.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.permission.data.manager.LocationPermissionStatusReader
import com.example.passedpath.feature.route.presentation.coordinator.RouteStateCoordinator
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val locationPermissionStatusReader: LocationPermissionStatusReader,
    initialDateKeyProvider: () -> String = ::todayDateKey,
    private val routeStateCoordinator: RouteStateCoordinator
) : ViewModel() {

    private val initialDateKey = initialDateKeyProvider()
    private var routeLoadJob: Job? = null

    private val _uiState = MutableStateFlow(
        MainUiState(
            selectedDateKey = initialDateKey,
            routeModeUiState = routeStateCoordinator.createInitialState(initialDateKey)
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshPermissionState()
        loadDayRoute(initialDateKey)
    }

    fun refreshPermissionState() {
        val permissionState = when {
            locationPermissionStatusReader.isBackgroundAlwaysGranted() -> LocationPermissionUiState.ALWAYS
            locationPermissionStatusReader.isForegroundGranted() -> LocationPermissionUiState.FOREGROUND_ONLY
            else -> LocationPermissionUiState.DENIED
        }

        _uiState.update { currentState ->
            if (permissionState == LocationPermissionUiState.DENIED) {
                currentState.copy(
                    permissionState = permissionState,
                    currentLocation = null,
                    hasCenteredOnCurrentLocation = false
                )
            } else {
                currentState.copy(permissionState = permissionState)
            }
        }
    }

    fun updateCurrentLocation(location: MainCoordinateUiState) {
        _uiState.update { currentState ->
            currentState.copy(currentLocation = location)
        }
    }

    fun markInitialCameraCentered() {
        _uiState.update { currentState ->
            currentState.copy(hasCenteredOnCurrentLocation = true)
        }
    }

    fun selectDate(dateKey: String) {
        loadDayRoute(dateKey)
    }

    fun handleRouteAction(action: RouteUiAction) {
        when (action) {
            RouteUiAction.RefreshTodayRoute -> loadDayRoute(_uiState.value.selectedDateKey)
            RouteUiAction.RetryPastRoute -> loadDayRoute(_uiState.value.selectedDateKey)
            RouteUiAction.ToggleTracking -> Unit
            RouteUiAction.EnterPastPlayback -> Unit
        }
    }

    private fun loadDayRoute(dateKey: String) {
        routeLoadJob?.cancel()
        routeLoadJob = viewModelScope.launch {
            routeStateCoordinator.loadRoute(dateKey).collect { routeState ->
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedDateKey = routeState.selectedDateKey,
                        routeModeUiState = routeState.routeModeUiState,
                        hasCenteredOnCurrentLocation = false
                    )
                }
            }
        }
    }
}

private fun todayDateKey(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
}

class MainViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                locationPermissionStatusReader = appContainer.locationPermissionStatusReader,
                routeStateCoordinator = RouteStateCoordinator(
                    dayRouteRepository = appContainer.dayRouteRepository,
                    todayDateKeyProvider = ::todayDateKey
                )
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
