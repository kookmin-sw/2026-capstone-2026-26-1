package com.example.passedpath.feature.permission.presentation.mapper

import com.example.passedpath.R
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.permission.presentation.model.PermissionOverlayUiModel

internal fun createPermissionOverlayUiModel(
    permissionState: LocationPermissionUiState,
    isLocationServiceEnabled: Boolean
): PermissionOverlayUiModel? {
    return when {
        permissionState == LocationPermissionUiState.DENIED -> PermissionOverlayUiModel(
            messageResId = R.string.permission_banner_denied_title,
            actionTextResId = R.string.permission_banner_action
        )

        permissionState == LocationPermissionUiState.FOREGROUND_ONLY -> PermissionOverlayUiModel(
            messageResId = R.string.permission_banner_foreground_title,
            actionTextResId = R.string.permission_banner_action
        )

        !isLocationServiceEnabled -> PermissionOverlayUiModel(
            messageResId = R.string.main_permission_off_title,
            actionTextResId = R.string.permission_banner_action
        )

        else -> null
    }
}
