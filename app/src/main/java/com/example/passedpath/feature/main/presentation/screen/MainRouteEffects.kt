package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.runtime.Composable
import com.example.passedpath.feature.locationtracking.data.manager.LocationTrackingServiceStateReader
import com.example.passedpath.feature.locationtracking.domain.tracker.LocationTracker
import com.example.passedpath.feature.main.presentation.effect.MainInitialLocationEffect
import com.example.passedpath.feature.main.presentation.effect.MainLifecycleRefreshEffect
import com.example.passedpath.feature.main.presentation.effect.MainLocationUpdatesEffect
import com.example.passedpath.feature.main.presentation.effect.MainTrackingRecoveryEffect
import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState

@Composable
internal fun MainRouteEffects(
    permissionState: LocationPermissionUiState,
    isLocationServiceEnabled: Boolean,
    currentLocation: MainCoordinateUiState?,
    isTrackingActive: Boolean,
    onRefreshPermissionState: () -> Unit,
    onRefreshLocationServiceState: () -> Unit,
    onCurrentLocationUpdated: (MainCoordinateUiState) -> Unit,
    locationTracker: LocationTracker,
    trackingServiceStateReader: LocationTrackingServiceStateReader,
    startLocationTracking: (Boolean) -> Unit,
    stopLocationTracking: (Boolean) -> Unit
) {
    MainLifecycleRefreshEffect(
        onRefreshPermissionState = onRefreshPermissionState,
        onRefreshLocationServiceState = onRefreshLocationServiceState
    )
    MainInitialLocationEffect(
        permissionState = permissionState,
        isLocationServiceEnabled = isLocationServiceEnabled,
        currentLocation = currentLocation,
        onCurrentLocationUpdated = onCurrentLocationUpdated,
        locationTracker = locationTracker
    )
    MainTrackingRecoveryEffect(
        permissionState = permissionState,
        isLocationServiceEnabled = isLocationServiceEnabled,
        isTrackingActive = isTrackingActive,
        trackingServiceStateReader = trackingServiceStateReader,
        startLocationTracking = startLocationTracking,
        stopLocationTracking = stopLocationTracking
    )
    MainLocationUpdatesEffect(
        permissionState = permissionState,
        isLocationServiceEnabled = isLocationServiceEnabled,
        locationTracker = locationTracker,
        onCurrentLocationUpdated = onCurrentLocationUpdated
    )
}

