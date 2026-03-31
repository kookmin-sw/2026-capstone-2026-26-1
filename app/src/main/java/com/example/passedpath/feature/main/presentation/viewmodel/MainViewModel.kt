package com.example.passedpath.feature.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.locationtracking.data.manager.LocationTrackingServiceStateReader
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.permission.data.manager.LocationPermissionStatusReader
import com.example.passedpath.feature.route.presentation.coordinator.RouteStateCoordinator
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val locationPermissionStatusReader: LocationPermissionStatusReader,
    initialDateKeyProvider: () -> String = ::todayDateKey,
    private val routeStateCoordinator: RouteStateCoordinator,
    private val trackingServiceStateReader: LocationTrackingServiceStateReader,
    private val startTracking: () -> Unit,
    private val stopTracking: () -> Unit
) : ViewModel() {

    private val initialDateKey = initialDateKeyProvider()
    private var routeLoadJob: Job? = null

    private val _uiState = MutableStateFlow(
        MainUiState(
            selectedDateKey = initialDateKey,
            routeModeUiState = routeStateCoordinator
                .createInitialState(initialDateKey)
                .withTrackingState(trackingServiceStateReader.isTracking.value)
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshPermissionState()
        observeTrackingState()
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
            RouteUiAction.ToggleTracking -> toggleTracking()
            RouteUiAction.EnterPastPlayback -> Unit
        }
    }

    fun dismissTrackingPermissionDialog() {
        _uiState.update { currentState ->
            currentState.copy(showTrackingPermissionDialog = false)
        }
    }

    private fun loadDayRoute(dateKey: String) {
        routeLoadJob?.cancel()
        routeLoadJob = viewModelScope.launch {
            routeStateCoordinator.loadRoute(dateKey).collect { routeState ->
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedDateKey = routeState.selectedDateKey,
                        routeModeUiState = routeState.routeModeUiState
                            .withTrackingState(trackingServiceStateReader.isTracking.value),
                        hasCenteredOnCurrentLocation = false
                    )
                }
            }
        }
    }

    private fun observeTrackingState() {
        viewModelScope.launch {
            trackingServiceStateReader.isTracking.collectLatest { isTracking ->
                _uiState.update { currentState ->
                    currentState.copy(
                        routeModeUiState = currentState.routeModeUiState.withTrackingState(isTracking)
                    )
                }
            }
        }
    }

    private fun toggleTracking() {
        if (_uiState.value.permissionState != LocationPermissionUiState.ALWAYS) {
            _uiState.update { currentState ->
                currentState.copy(showTrackingPermissionDialog = true)
            }
            return
        }

        when (val routeMode = _uiState.value.routeModeUiState) {
            is MainRouteModeUiState.Today -> {
                if (routeMode.isTrackingEnabled) {
                    stopTracking()
                } else {
                    startTracking()
                }
            }

            is MainRouteModeUiState.Past -> Unit
        }
    }
}

private fun MainRouteModeUiState.withTrackingState(isTracking: Boolean): MainRouteModeUiState {
    return when (this) {
        is MainRouteModeUiState.Today -> copy(isTrackingEnabled = isTracking)
        is MainRouteModeUiState.Past -> this
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
                ),
                trackingServiceStateReader = appContainer.locationTrackingServiceStateReader,
                startTracking = appContainer.startLocationTrackingUseCase::invoke,
                stopTracking = appContainer.stopLocationTrackingUseCase::invoke
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
