package com.example.passedpath.feature.route.presentation.screen

import androidx.compose.runtime.Composable
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction

@Composable
fun MainRouteSection(
    routeMode: MainRouteModeUiState,
    onRouteAction: (RouteUiAction) -> Unit
) {
    when (routeMode) {
        is MainRouteModeUiState.Today -> TodayRouteSection(
            routeMode = routeMode,
            onRouteAction = onRouteAction
        )
        is MainRouteModeUiState.Past -> PastRouteSection(
            routeMode = routeMode,
            onRouteAction = onRouteAction
        )
    }
}
