package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.example.passedpath.R
import com.example.passedpath.feature.daynote.presentation.state.DayNoteUiState
import com.example.passedpath.feature.main.presentation.policy.reduceForBottomSheetTabSelection
import com.example.passedpath.feature.main.presentation.policy.reduceForDateChange
import com.example.passedpath.feature.main.presentation.policy.reduceForPlaceMarkerClick
import com.example.passedpath.feature.main.presentation.policy.reduceForSelectedPlaceHandled
import com.example.passedpath.feature.main.presentation.policy.reduceForSheetCommandConsumed
import com.example.passedpath.feature.main.presentation.policy.reduceForSheetHideRequest
import com.example.passedpath.feature.main.presentation.policy.reduceForSheetValueChange
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.place.presentation.state.PlaceUiState
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.PlaceMarkerUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.PermissionSettingDialog
import com.example.passedpath.ui.component.dialog.BaseConfirmDialog
import com.example.passedpath.ui.component.toast.ToastOverlayHost
import com.example.passedpath.ui.component.toast.ToastOverlayItem

@Composable
fun MainScreen(
    uiState: MainUiState,
    dayNoteUiState: DayNoteUiState,
    placeUiState: PlaceUiState,
    markerPlaces: List<PlaceMarkerUiState>,
    onCameraIntentConsumed: () -> Unit,
    onDateSelected: (String) -> Unit,
    onDateSelectionRequested: (String) -> Unit,
    onBookmarkClick: () -> Unit,
    onRouteAction: (RouteUiAction) -> Unit,
    onDayNoteTitleChanged: (String) -> Unit,
    onDayNoteMemoChanged: (String) -> Unit,
    onDayNoteSaveClick: () -> Unit,
    onPlaceListRefreshRequested: (String) -> Unit,
    onNavigateToAddPlace: (String) -> Unit,
    onReorderPlaces: (List<Long>) -> Unit,
    onTrackingPermissionDialogConfirm: () -> Unit,
    onTrackingPermissionDialogDismiss: () -> Unit,
    onPermissionActionClick: () -> Unit,
    mainTabReselectionEvent: Int,
    showUnsavedDayNoteDialog: Boolean,
    onDismissUnsavedDayNoteDialog: () -> Unit,
    onConfirmUnsavedDayNoteDialog: () -> Unit,
    debugActions: MainDebugActions
) {
    var localUiState by rememberSaveable(stateSaver = MainScreenLocalUiStateSaver) {
        mutableStateOf(MainScreenLocalUiState())
    }
    val focusManager = LocalFocusManager.current

    fun dispatchInteraction(result: com.example.passedpath.feature.main.presentation.policy.MainScreenInteractionResult) {
        localUiState = result.state
        if (result.shouldRefreshPlaces) {
            onPlaceListRefreshRequested(uiState.selectedDateKey)
        }
    }
    fun handleSheetValueChanged(bottomSheetValue: MainBottomSheetValue) {
        dispatchInteraction(
            reduceForSheetValueChange(
                state = localUiState,
                bottomSheetValue = bottomSheetValue
            )
        )
    }

    fun handleSheetCommandConsumed(consumedValue: MainBottomSheetValue) {
        dispatchInteraction(
            reduceForSheetCommandConsumed(
                state = localUiState,
                consumedValue = consumedValue
            )
        )
    }

    fun hideBottomSheet() {
        dispatchInteraction(reduceForSheetHideRequest(localUiState))
    }

    fun handlePlaceMarkerClick(placeId: Long) {
        dispatchInteraction(
            reduceForPlaceMarkerClick(
                state = localUiState,
                placeId = placeId
            )
        )
    }

    fun handleBottomSheetTabSelected(tab: MainBottomSheetTab) {
        dispatchInteraction(
            reduceForBottomSheetTabSelection(
                state = localUiState,
                selectedTab = tab
            )
        )
    }

    val dayNoteToastMessage = dayNoteUiState.errorMessage ?: dayNoteUiState.successMessage
    val bookmarkToastMessage = uiState.bookmarkToggleUiState.feedbackMessage
    val shouldShowPastEmptyToast =
        uiState.routeModeUiState is MainRouteModeUiState.Past &&
            uiState.routeModeUiState.isRouteEmpty &&
            uiState.routeModeUiState.routeErrorMessage == null &&
            !uiState.routeModeUiState.isRouteLoading
    val overlayToasts = buildList {
        if (dayNoteToastMessage != null) {
            add(
                ToastOverlayItem(
                    message = dayNoteToastMessage,
                    triggerKey = "daynote:${dayNoteUiState.feedbackEventId}:$dayNoteToastMessage"
                )
            )
        }
        if (bookmarkToastMessage != null) {
            add(
                ToastOverlayItem(
                    message = bookmarkToastMessage,
                    triggerKey = "bookmark:${uiState.bookmarkToggleUiState.feedbackEventId}:$bookmarkToastMessage"
                )
            )
        }
        if (shouldShowPastEmptyToast) {
            add(
                ToastOverlayItem(
                    message = stringResource(R.string.route_empty_past_toast),
                    triggerKey = "route-empty:${uiState.selectedDateKey}"
                )
            )
        }
    }

    LaunchedEffect(uiState.selectedDateKey) {
        dispatchInteraction(reduceForDateChange(localUiState))
    }

    LaunchedEffect(mainTabReselectionEvent) {
        if (mainTabReselectionEvent == 0) return@LaunchedEffect
        focusManager.clearFocus(force = true)
        hideBottomSheet()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MainBottomSheetScaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            requestedSheetValue = localUiState.requestedSheetValue,
            onSheetValueChanged = ::handleSheetValueChanged,
            onSheetCommandConsumed = ::handleSheetCommandConsumed,
            content = { floatingBottomPadding ->
                MainMapSection(
                    uiState = uiState,
                    markerPlaces = markerPlaces,
                    onCameraIntentConsumed = onCameraIntentConsumed,
                    onDateSelected = onDateSelectionRequested,
                    onBookmarkClick = onBookmarkClick,
                    onRouteAction = onRouteAction,
                    onStatsClick = {},
                    onMoreClick = {},
                    onMapClick = {
                        focusManager.clearFocus(force = true)
                        hideBottomSheet()
                    },
                    onPlaceMarkerClick = ::handlePlaceMarkerClick,
                    onPermissionActionClick = onPermissionActionClick,
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
                        dispatchInteraction(reduceForSelectedPlaceHandled(localUiState))
                    },
                    onDayNoteTitleChanged = onDayNoteTitleChanged,
                    onDayNoteMemoChanged = onDayNoteMemoChanged,
                    onDayNoteSaveClick = onDayNoteSaveClick,
                    selectedTab = localUiState.selectedBottomSheetTab,
                    onTabSelected = ::handleBottomSheetTabSelected,
                    onPlaceRetryClick = {
                        onPlaceListRefreshRequested(uiState.selectedDateKey)
                    },
                    onAddPlaceClick = {
                        onNavigateToAddPlace(uiState.selectedDateKey)
                    },
                    onReorderPlaces = onReorderPlaces
                )
            }
        )

        ToastOverlayHost(
            toasts = overlayToasts,
            modifier = Modifier.align(Alignment.BottomCenter)
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
