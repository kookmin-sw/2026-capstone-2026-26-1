package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.BuildConfig
import com.example.passedpath.R
import com.example.passedpath.feature.main.presentation.component.MainMorePopupMenu
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.route.presentation.screen.RouteMapContent
import com.example.passedpath.feature.route.presentation.state.PlaceMarkerUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.component.floating.FloatingButtonColumn
import com.example.passedpath.ui.component.floating.FloatingCircleIconButton
import com.example.passedpath.ui.state.CoordinateUiState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
internal fun MainMapSection(
    uiState: MainUiState,
    markerPlaces: List<PlaceMarkerUiState>,
    focusedPlaceId: Long?,
    onFocusedPlaceHandled: () -> Unit,
    onCameraIntentConsumed: () -> Unit,
    onDateSelected: (String) -> Unit,
    onBookmarkClick: () -> Unit,
    onRouteAction: (RouteUiAction) -> Unit,
    onStatsClick: () -> Unit,
    onMoreClick: () -> Unit,
    onMorePlaceBookmarkClick: () -> Unit = {},
    onMoreDeleteRecordClick: () -> Unit = {},
    onMapClick: () -> Unit,
    onPlaceMarkerClick: (Long) -> Unit,
    onPermissionActionClick: () -> Unit,
    debugActions: MainDebugActions,
    floatingBottomPadding: androidx.compose.ui.unit.Dp,
    showCurrentLocationButton: Boolean
) {
    val routeAccentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val fallbackPosition = LatLng(37.5662952, 126.9779451)
    val mapCameraBottomPadding = (BottomSheetMiddleVisibleHeight + BottomSheetFloatingPadding) * 0.3f
    val currentLocationBottomPadding =
        floatingBottomPadding.coerceAtMost(BottomSheetMiddleVisibleHeight + BottomSheetFloatingPadding)
    val currentLocation = if (uiState.permissionState == LocationPermissionUiState.DENIED) {
        null
    } else {
        uiState.currentLocation
    }
    val routePoints = uiState.selectedRoute.polylinePoints.map(CoordinateUiState::toLatLng)
    val initialCameraTarget =
        routePoints.firstOrNull() ?: currentLocation?.toLatLng() ?: fallbackPosition
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCameraTarget, 15f)
    }
    val coroutineScope = rememberCoroutineScope()
    var isMapLoaded by remember { mutableStateOf(false) }
    var isDebugPanelVisible by rememberSaveable { mutableStateOf(false) }
    var isMoreMenuVisible by rememberSaveable { mutableStateOf(false) }
    val currentOnFocusedPlaceHandled by rememberUpdatedState(onFocusedPlaceHandled)

    MainMapCameraEffects(
        isMapLoaded = isMapLoaded,
        pendingCameraIntent = uiState.pendingCameraIntent,
        routePoints = routePoints,
        currentLocation = currentLocation,
        cameraPositionState = cameraPositionState,
        onCameraIntentConsumed = onCameraIntentConsumed
    )

    LaunchedEffect(isMapLoaded, focusedPlaceId, markerPlaces) {
        val placeId = focusedPlaceId ?: return@LaunchedEffect
        if (!isMapLoaded) return@LaunchedEffect

        val target = markerCameraTarget(
            markerPlaces = markerPlaces,
            placeId = placeId
        )
        if (target != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(target, 17f)
            )
        }
        currentOnFocusedPlaceHandled()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            contentPadding = PaddingValues(bottom = mapCameraBottomPadding),
            properties = MapProperties(isMyLocationEnabled = false),
            onMapLoaded = { isMapLoaded = true },
            onMapClick = {
                isMoreMenuVisible = false
                onMapClick()
            }
        ) {
            RouteMapContent(
                routeModeUiState = uiState.routeModeUiState,
                markerPlaces = markerPlaces,
                routeAccentColor = routeAccentColor,
                onPlaceMarkerClick = { placeId ->
                    markerCameraTarget(
                        markerPlaces = markerPlaces,
                        placeId = placeId
                    )?.let { target ->
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(target, 17f)
                            )
                        }
                    }
                    onPlaceMarkerClick(placeId)
                }
            )

            currentLocation?.let { location ->
                CurrentLocationMapMarker(currentLocation = location)
            }
        }

        MainMapOverlayContent(
            uiState = uiState,
            onDateSelected = onDateSelected,
            onBookmarkClick = onBookmarkClick,
            onRouteAction = onRouteAction,
            onPermissionActionClick = onPermissionActionClick,
            debugActions = debugActions,
            floatingBottomPadding = floatingBottomPadding,
            bottomEndControlsBottomPadding = currentLocationBottomPadding,
            isDebugPanelVisible = isDebugPanelVisible,
            onCloseDebugPanel = { isDebugPanelVisible = false },
            topStartControls = {
                StatsButton(
                    onClick = onStatsClick,
                    modifier = Modifier
                )
                if (BuildConfig.DEBUG) {
                    DebugPanelButton(
                        onClick = { isDebugPanelVisible = !isDebugPanelVisible },
                        modifier = Modifier
                    )
                }
            },
            topEndControls = {
                MainMoreButtonMenu(
                    isMenuVisible = isMoreMenuVisible,
                    onMoreClick = {
                        isMoreMenuVisible = !isMoreMenuVisible
                        onMoreClick()
                    },
                    onPlaceBookmarkClick = {
                        isMoreMenuVisible = false
                        onMorePlaceBookmarkClick()
                    },
                    onDeleteRecordClick = {
                        isMoreMenuVisible = false
                        onMoreDeleteRecordClick()
                    },
                    modifier = Modifier
                )
            },
            floatingControls = {
                FloatingMapButtons(
                    onCurrentLocationClick = currentLocation?.takeIf {
                        showCurrentLocationButton
                    }?.let {
                        {
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        it.toLatLng(),
                                        17f
                                    )
                                )
                            }
                        }
                    }
                )
            }
        )
    }
}

@Composable
private fun MainMoreButtonMenu(
    isMenuVisible: Boolean,
    onMoreClick: () -> Unit,
    onPlaceBookmarkClick: () -> Unit,
    onDeleteRecordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = androidx.compose.ui.Alignment.End
    ) {
        MoreButton(
            onClick = onMoreClick,
            modifier = Modifier
        )

        if (isMenuVisible) {
            Spacer(modifier = Modifier.height(8.dp))
            MainMorePopupMenu(
                onPlaceBookmarkClick = onPlaceBookmarkClick,
                onDeleteRecordClick = onDeleteRecordClick,
                modifier = Modifier
            )
        }
    }
}

@Composable
private fun FloatingMapButtons(
    onCurrentLocationClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    FloatingButtonColumn(modifier = modifier) {
        onCurrentLocationClick?.let { onClick ->
            CurrentLocationButton(
                onClick = onClick,
                modifier = Modifier
            )
        }
    }
}

internal fun CoordinateUiState.toLatLng(): LatLng = LatLng(latitude, longitude)

private fun markerCameraTarget(
    markerPlaces: List<PlaceMarkerUiState>,
    placeId: Long
): LatLng? {
    return markerPlaces
        .firstOrNull { it.placeId == placeId }
        ?.let { place -> LatLng(place.latitude, place.longitude) }
}

@Composable
private fun StatsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingCircleIconButton(
        onClick = onClick,
        iconResId = R.drawable.ic_stats,
        contentDescriptionResId = R.string.main_stats,
        modifier = modifier
    )
}

@Composable
private fun DebugPanelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingCircleIconButton(
        onClick = onClick,
        iconResId = R.drawable.ic_info_circle,
        contentDescriptionResId = R.string.debug_panel_open,
        modifier = modifier
    )
}

@Composable
private fun MoreButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingCircleIconButton(
        onClick = onClick,
        iconResId = R.drawable.ic_more,
        contentDescriptionResId = R.string.main_more,
        modifier = modifier
    )
}

@Composable
private fun CurrentLocationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingCircleIconButton(
        onClick = onClick,
        iconResId = R.drawable.ic_current_location,
        contentDescriptionResId = R.string.main_move_to_current_location,
        modifier = modifier
    )
}

@Preview(showBackground = true, name = "Permission Overlay")
@Composable
private fun PermissionOverlayPreview() {
    com.example.passedpath.ui.theme.PassedPathTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6))
                .padding(16.dp)
        )
    }
}
