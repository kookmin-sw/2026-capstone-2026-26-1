package com.example.passedpath.feature.route.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.PlaceMarkerUiState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.Polyline

@Composable
fun RouteMapContent(
    routeModeUiState: MainRouteModeUiState,
    routeAccentColor: Color
) {
    val selectedRoute = routeModeUiState.route
    val routePoints = selectedRoute.polylinePoints.map(MainCoordinateUiState::toLatLng)
    if (routePoints.size >= 2) {
        Polyline(
            points = routePoints,
            color = routeAccentColor,
            width = 14f
        )
    }

    if (selectedRoute.hasLocationData) {
        selectedRoute.places.forEach { place ->
            MarkerComposable(
                state = com.google.maps.android.compose.MarkerState(
                    position = LatLng(place.latitude, place.longitude)
                ),
                title = place.placeName.ifBlank { "Place ${place.orderIndex}" },
                anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f)
            ) {
                PlaceOrderMarker(place = place, routeAccentColor = routeAccentColor)
            }
        }
    }
}

@Composable
private fun PlaceOrderMarker(
    place: PlaceMarkerUiState,
    routeAccentColor: Color
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = place.orderIndex.toString(),
            color = routeAccentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RouteStatusOverlay(
    routeModeUiState: MainRouteModeUiState,
    hasRouteLocationData: Boolean,
    onRetryRoute: () -> Unit
) {
    val routeErrorMessage = routeModeUiState.routeErrorMessage
    val routeEmptyMessage = routeModeUiState.routeEmptyMessage
    val routeAccentColor = MaterialTheme.colorScheme.primary
    val shouldShowNoLocationData =
        !routeModeUiState.isRouteLoading && routeErrorMessage == null && !hasRouteLocationData
    if (!routeModeUiState.isRouteLoading && routeErrorMessage == null && !shouldShowNoLocationData) {
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.18f))
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
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
                    routeModeUiState.isRouteLoading -> {
                        CircularProgressIndicator(color = routeAccentColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = loadingTitle(routeModeUiState), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = loadingMessage(routeModeUiState),
                            color = Color(0xFF4B5563),
                            textAlign = TextAlign.Center
                        )
                    }
                    routeErrorMessage != null -> {
                        Text(text = "Route Load Failed", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = routeErrorMessage,
                            color = Color(0xFF9D1C1C),
                            textAlign = TextAlign.Center
                        )
                        if (routeModeUiState is MainRouteModeUiState.Past) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onRetryRoute) {
                                Text(text = "Retry")
                            }
                        }
                    }
                    else -> {
                        Text(text = emptyTitle(routeModeUiState), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = routeEmptyMessage ?: emptyMessage(routeModeUiState),
                            color = Color(0xFF4B5563),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private fun loadingTitle(routeModeUiState: MainRouteModeUiState): String {
    return when (routeModeUiState) {
        is MainRouteModeUiState.Today -> "Loading today's route"
        is MainRouteModeUiState.Past -> "Loading past route"
    }
}

private fun loadingMessage(routeModeUiState: MainRouteModeUiState): String {
    return when (routeModeUiState) {
        is MainRouteModeUiState.Today -> "Observing today's local path updates."
        is MainRouteModeUiState.Past -> "Fetching the selected day's path and places."
    }
}

private fun emptyTitle(routeModeUiState: MainRouteModeUiState): String {
    return when (routeModeUiState) {
        is MainRouteModeUiState.Today -> "No Route Yet"
        is MainRouteModeUiState.Past -> "No Location Data"
    }
}

private fun emptyMessage(routeModeUiState: MainRouteModeUiState): String {
    return when (routeModeUiState) {
        is MainRouteModeUiState.Today -> "Today's route will appear here once local tracking data is recorded."
        is MainRouteModeUiState.Past -> "There is no route path data to show on the map for this day."
    }
}

private fun MainCoordinateUiState.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}
