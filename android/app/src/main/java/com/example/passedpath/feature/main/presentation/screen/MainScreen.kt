package com.example.passedpath.feature.main.presentation.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.daynote.presentation.state.DayNoteUiState
import com.example.passedpath.feature.main.presentation.policy.reduceForBottomSheetTabSelection
import com.example.passedpath.feature.main.presentation.policy.reduceForDateChange
import com.example.passedpath.feature.main.presentation.policy.reduceForMapFocusHandled
import com.example.passedpath.feature.main.presentation.policy.reduceForPlaceCreated
import com.example.passedpath.feature.main.presentation.policy.reduceForPlaceCardClick
import com.example.passedpath.feature.main.presentation.policy.reduceForPlaceMarkerClick
import com.example.passedpath.feature.main.presentation.policy.reduceForSelectedPlaceHandled
import com.example.passedpath.feature.main.presentation.policy.reduceForSheetCommandConsumed
import com.example.passedpath.feature.main.presentation.policy.reduceForSheetHideRequest
import com.example.passedpath.feature.main.presentation.policy.reduceForSheetValueChange
import com.example.passedpath.feature.main.presentation.policy.shouldShowCurrentLocationButton
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.place.domain.model.VisitedPlace
import com.example.passedpath.feature.place.presentation.component.PlaceDeleteConfirmDialog
import com.example.passedpath.feature.place.presentation.component.PlaceEditNameBottomSheet
import com.example.passedpath.feature.place.presentation.state.PlaceUiState
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.PlaceMarkerUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.ui.PermissionSettingDialog
import com.example.passedpath.ui.component.dialog.BaseConfirmDialog
import com.example.passedpath.ui.component.input.BaseKeyboardInputBar
import com.example.passedpath.ui.component.toast.ToastOverlayHost
import com.example.passedpath.ui.component.toast.ToastOverlayItem

data class PlaceCreatedEvent(
    val id: Int,
    val placeId: Long
)

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
    onCloseReorderGuideBanner: () -> Unit,
    onUpdatePlaceName: (Long, String) -> Unit,
    onConfirmDeletePlace: (Long) -> Unit,
    onTrackingPermissionDialogConfirm: () -> Unit,
    onTrackingPermissionDialogDismiss: () -> Unit,
    onPermissionActionClick: () -> Unit,
    mainTabReselectionEvent: Int,
    placeCreatedEvent: PlaceCreatedEvent?,
    onPlaceCreatedEventHandled: (Int) -> Unit,
    showUnsavedDayNoteDialog: Boolean,
    onDismissUnsavedDayNoteDialog: () -> Unit,
    onConfirmUnsavedDayNoteDialog: () -> Unit,
    debugActions: MainDebugActions
) {
    var localUiState by rememberSaveable(stateSaver = MainScreenLocalUiStateSaver) {
        mutableStateOf(MainScreenLocalUiState())
    }
    var pendingDeletePlace by remember {
        mutableStateOf<VisitedPlace?>(null)
    }
    var pendingEditPlace by remember {
        mutableStateOf<VisitedPlace?>(null)
    }
    var editPlaceName by remember {
        mutableStateOf("")
    }
    var isPlaceNameFocused by remember {
        mutableStateOf(false)
    }
    val focusManager = LocalFocusManager.current

    fun hideEditKeyboard() {
        isPlaceNameFocused = false
        focusManager.clearFocus(force = true)
    }

    fun dismissPlaceEdit() {
        pendingEditPlace = null
        editPlaceName = ""
        hideEditKeyboard()
    }

    fun submitPlaceEdit() {
        val place = pendingEditPlace ?: return
        val trimmedPlaceName = editPlaceName.trim()
        if (trimmedPlaceName.isBlank() || trimmedPlaceName == place.placeName.trim()) return
        dismissPlaceEdit()
        onUpdatePlaceName(place.placeId, trimmedPlaceName)
    }

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

    fun handlePlaceCardClick(placeId: Long) {
        dispatchInteraction(
            reduceForPlaceCardClick(
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
    val placeToastMessage = placeUiState.errorMessage ?: placeUiState.successMessage
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
        if (placeToastMessage != null) {
            add(
                ToastOverlayItem(
                    message = placeToastMessage,
                    triggerKey = "place:${placeUiState.feedbackEventId}:$placeToastMessage"
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

    LaunchedEffect(placeUiState.feedbackEventId, placeToastMessage) {
        if (placeToastMessage == null) return@LaunchedEffect
        Log.d(
            "PlaceFlow",
            "place toast eventId=${placeUiState.feedbackEventId} isError=${placeUiState.errorMessage != null} message=$placeToastMessage"
        )
    }

    LaunchedEffect(uiState.selectedDateKey) {
        pendingDeletePlace = null
        dismissPlaceEdit()
        dispatchInteraction(reduceForDateChange(localUiState))
    }

    LaunchedEffect(mainTabReselectionEvent) {
        if (mainTabReselectionEvent == 0) return@LaunchedEffect
        focusManager.clearFocus(force = true)
        hideBottomSheet()
    }

    LaunchedEffect(placeCreatedEvent?.id) {
        val event = placeCreatedEvent ?: return@LaunchedEffect
        dispatchInteraction(
            reduceForPlaceCreated(
                state = localUiState,
                placeId = event.placeId
            )
        )
        onPlaceCreatedEventHandled(event.id)
    }

    BackHandler(enabled = pendingEditPlace != null) {
        if (isPlaceNameFocused) {
            hideEditKeyboard()
        } else {
            dismissPlaceEdit()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MainBottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            requestedSheetValue = localUiState.requestedSheetValue,
            onSheetValueChanged = ::handleSheetValueChanged,
            onSheetCommandConsumed = ::handleSheetCommandConsumed,
            content = { floatingBottomPadding ->
                MainMapSection(
                    uiState = uiState,
                    markerPlaces = markerPlaces,
                    focusedPlaceId = localUiState.focusedPlaceId,
                    onFocusedPlaceHandled = {
                        dispatchInteraction(reduceForMapFocusHandled(localUiState))
                    },
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
                    floatingBottomPadding = floatingBottomPadding,
                    showCurrentLocationButton = shouldShowCurrentLocationButton(
                        bottomSheetValue = localUiState.bottomSheetValue
                    )
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
                    onReorderPlaces = onReorderPlaces,
                    onCloseReorderGuideBanner = onCloseReorderGuideBanner,
                    onEditPlaceClick = { placeId ->
                        placeUiState.placeList.places.firstOrNull { place ->
                            place.placeId == placeId
                        }?.let { place ->
                            pendingEditPlace = place
                            editPlaceName = place.placeName
                            isPlaceNameFocused = false
                        }
                    },
                    onPlaceClick = ::handlePlaceCardClick,
                    onDeletePlaceRequested = { placeId ->
                        pendingDeletePlace = placeUiState.placeList.places.firstOrNull { place ->
                            place.placeId == placeId
                        }
                    }
                )
            }
        )

        ToastOverlayHost(
            toasts = overlayToasts,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        pendingEditPlace?.let { place ->
            PlaceEditNameOverlay(
                place = place,
                placeName = editPlaceName,
                isSubmitting = placeUiState.isSubmitting,
                isNameFocused = isPlaceNameFocused,
                onPlaceNameChange = { editPlaceName = it },
                onNameFocusChanged = { isPlaceNameFocused = it },
                onClearInputFocus = ::hideEditKeyboard,
                onDismiss = ::dismissPlaceEdit,
                onSubmit = ::submitPlaceEdit,
                modifier = Modifier.fillMaxSize()
            )
        }
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
            message = "변경사항을 저장하지 않으면 사라집니다",
            dismissText = "취소",
            confirmText = "저장",
            onDismiss = onDismissUnsavedDayNoteDialog,
            onConfirm = onConfirmUnsavedDayNoteDialog
        )
    }

    pendingDeletePlace?.let { place ->
        PlaceDeleteConfirmDialog(
            placeName = place.placeName.ifBlank { "이 장소" },
            onDismiss = {
                pendingDeletePlace = null
            },
            onConfirm = {
                pendingDeletePlace = null
                onConfirmDeletePlace(place.placeId)
            }
        )
    }
}

@Composable
private fun PlaceEditNameOverlay(
    place: VisitedPlace,
    placeName: String,
    isSubmitting: Boolean,
    isNameFocused: Boolean,
    onPlaceNameChange: (String) -> Unit,
    onNameFocusChanged: (Boolean) -> Unit,
    onClearInputFocus: () -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canSubmit = placeName.trim().isNotBlank() &&
        placeName.trim() != place.placeName.trim() &&
        !isSubmitting

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.34f))
                .clickable(onClick = onClearInputFocus)
        )
        PlaceEditNameBottomSheet(
            placeName = placeName,
            originalPlaceName = place.placeName,
            roadAddress = place.roadAddress,
            onPlaceNameChange = onPlaceNameChange,
            onNameFocusChanged = onNameFocusChanged,
            onClearInputFocus = onClearInputFocus,
            onDismiss = onDismiss,
            onSubmit = onSubmit,
            isSubmitting = isSubmitting,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        if (isNameFocused) {
            BaseKeyboardInputBar(
                title = stringResource(R.string.place_edit_keyboard_title),
                value = placeName,
                onValueChange = onPlaceNameChange,
                onDismiss = onClearInputFocus,
                onConfirmInput = onClearInputFocus,
                enabled = canSubmit,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(bottom = 2.dp)
            )
        }
    }
}
