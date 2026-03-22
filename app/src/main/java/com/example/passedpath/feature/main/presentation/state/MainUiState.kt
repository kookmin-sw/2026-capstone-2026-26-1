package com.example.passedpath.feature.main.presentation.state

data class MainCoordinateUiState(
    val latitude: Double,
    val longitude: Double
)

data class DailyPathUiState(
    val dateKey: String,
    val points: List<MainCoordinateUiState> = emptyList()
)

data class MainUiState(
    val permissionState: LocationPermissionUiState = LocationPermissionUiState.DENIED,
    val currentLocation: MainCoordinateUiState? = null,
    val hasCenteredOnCurrentLocation: Boolean = false,
    val todayPath: DailyPathUiState = DailyPathUiState(dateKey = "")
)
