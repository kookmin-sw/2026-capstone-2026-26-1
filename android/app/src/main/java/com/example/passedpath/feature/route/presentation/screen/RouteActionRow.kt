package com.example.passedpath.feature.route.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.passedpath.feature.route.presentation.action.RouteActionUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.component.FloatingButtonRow

@Composable
internal fun RouteActionRow(
    actionUiState: RouteActionUiState,
    onRouteAction: (RouteUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingButtonRow(modifier = modifier) {
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
