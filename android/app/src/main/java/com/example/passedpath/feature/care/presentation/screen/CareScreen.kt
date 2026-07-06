package com.example.passedpath.feature.care.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.feature.care.presentation.component.CareEmptyDependentsBanner
import com.example.passedpath.feature.care.presentation.component.CareDependentMapMarker
import com.example.passedpath.feature.care.presentation.component.CareDependentSelectorRow
import com.example.passedpath.feature.care.presentation.component.CareInviteBottomSheet
import com.example.passedpath.feature.care.presentation.component.CareVisitedPlaceMapMarker
import com.example.passedpath.feature.care.presentation.component.ProtectedPersonBottomSheet
import com.example.passedpath.feature.care.presentation.component.careDependentAvatarPalette
import com.example.passedpath.feature.care.presentation.model.ProtectedPersonBottomSheetTab
import com.example.passedpath.feature.care.presentation.state.CareDependentMapMarkerUiState
import com.example.passedpath.feature.care.presentation.state.CareUiState
import com.example.passedpath.feature.care.presentation.state.CareVisitedPlaceMarkerUiState
import com.example.passedpath.feature.route.presentation.screen.RouteMapContent
import com.example.passedpath.ui.component.bottomsheet.BaseAnchoredBottomSheetScaffold
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetValue
import com.example.passedpath.ui.component.feedback.MapOverlayNetworkFailureDialog
import com.example.passedpath.ui.component.floating.FloatingButtonColumn
import com.example.passedpath.ui.component.floating.FloatingCircleIconButton
import com.example.passedpath.ui.component.modal.PassedPathBottomModal
import com.example.passedpath.ui.state.CoordinateUiState
import com.example.passedpath.ui.theme.Black
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Green100
import com.example.passedpath.ui.theme.Green50
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.Green700
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

@Composable
fun CareScreen(
    uiState: CareUiState,
    onDependentSelected: (Long?) -> Unit,
    onRetryClick: () -> Unit,
    onInviteClick: () -> Unit,
    onSheetValueChanged: (BaseBottomSheetValue) -> Unit,
    onSheetCommandConsumed: (BaseBottomSheetValue) -> Unit,
    onTabSelected: (ProtectedPersonBottomSheetTab) -> Unit,
    onPlaceMarkerClick: (Long) -> Unit,
    onPlaceCardClick: (Long) -> Unit,
    onSelectedPlaceHandled: () -> Unit,
    onFocusedPlaceHandled: () -> Unit,
    onMapClick: () -> Unit,
    onPlaceRetryClick: () -> Unit,
    onPlaceGuideBannerClose: () -> Unit,
    onSummaryRetryClick: () -> Unit,
    onRouteRetryClick: () -> Unit,
    onProtectedPersonWeeklySummaryClick: () -> Unit,
    onProtectedPersonPastRouteClick: () -> Unit,
    onLocationStreamRetryClick: () -> Unit,
    onLocationStreamErrorDismiss: () -> Unit,
    onInviteDismiss: () -> Unit,
    onInviteRetryClick: () -> Unit,
    onInviteLinkCopyClick: (String) -> Unit,
    onInviteLinkShareClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        BaseAnchoredBottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            initialSheetValue = uiState.bottomSheetValue,
            requestedSheetValue = uiState.requestedSheetValue,
            onSheetValueChanged = onSheetValueChanged,
            onSheetCommandConsumed = onSheetCommandConsumed,
            content = {
                CareMapContent(
                    uiState = uiState,
                    onDependentSelected = onDependentSelected,
                    onRetryClick = onRetryClick,
                    onInviteClick = onInviteClick,
                    onPlaceMarkerClick = onPlaceMarkerClick,
                    onFocusedPlaceHandled = onFocusedPlaceHandled,
                    onMapClick = onMapClick,
                    onProtectedPersonWeeklySummaryClick = onProtectedPersonWeeklySummaryClick,
                    onProtectedPersonPastRouteClick = onProtectedPersonPastRouteClick,
                    onLocationStreamRetryClick = onLocationStreamRetryClick,
                    onLocationStreamErrorDismiss = onLocationStreamErrorDismiss,
                    onRouteRetryClick = onRouteRetryClick
                )
            },
            sheet = { sheetModifier ->
                if (uiState.selectedDependent != null) {
                    ProtectedPersonBottomSheet(
                        selectedTab = uiState.selectedBottomSheetTab,
                        onTabSelected = onTabSelected,
                        placeListUiState = uiState.placeListUiState,
                        summaryUiState = uiState.summaryUiState,
                        selectedPlaceId = uiState.selectedPlaceId,
                        onSelectedPlaceHandled = onSelectedPlaceHandled,
                        onPlaceClick = onPlaceCardClick,
                        onPlaceRetryClick = onPlaceRetryClick,
                        onPlaceGuideBannerClose = onPlaceGuideBannerClose,
                        onSummaryRetryClick = onSummaryRetryClick,
                        modifier = sheetModifier
                    )
                }
            }
        )

        if (uiState.inviteUiState.isVisible) {
            PassedPathBottomModal(
                onDimClick = onInviteDismiss,
                modifier = Modifier.background(Black.copy(alpha = 0.22f)),
                onBackPress = onInviteDismiss
            ) {
                CareInviteBottomSheet(
                    uiState = uiState.inviteUiState,
                    onCopyClick = onInviteLinkCopyClick,
                    onShareClick = onInviteLinkShareClick,
                    onRetryClick = onInviteRetryClick,
                    onDismiss = onInviteDismiss
                )
            }
        }
    }
}

@Composable
private fun CareMapContent(
    uiState: CareUiState,
    onDependentSelected: (Long?) -> Unit,
    onRetryClick: () -> Unit,
    onInviteClick: () -> Unit,
    onPlaceMarkerClick: (Long) -> Unit,
    onFocusedPlaceHandled: () -> Unit,
    onMapClick: () -> Unit,
    onProtectedPersonWeeklySummaryClick: () -> Unit,
    onProtectedPersonPastRouteClick: () -> Unit,
    onLocationStreamRetryClick: () -> Unit,
    onLocationStreamErrorDismiss: () -> Unit,
    onRouteRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
    val markerDependents = uiState.mapMarkers
    val visitedPlaceMarkers = if (uiState.selectedDependentUserId == null) {
        emptyList()
    } else {
        uiState.visitedPlaceMarkers
    }
    val routePoints = if (uiState.selectedDependentUserId == null) {
        emptyList()
    } else {
        uiState.route.mapPolylinePoints.map(CoordinateUiState::toLatLng)
    }
    val initialCameraTarget = markerDependents.firstOrNull()?.toLatLng() ?: SeoulFallbackLatLng
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCameraTarget, InitialCareMapZoom)
    }
    val coroutineScope = rememberCoroutineScope()
    var isMapLoaded by remember { mutableStateOf(false) }
    var fittedRouteKey by rememberSaveable { mutableStateOf<String?>(null) }
    val routeFitKey = routePoints.toCareRouteFitKey(
        dependentUserId = uiState.selectedDependentUserId,
        dateKey = uiState.selectedDateKey
    )

    LaunchedEffect(uiState.selectedDependentUserId) {
        if (uiState.selectedDependentUserId == null) {
            fittedRouteKey = null
        }
    }

    LaunchedEffect(isMapLoaded, uiState.selectedDependentUserId, uiState.hasLoaded) {
        if (!isMapLoaded) return@LaunchedEffect
        val targetDependent = uiState.selectedDependentUserId
            ?.let { selectedId ->
                markerDependents.firstOrNull { dependent ->
                    dependent.dependentUserId == selectedId
                }
            }
            ?: markerDependents.firstOrNull()
            ?: return@LaunchedEffect
        val zoom = if (uiState.selectedDependentUserId == null) {
            InitialCareMapZoom
        } else {
            SelectedCareMapZoom
        }
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(targetDependent.toLatLng(), zoom)
        )
    }

    LaunchedEffect(isMapLoaded, routeFitKey, uiState.isRouteLoading) {
        if (!isMapLoaded || uiState.isRouteLoading) return@LaunchedEffect
        if (uiState.selectedDependentUserId == null || routePoints.isEmpty()) {
            return@LaunchedEffect
        }
        if (fittedRouteKey == routeFitKey) return@LaunchedEffect

        cameraPositionState.animate(createCareRouteCameraUpdate(routePoints))
        fittedRouteKey = routeFitKey
    }

    LaunchedEffect(isMapLoaded, uiState.focusedPlaceId, visitedPlaceMarkers) {
        val placeId = uiState.focusedPlaceId ?: return@LaunchedEffect
        if (!isMapLoaded) return@LaunchedEffect

        val target = visitedPlaceMarkers
            .firstOrNull { marker -> marker.placeId == placeId }
            ?.toLatLng()
        if (target != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(target, FocusedPlaceMapZoom)
            )
        }
        onFocusedPlaceHandled()
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapStyleOptions = mapStyleOptions
            ),
            uiSettings = mapUiSettings,
            onMapLoaded = { isMapLoaded = true },
            onMapClick = { onMapClick() }
        ) {
            RouteMapContent(
                routePoints = routePoints,
                markerPlaces = emptyList(),
                routeAccentColor = Green500
            )

            markerDependents.forEachIndexed { index, dependent ->
                CareDependentMarker(
                    dependent = dependent,
                    paletteIndex = index,
                    selected = uiState.selectedDependentUserId == dependent.dependentUserId,
                    onClick = {
                        onDependentSelected(dependent.dependentUserId)
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    dependent.toLatLng(),
                                    SelectedCareMapZoom
                                )
                            )
                        }
                    }
                )
            }

            visitedPlaceMarkers.forEach { marker ->
                CareVisitedPlaceMarker(
                    marker = marker,
                    onClick = {
                        onPlaceMarkerClick(marker.placeId)
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    marker.toLatLng(),
                                    FocusedPlaceMapZoom
                                )
                            )
                        }
                    }
                )
            }
        }

        CareTopSelectorSurface(
            uiState = uiState,
            onDependentSelected = onDependentSelected,
            onInviteClick = onInviteClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        CareMapStatusOverlay(
            uiState = uiState,
            onRetryClick = onRetryClick,
            onLocationStreamRetryClick = onLocationStreamRetryClick,
            onLocationStreamErrorDismiss = onLocationStreamErrorDismiss,
            onRouteRetryClick = onRouteRetryClick,
            avoidTopStartFloatingActions = uiState.selectedDependentUserId != null,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        if (uiState.selectedDependentUserId != null) {
            CareProtectedPersonFloatingActions(
                onWeeklySummaryClick = onProtectedPersonWeeklySummaryClick,
                onPastRouteClick = onProtectedPersonPastRouteClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(top = CareStatusTopPadding, start = 16.dp)
            )
        }
    }
}

@Composable
private fun CareDependentMarker(
    dependent: CareDependentMapMarkerUiState,
    paletteIndex: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    key(dependent.dependentUserId) {
        val position = remember(
            dependent.dependentUserId,
            dependent.latitude,
            dependent.longitude
        ) {
            dependent.toLatLng()
        }
        val markerState = rememberMarkerState(
            key = "care-dependent:${dependent.dependentUserId}",
            position = position
        )

        LaunchedEffect(position) {
            markerState.position = position
        }

        MarkerComposable(
            state = markerState,
            title = dependent.nickname,
            anchor = Offset(0.5f, 0.92f),
            zIndex = if (selected) {
                SelectedDependentMarkerZIndex
            } else {
                DependentMarkerZIndex
            },
            onClick = {
                onClick()
                true
            }
        ) {
            CareDependentMapMarker(
                nickname = dependent.nickname,
                profileImageUrl = dependent.profileImageUrl,
                palette = careDependentAvatarPalette(paletteIndex),
                selected = selected
            )
        }
    }
}

@Composable
private fun CareVisitedPlaceMarker(
    marker: CareVisitedPlaceMarkerUiState,
    onClick: () -> Unit
) {
    val position = remember(marker.placeId, marker.latitude, marker.longitude) {
        marker.toLatLng()
    }
    val markerState = rememberMarkerState(
        key = "care-place:${marker.placeId}:${marker.latitude}:${marker.longitude}",
        position = position
    )

    LaunchedEffect(position) {
        markerState.position = position
    }

    MarkerComposable(
        marker.placeId,
        marker.displayOrderIndex,
        state = markerState,
        title = marker.placeName,
        anchor = Offset(0.5f, 0.5f),
        zIndex = VisitedPlaceMarkerZIndex,
        onClick = {
            onClick()
            true
        }
    ) {
        CareVisitedPlaceMapMarker(orderIndex = marker.displayOrderIndex)
    }
}

@Composable
private fun CareTopSelectorSurface(
    uiState: CareUiState,
    onDependentSelected: (Long?) -> Unit,
    onInviteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 14.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CareDependentSelectorRow(
                dependents = uiState.dependents,
                selectedDependentUserId = uiState.selectedDependentUserId,
                isLoading = uiState.isLoading && !uiState.hasLoaded,
                onSelectAllClick = { onDependentSelected(null) },
                onDependentClick = { dependentUserId ->
                    onDependentSelected(dependentUserId)
                },
                onInviteClick = onInviteClick
            )
        }
    }
}

@Composable
private fun CareMapStatusOverlay(
    uiState: CareUiState,
    onRetryClick: () -> Unit,
    onLocationStreamRetryClick: () -> Unit,
    onLocationStreamErrorDismiss: () -> Unit,
    onRouteRetryClick: () -> Unit,
    avoidTopStartFloatingActions: Boolean,
    modifier: Modifier = Modifier
) {
    var dismissedErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var dismissedLocationStreamErrorMessage by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(uiState.errorMessage, uiState.isLoading) {
        if (uiState.errorMessage == null || uiState.isLoading) {
            dismissedErrorMessage = null
        }
    }

    LaunchedEffect(uiState.locationStreamErrorMessage) {
        if (uiState.locationStreamErrorMessage == null) {
            dismissedLocationStreamErrorMessage = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                top = if (avoidTopStartFloatingActions) {
                    CareStatusTopPadding + CareProtectedPersonFloatingActionsHeight + 12.dp
                } else {
                    CareStatusTopPadding
                },
                start = 16.dp,
                end = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val locationStreamErrorMessage = uiState.locationStreamErrorMessage
        if (locationStreamErrorMessage != null &&
            dismissedLocationStreamErrorMessage != locationStreamErrorMessage
        ) {
            CareLocationStreamFailureBanner(
                onRetryClick = {
                    dismissedLocationStreamErrorMessage = locationStreamErrorMessage
                    onLocationStreamRetryClick()
                },
                onDismiss = {
                    dismissedLocationStreamErrorMessage = locationStreamErrorMessage
                    onLocationStreamErrorDismiss()
                }
            )
        }

        if (uiState.routeErrorMessage != null) {
            CareRouteFailureBanner(onRetryClick = onRouteRetryClick)
        }

        when {
            uiState.hasLoaded && uiState.dependents.isEmpty() && !uiState.isLoading -> {
                CareEmptyDependentsBanner()
            }
        }
    }

    val errorMessage = uiState.errorMessage
    if (errorMessage != null && dismissedErrorMessage != errorMessage) {
        MapOverlayNetworkFailureDialog(
            retryText = stringResource(R.string.route_retry),
            onDismiss = { dismissedErrorMessage = errorMessage },
            onRetryClick = {
                dismissedErrorMessage = errorMessage
                onRetryClick()
            }
        )
    }
}

@Composable
private fun CareRouteFailureBanner(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Green50,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Green100),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.care_route_error),
                modifier = Modifier.weight(1f),
                color = Gray500,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            TextButton(onClick = onRetryClick) {
                Text(
                    text = stringResource(R.string.route_retry),
                    color = Green700,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CareLocationStreamFailureBanner(
    onRetryClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Green50,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Green100),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.care_location_stream_error),
                modifier = Modifier.weight(1f),
                color = Gray500,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            TextButton(onClick = onRetryClick) {
                Text(
                    text = stringResource(R.string.care_location_stream_retry),
                    color = Green700,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.care_location_stream_close),
                    color = Gray500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CareProtectedPersonFloatingActions(
    onWeeklySummaryClick: () -> Unit,
    onPastRouteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingButtonColumn(modifier = modifier) {
        FloatingCircleIconButton(
            onClick = onWeeklySummaryClick,
            iconResId = R.drawable.ic_stats,
            contentDescriptionResId = R.string.care_protected_person_weekly_summary
        )
        FloatingCircleIconButton(
            onClick = onPastRouteClick,
            iconResId = R.drawable.ic_bottom_nav_route,
            contentDescriptionResId = R.string.care_protected_person_past_route
        )
    }
}

@Preview(showBackground = true, name = "Care Protected Person Floating Actions")
@Composable
private fun CareProtectedPersonFloatingActionsPreview() {
    PassedPathTheme {
        Box(
            modifier = Modifier
                .background(Green50)
                .padding(16.dp)
        ) {
            CareProtectedPersonFloatingActions(
                onWeeklySummaryClick = {},
                onPastRouteClick = {}
            )
        }
    }
}

private fun CareDependentMapMarkerUiState.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

private fun CareVisitedPlaceMarkerUiState.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

private fun CoordinateUiState.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

private fun List<LatLng>.toCareRouteFitKey(
    dependentUserId: Long?,
    dateKey: String
): String {
    val first = firstOrNull()
    val last = lastOrNull()
    return "$dependentUserId:$dateKey:$size:" +
        "${first?.latitude}:${first?.longitude}:${last?.latitude}:${last?.longitude}"
}

private fun createCareRouteCameraUpdate(routePoints: List<LatLng>) =
    when {
        routePoints.size == 1 -> CameraUpdateFactory.newLatLngZoom(
            routePoints.first(),
            SelectedCareMapZoom
        )

        else -> {
            val boundsBuilder = LatLngBounds.builder()
            routePoints.forEach(boundsBuilder::include)
            CameraUpdateFactory.newLatLngBounds(
                boundsBuilder.build(),
                CareRouteBoundsPaddingPx
            )
        }
    }

private val SeoulFallbackLatLng = LatLng(37.5662952, 126.9779451)
private val CareStatusTopPadding = 128.dp
private val CareProtectedPersonFloatingActionsHeight = 92.dp
private const val InitialCareMapZoom = 13f
private const val SelectedCareMapZoom = 16f
private const val FocusedPlaceMapZoom = 17f
private const val DependentMarkerZIndex = 0f
private const val SelectedDependentMarkerZIndex = 1f
private const val VisitedPlaceMarkerZIndex = 2f
private const val CareRouteBoundsPaddingPx = 160
