package com.example.passedpath.feature.main.presentation.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.passedpath.debug.AppDebugLogger
import com.example.passedpath.debug.DebugLogTag
import com.example.passedpath.feature.locationtracking.data.manager.LocationTrackingServiceStateReader
import com.example.passedpath.feature.main.presentation.policy.TrackingRecoveryDecision
import com.example.passedpath.feature.main.presentation.policy.decideTrackingRecovery
import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState

@Composable
internal fun MainTrackingRecoveryEffect(
    permissionState: LocationPermissionUiState,
    isLocationServiceEnabled: Boolean,
    isTrackingActive: Boolean,
    trackingServiceStateReader: LocationTrackingServiceStateReader,
    startLocationTracking: (Boolean) -> Unit,
    stopLocationTracking: (Boolean) -> Unit
) {
    LaunchedEffect(permissionState, isLocationServiceEnabled, isTrackingActive) {
        val isTrackingEnabledByUser = trackingServiceStateReader.isTrackingEnabledByUser()

        when (
            decideTrackingRecovery(
                permissionState = permissionState,
                isLocationServiceEnabled = isLocationServiceEnabled,
                isTrackingActive = isTrackingActive,
                isTrackingEnabledByUser = isTrackingEnabledByUser
            )
        ) {
            TrackingRecoveryDecision.RestartTracking -> {
                AppDebugLogger.debug(
                    DebugLogTag.TRACKING,
                    "auto-restart tracking because userEnabled=true and service inactive"
                )
                startLocationTracking(false)
            }

            TrackingRecoveryDecision.StopTracking -> {
                AppDebugLogger.debug(
                    DebugLogTag.TRACKING,
                    "stop tracking by recovery decision userEnabled=$isTrackingEnabledByUser " +
                        "permission=$permissionState gpsEnabled=$isLocationServiceEnabled"
                )
                stopLocationTracking(false)
            }

            TrackingRecoveryDecision.NoOp -> Unit
        }
    }
}
