package com.example.passedpath.feature.permission.presentation.policy

import com.example.passedpath.feature.permission.data.manager.LocationPermissionStatusReader
import com.example.passedpath.feature.permission.data.manager.LocationServiceStatusReader
import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState

class LocationAccessStateResolver(
    private val locationPermissionStatusReader: LocationPermissionStatusReader,
    private val locationServiceStatusReader: LocationServiceStatusReader
) {
    fun resolvePermissionState(): LocationPermissionUiState {
        return resolveLocationPermissionUiState(
            isBackgroundAlwaysGranted = locationPermissionStatusReader.isBackgroundAlwaysGranted(),
            isForegroundGranted = locationPermissionStatusReader.isForegroundGranted()
        )
    }

    fun isLocationServiceEnabled(): Boolean {
        return locationServiceStatusReader.isLocationServiceEnabled()
    }
}
