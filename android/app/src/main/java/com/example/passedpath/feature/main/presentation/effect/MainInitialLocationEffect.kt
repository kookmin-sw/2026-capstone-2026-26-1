package com.example.passedpath.feature.main.presentation.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.passedpath.feature.locationtracking.domain.tracker.LocationTracker
import com.example.passedpath.feature.permission.presentation.policy.canReceiveLocationUpdates
import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState
import com.example.passedpath.ui.state.CoordinateUiState

@Composable
internal fun MainInitialLocationEffect(
    permissionState: LocationPermissionUiState,
    isLocationServiceEnabled: Boolean,
    currentLocation: CoordinateUiState?,
    onCurrentLocationUpdated: (CoordinateUiState) -> Unit,
    locationTracker: LocationTracker
) {
    LaunchedEffect(permissionState, isLocationServiceEnabled, currentLocation) {
        if (
            canReceiveLocationUpdates(
                permissionState = permissionState,
                isLocationServiceEnabled = isLocationServiceEnabled
            ) && currentLocation == null
        ) {
            locationTracker.getCurrentLocation()?.let { trackedLocation ->
                onCurrentLocationUpdated(trackedLocation.toCoordinateUiState())
            }
        }
    }
}
