package com.example.passedpath.feature.main.presentation.policy

import com.example.passedpath.feature.permission.presentation.policy.canRunTracking
import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState

internal sealed interface TrackingRecoveryDecision {
    data object RestartTracking : TrackingRecoveryDecision
    data object StopTracking : TrackingRecoveryDecision
    data object NoOp : TrackingRecoveryDecision
}

internal fun decideTrackingRecovery(
    permissionState: LocationPermissionUiState,
    isLocationServiceEnabled: Boolean,
    isTrackingActive: Boolean,
    isTrackingEnabledByUser: Boolean
): TrackingRecoveryDecision {
    val canRun = canRunTracking(permissionState, isLocationServiceEnabled)

    return when {
        canRun && isTrackingEnabledByUser && !isTrackingActive -> {
            TrackingRecoveryDecision.RestartTracking
        }

        canRun && !isTrackingEnabledByUser && isTrackingActive -> {
            TrackingRecoveryDecision.StopTracking
        }

        !canRun && isTrackingActive -> TrackingRecoveryDecision.StopTracking
        else -> TrackingRecoveryDecision.NoOp
    }
}
