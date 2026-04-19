package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.passedpath.feature.daynote.presentation.state.DayNoteUiState
import com.example.passedpath.feature.main.presentation.policy.reduceForBottomSheetTabSelection
import com.example.passedpath.feature.main.presentation.policy.reduceForDateChange
import com.example.passedpath.feature.main.presentation.policy.reduceForPlaceCreateSheetVisibility
import com.example.passedpath.feature.main.presentation.policy.reduceForPlaceMarkerClick
import com.example.passedpath.feature.main.presentation.policy.reduceForSelectedPlaceHandled
import com.example.passedpath.feature.main.presentation.policy.reduceForSheetValueChange
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.place.presentation.screen.PlaceCreateBottomSheet
import com.example.passedpath.feature.place.presentation.state.PlaceUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.PermissionSettingDialog
import com.example.passedpath.ui.component.BaseConfirmDialog

@Composable
fun MainScreen(
    uiState: MainUiState,
    dayNoteUiState: DayNoteUiState,
    placeUiState: PlaceUiState,
    onInitialCameraCentered: () -> Unit,
    onDateSelected: (String) -> Unit,
    onDateSelectionRequested: (String) -> Unit,
    onRouteAction: (RouteUiAction) -> Unit,
    onDayNoteTitleChanged: (String) -> Unit,
    onDayNoteMemoChanged: (String) -> Unit,
    onDayNoteSaveClick: () -> Unit,
    onPlaceListRefreshRequested: (String) -> Unit,
    onTrackingPermissionDialogConfirm: () -> Unit,
    onTrackingPermissionDialogDismiss: () -> Unit,
    onPermissionBannerConfirm: () -> Unit,
    showUnsavedDayNoteDialog: Boolean,
    onDismissUnsavedDayNoteDialog: () -> Unit,
    onConfirmUnsavedDayNoteDialog: () -> Unit,
    debugActions: MainDebugActions
) {
    var localUiState by rememberSaveable(stateSaver = MainScreenLocalUiStateSaver) {
        mutableStateOf(MainScreenLocalUiState())
    }

    LaunchedEffect(uiState.selectedDateKey) {
        localUiState = reduceForDateChange(localUiState)
    }

    MainBottomSheetScaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        requestedSheetValue = localUiState.requestedSheetValue,
        onSheetValueChanged = { bottomSheetValue ->
            localUiState = reduceForSheetValueChange(
                state = localUiState,
                bottomSheetValue = bottomSheetValue
            )
        },
        content = { floatingBottomPadding ->
            MainMapSection(
                uiState = uiState,
                onInitialCameraCentered = onInitialCameraCentered,
                onDateSelected = onDateSelectionRequested,
                onRouteAction = onRouteAction,
                onPlaceMarkerClick = { placeId ->
                    val result = reduceForPlaceMarkerClick(
                        state = localUiState,
                        placeId = placeId
                    )
                    localUiState = result.state
                    if (result.shouldRefreshPlaces) {
                        onPlaceListRefreshRequested(uiState.selectedDateKey)
                    }
                },
                onPermissionBannerConfirm = onPermissionBannerConfirm,
                debugActions = debugActions,
                floatingBottomPadding = floatingBottomPadding
            )
        },
        sheet = { sheetModifier ->
            MainBottomSheet(
                modifier = sheetModifier,
                selectedDateKey = uiState.selectedDateKey,
                placeUiState = placeUiState,
                dayNoteUiState = dayNoteUiState,
                selectedPlaceId = localUiState.selectedPlaceId,
                onSelectedPlaceHandled = {
                    localUiState = reduceForSelectedPlaceHandled(localUiState)
                },
                onDayNoteTitleChanged = onDayNoteTitleChanged,
                onDayNoteMemoChanged = onDayNoteMemoChanged,
                onDayNoteSaveClick = onDayNoteSaveClick,
                selectedTab = localUiState.selectedBottomSheetTab,
                onTabSelected = { tab ->
                    val result = reduceForBottomSheetTabSelection(
                        state = localUiState,
                        selectedTab = tab
                    )
                    localUiState = result.state
                    if (result.shouldRefreshPlaces) {
                        onPlaceListRefreshRequested(uiState.selectedDateKey)
                    }
                },
                onAddPlaceClick = {
                    localUiState = reduceForPlaceCreateSheetVisibility(
                        state = localUiState,
                        isVisible = true
                    )
                }
            )
        }
    )

    if (localUiState.isPlaceCreateSheetVisible) {
        PlaceCreateBottomSheet(
            selectedDateKey = uiState.selectedDateKey,
            onDismiss = {
                localUiState = reduceForPlaceCreateSheetVisibility(
                    state = localUiState,
                    isVisible = false
                )
            },
            onCreated = {
                localUiState = reduceForPlaceCreateSheetVisibility(
                    state = localUiState,
                    isVisible = false
                )
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
