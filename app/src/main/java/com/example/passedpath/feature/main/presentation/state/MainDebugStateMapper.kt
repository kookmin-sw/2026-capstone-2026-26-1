package com.example.passedpath.feature.main.presentation.state

import com.example.passedpath.feature.route.presentation.coordinator.RouteDebugSnapshot

internal fun MainUiState.withDebugState(
    isTrackingEnabledByUser: Boolean,
    routeDebugSnapshot: RouteDebugSnapshot? = debugUiState.toRouteDebugSnapshot()
): MainUiState {
    return copy(
        debugUiState = createMainDebugUiState(
            selectedDateKey = selectedDateKey,
            routeModeUiState = routeModeUiState,
            permissionState = permissionState,
            isLocationServiceEnabled = isLocationServiceEnabled,
            isTrackingActive = isTrackingActive,
            isTrackingEnabledByUser = isTrackingEnabledByUser,
            routeDebugSnapshot = routeDebugSnapshot
        )
    )
}

private fun MainDebugUiState.toRouteDebugSnapshot(): RouteDebugSnapshot? {
    return if (selectedDateKey.isBlank()) {
        null
    } else {
        RouteDebugSnapshot(
            source = routeSource,
            status = routeStatus,
            message = lastRouteMessage
        )
    }
}
