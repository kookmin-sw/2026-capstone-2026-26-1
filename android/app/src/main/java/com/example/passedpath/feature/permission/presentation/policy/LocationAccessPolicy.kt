package com.example.passedpath.feature.permission.presentation.policy

import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState

internal enum class PermissionActionTarget {
    OpenAppSettings,
    OpenLocationSettings,
    None
}

internal fun resolveLocationPermissionUiState(
    isBackgroundAlwaysGranted: Boolean,
    isForegroundGranted: Boolean
): LocationPermissionUiState {
    return when {
        isBackgroundAlwaysGranted -> LocationPermissionUiState.ALWAYS
        isForegroundGranted -> LocationPermissionUiState.FOREGROUND_ONLY
        else -> LocationPermissionUiState.DENIED
    }
}

internal fun canReceiveLocationUpdates(
    permissionState: LocationPermissionUiState,
    isLocationServiceEnabled: Boolean
): Boolean {
    return permissionState != LocationPermissionUiState.DENIED && isLocationServiceEnabled
}

internal fun canRunTracking(
    permissionState: LocationPermissionUiState,
    isLocationServiceEnabled: Boolean
): Boolean {
    return permissionState == LocationPermissionUiState.ALWAYS && isLocationServiceEnabled
}

internal fun resolvePermissionActionTarget(
    permissionState: LocationPermissionUiState,
    isLocationServiceEnabled: Boolean
): PermissionActionTarget {
    return when {
        permissionState != LocationPermissionUiState.ALWAYS -> PermissionActionTarget.OpenAppSettings
        !isLocationServiceEnabled -> PermissionActionTarget.OpenLocationSettings
        else -> PermissionActionTarget.None
    }
}
