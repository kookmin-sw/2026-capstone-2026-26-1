package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.place.presentation.screen.PlaceCreateBottomSheet
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.PermissionSettingDialog

@Composable
fun MainScreen(
    uiState: MainUiState,
    onInitialCameraCentered: () -> Unit,
    onDateSelected: (String) -> Unit,
    onRouteAction: (RouteUiAction) -> Unit,
    onTrackingPermissionDialogConfirm: () -> Unit,
    onTrackingPermissionDialogDismiss: () -> Unit,
    onPermissionBannerConfirm: () -> Unit,
    debugActions: MainDebugActions
) {
    var selectedBottomSheetTab by rememberSaveable { mutableStateOf(MainBottomSheetTab.PLACE) }
    var isPlaceCreateSheetVisible by rememberSaveable { mutableStateOf(false) }

    MainBottomSheetScaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        content = { floatingBottomPadding ->
            MainMapSection(
                uiState = uiState,
                onInitialCameraCentered = onInitialCameraCentered,
                onDateSelected = onDateSelected,
                onRouteAction = onRouteAction,
                onPermissionBannerConfirm = onPermissionBannerConfirm,
                debugActions = debugActions,
                floatingBottomPadding = floatingBottomPadding
            )
        },
        sheet = { sheetModifier ->
            MainBottomSheet(
                modifier = sheetModifier,
                places = uiState.selectedRoute.places,
                selectedDateKey = uiState.selectedDateKey,
                routeTitle = uiState.selectedRoute.title,
                routeMemo = uiState.selectedRoute.memo,
                isRouteLoading = uiState.isRouteLoading,
                isRouteEmpty = uiState.isRouteEmpty,
                routeErrorMessage = uiState.routeErrorMessage,
                selectedTab = selectedBottomSheetTab,
                onTabSelected = { selectedBottomSheetTab = it },
                onAddPlaceClick = { isPlaceCreateSheetVisible = true }
            )
        }
    )

    if (isPlaceCreateSheetVisible) {
        PlaceCreateBottomSheet(
            selectedDateKey = uiState.selectedDateKey,
            onDismiss = { isPlaceCreateSheetVisible = false },
            onCreated = {
                isPlaceCreateSheetVisible = false
                onDateSelected(uiState.selectedDateKey)
            }
        )
    }

    if (uiState.showTrackingPermissionDialog) {
        PermissionSettingDialog(
            onConfirm = onTrackingPermissionDialogConfirm,
            onDismiss = onTrackingPermissionDialogDismiss
        )
    }
}
