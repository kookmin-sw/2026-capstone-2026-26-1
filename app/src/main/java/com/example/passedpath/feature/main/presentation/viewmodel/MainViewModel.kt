package com.example.passedpath.feature.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.debug.AppDebugLogger
import com.example.passedpath.debug.DebugLogTag
import com.example.passedpath.feature.locationtracking.data.manager.LocationTrackingServiceStateReader
import com.example.passedpath.feature.main.presentation.policy.TrackingToggleDecision
import com.example.passedpath.feature.main.presentation.policy.decideTrackingToggle
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.main.presentation.state.withDebugState
import com.example.passedpath.feature.permission.data.manager.LocationPermissionStatusReader
import com.example.passedpath.feature.permission.data.manager.LocationServiceStatusReader
import com.example.passedpath.feature.permission.presentation.policy.resolveLocationPermissionUiState
import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState
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
    private val locationServiceStatusReader: LocationServiceStatusReader,
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
            isLocationServiceEnabled = locationServiceStatusReader.isLocationServiceEnabled(),
            isTrackingActive = trackingServiceStateReader.isTracking.value,
            selectedDateKey = initialDateKey,
            routeModeUiState = routeStateCoordinator
                .createInitialState(initialDateKey)
                .withTrackingState(trackingServiceStateReader.isTracking.value)
        ).withDebugState(
            isTrackingEnabledByUser = userTrackingEnabled()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshPermissionState()
        refreshLocationServiceState()
        observeTrackingState()
        loadDayRoute(initialDateKey)
    }

    fun refreshPermissionState() {
        val permissionState = resolveLocationPermissionUiState(
            isBackgroundAlwaysGranted = locationPermissionStatusReader.isBackgroundAlwaysGranted(),
            isForegroundGranted = locationPermissionStatusReader.isForegroundGranted()
        )
        AppDebugLogger.debug(
            DebugLogTag.PERMISSION,
            "refreshPermissionState result=$permissionState"
        )

        _uiState.update { currentState ->
            val nextState = if (permissionState == LocationPermissionUiState.DENIED) {
                currentState.copy(
                    permissionState = permissionState,
                    currentLocation = null,
                    hasCenteredOnCurrentLocation = false
                )
            } else {
                currentState.copy(permissionState = permissionState)
            }
            nextState.withDebugState(isTrackingEnabledByUser = userTrackingEnabled())
        }
    }

    fun refreshLocationServiceState() {
        val isEnabled = locationServiceStatusReader.isLocationServiceEnabled()
        AppDebugLogger.debug(
            DebugLogTag.PERMISSION,
            "refreshLocationServiceState enabled=$isEnabled"
        )
        _uiState.update { currentState ->
            currentState.copy(isLocationServiceEnabled = isEnabled).withDebugState(
                isTrackingEnabledByUser = userTrackingEnabled()
            )
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
        AppDebugLogger.debug(
            DebugLogTag.MAIN_FLOW,
            "selectDate dateKey=$dateKey previous=${_uiState.value.selectedDateKey}"
        )
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
            currentState.copy(showTrackingPermissionDialog = false).withDebugState(
                isTrackingEnabledByUser = userTrackingEnabled()
            )
        }
    }

    private fun loadDayRoute(dateKey: String) {
        if (routeLoadJob != null) {
            AppDebugLogger.debug(
                DebugLogTag.MAIN_FLOW,
                "cancel stale route load previousDateKey=${_uiState.value.selectedDateKey}"
            )
        }
        routeLoadJob?.cancel()
        routeLoadJob = viewModelScope.launch {
            AppDebugLogger.debug(
                DebugLogTag.MAIN_FLOW,
                "loadDayRoute begin dateKey=$dateKey"
            )
            routeStateCoordinator.loadRoute(dateKey).collect { routeState ->
                AppDebugLogger.debug(
                    DebugLogTag.MAIN_FLOW,
                    "loadDayRoute update dateKey=${routeState.selectedDateKey} status=${routeState.debugSnapshot?.status ?: "unknown"}"
                )
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedDateKey = routeState.selectedDateKey,
                        routeModeUiState = routeState.routeModeUiState
                            .withTrackingState(trackingServiceStateReader.isTracking.value),
                        hasCenteredOnCurrentLocation = false
                    ).withDebugState(
                        isTrackingEnabledByUser = userTrackingEnabled(),
                        routeDebugSnapshot = routeState.debugSnapshot
                    )
                }
            }
        }
    }

    private fun observeTrackingState() {
        viewModelScope.launch {
            trackingServiceStateReader.isTracking.collectLatest { isTracking ->
                AppDebugLogger.debug(
                    DebugLogTag.TRACKING,
                    "trackingState changed active=$isTracking userEnabled=${userTrackingEnabled()}"
                )
                _uiState.update { currentState ->
                    currentState.copy(
                        isTrackingActive = isTracking,
                        routeModeUiState = currentState.routeModeUiState.withTrackingState(isTracking)
                    ).withDebugState(
                        isTrackingEnabledByUser = userTrackingEnabled()
                    )
                }
            }
        }
    }

    private fun toggleTracking() {
        val decision = decideTrackingToggle(
            permissionState = _uiState.value.permissionState,
            routeModeUiState = _uiState.value.routeModeUiState
        )
        AppDebugLogger.debug(
            DebugLogTag.TRACKING,
            "toggleTracking decision=$decision permission=${_uiState.value.permissionState} mode=${_uiState.value.routeModeUiState::class.java.simpleName}"
        )
        when (decision) {
            TrackingToggleDecision.ShowPermissionDialog -> {
                _uiState.update { currentState ->
                    currentState.copy(showTrackingPermissionDialog = true).withDebugState(
                        isTrackingEnabledByUser = userTrackingEnabled()
                    )
                }
            }

            TrackingToggleDecision.StartTracking -> startTracking()
            TrackingToggleDecision.StopTracking -> stopTracking()
            TrackingToggleDecision.NoOp -> Unit
        }
    }

    private fun userTrackingEnabled(): Boolean {
        return trackingServiceStateReader.isTrackingEnabledByUser()
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
                locationServiceStatusReader = appContainer.locationServiceStatusReader,
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
