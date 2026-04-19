package com.example.passedpath.feature.route.presentation.action

import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState

internal fun buildRouteActionUiState(routeMode: MainRouteModeUiState): RouteActionUiState {
    return when (routeMode) {
        is MainRouteModeUiState.Today -> RouteActionUiState(
            showRefresh = routeMode.canRefreshDistance,
            showTrackingToggle = routeMode.isTrackingToggleVisible,
            isTrackingEnabled = routeMode.isTrackingEnabled
        )

        is MainRouteModeUiState.Past -> RouteActionUiState(
            showPlayback = routeMode.isPlaybackEntryVisible
        )
    }
}
