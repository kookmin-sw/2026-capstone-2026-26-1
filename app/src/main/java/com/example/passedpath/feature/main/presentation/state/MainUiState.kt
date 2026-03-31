package com.example.passedpath.feature.main.presentation.state

import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.SelectedDayRouteUiState

data class MainCoordinateUiState(
    val latitude: Double,
    val longitude: Double
)

data class MainUiState(
    val permissionState: LocationPermissionUiState = LocationPermissionUiState.DENIED,
    val currentLocation: MainCoordinateUiState? = null,
    val hasCenteredOnCurrentLocation: Boolean = false,
    val showTrackingPermissionDialog: Boolean = false,
    val isForegroundPermissionBannerDismissed: Boolean = false,
    val selectedDateKey: String = "",
    val routeModeUiState: MainRouteModeUiState = MainRouteModeUiState.Today(
        route = SelectedDayRouteUiState(dateKey = "")
    )
) {
    val selectedRoute: SelectedDayRouteUiState
        get() = routeModeUiState.route

    val isRouteLoading: Boolean
        get() = routeModeUiState.isRouteLoading

    val isRouteEmpty: Boolean
        get() = routeModeUiState.isRouteEmpty

    val routeEmptyMessage: String?
        get() = routeModeUiState.routeEmptyMessage

    val routeErrorMessage: String?
        get() = routeModeUiState.routeErrorMessage

    val showForegroundPermissionBanner: Boolean
        get() = permissionState == LocationPermissionUiState.FOREGROUND_ONLY &&
            !isForegroundPermissionBannerDismissed
}
