package com.example.passedpath.feature.main.presentation.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import com.example.passedpath.feature.locationtracking.domain.tracker.LocationTracker
import com.example.passedpath.feature.permission.presentation.policy.canReceiveLocationUpdates
import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState
import com.example.passedpath.ui.state.CoordinateUiState

@Composable
internal fun MainInitialLocationEffect(
    permissionState: LocationPermissionUiState,
    isLocationServiceEnabled: Boolean,
    currentLocationState: State<CoordinateUiState?>,
    onCurrentLocationUpdated: (CoordinateUiState) -> Unit,
    locationTracker: LocationTracker
) {
    val currentLocation = currentLocationState.value

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
