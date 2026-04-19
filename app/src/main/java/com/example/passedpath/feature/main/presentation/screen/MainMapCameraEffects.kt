package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.passedpath.feature.main.presentation.policy.createRouteCameraUpdate
import com.example.passedpath.feature.main.presentation.state.MainCameraIntent
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.CameraPositionState

@Composable
internal fun MainMapCameraEffects(
    isMapLoaded: Boolean,
    pendingCameraIntent: MainCameraIntent?,
    routePoints: List<com.google.android.gms.maps.model.LatLng>,
    currentLocation: MainCoordinateUiState?,
    cameraPositionState: CameraPositionState,
    onCameraIntentConsumed: () -> Unit
) {
    LaunchedEffect(isMapLoaded, pendingCameraIntent, routePoints, currentLocation) {
        if (!isMapLoaded) return@LaunchedEffect

        when (pendingCameraIntent) {
            MainCameraIntent.FitRoute -> {
                if (routePoints.isEmpty()) return@LaunchedEffect
                cameraPositionState.move(createRouteCameraUpdate(routePoints))
                onCameraIntentConsumed()
            }

            MainCameraIntent.CenterCurrentLocation -> {
                val resolvedCurrentLocation = currentLocation ?: return@LaunchedEffect
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(resolvedCurrentLocation.toLatLng(), 17f)
                )
                onCameraIntentConsumed()
            }

            null -> Unit
        }
    }
}
