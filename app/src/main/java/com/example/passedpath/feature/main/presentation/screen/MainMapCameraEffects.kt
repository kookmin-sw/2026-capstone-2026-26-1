package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.passedpath.feature.main.presentation.policy.createRouteCameraUpdate
import com.example.passedpath.feature.main.presentation.policy.shouldCenterOnCurrentLocation
import com.example.passedpath.feature.main.presentation.policy.shouldCenterOnRoute
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState

@Composable
internal fun MainMapCameraEffects(
    isMapLoaded: Boolean,
    selectedDateKey: String,
    routePoints: List<LatLng>,
    currentLocation: MainCoordinateUiState?,
    hasCenteredOnCurrentLocation: Boolean,
    cameraPositionState: CameraPositionState,
    onInitialCameraCentered: () -> Unit
) {
    LaunchedEffect(isMapLoaded, selectedDateKey, routePoints) {
        if (shouldCenterOnRoute(isMapLoaded = isMapLoaded, routePoints = routePoints)) {
            cameraPositionState.move(createRouteCameraUpdate(routePoints))
            onInitialCameraCentered()
        }
    }

    LaunchedEffect(
        isMapLoaded,
        currentLocation,
        hasCenteredOnCurrentLocation,
        routePoints
    ) {
        if (
            shouldCenterOnCurrentLocation(
                isMapLoaded = isMapLoaded,
                routePoints = routePoints,
                currentLocation = currentLocation,
                hasCenteredOnCurrentLocation = hasCenteredOnCurrentLocation
            )
        ) {
            val resolvedCurrentLocation = requireNotNull(currentLocation)
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(resolvedCurrentLocation.toLatLng(), 17f)
            )
            onInitialCameraCentered()
        }
    }
}
