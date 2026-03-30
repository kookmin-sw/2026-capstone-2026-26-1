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

@Composable
internal fun PastRouteSection(routeMode: MainRouteModeUiState.Past) {
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
