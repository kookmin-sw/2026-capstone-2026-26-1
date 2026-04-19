package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.passedpath.BuildConfig
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.permission.presentation.mapper.createPermissionOverlayUiModel
import com.example.passedpath.feature.route.presentation.screen.RouteFloatingControls
import com.example.passedpath.feature.route.presentation.screen.RouteStatusOverlay
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.component.banner.PermissionBanner

@Composable
internal fun BoxScope.MainMapOverlayContent(
    uiState: MainUiState,
    onDateSelected: (String) -> Unit,
    onRouteAction: (RouteUiAction) -> Unit,
    onPermissionBannerConfirm: () -> Unit,
    debugActions: MainDebugActions,
    floatingBottomPadding: Dp,
    isDebugPanelExpanded: Boolean,
    onToggleDebugPanelExpanded: () -> Unit,
    currentLocationButton: @Composable (() -> Unit)? = null
) {
    val permissionOverlayUiModel = createPermissionOverlayUiModel(
        permissionState = uiState.permissionState,
        isLocationServiceEnabled = uiState.isLocationServiceEnabled
    )

    RouteStatusOverlay(
        routeModeUiState = uiState.routeModeUiState,
        onRouteAction = onRouteAction
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MainDateTopBar(
                selectedDateKey = uiState.selectedDateKey,
                onDateSelected = onDateSelected
            )
            Spacer(modifier = Modifier.size(10.dp))
            RouteFloatingControls(
                routeMode = uiState.routeModeUiState,
                onRouteAction = onRouteAction
            )
            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.size(10.dp))
                MainDebugPanel(
                    debugUiState = uiState.debugUiState,
                    onRefreshSystemState = debugActions.refreshSystemState,
                    onReloadRoute = debugActions.reloadRoute,
                    isExpanded = isDebugPanelExpanded,
                    onToggleExpanded = onToggleDebugPanelExpanded
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            currentLocationButton?.invoke()
        }
    }

    permissionOverlayUiModel?.let { overlayUiModel ->
        PermissionBanner(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .padding(bottom = floatingBottomPadding),
            message = stringResource(overlayUiModel.messageResId),
            actionText = stringResource(overlayUiModel.actionTextResId),
            onClickAction = onPermissionBannerConfirm
        )
    }
}
