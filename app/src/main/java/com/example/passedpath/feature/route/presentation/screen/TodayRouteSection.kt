package com.example.passedpath.feature.route.presentation.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import java.util.Locale

@Composable
internal fun TodayRouteSection(routeMode: MainRouteModeUiState.Today) {
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

internal fun Double.formatDistanceKm(): String {
    return String.format(Locale.US, "%.2f km", this)
}
