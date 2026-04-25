package com.example.passedpath.feature.route.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.passedpath.feature.route.presentation.action.buildRouteActionUiState
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.component.FloatingButtonRow

@Composable
internal fun RouteTopCenterControls(
    routeMode: MainRouteModeUiState,
    onRouteAction: (RouteUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val actionUiState = buildRouteActionUiState(routeMode)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (actionUiState.showPlayback) {
            FloatingButtonRow {
                RoutePlaybackButton(
                    onClick = { onRouteAction(RouteUiAction.EnterPastPlayback) }
                )
            }
        }
    }
}

@Composable
internal fun RouteTopEndControls(
    routeMode: MainRouteModeUiState,
    onRouteAction: (RouteUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val actionUiState = buildRouteActionUiState(routeMode)
    if (actionUiState.showTrackingToggle) {
        TrackingToggleButton(
            isTracking = actionUiState.isTrackingEnabled,
            onClick = { onRouteAction(RouteUiAction.ToggleTracking) },
            modifier = modifier
        )
    }
}

