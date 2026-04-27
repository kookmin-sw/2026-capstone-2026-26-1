package com.example.passedpath.feature.main.presentation.policy

import com.example.passedpath.feature.main.presentation.state.MainCameraIntent
import com.example.passedpath.feature.route.presentation.coordinator.RouteLoadState
import com.example.passedpath.ui.state.CoordinateUiState
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

private const val RouteBoundsPaddingPx = 180

internal fun resolveCameraIntentAfterRouteState(
    currentDateKey: String,
    currentRouteHasLocationData: Boolean,
    currentLocation: CoordinateUiState?,
    routeState: RouteLoadState
): MainCameraIntent? {
    if (routeState.routeModeUiState.isRouteLoading) {
        return null
    }

    val nextRouteHasLocationData = routeState.routeModeUiState.route.hasLocationData
    val didDateChange = currentDateKey != routeState.selectedDateKey
    val didRouteBecomeAvailable = !currentRouteHasLocationData && nextRouteHasLocationData

    return when {
        nextRouteHasLocationData && (didDateChange || didRouteBecomeAvailable) -> MainCameraIntent.FitRoute
        didDateChange && currentLocation != null -> MainCameraIntent.CenterCurrentLocation
        else -> null
    }
}

internal fun shouldRequestCurrentLocationCamera(
    currentRouteHasLocationData: Boolean,
    previousLocation: CoordinateUiState?
): Boolean {
    return !currentRouteHasLocationData && previousLocation == null
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
