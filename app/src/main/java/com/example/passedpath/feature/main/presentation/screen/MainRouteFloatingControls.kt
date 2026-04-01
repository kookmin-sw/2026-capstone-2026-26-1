package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.route.presentation.screen.TrackingToggleButton
import com.example.passedpath.feature.route.presentation.screen.formatDistanceKm
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.component.RoundedWhiteButton
import com.example.passedpath.ui.theme.Gray700

@Composable
internal fun MainRouteFloatingControls(
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
                        TrackingToggleButton(
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
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.96f),
        tonalElevation = 2.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.route_distance, distanceKm.formatDistanceKm()),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FloatingPillButton(text: String, onClick: () -> Unit) {
    RoundedWhiteButton(onClick = onClick, shadowElevation = 6.dp) {
        Text(text = text, color = Gray700, fontWeight = FontWeight.Medium)
    }
}
