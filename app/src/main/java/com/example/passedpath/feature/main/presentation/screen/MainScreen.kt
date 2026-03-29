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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.main.presentation.state.PlaceMarkerUiState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CurrentLocationGlowBase = Color(0xFF006B5F)
private val RouteLineColor = Color(0xFF0A7A6C)
private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@Composable
fun MainScreen(
    uiState: MainUiState,
    onInitialCameraCentered: () -> Unit,
    onDateSelected: (String) -> Unit,
    onRetryRoute: () -> Unit
) {
    val context = LocalContext.current
    val fallbackPosition = LatLng(37.5662952, 126.9779451)
    val currentLocation = if (uiState.permissionState == LocationPermissionUiState.DENIED) {
        null
    } else {
        uiState.currentLocation
    }
    val routePoints = uiState.selectedRoute.polylinePoints.map(MainCoordinateUiState::toLatLng)
    val routePlaces = uiState.selectedRoute.places
    val hasRouteLocationData = uiState.selectedRoute.hasLocationData
    val initialCameraTarget = routePoints.firstOrNull() ?: currentLocation?.toLatLng() ?: fallbackPosition
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCameraTarget, 15f)
    }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.selectedDateKey, routePoints) {
        if (routePoints.isNotEmpty()) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(routePoints.first(), 14f)
            )
            onInitialCameraCentered()
        }
    }

    LaunchedEffect(currentLocation, uiState.hasCenteredOnCurrentLocation, routePoints) {
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
            properties = MapProperties(isMyLocationEnabled = false)
        ) {
            if (routePoints.size >= 2) {
                Polyline(
                    points = routePoints,
                    color = RouteLineColor,
                    width = 14f
                )
            }

            if (uiState.selectedRoute.hasLocationData) {
                routePlaces.forEach { place ->
                    MarkerComposable(
                        state = com.google.maps.android.compose.MarkerState(
                            position = LatLng(place.latitude, place.longitude)
                        ),
                        title = place.placeName.ifBlank { "Place ${place.orderIndex}" },
                        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f)
                    ) {
                        PlaceOrderMarker(place = place)
                    }
                }
            }

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
            isLoading = uiState.isRouteLoading,
            hasRouteLocationData = hasRouteLocationData,
            errorMessage = uiState.routeErrorMessage,
            onRetryRoute = onRetryRoute
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = stringResource(R.string.main_title), fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = permissionText(uiState.permissionState))
                        }
                        Button(
                            onClick = {
                                showDatePicker(
                                    context = context,
                                    initialDateKey = uiState.selectedDateKey,
                                    onDateSelected = onDateSelected
                                )
                            }
                        ) {
                            Text(text = "Pick date")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Selected date: ${uiState.selectedRoute.dateKey}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Distance: ${uiState.selectedRoute.totalDistanceKm.formatDistanceKm()}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Path points: ${uiState.selectedRoute.pathPointCount}")
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
                            contentDescription = "Move to current location"
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
                            Text(text = "Location permission is off")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Current location stays hidden until fine location is granted.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteStatusOverlay(
    isLoading: Boolean,
    hasRouteLocationData: Boolean,
    errorMessage: String?,
    onRetryRoute: () -> Unit
) {
    val shouldShowNoLocationData = !isLoading && errorMessage == null && !hasRouteLocationData
    if (!isLoading && errorMessage == null && !shouldShowNoLocationData) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.18f))
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(color = RouteLineColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Loading route", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Fetching the selected day's path and places.",
                            color = Color(0xFF4B5563),
                            textAlign = TextAlign.Center
                        )
                    }
                    errorMessage != null -> {
                        Text(text = "Route Load Failed", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFF9D1C1C),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetryRoute) {
                            Text(text = "Retry")
                        }
                    }
                    else -> {
                        Text(text = "No Location Data", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "There is no route path data to show on the map for this day.",
                            color = Color(0xFF4B5563),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceOrderMarker(place: PlaceMarkerUiState) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = place.orderIndex.toString(),
            color = RouteLineColor,
            fontWeight = FontWeight.Bold
        )
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

private fun permissionText(permissionState: LocationPermissionUiState): String {
    return when (permissionState) {
        LocationPermissionUiState.ALWAYS -> "Background location is enabled"
        LocationPermissionUiState.FOREGROUND_ONLY -> "Foreground location is enabled"
        LocationPermissionUiState.DENIED -> "Location permission is not granted"
    }
}

private fun MainCoordinateUiState.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

private fun Double.formatDistanceKm(): String {
    return String.format(Locale.US, "%.2f km", this)
}
