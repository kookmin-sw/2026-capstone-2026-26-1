package com.example.passedpath.feature.route.presentation.screen

import androidx.compose.runtime.Composable
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState

@Composable
fun MainRouteSection(routeMode: MainRouteModeUiState) {
    when (routeMode) {
        is MainRouteModeUiState.Today -> TodayRouteSection(routeMode = routeMode)
        is MainRouteModeUiState.Past -> PastRouteSection(routeMode = routeMode)
    }
}
