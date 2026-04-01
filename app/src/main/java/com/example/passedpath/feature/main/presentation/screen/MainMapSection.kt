package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.route.presentation.screen.RouteMapContent
import com.example.passedpath.feature.route.presentation.screen.RouteStatusOverlay
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.component.overlay.PermissionOverlay
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

private val CurrentLocationGlowBase = Color(0xFF006B5F)
private const val RouteBoundsPaddingPx = 180

@Composable
internal fun MainMapSection(
    uiState: MainUiState,
    onInitialCameraCentered: () -> Unit,
    onDateSelected: (String) -> Unit,
    onRouteAction: (RouteUiAction) -> Unit,
    onPermissionBannerConfirm: () -> Unit,
    floatingBottomPadding: androidx.compose.ui.unit.Dp
) {
    val routeAccentColor = MaterialTheme.colorScheme.primary
    val fallbackPosition = LatLng(37.5662952, 126.9779451)
    val currentLocation = if (uiState.permissionState == LocationPermissionUiState.DENIED) {
        null
    } else {
        uiState.currentLocation
    }
    val routePoints = uiState.selectedRoute.polylinePoints.map(MainCoordinateUiState::toLatLng)
    val hasRouteLocationData = uiState.selectedRoute.hasLocationData
    val initialCameraTarget = routePoints.firstOrNull() ?: currentLocation?.toLatLng() ?: fallbackPosition
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCameraTarget, 15f)
    }
    val coroutineScope = rememberCoroutineScope()
    var isMapLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(isMapLoaded, uiState.selectedDateKey, routePoints) {
        if (!isMapLoaded) return@LaunchedEffect
        if (routePoints.isNotEmpty()) {
            cameraPositionState.move(buildRouteCameraUpdate(routePoints))
            onInitialCameraCentered()
        }
    }

    LaunchedEffect(isMapLoaded, currentLocation, uiState.hasCenteredOnCurrentLocation, routePoints) {
        if (!isMapLoaded) return@LaunchedEffect
        if (routePoints.isEmpty() && currentLocation != null && !uiState.hasCenteredOnCurrentLocation) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(currentLocation.toLatLng(), 17f)
            )
            onInitialCameraCentered()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            onMapLoaded = { isMapLoaded = true }
        ) {
            RouteMapContent(
                routeModeUiState = uiState.routeModeUiState,
                routeAccentColor = routeAccentColor
            )

            currentLocation?.let {
                MarkerComposable(
                    state = com.google.maps.android.compose.MarkerState(position = it.toLatLng()),
                    title = stringResource(R.string.main_map_marker_title),
                    anchor = Offset(0.5f, 0.58f)
                ) {
                    Box(
                        modifier = Modifier.size(104.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(94.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            CurrentLocationGlowBase.copy(alpha = 0.80f),
                                            CurrentLocationGlowBase.copy(alpha = 0.50f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Image(
                            painter = painterResource(id = R.drawable.current_location_marker),
                            contentDescription = stringResource(R.string.main_map_marker_title),
                            modifier = Modifier.size(70.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }

        RouteStatusOverlay(
            routeModeUiState = uiState.routeModeUiState,
            hasRouteLocationData = hasRouteLocationData,
            onRouteAction = onRouteAction
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MainDateTopBar(
                    selectedDateKey = uiState.selectedDateKey,
                    onDateSelected = onDateSelected
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
                MainRouteFloatingControls(
                    routeMode = uiState.routeModeUiState,
                    onRouteAction = onRouteAction
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                if (currentLocation != null) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(currentLocation.toLatLng(), 17f)
                                )
                            }
                        },
                        modifier = Modifier.padding(bottom = floatingBottomPadding)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.my_location_24px),
                            contentDescription = stringResource(R.string.main_move_to_current_location)
                        )
                    }
                }
            }
        }

        if (uiState.showPermissionOverlay) {
            PermissionOverlay(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = floatingBottomPadding),
                message = permissionOverlayMessage(uiState.permissionState, uiState.isLocationServiceEnabled),
                actionText = permissionOverlayActionText(uiState.permissionState, uiState.isLocationServiceEnabled),
                onClickAction = onPermissionBannerConfirm
            )
        }
    }
}

@Composable
private fun permissionOverlayMessage(
    permissionState: LocationPermissionUiState,
    isLocationServiceEnabled: Boolean
): String {
    return when {
        permissionState == LocationPermissionUiState.DENIED -> stringResource(R.string.permission_banner_denied_title)
        permissionState == LocationPermissionUiState.FOREGROUND_ONLY -> stringResource(R.string.permission_banner_foreground_title)
        !isLocationServiceEnabled -> stringResource(R.string.main_permission_off_title)
        else -> ""
    }
}

@Composable
private fun permissionOverlayActionText(
    permissionState: LocationPermissionUiState,
    isLocationServiceEnabled: Boolean
): String {
    return when {
        permissionState != LocationPermissionUiState.ALWAYS -> stringResource(R.string.permission_banner_action)
        !isLocationServiceEnabled -> stringResource(R.string.permission_banner_action)
        else -> ""
    }
}

private fun MainCoordinateUiState.toLatLng(): LatLng = LatLng(latitude, longitude)

private fun buildRouteCameraUpdate(routePoints: List<LatLng>) = when {
    routePoints.isEmpty() -> CameraUpdateFactory.zoomTo(15f)
    routePoints.size == 1 -> CameraUpdateFactory.newLatLngZoom(routePoints.first(), 14f)
    else -> {
        val boundsBuilder = LatLngBounds.builder()
        routePoints.forEach(boundsBuilder::include)
        CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), RouteBoundsPaddingPx)
    }
}

@Preview(showBackground = true, name = "Permission Overlay")
@Composable
private fun PermissionOverlayPreview() {
    com.example.passedpath.ui.theme.PassedPathTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F6))
                .padding(16.dp)
        ) {
            PermissionOverlay(
                message = stringResource(R.string.permission_banner_foreground_title),
                actionText = stringResource(R.string.permission_banner_action),
                onClickAction = {}
            )
        }
    }
}
