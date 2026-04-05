package com.example.passedpath.feature.main.presentation.state

import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.route.presentation.coordinator.RouteDebugSnapshot
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.PlaceMarkerUiState
import com.example.passedpath.feature.route.presentation.state.SelectedDayRouteUiState

data class MainCoordinateUiState(
    val latitude: Double,
    val longitude: Double
)

data class MainUiState(
    val permissionState: LocationPermissionUiState = LocationPermissionUiState.DENIED,
    val isLocationServiceEnabled: Boolean = true,
    val isTrackingActive: Boolean = false,
    val currentLocation: MainCoordinateUiState? = null,
    val hasCenteredOnCurrentLocation: Boolean = false,
    val showTrackingPermissionDialog: Boolean = false,
    val selectedDateKey: String = "",
    val routeModeUiState: MainRouteModeUiState = MainRouteModeUiState.Today(
        route = SelectedDayRouteUiState(dateKey = "")
    ),
    val debugUiState: MainDebugUiState = MainDebugUiState()
) {
    val selectedRoute: SelectedDayRouteUiState
        get() = routeModeUiState.route

    val mapPlaces: List<PlaceMarkerUiState>
        get() = selectedRoute.markerPlaces

    val isRouteLoading: Boolean
        get() = routeModeUiState.isRouteLoading

    val isRouteEmpty: Boolean
        get() = routeModeUiState.isRouteEmpty

    val routeEmptyMessage: String?
        get() = routeModeUiState.routeEmptyMessage

    val routeErrorMessage: String?
        get() = routeModeUiState.routeErrorMessage
}

data class MainDebugUiState(
    val selectedDateKey: String = "",
    val routeMode: String = "today",
    val routeSource: String = "local",
    val routeStatus: String = "idle",
    val permissionState: LocationPermissionUiState = LocationPermissionUiState.DENIED,
    val isLocationServiceEnabled: Boolean = false,
    val isTrackingActive: Boolean = false,
    val isTrackingEnabledByUser: Boolean = true,
    val lastRouteMessage: String? = null,
    val recentTrackingEvents: List<String> = emptyList()
)

internal fun createMainDebugUiState(
    selectedDateKey: String,
    routeModeUiState: MainRouteModeUiState,
    permissionState: LocationPermissionUiState,
    isLocationServiceEnabled: Boolean,
    isTrackingActive: Boolean,
    isTrackingEnabledByUser: Boolean,
    routeDebugSnapshot: RouteDebugSnapshot?,
    recentTrackingEvents: List<String> = emptyList()
): MainDebugUiState {
    val defaultSource = when (routeModeUiState) {
        is MainRouteModeUiState.Today -> "local"
        is MainRouteModeUiState.Past -> "remote"
    }

    val defaultStatus = when {
        routeModeUiState.isRouteLoading -> "loading"
        routeModeUiState.routeErrorMessage != null -> "error"
        routeModeUiState.isRouteEmpty -> "empty"
        routeModeUiState.route.hasLocationData -> "success"
        else -> "idle"
    }

    return MainDebugUiState(
        selectedDateKey = selectedDateKey,
        routeMode = when (routeModeUiState) {
            is MainRouteModeUiState.Today -> "today"
            is MainRouteModeUiState.Past -> "past"
        },
        routeSource = routeDebugSnapshot?.source ?: defaultSource,
        routeStatus = routeDebugSnapshot?.status ?: defaultStatus,
        permissionState = permissionState,
        isLocationServiceEnabled = isLocationServiceEnabled,
        isTrackingActive = isTrackingActive,
        isTrackingEnabledByUser = isTrackingEnabledByUser,
        lastRouteMessage = routeDebugSnapshot?.message,
        recentTrackingEvents = recentTrackingEvents
    )
}
