package com.example.passedpath.feature.route.presentation.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import java.util.Locale

@Composable
internal fun TodayRouteSection(
    routeMode: MainRouteModeUiState.Today,
    onRouteAction: (RouteUiAction) -> Unit
) {
    val routeAccentColor = MaterialTheme.colorScheme.primary
    Text(
        text = stringResource(R.string.route_today_title),
        fontWeight = FontWeight.SemiBold,
        color = routeAccentColor
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(
            R.string.route_distance,
            routeMode.route.totalDistanceKm.formatDistanceKm()
        )
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = stringResource(R.string.route_path_points, routeMode.route.pathPointCount))
    if (routeMode.canRefreshDistance) {
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onRouteAction(RouteUiAction.RefreshTodayRoute) }) {
            Text(text = stringResource(R.string.route_refresh))
        }
    }
    if (routeMode.isTrackingToggleVisible) {
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onRouteAction(RouteUiAction.ToggleTracking) }) {
            Text(text = stringResource(R.string.route_tracking_toggle))
        }
    }
}

internal fun Double.formatDistanceKm(): String {
    return String.format(Locale.US, "%.2f km", this)
}
