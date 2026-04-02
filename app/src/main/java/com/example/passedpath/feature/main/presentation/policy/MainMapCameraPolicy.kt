package com.example.passedpath.feature.main.presentation.policy

import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

private const val RouteBoundsPaddingPx = 180

internal fun shouldCenterOnRoute(
    isMapLoaded: Boolean,
    routePoints: List<LatLng>
): Boolean {
    return isMapLoaded && routePoints.isNotEmpty()
}

internal fun shouldCenterOnCurrentLocation(
    isMapLoaded: Boolean,
    routePoints: List<LatLng>,
    currentLocation: MainCoordinateUiState?,
    hasCenteredOnCurrentLocation: Boolean
): Boolean {
    return isMapLoaded &&
        routePoints.isEmpty() &&
        currentLocation != null &&
        !hasCenteredOnCurrentLocation
}

internal fun createRouteCameraUpdate(routePoints: List<LatLng>): CameraUpdate {
    return when {
        routePoints.isEmpty() -> CameraUpdateFactory.zoomTo(15f)
        routePoints.size == 1 -> CameraUpdateFactory.newLatLngZoom(routePoints.first(), 14f)
        else -> {
            val boundsBuilder = LatLngBounds.builder()
            routePoints.forEach(boundsBuilder::include)
            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), RouteBoundsPaddingPx)
        }
    }
}
