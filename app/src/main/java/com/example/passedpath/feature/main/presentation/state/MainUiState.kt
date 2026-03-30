package com.example.passedpath.feature.main.presentation.state

data class MainCoordinateUiState(
    val latitude: Double,
    val longitude: Double
)

data class PlaceMarkerUiState(
    val placeId: Long,
    val placeName: String,
    val latitude: Double,
    val longitude: Double,
    val orderIndex: Int
)

data class SelectedDayRouteUiState(
    val dateKey: String,
    val polylinePoints: List<MainCoordinateUiState> = emptyList(),
    val totalDistanceKm: Double = 0.0,
    val pathPointCount: Int = 0,
    val places: List<PlaceMarkerUiState> = emptyList()
) {
    val hasLocationData: Boolean
        get() = polylinePoints.isNotEmpty()
}

sealed interface MainRouteModeUiState {
    val route: SelectedDayRouteUiState
    val isRouteLoading: Boolean
    val isRouteEmpty: Boolean
    val routeEmptyMessage: String?
    val routeErrorMessage: String?

    data class Today(
        override val route: SelectedDayRouteUiState,
        override val isRouteLoading: Boolean = false,
        override val isRouteEmpty: Boolean = false,
        override val routeEmptyMessage: String? = null,
        override val routeErrorMessage: String? = null,
        val canRefreshDistance: Boolean = true,
        val isTrackingToggleVisible: Boolean = true
    ) : MainRouteModeUiState

    data class Past(
        override val route: SelectedDayRouteUiState,
        override val isRouteLoading: Boolean = false,
        override val isRouteEmpty: Boolean = false,
        override val routeEmptyMessage: String? = null,
        override val routeErrorMessage: String? = null,
        val isPlaybackEntryVisible: Boolean = true
    ) : MainRouteModeUiState
}

data class MainUiState(
    val permissionState: LocationPermissionUiState = LocationPermissionUiState.DENIED,
    val currentLocation: MainCoordinateUiState? = null,
    val hasCenteredOnCurrentLocation: Boolean = false,
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
}
