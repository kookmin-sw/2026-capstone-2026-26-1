package com.example.passedpath.feature.main.presentation.policy

import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState

internal sealed interface TrackingToggleDecision {
    data object ShowPermissionDialog : TrackingToggleDecision
    data object StartTracking : TrackingToggleDecision
    data object StopTracking : TrackingToggleDecision
    data object NoOp : TrackingToggleDecision
}

internal fun decideTrackingToggle(
    permissionState: LocationPermissionUiState,
    routeModeUiState: MainRouteModeUiState
): TrackingToggleDecision {
    if (permissionState != LocationPermissionUiState.ALWAYS) {
        return TrackingToggleDecision.ShowPermissionDialog
    }

    return when (routeModeUiState) {
        is MainRouteModeUiState.Today -> {
            if (routeModeUiState.isTrackingEnabled) {
                TrackingToggleDecision.StopTracking
            } else {
                TrackingToggleDecision.StartTracking
            }
        }

        is MainRouteModeUiState.Past -> TrackingToggleDecision.NoOp
    }
}
