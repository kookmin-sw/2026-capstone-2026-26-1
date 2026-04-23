package com.example.passedpath.feature.route.presentation.state

import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState

data class PlaceMarkerUiState(
    val placeId: Long,
    val placeName: String,
    val roadAddress: String,
    val latitude: Double,
    val longitude: Double,
    val orderIndex: Int
)

data class RoutePolylineSegmentUiState(
    val start: MainCoordinateUiState,
    val end: MainCoordinateUiState
)

data class SelectedDayRouteUiState(
    val dateKey: String,
    val title: String = "",
    val memo: String = "",
    val isBookmarked: Boolean = false,
    val polylinePoints: List<MainCoordinateUiState> = emptyList(),
    val routeSegments: List<RoutePolylineSegmentUiState> = emptyList(),
    val totalDistanceKm: Double = 0.0,
    val pathPointCount: Int = 0,
    val markerPlaces: List<PlaceMarkerUiState> = emptyList()
) {
    val hasLocationData: Boolean
        get() = polylinePoints.isNotEmpty()

    // Route owns the initial marker seed. Place-sheet-specific read state will split later.
    val places: List<PlaceMarkerUiState>
        get() = markerPlaces
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
        val isTrackingToggleVisible: Boolean = true,
        val isTrackingEnabled: Boolean = false
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

sealed interface RouteUiAction {
    data object RefreshTodayRoute : RouteUiAction
    data object ToggleTracking : RouteUiAction
    data object RetryPastRoute : RouteUiAction
    data object EnterPastPlayback : RouteUiAction
}
