package com.example.passedpath.feature.main.presentation.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.daynote.presentation.screen.DayNoteBottomSheetContent
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.place.presentation.screen.PlaceBottomSheetContent
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.PermissionSettingDialog
import com.example.passedpath.ui.component.RoundedWhiteButton
import com.example.passedpath.ui.component.overlay.PermissionOverlay
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray700
import com.example.passedpath.ui.theme.Primary
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private val CurrentLocationGlowBase = Color(0xFF006B5F)
private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val TopBarDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. EEE", Locale.KOREAN)
private const val RouteBoundsPaddingPx = 180

@Composable
fun MainScreen(
    uiState: MainUiState,
    onInitialCameraCentered: () -> Unit,
    onDateSelected: (String) -> Unit,
    onRouteAction: (RouteUiAction) -> Unit,
    onTrackingPermissionDialogConfirm: () -> Unit,
    onTrackingPermissionDialogDismiss: () -> Unit,
    onPermissionBannerConfirm: () -> Unit
) {
    val context = LocalContext.current
    val routeAccentColor = MaterialTheme.colorScheme.primary
    val fallbackPosition = LatLng(37.5662952, 126.9779451)
    var selectedBottomSheetTab by rememberSaveable { mutableStateOf(MainBottomSheetTab.PLACE) }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            onMapLoaded = { isMapLoaded = true }
        ) {
            com.example.passedpath.feature.route.presentation.screen.RouteMapContent(
                routeModeUiState = uiState.routeModeUiState,
                routeAccentColor = routeAccentColor
            )

            currentLocation?.let {
                MarkerComposable(
                    state = com.google.maps.android.compose.MarkerState(position = it.toLatLng()),
                    title = stringResource(R.string.main_map_marker_title),
                    anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.58f)
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

        com.example.passedpath.feature.route.presentation.screen.RouteStatusOverlay(
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
                DateTopBar(
                    selectedDateKey = uiState.selectedDateKey,
                    onPreviousDate = {
                        onDateSelected(shiftDate(uiState.selectedDateKey, -1))
                    },
                    onNextDate = {
                        onDateSelected(shiftDate(uiState.selectedDateKey, 1))
                    },
                    onOpenDatePicker = {
                        showDatePicker(
                            context = context,
                            initialDateKey = uiState.selectedDateKey,
                            onDateSelected = onDateSelected
                        )
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                RouteFloatingControls(
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
                                    CameraUpdateFactory.newLatLngZoom(
                                        currentLocation.toLatLng(),
                                        17f
                                    )
                                )
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.my_location_24px),
                            contentDescription = stringResource(R.string.main_move_to_current_location)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        if (uiState.showPermissionBanner) {
            PermissionOverlay(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                message = permissionOverlayMessage(uiState.permissionState),
                actionText = stringResource(R.string.permission_banner_action),
                onClickAction = onPermissionBannerConfirm
            )
        }

        MainBottomSheet(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedTab = selectedBottomSheetTab,
            onTabSelected = { selectedBottomSheetTab = it }
        )
    }

    if (uiState.showTrackingPermissionDialog) {
        PermissionSettingDialog(
            onConfirm = onTrackingPermissionDialogConfirm,
            onDismiss = onTrackingPermissionDialogDismiss
        )
    }
}

@Composable
private fun DateTopBar(
    selectedDateKey: String,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onOpenDatePicker: () -> Unit
) {
    val selectedDate = parseDateOrToday(selectedDateKey)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.96f),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            androidx.compose.material3.TextButton(onClick = onPreviousDate) {
                Text(text = "<")
            }
            Text(
                text = selectedDate.format(TopBarDateFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            androidx.compose.material3.TextButton(onClick = onOpenDatePicker) {
                Text(text = stringResource(R.string.main_pick_date))
            }
            androidx.compose.material3.TextButton(onClick = onNextDate) {
                Text(text = ">")
            }
        }
    }
}

@Composable
private fun RouteFloatingControls(
    routeMode: MainRouteModeUiState,
    onRouteAction: (RouteUiAction) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RouteDistanceChip(distanceKm = routeMode.route.totalDistanceKm)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (routeMode) {
                is MainRouteModeUiState.Today -> {
                    if (routeMode.canRefreshDistance) {
                        FloatingPillButton(
                            text = stringResource(R.string.route_refresh),
                            onClick = { onRouteAction(RouteUiAction.RefreshTodayRoute) }
                        )
                    }
                    if (routeMode.isTrackingToggleVisible) {
                        TrackingFloatingPill(
                            isTracking = routeMode.isTrackingEnabled,
                            onClick = { onRouteAction(RouteUiAction.ToggleTracking) }
                        )
                    }
                }

                is MainRouteModeUiState.Past -> {
                    if (routeMode.isPlaybackEntryVisible) {
                        FloatingPillButton(
                            text = stringResource(R.string.route_open_playback),
                            onClick = { onRouteAction(RouteUiAction.EnterPastPlayback) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteDistanceChip(distanceKm: Double) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.96f),
        tonalElevation = 2.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.route_distance, formatDistanceKm(distanceKm)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FloatingPillButton(
    text: String,
    onClick: () -> Unit
) {
    RoundedWhiteButton(
        onClick = onClick,
        shadowElevation = 6.dp
    ) {
        Text(
            text = text,
            color = Gray700,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TrackingFloatingPill(
    isTracking: Boolean,
    onClick: () -> Unit
) {
    RoundedWhiteButton(
        onClick = onClick,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isTracking) Primary else Gray500)
        )
        Text(
            text = stringResource(
                if (isTracking) R.string.route_tracking_stop else R.string.route_tracking_start
            ),
            color = Gray700,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MainBottomSheet(
    selectedTab: MainBottomSheetTab,
    onTabSelected: (MainBottomSheetTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 14.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 22.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            Spacer(modifier = Modifier.height(12.dp))
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                MainBottomSheetTab.entries.forEach { tab ->
                    Tab(
                        selected = tab == selectedTab,
                        onClick = { onTabSelected(tab) },
                        text = {
                            Text(
                                text = stringResource(tab.titleResId),
                                fontWeight = if (tab == selectedTab) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            when (selectedTab) {
                MainBottomSheetTab.PLACE -> PlaceBottomSheetContent(
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                MainBottomSheetTab.DAYNOTE -> DayNoteBottomSheetContent(
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

private fun showDatePicker(
    context: android.content.Context,
    initialDateKey: String,
    onDateSelected: (String) -> Unit
) {
    val initialDate = runCatching { LocalDate.parse(initialDateKey, DateFormatter) }
        .getOrDefault(LocalDate.now())

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected(
                LocalDate.of(year, month + 1, dayOfMonth).format(DateFormatter)
            )
        },
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth
    ).show()
}

@Composable
private fun permissionOverlayMessage(permissionState: LocationPermissionUiState): String {
    return when (permissionState) {
        LocationPermissionUiState.DENIED -> stringResource(R.string.permission_banner_denied_title)
        LocationPermissionUiState.FOREGROUND_ONLY -> stringResource(R.string.permission_banner_foreground_title)
        LocationPermissionUiState.ALWAYS -> ""
    }
}

private fun parseDateOrToday(dateKey: String): LocalDate {
    return runCatching { LocalDate.parse(dateKey, DateFormatter) }
        .getOrDefault(LocalDate.now())
}

private fun shiftDate(dateKey: String, days: Long): String {
    return parseDateOrToday(dateKey)
        .plusDays(days)
        .format(DateFormatter)
}

private fun MainCoordinateUiState.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

private fun formatDistanceKm(distanceKm: Double): String {
    return String.format(Locale.US, "%.2f km", distanceKm)
}

private fun buildRouteCameraUpdate(routePoints: List<LatLng>) = when {
    routePoints.isEmpty() -> CameraUpdateFactory.zoomTo(15f)
    routePoints.size == 1 -> CameraUpdateFactory.newLatLngZoom(routePoints.first(), 14f)
    else -> {
        val boundsBuilder = LatLngBounds.builder()
        routePoints.forEach(boundsBuilder::include)
        CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), RouteBoundsPaddingPx)
    }
}

private enum class MainBottomSheetTab(val titleResId: Int) {
    PLACE(R.string.record_sheet_tab_place),
    DAYNOTE(R.string.record_sheet_tab_daynote)
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
                message = "위치 권한이 거부되어 기록이 남지 않고 있어요",
                actionText = "권한 설정하기",
                onClickAction = {}
            )
        }
    }
}
