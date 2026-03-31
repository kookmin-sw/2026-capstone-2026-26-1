package com.example.passedpath.feature.route.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.component.RoundedWhiteButton
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray600
import com.example.passedpath.ui.theme.Primary
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
        TrackingToggleButton(
            isTracking = routeMode.isTrackingEnabled,
            onClick = { onRouteAction(RouteUiAction.ToggleTracking) }
        )
    }
}

@Composable
internal fun TrackingToggleButton(
    isTracking: Boolean,
    onClick: () -> Unit
) {
    RoundedWhiteButton(
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isTracking) Primary else Gray400)
        )

        Text(
            text = stringResource(
                if (isTracking) {
                    R.string.route_tracking_stop
                } else {
                    R.string.route_tracking_start
                }
            ),
            color = if (isTracking) Gray600 else Gray400
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_swap),
            contentDescription = null,
            tint = Gray400

        )
    }
}

internal fun Double.formatDistanceKm(): String {
    return String.format(Locale.US, "%.2f km", this)
}
