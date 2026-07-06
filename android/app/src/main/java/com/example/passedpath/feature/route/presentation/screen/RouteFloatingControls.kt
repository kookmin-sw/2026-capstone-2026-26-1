package com.example.passedpath.feature.route.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.passedpath.feature.route.presentation.action.buildRouteActionUiState
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction

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

