package com.example.passedpath.feature.route.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.passedpath.feature.route.presentation.action.RouteActionUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction

@Composable
internal fun RouteActionRow(
    actionUiState: RouteActionUiState,
    onRouteAction: (RouteUiAction) -> Unit,
    modifier: Modifier = Modifier,
    useFloatingStyle: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (actionUiState.showRefresh) {
            RouteRefreshButton(
                useFloatingStyle = useFloatingStyle,
                onClick = { onRouteAction(RouteUiAction.RefreshTodayRoute) }
            )
        }
        if (actionUiState.showTrackingToggle) {
            TrackingToggleButton(
                isTracking = actionUiState.isTrackingEnabled,
                onClick = { onRouteAction(RouteUiAction.ToggleTracking) }
            )
        }
        if (actionUiState.showPlayback) {
            RoutePlaybackButton(
                onClick = { onRouteAction(RouteUiAction.EnterPastPlayback) }
            )
        }
    }
}
