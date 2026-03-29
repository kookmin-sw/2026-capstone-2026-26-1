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
)

data class MainUiState(
    val permissionState: LocationPermissionUiState = LocationPermissionUiState.DENIED,
    val currentLocation: MainCoordinateUiState? = null,
    val hasCenteredOnCurrentLocation: Boolean = false,
    val selectedDateKey: String = "",
    val selectedRoute: SelectedDayRouteUiState = SelectedDayRouteUiState(dateKey = ""),
    val isRouteLoading: Boolean = false,
    val isRouteEmpty: Boolean = false,
    val routeEmptyMessage: String? = null,
    val routeErrorMessage: String? = null
)
