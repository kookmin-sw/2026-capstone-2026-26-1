package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.BuildConfig
import com.example.passedpath.R
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmarkSummary
import com.example.passedpath.feature.placebookmark.presentation.component.PlaceBookmarkBadge
import com.example.passedpath.feature.route.presentation.screen.RouteMapContent
import com.example.passedpath.feature.route.presentation.state.PlaceMarkerUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.component.button.BaseCircleButton
import com.example.passedpath.ui.component.floating.FloatingButtonColumn
import com.example.passedpath.ui.component.floating.FloatingCircleIconButton
import com.example.passedpath.ui.state.CoordinateUiState
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green100
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.White
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
internal fun MainMapSection(
    uiState: MainUiState,
    markerPlaces: List<PlaceMarkerUiState>,
    bookmarkMarkers: List<PlaceBookmarkSummary>,
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
    val context = LocalContext.current
    val mapStyleOptions = remember(context) {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.main_map_style)
    }
    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false
        )
    }
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
    var selectedBookmarkPlaceId by rememberSaveable { mutableStateOf<Long?>(null) }
    var isBookmarkMarkersVisible by rememberSaveable { mutableStateOf(true) }
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

    LaunchedEffect(bookmarkMarkers, selectedBookmarkPlaceId) {
        val selectedPlaceId = selectedBookmarkPlaceId ?: return@LaunchedEffect
        if (bookmarkMarkers.none { it.bookmarkPlaceId == selectedPlaceId }) {
            selectedBookmarkPlaceId = null
        }
    }

    LaunchedEffect(isBookmarkMarkersVisible) {
        if (!isBookmarkMarkersVisible) {
            selectedBookmarkPlaceId = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            contentPadding = PaddingValues(bottom = mapCameraBottomPadding),
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapStyleOptions = mapStyleOptions
            ),
            uiSettings = mapUiSettings,
            onMapLoaded = { isMapLoaded = true },
            onMapClick = {
                selectedBookmarkPlaceId = null
                isMoreMenuVisible = false
                onMapClick()
            }
        ) {
            if (isBookmarkMarkersVisible) {
                PlaceBookmarkMapMarkers(
                    bookmarkMarkers = bookmarkMarkers,
                    selectedBookmarkPlaceId = selectedBookmarkPlaceId,
                    onBookmarkMarkerClick = { bookmarkPlaceId ->
                        selectedBookmarkPlaceId = bookmarkPlaceId
                    }
                )
            }

            RouteMapContent(
                routeModeUiState = uiState.routeModeUiState,
                markerPlaces = markerPlaces,
                routeAccentColor = routeAccentColor,
                onPlaceMarkerClick = { placeId ->
                    selectedBookmarkPlaceId = null
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
            isMoreMenuVisible = isMoreMenuVisible,
            onMoreClick = {
                isMoreMenuVisible = !isMoreMenuVisible
                onMoreClick()
            },
            onMoreDismissRequest = {
                isMoreMenuVisible = false
            },
            onMorePlaceBookmarkClick = {
                isMoreMenuVisible = false
                onMorePlaceBookmarkClick()
            },
            onMoreDeleteRecordClick = {
                isMoreMenuVisible = false
                onMoreDeleteRecordClick()
            },
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
            floatingControls = {
                FloatingMapButtons(
                    isBookmarkMarkersVisible = isBookmarkMarkersVisible,
                    onBookmarkMarkersToggleClick = {
                        isBookmarkMarkersVisible = !isBookmarkMarkersVisible
                    },
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
private fun PlaceBookmarkMapMarkers(
    bookmarkMarkers: List<PlaceBookmarkSummary>,
    selectedBookmarkPlaceId: Long?,
    onBookmarkMarkerClick: (Long) -> Unit
) {
    bookmarkMarkers.forEach { place ->
        MarkerComposable(
            state = MarkerState(position = LatLng(place.latitude, place.longitude)),
            title = place.placeName.ifBlank { place.roadAddress },
            anchor = Offset(0.5f, 0.5f),
            zIndex = PlaceBookmarkMarkerZIndex,
            onClick = {
                onBookmarkMarkerClick(place.bookmarkPlaceId)
                true
            }
        ) {
            PlaceBookmarkMapMarker(type = place.type)
        }
    }

    bookmarkMarkers
        .firstOrNull { place -> place.bookmarkPlaceId == selectedBookmarkPlaceId }
        ?.let { place ->
            PlaceBookmarkCalloutMarker(placeBookmark = place)
        }
}

@Composable
private fun PlaceBookmarkCalloutMarker(placeBookmark: PlaceBookmarkSummary) {
    MarkerComposable(
        state = MarkerState(position = LatLng(placeBookmark.latitude, placeBookmark.longitude)),
        anchor = Offset(0.5f, 1f),
        zIndex = PlaceBookmarkCalloutZIndex,
        onClick = { true }
    ) {
        PlaceBookmarkCallout(placeBookmark = placeBookmark)
    }
}

@Composable
private fun PlaceBookmarkCallout(placeBookmark: PlaceBookmarkSummary) {
    val calloutShape = PlaceBookmarkCalloutShape(
        cornerRadius = 12.dp,
        tailWidth = 16.dp,
        tailHeight = 8.dp
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .widthIn(min = 132.dp, max = 220.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = calloutShape,
                    clip = false
                )
                .drawBehind {
                    val path = createPlaceBookmarkCalloutPath(
                        size = size,
                        cornerRadius = 12.dp.toPx(),
                        tailWidth = 16.dp.toPx(),
                        tailHeight = 8.dp.toPx()
                    )
                    drawPath(path = path, color = White)
                    drawPath(
                        path = path,
                        color = Green100,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 14.dp,
                    top = 10.dp,
                    end = 14.dp,
                    bottom = 18.dp
                )
            ) {
                Text(
                    text = placeBookmark.placeName.ifBlank { placeBookmark.roadAddress },
                    color = Gray900,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = placeBookmark.roadAddress,
                    color = Gray500,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(modifier = Modifier.size(height = 24.dp, width = 1.dp))
    }
}

private class PlaceBookmarkCalloutShape(
    private val cornerRadius: androidx.compose.ui.unit.Dp,
    private val tailWidth: androidx.compose.ui.unit.Dp,
    private val tailHeight: androidx.compose.ui.unit.Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return with(density) {
            Outline.Generic(
                createPlaceBookmarkCalloutPath(
                    size = size,
                    cornerRadius = cornerRadius.toPx(),
                    tailWidth = tailWidth.toPx(),
                    tailHeight = tailHeight.toPx()
                )
            )
        }
    }
}

private fun createPlaceBookmarkCalloutPath(
    size: Size,
    cornerRadius: Float,
    tailWidth: Float,
    tailHeight: Float
): Path {
    val width = size.width
    val height = size.height
    val bodyBottom = (height - tailHeight).coerceAtLeast(0f)
    val radius = cornerRadius.coerceAtMost(width / 2f).coerceAtMost(bodyBottom / 2f)
    val tailHalfWidth = (tailWidth / 2f).coerceAtMost(width / 2f)
    val centerX = width / 2f

    return Path().apply {
        moveTo(radius, 0f)
        lineTo(width - radius, 0f)
        quadraticTo(width, 0f, width, radius)
        lineTo(width, bodyBottom - radius)
        quadraticTo(width, bodyBottom, width - radius, bodyBottom)
        lineTo(centerX + tailHalfWidth, bodyBottom)
        lineTo(centerX, height)
        lineTo(centerX - tailHalfWidth, bodyBottom)
        lineTo(radius, bodyBottom)
        quadraticTo(0f, bodyBottom, 0f, bodyBottom - radius)
        lineTo(0f, radius)
        quadraticTo(0f, 0f, radius, 0f)
        close()
    }
}

@Composable
private fun PlaceBookmarkMapMarker(type: BookmarkPlaceType) {
    Box(
        modifier = Modifier
            .size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .offset(y = 2.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.36f))
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            PlaceBookmarkBadge(
                type = type,
                size = 32.dp,
                iconSize = 17.dp
            )
        }
    }
}

@Composable
private fun FloatingMapButtons(
    isBookmarkMarkersVisible: Boolean,
    onBookmarkMarkersToggleClick: () -> Unit,
    onCurrentLocationClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    FloatingButtonColumn(modifier = modifier) {
        BookmarkMarkerToggleButton(
            isVisible = isBookmarkMarkersVisible,
            onClick = onBookmarkMarkersToggleClick,
            modifier = Modifier
        )
        onCurrentLocationClick?.let { onClick ->
            CurrentLocationButton(
                onClick = onClick,
                modifier = Modifier
            )
        }
    }
}

@Composable
private fun BookmarkMarkerToggleButton(
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseCircleButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = White
    ) {
        Icon(
            painter = painterResource(
                id = if (isVisible) {
                    R.drawable.ic_bookmark_pin_filled
                } else {
                    R.drawable.ic_bookmark_other
                }
            ),
            contentDescription = stringResource(R.string.main_toggle_place_bookmark_markers),
            tint = if (isVisible) Green500 else Gray400,
            modifier = Modifier.size(20.dp)
        )
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

private const val PlaceBookmarkMarkerZIndex = -1f
private const val PlaceBookmarkCalloutZIndex = -1.1f

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
