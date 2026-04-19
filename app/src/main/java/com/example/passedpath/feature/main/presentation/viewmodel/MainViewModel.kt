package com.example.passedpath.feature.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.debug.AppDebugLogger
import com.example.passedpath.debug.DebugLogTag
import com.example.passedpath.feature.locationtracking.data.manager.LocationTrackingServiceStateReader
import com.example.passedpath.feature.locationtracking.domain.usecase.ObserveRecentTrackingEventsUseCase
import com.example.passedpath.feature.main.presentation.policy.MainRouteActionRequest
import com.example.passedpath.feature.main.presentation.policy.RouteReloadTrigger
import com.example.passedpath.feature.main.presentation.policy.TrackingToggleDecision
import com.example.passedpath.feature.main.presentation.policy.createRouteReloadRequest
import com.example.passedpath.feature.main.presentation.policy.decideTrackingToggle
import com.example.passedpath.feature.main.presentation.policy.resolveCameraIntentAfterRouteState
import com.example.passedpath.feature.main.presentation.policy.resolveMainRouteActionRequest
import com.example.passedpath.feature.main.presentation.policy.shouldRequestCurrentLocationCamera
import com.example.passedpath.feature.main.presentation.state.MainCameraIntent
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.main.presentation.state.toPlaceMarkerUiState
import com.example.passedpath.feature.main.presentation.state.withDebugState
import com.example.passedpath.feature.place.domain.model.VisitedPlace
import com.example.passedpath.feature.permission.data.manager.LocationPermissionStatusReader
import com.example.passedpath.feature.permission.data.manager.LocationServiceStatusReader
import com.example.passedpath.feature.permission.presentation.policy.resolveLocationPermissionUiState
import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.route.presentation.coordinator.RouteLoadState
import com.example.passedpath.feature.route.presentation.coordinator.RouteStateCoordinator
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.feature.route.presentation.state.SelectedDayRouteUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.Flow
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
    private val observeRecentTrackingEvents: ObserveRecentTrackingEventsUseCase,
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
        observeTrackingDebugLogs()
        reloadRoute(
            createRouteReloadRequest(
                dateKey = initialDateKey,
                trigger = RouteReloadTrigger.InitialLoad
            )
        )
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
                    pendingCameraIntent = null
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
            currentState.copy(
                currentLocation = location,
                pendingCameraIntent = when {
                    shouldRequestCurrentLocationCamera(
                        currentRouteHasLocationData = currentState.selectedRoute.hasLocationData,
                        previousLocation = currentState.currentLocation
                    ) -> MainCameraIntent.CenterCurrentLocation

                    else -> currentState.pendingCameraIntent
                }
            )
        }
    }

    fun consumeCameraIntent() {
        _uiState.update { currentState ->
            currentState.copy(pendingCameraIntent = null)
        }
    }

    fun selectDate(dateKey: String) {
        AppDebugLogger.debug(
            DebugLogTag.MAIN_FLOW,
            "selectDate dateKey=$dateKey previous=${_uiState.value.selectedDateKey}"
        )
        reloadRoute(
            createRouteReloadRequest(
                dateKey = dateKey,
                trigger = RouteReloadTrigger.DateSelection
            )
        )
    }

    fun updateFetchedMapPlaces(dateKey: String, places: List<VisitedPlace>) {
        _uiState.update { currentState ->
            if (currentState.selectedDateKey != dateKey) {
                currentState
            } else {
                currentState.copy(
                    fetchedMapPlaces = places
                        .sortedBy(VisitedPlace::orderIndex)
                        .map(VisitedPlace::toPlaceMarkerUiState)
                )
            }
        }
    }

    fun clearFetchedMapPlaces(dateKey: String) {
        _uiState.update { currentState ->
            if (currentState.selectedDateKey != dateKey) {
                currentState
            } else {
                currentState.copy(fetchedMapPlaces = null)
            }
        }
    }

    fun applyDayNoteSnapshotPatch(
        dateKey: String,
        title: String? = null,
        memo: String? = null,
        shouldUpdateTitle: Boolean = false,
        shouldUpdateMemo: Boolean = false
    ) {
        if (!shouldUpdateTitle && !shouldUpdateMemo) return

        _uiState.update { currentState ->
            if (currentState.selectedDateKey != dateKey) {
                currentState
            } else {
                currentState.copy(
                    routeModeUiState = currentState.routeModeUiState.updateRouteSnapshot { route ->
                        route.copy(
                            title = if (shouldUpdateTitle) title.orEmpty() else route.title,
                            memo = if (shouldUpdateMemo) memo.orEmpty() else route.memo
                        )
                    }
                )
            }
        }
    }

    fun handleRouteAction(action: RouteUiAction) {
        executeRouteAction(
            resolveMainRouteActionRequest(
                action = action,
                selectedDateKey = _uiState.value.selectedDateKey
            )
        )
    }

    fun dismissTrackingPermissionDialog() {
        _uiState.update { currentState ->
            currentState.copy(showTrackingPermissionDialog = false).withDebugState(
                isTrackingEnabledByUser = userTrackingEnabled()
            )
        }
    }

    private fun executeRouteAction(request: MainRouteActionRequest) {
        when (request) {
            is MainRouteActionRequest.ReloadRoute -> reloadRoute(request)
            MainRouteActionRequest.ToggleTracking -> toggleTracking()
            MainRouteActionRequest.OpenPastPlayback -> Unit
        }
    }

    private fun reloadRoute(request: MainRouteActionRequest.ReloadRoute) {
        cancelPreviousRouteReloadIfNeeded()
        routeLoadJob = viewModelScope.launch {
            AppDebugLogger.debug(
                DebugLogTag.MAIN_FLOW,
                "reloadRoute begin dateKey=${request.dateKey} trigger=${request.trigger}"
            )
            collectRouteState(request)
        }
    }

    private fun cancelPreviousRouteReloadIfNeeded() {
        if (routeLoadJob != null) {
            AppDebugLogger.debug(
                DebugLogTag.MAIN_FLOW,
                "cancel stale route load previousDateKey=${_uiState.value.selectedDateKey}"
            )
        }
        routeLoadJob?.cancel()
    }

    private suspend fun collectRouteState(request: MainRouteActionRequest.ReloadRoute) {
        routeLoadFlow(request.dateKey).collect { routeState ->
            AppDebugLogger.debug(
                DebugLogTag.MAIN_FLOW,
                "reloadRoute update dateKey=${routeState.selectedDateKey} trigger=${request.trigger} status=${routeState.debugSnapshot?.status ?: "unknown"}"
            )
            applyLoadedRouteState(routeState)
        }
    }

    private fun routeLoadFlow(dateKey: String): Flow<RouteLoadState> {
        return routeStateCoordinator.loadRoute(dateKey)
    }

    private fun applyLoadedRouteState(
        routeState: RouteLoadState
    ) {
        _uiState.update { currentState ->
            val nextCameraIntent = resolveCameraIntentAfterRouteState(
                currentDateKey = currentState.selectedDateKey,
                currentRouteHasLocationData = currentState.selectedRoute.hasLocationData,
                currentLocation = currentState.currentLocation,
                routeState = routeState
            )
            currentState.copy(
                selectedDateKey = routeState.selectedDateKey,
                fetchedMapPlaces = if (currentState.selectedDateKey == routeState.selectedDateKey) {
                    currentState.fetchedMapPlaces
                } else {
                    null
                },
                routeModeUiState = routeState.routeModeUiState
                    .withTrackingState(trackingServiceStateReader.isTracking.value),
                pendingCameraIntent = nextCameraIntent ?: currentState.pendingCameraIntent
            ).withDebugState(
                isTrackingEnabledByUser = userTrackingEnabled(),
                routeDebugSnapshot = routeState.debugSnapshot
            )
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

    private fun observeTrackingDebugLogs() {
        viewModelScope.launch {
            observeRecentTrackingEvents(limit = 5).collectLatest { recentEvents ->
                _uiState.update { currentState ->
                    currentState.withDebugState(
                        isTrackingEnabledByUser = userTrackingEnabled(),
                        recentTrackingEvents = recentEvents
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

private fun MainRouteModeUiState.updateRouteSnapshot(
    transform: (SelectedDayRouteUiState) -> SelectedDayRouteUiState
): MainRouteModeUiState {
    return when (this) {
        is MainRouteModeUiState.Today -> copy(route = transform(route))
        is MainRouteModeUiState.Past -> copy(route = transform(route))
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
                observeRecentTrackingEvents = appContainer.observeRecentTrackingEventsUseCase,
                trackingServiceStateReader = appContainer.locationTrackingServiceStateReader,
                startTracking = appContainer.startLocationTrackingUseCase::invoke,
                stopTracking = appContainer.stopLocationTrackingUseCase::invoke
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
