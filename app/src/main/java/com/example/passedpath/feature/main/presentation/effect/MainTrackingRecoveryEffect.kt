package com.example.passedpath.feature.main.presentation.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.passedpath.feature.locationtracking.data.manager.LocationTrackingServiceStateReader
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState

@Composable
internal fun MainTrackingRecoveryEffect(
    permissionState: LocationPermissionUiState,
    isLocationServiceEnabled: Boolean,
    isTrackingActive: Boolean,
    trackingServiceStateReader: LocationTrackingServiceStateReader,
    startLocationTracking: (Boolean) -> Unit,
    stopLocationTracking: (Boolean) -> Unit
) {
    LaunchedEffect(permissionState, isLocationServiceEnabled, isTrackingActive) {
        val canRunTracking =
            permissionState == LocationPermissionUiState.ALWAYS && isLocationServiceEnabled

        if (canRunTracking) {
            if (trackingServiceStateReader.isTrackingEnabledByUser() && !isTrackingActive) {
                startLocationTracking(false)
            } else if (!trackingServiceStateReader.isTrackingEnabledByUser() && isTrackingActive) {
                stopLocationTracking(false)
            }
        } else if (isTrackingActive) {
            stopLocationTracking(false)
        }
    }
}
