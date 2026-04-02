package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.passedpath.feature.daynote.presentation.state.DayNoteUiState
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.place.presentation.screen.PlaceCreateBottomSheet
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.PermissionSettingDialog
import com.example.passedpath.ui.component.BaseConfirmDialog

@Composable
fun MainScreen(
    uiState: MainUiState,
    dayNoteUiState: DayNoteUiState,
    onInitialCameraCentered: () -> Unit,
    onDateSelected: (String) -> Unit,
    onDateSelectionRequested: (String) -> Unit,
    onRouteAction: (RouteUiAction) -> Unit,
    onDayNoteTitleChanged: (String) -> Unit,
    onDayNoteMemoChanged: (String) -> Unit,
    onDayNoteSaveClick: () -> Unit,
    onTrackingPermissionDialogConfirm: () -> Unit,
    onTrackingPermissionDialogDismiss: () -> Unit,
    onPermissionBannerConfirm: () -> Unit,
    showUnsavedDayNoteDialog: Boolean,
    onDismissUnsavedDayNoteDialog: () -> Unit,
    onConfirmUnsavedDayNoteDialog: () -> Unit,
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
                onDateSelected = onDateSelectionRequested,
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
                dayNoteUiState = dayNoteUiState,
                onDayNoteTitleChanged = onDayNoteTitleChanged,
                onDayNoteMemoChanged = onDayNoteMemoChanged,
                onDayNoteSaveClick = onDayNoteSaveClick,
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

    if (showUnsavedDayNoteDialog) {
        BaseConfirmDialog(
            title = "변경 사항을 저장할까요?",
            message = "저장하지 않으면 작성 중인 내용이 사라집니다.",
            dismissText = "취소",
            confirmText = "저장",
            onDismiss = onDismissUnsavedDayNoteDialog,
            onConfirm = onConfirmUnsavedDayNoteDialog
        )
    }
}
