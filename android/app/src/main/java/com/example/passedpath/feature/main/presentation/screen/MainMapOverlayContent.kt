package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.passedpath.BuildConfig
import com.example.passedpath.R
import com.example.passedpath.feature.main.presentation.component.MainMoreActionSheet
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.route.presentation.screen.RouteStatusOverlay
import com.example.passedpath.feature.route.presentation.screen.RouteTopEndControls
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.component.modal.PassedPathBottomModal
import com.example.passedpath.ui.component.floating.FloatingButtonColumn
import com.example.passedpath.ui.component.feedback.MapOverlayNetworkFailureDialog
import com.example.passedpath.ui.component.loading.BaseLoadingLine
import com.example.passedpath.ui.theme.Black

private val RouteOverlayTopGap = 21.dp
private val RouteOverlaySidePadding = 16.dp

@Composable
internal fun BoxScope.MainMapOverlayContent(
    uiState: MainUiState,
    onDateSelected: (String) -> Unit,
    onBookmarkClick: () -> Unit,
    isMoreMenuVisible: Boolean,
    onMoreClick: () -> Unit,
    onMoreDismissRequest: () -> Unit,
    onMorePlaceBookmarkClick: () -> Unit,
    onMoreDeleteRecordClick: () -> Unit,
    onRouteAction: (RouteUiAction) -> Unit,
    debugActions: MainDebugActions,
    isDebugPanelVisible: Boolean,
    onCloseDebugPanel: () -> Unit,
    topStartControls: @Composable (() -> Unit)? = null
) {
    val routeModeUiState = uiState.routeModeUiState
    val routeErrorMessage = routeModeUiState.routeErrorMessage
    val routeErrorKey = routeErrorMessage?.let { message ->
        "${routeModeUiState.route.dateKey}:$message"
    }
    val isPastRouteLoading = routeModeUiState is MainRouteModeUiState.Past &&
        routeModeUiState.isRouteLoading
    var dismissedRouteErrorKey by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(routeErrorKey, routeModeUiState.isRouteLoading) {
        if (routeErrorKey == null || routeModeUiState.isRouteLoading) {
            dismissedRouteErrorKey = null
        }
    }

    if (routeErrorMessage == null) {
        RouteStatusOverlay(
            routeModeUiState = routeModeUiState,
            onRouteAction = onRouteAction
        )
    }

    RouteTopBars(
        route = uiState.selectedRoute,
        isBookmarkUpdating = uiState.bookmarkToggleUiState.isUpdating(uiState.selectedDateKey),
        onDateSelected = onDateSelected,
        onBookmarkClick = onBookmarkClick,
        onMoreClick = onMoreClick,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
    )

    when {
        isPastRouteLoading -> {
            BaseLoadingLine(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = RouteTopBarsHeight)
            )
        }

    }

    if (
        routeErrorMessage != null &&
        routeErrorKey != null &&
        dismissedRouteErrorKey != routeErrorKey
    ) {
        val retryAction = when (routeModeUiState) {
            is MainRouteModeUiState.Today -> RouteUiAction.RefreshTodayRoute
            is MainRouteModeUiState.Past -> RouteUiAction.RetryPastRoute
        }
        MapOverlayNetworkFailureDialog(
            retryText = stringResource(R.string.route_retry),
            message = routeErrorMessage,
            onDismiss = { dismissedRouteErrorKey = routeErrorKey },
            onRetryClick = {
                dismissedRouteErrorKey = routeErrorKey
                onRouteAction(retryAction)
            }
        )
    }

    FloatingButtonColumn(
        modifier = Modifier
            .align(Alignment.TopStart)
            .statusBarsPadding()
            .padding(
                top = RouteTopBarsHeight + RouteOverlayTopGap,
                start = RouteOverlaySidePadding
            )
    ) {
        topStartControls?.invoke()
    }

    FloatingButtonColumn(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .statusBarsPadding()
            .padding(
                top = RouteTopBarsHeight + RouteOverlayTopGap,
                end = RouteOverlaySidePadding
            )
    ) {
        RouteTopEndControls(
            routeMode = uiState.routeModeUiState,
            onRouteAction = onRouteAction
        )
    }

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = RouteTopBarsHeight + 160.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (BuildConfig.DEBUG && isDebugPanelVisible) {
            MainDebugPanel(
                debugUiState = uiState.debugUiState,
                onRefreshSystemState = debugActions.refreshSystemState,
                onReloadRoute = debugActions.reloadRoute,
                onClose = onCloseDebugPanel
            )
        }
    }

    if (isMoreMenuVisible) {
        PassedPathBottomModal(
            onDimClick = onMoreDismissRequest,
            modifier = Modifier.background(Black.copy(alpha = 0.22f)),
            onBackPress = onMoreDismissRequest
        ) {
            MainMoreActionSheet(
                onPlaceBookmarkClick = onMorePlaceBookmarkClick,
                onDeleteRecordClick = onMoreDeleteRecordClick
            )
        }
    }
}
