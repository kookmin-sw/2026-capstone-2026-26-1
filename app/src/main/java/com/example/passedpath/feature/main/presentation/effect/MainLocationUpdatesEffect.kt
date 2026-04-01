package com.example.passedpath.feature.main.presentation.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.example.passedpath.feature.locationtracking.domain.tracker.LocationTracker
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState

@Composable
internal fun MainLocationUpdatesEffect(
    permissionState: LocationPermissionUiState,
    isLocationServiceEnabled: Boolean,
    locationTracker: LocationTracker,
    onCurrentLocationUpdated: (MainCoordinateUiState) -> Unit
) {
    DisposableEffect(permissionState, isLocationServiceEnabled, locationTracker) {
        val canReceiveLocationUpdates =
            (permissionState == LocationPermissionUiState.ALWAYS ||
                permissionState == LocationPermissionUiState.FOREGROUND_ONLY) &&
                isLocationServiceEnabled

        if (!canReceiveLocationUpdates) {
            onDispose { }
        } else {
            val trackingSession = locationTracker.startLocationUpdates { trackedLocation ->
                onCurrentLocationUpdated(trackedLocation.toMainCoordinateUiState())
            }

            onDispose {
                trackingSession.stop()
            }
        }
    }
}
