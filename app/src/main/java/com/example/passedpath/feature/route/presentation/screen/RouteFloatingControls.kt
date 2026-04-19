package com.example.passedpath.feature.route.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.passedpath.feature.route.presentation.action.buildRouteActionUiState
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction

@Composable
internal fun RouteFloatingControls(
    routeMode: MainRouteModeUiState,
    onRouteAction: (RouteUiAction) -> Unit
) {
    val actionUiState = buildRouteActionUiState(routeMode)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RouteActionRow(
            actionUiState = actionUiState,
            onRouteAction = onRouteAction
        )
    }
}

