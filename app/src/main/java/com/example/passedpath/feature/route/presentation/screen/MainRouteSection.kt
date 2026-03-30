package com.example.passedpath.feature.route.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import java.util.Locale

@Composable
fun MainRouteSection(routeMode: MainRouteModeUiState) {
    when (routeMode) {
        is MainRouteModeUiState.Today -> TodayContent(routeMode = routeMode)
        is MainRouteModeUiState.Past -> PastContent(routeMode = routeMode)
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

@Composable
private fun TodayContent(routeMode: MainRouteModeUiState.Today) {
    val routeAccentColor = MaterialTheme.colorScheme.primary
    Text(text = "Today route", fontWeight = FontWeight.SemiBold, color = routeAccentColor)
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = "Distance: ${routeMode.route.totalDistanceKm.formatDistanceKm()}")
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = "Path points: ${routeMode.route.pathPointCount}")
    if (routeMode.isTrackingToggleVisible) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Tracking controls stay in the today route mode.")
    }
    if (routeMode.canRefreshDistance) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Distance can refresh in real time for today.")
    }
}

@Composable
private fun PastContent(routeMode: MainRouteModeUiState.Past) {
    val routeAccentColor = MaterialTheme.colorScheme.primary
    Text(text = "Past route", fontWeight = FontWeight.SemiBold, color = routeAccentColor)
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = "Distance: ${routeMode.route.totalDistanceKm.formatDistanceKm()}")
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = "Path points: ${routeMode.route.pathPointCount}")
    if (routeMode.isPlaybackEntryVisible) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Playback entry belongs to past route mode.")
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

private fun Double.formatDistanceKm(): String {
    return String.format(Locale.US, "%.2f km", this)
}
