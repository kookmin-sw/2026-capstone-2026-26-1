package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.passedpath.BuildConfig
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.permission.presentation.mapper.createPermissionOverlayUiModel
import com.example.passedpath.feature.route.presentation.screen.RouteStatusOverlay
import com.example.passedpath.feature.route.presentation.screen.RouteTopCenterControls
import com.example.passedpath.feature.route.presentation.screen.RouteTopEndControls
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.component.floating.FloatingButtonColumn
import com.example.passedpath.ui.component.banner.ActionBottomBanner

@Composable
internal fun BoxScope.MainMapOverlayContent(
    uiState: MainUiState,
    onDateSelected: (String) -> Unit,
    onBookmarkClick: () -> Unit,
    onRouteAction: (RouteUiAction) -> Unit,
    onPermissionActionClick: () -> Unit,
    debugActions: MainDebugActions,
    floatingBottomPadding: Dp,
    bottomEndControlsBottomPadding: Dp = floatingBottomPadding,
    isDebugPanelExpanded: Boolean,
    onToggleDebugPanelExpanded: () -> Unit,
    topStartControls: @Composable (() -> Unit)? = null,
    topEndControls: @Composable (() -> Unit)? = null,
    floatingControls: @Composable (() -> Unit)? = null
) {
    val permissionOverlayUiModel = createPermissionOverlayUiModel(
        permissionState = uiState.permissionState,
        isLocationServiceEnabled = uiState.isLocationServiceEnabled
    )

    RouteStatusOverlay(
        routeModeUiState = uiState.routeModeUiState,
        onRouteAction = onRouteAction
    )

    RouteTopBars(
        route = uiState.selectedRoute,
        isBookmarkUpdating = uiState.bookmarkToggleUiState.isUpdating(uiState.selectedDateKey),
        onDateSelected = onDateSelected,
        onBookmarkClick = onBookmarkClick,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
    )

    FloatingButtonColumn(
        modifier = Modifier
            .align(Alignment.TopStart)
            .statusBarsPadding()
            .padding(top = RouteTopBarsHeight + 21.dp, start = 16.dp)
    ) {
        topStartControls?.invoke()
    }

    FloatingButtonColumn(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .statusBarsPadding()
            .padding(top = RouteTopBarsHeight + 21.dp, end = 16.dp)
    ) {
        RouteTopEndControls(
            routeMode = uiState.routeModeUiState,
            onRouteAction = onRouteAction
        )
        topEndControls?.invoke()
    }

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = RouteTopBarsHeight + 120.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RouteTopCenterControls(
            routeMode = uiState.routeModeUiState,
            onRouteAction = onRouteAction
        )
        if (BuildConfig.DEBUG) {
            MainDebugPanel(
                debugUiState = uiState.debugUiState,
                onRefreshSystemState = debugActions.refreshSystemState,
                onReloadRoute = debugActions.reloadRoute,
                isExpanded = isDebugPanelExpanded,
                onToggleExpanded = onToggleDebugPanelExpanded
            )
        }
    }

    FloatingButtonColumn(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp, bottom = bottomEndControlsBottomPadding)
    ) {
        floatingControls?.invoke()
    }

    permissionOverlayUiModel?.let { overlayUiModel ->
        ActionBottomBanner(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .padding(bottom = floatingBottomPadding),
            message = stringResource(overlayUiModel.messageResId),
            actionText = stringResource(overlayUiModel.actionTextResId),
            onClickAction = onPermissionActionClick
        )
    }
}
