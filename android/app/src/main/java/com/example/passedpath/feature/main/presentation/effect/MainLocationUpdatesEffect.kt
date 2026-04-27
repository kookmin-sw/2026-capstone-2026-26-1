package com.example.passedpath.feature.main.presentation.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.example.passedpath.feature.locationtracking.domain.tracker.LocationTracker
import com.example.passedpath.feature.permission.presentation.policy.canReceiveLocationUpdates
import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState
import com.example.passedpath.ui.state.CoordinateUiState

@Composable
internal fun MainLocationUpdatesEffect(
    permissionState: LocationPermissionUiState,
    isLocationServiceEnabled: Boolean,
    locationTracker: LocationTracker,
    onCurrentLocationUpdated: (CoordinateUiState) -> Unit
) {
    DisposableEffect(permissionState, isLocationServiceEnabled, locationTracker) {
        if (!canReceiveLocationUpdates(permissionState, isLocationServiceEnabled)) {
            onDispose { }
        } else {
            val trackingSession = locationTracker.startLocationUpdates { trackedLocation ->
                onCurrentLocationUpdated(trackedLocation.toCoordinateUiState())
            }

            onDispose {
                trackingSession.stop()
            }
        }
    }
}
