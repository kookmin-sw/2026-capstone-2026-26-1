package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.route.presentation.screen.MainRouteSection
import com.example.passedpath.feature.route.presentation.screen.RouteMapContent
import com.example.passedpath.feature.route.presentation.screen.RouteStatusOverlay
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.PermissionSettingDialog
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
    onOpenCalendar: () -> Unit,
    onRouteAction: (RouteUiAction) -> Unit,
    onTrackingPermissionDialogConfirm: () -> Unit,
    onTrackingPermissionDialogDismiss: () -> Unit
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
            RouteMapContent(
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

        RouteStatusOverlay(
            routeModeUiState = uiState.routeModeUiState,
            hasRouteLocationData = hasRouteLocationData,
            onRouteAction = onRouteAction
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                MainDateTopBar(
                    selectedDateKey = uiState.selectedDateKey,
                    onPreviousDate = {
                        onDateSelected(shiftDate(uiState.selectedDateKey, -1))
                    },
                    onNextDate = {
                        onDateSelected(shiftDate(uiState.selectedDateKey, 1))
                    },
                    onOpenCalendar = onOpenCalendar
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        MainRouteSection(
                            routeMode = uiState.routeModeUiState,
                            onRouteAction = onRouteAction
                        )
                    }
                }
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

                if (uiState.permissionState == LocationPermissionUiState.DENIED) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(text = stringResource(R.string.main_permission_off_title))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = stringResource(R.string.main_permission_off_message))
                        }
                    }
                }
            }
        }
    }

    if (uiState.showTrackingPermissionDialog) {
        PermissionSettingDialog(
            onConfirm = onTrackingPermissionDialogConfirm,
            onDismiss = onTrackingPermissionDialogDismiss
        )
    }
}

@Composable
private fun MainDateTopBar(
    selectedDateKey: String,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    val selectedDate = parseDateOrToday(selectedDateKey)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateArrowButton(
                symbol = "<",
                onClick = onPreviousDate
            )
            Text(
                text = selectedDate.format(TopBarDateFormatter),
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenCalendar)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            DateArrowButton(
                symbol = ">",
                onClick = onNextDate
            )
        }
    }
}

@Composable
private fun DateArrowButton(
    symbol: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)
    ) {
        IconButton(onClick = onClick) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
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

private fun buildRouteCameraUpdate(routePoints: List<LatLng>) = when {
    routePoints.isEmpty() -> CameraUpdateFactory.zoomTo(15f)
    routePoints.size == 1 -> CameraUpdateFactory.newLatLngZoom(routePoints.first(), 14f)
    else -> {
        val boundsBuilder = LatLngBounds.builder()
        routePoints.forEach(boundsBuilder::include)
        CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), RouteBoundsPaddingPx)
    }
}


