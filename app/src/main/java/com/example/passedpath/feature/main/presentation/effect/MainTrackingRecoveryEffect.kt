package com.example.passedpath.feature.main.presentation.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.passedpath.debug.AppDebugLogger
import com.example.passedpath.debug.DebugLogTag
import com.example.passedpath.feature.locationtracking.data.manager.LocationTrackingServiceStateReader
import com.example.passedpath.feature.permission.presentation.policy.canRunTracking
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
        if (canRunTracking(permissionState, isLocationServiceEnabled)) {
            if (trackingServiceStateReader.isTrackingEnabledByUser() && !isTrackingActive) {
                AppDebugLogger.debug(
                    DebugLogTag.TRACKING,
                    "auto-restart tracking because userEnabled=true and service inactive"
                )
                startLocationTracking(false)
            } else if (!trackingServiceStateReader.isTrackingEnabledByUser() && isTrackingActive) {
                AppDebugLogger.debug(
                    DebugLogTag.TRACKING,
                    "auto-stop tracking because userEnabled=false and service active"
                )
                stopLocationTracking(false)
            }
        } else if (isTrackingActive) {
            AppDebugLogger.debug(
                DebugLogTag.TRACKING,
                "force-stop tracking because tracking cannot run permission=$permissionState gpsEnabled=$isLocationServiceEnabled"
            )
            stopLocationTracking(false)
        }
    }
}
