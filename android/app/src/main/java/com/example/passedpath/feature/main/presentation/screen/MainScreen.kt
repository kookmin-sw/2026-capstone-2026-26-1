package com.example.passedpath.feature.main.presentation.screen

import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
import com.example.passedpath.feature.place.domain.model.PlaceSearchResult
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
import com.example.passedpath.ui.component.modal.PassedPathBottomModal
import com.example.passedpath.ui.component.toast.ToastOverlayHost
import com.example.passedpath.ui.component.toast.ToastOverlayItem

data class PlaceCreatedEvent(
    val id: Int,
    val placeId: Long
)

data class PlaceEditSearchResultEvent(
    val id: Int,
    val place: PlaceSearchResult
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
    onNavigateToEditPlaceSearch: (String) -> Unit,
    onReorderPlaces: (List<Long>) -> Unit,
    onCloseReorderGuideBanner: () -> Unit,
    onUpdatePlace: (Long, String, String, Double, Double) -> Unit,
    onConfirmDeletePlace: (Long) -> Unit,
    onTrackingPermissionDialogConfirm: () -> Unit,
    onTrackingPermissionDialogDismiss: () -> Unit,
    onPermissionActionClick: () -> Unit,
    mainTabReselectionEvent: Int,
    placeCreatedEvent: PlaceCreatedEvent?,
    onPlaceCreatedEventHandled: (Int) -> Unit,
    placeEditSearchResultEvent: PlaceEditSearchResultEvent?,
    onPlaceEditSearchResultEventHandled: (Int) -> Unit,
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
    var pendingEditPlaceId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editPlaceName by rememberSaveable { mutableStateOf("") }
    var editRoadAddress by rememberSaveable { mutableStateOf("") }
    var editLatitude by rememberSaveable { mutableStateOf(0.0) }
    var editLongitude by rememberSaveable { mutableStateOf(0.0) }
    var isPlaceNameFocused by rememberSaveable { mutableStateOf(false) }
    var observedDateKey by rememberSaveable { mutableStateOf(uiState.selectedDateKey) }
    val pendingEditPlace = pendingEditPlaceId?.let { placeId ->
        placeUiState.placeList.places.firstOrNull { place -> place.placeId == placeId }
    }
    val focusManager = LocalFocusManager.current

    fun hideEditKeyboard() {
        isPlaceNameFocused = false
        focusManager.clearFocus(force = true)
    }

    fun dismissPlaceEdit() {
        pendingEditPlaceId = null
        editPlaceName = ""
        editRoadAddress = ""
        editLatitude = 0.0
        editLongitude = 0.0
        hideEditKeyboard()
    }

    fun submitPlaceEdit() {
        val place = pendingEditPlace ?: return
        val trimmedPlaceName = editPlaceName.trim()
        val trimmedRoadAddress = editRoadAddress.trim()
        val isUnchanged = trimmedPlaceName == place.placeName.trim() &&
            trimmedRoadAddress == place.roadAddress.trim() &&
            editLatitude == place.latitude &&
            editLongitude == place.longitude
        if (trimmedPlaceName.isBlank() || trimmedRoadAddress.isBlank() || isUnchanged) return
        dismissPlaceEdit()
        onUpdatePlace(
            place.placeId,
            trimmedPlaceName,
            trimmedRoadAddress,
            editLatitude,
            editLongitude
        )
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
        if (observedDateKey != uiState.selectedDateKey) {
            observedDateKey = uiState.selectedDateKey
            pendingDeletePlace = null
            dismissPlaceEdit()
            dispatchInteraction(reduceForDateChange(localUiState))
        }
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

    LaunchedEffect(placeEditSearchResultEvent?.id) {
        val event = placeEditSearchResultEvent ?: return@LaunchedEffect
        if (pendingEditPlaceId != null) {
            editPlaceName = event.place.name
            editRoadAddress = event.place.displayAddress
            editLatitude = event.place.latitude
            editLongitude = event.place.longitude
            hideEditKeyboard()
        }
        onPlaceEditSearchResultEventHandled(event.id)
    }

    LaunchedEffect(pendingEditPlaceId, placeUiState.placeList.places) {
        val placeId = pendingEditPlaceId ?: return@LaunchedEffect
        if (placeUiState.placeList.hasLoaded &&
            placeUiState.placeList.places.none { place -> place.placeId == placeId }
        ) {
            dismissPlaceEdit()
        }
    }

    BackHandler(enabled = pendingEditPlaceId != null) {
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
                            pendingEditPlaceId = place.placeId
                            editPlaceName = place.placeName
                            editRoadAddress = place.roadAddress
                            editLatitude = place.latitude
                            editLongitude = place.longitude
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
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(MainScreenOverlayZIndex.Toast)
        )

        pendingEditPlace?.let { place ->
            PlaceEditNameOverlay(
                place = place,
                placeName = editPlaceName,
                roadAddress = editRoadAddress,
                latitude = editLatitude,
                longitude = editLongitude,
                isSubmitting = placeUiState.isSubmitting,
                isNameFocused = isPlaceNameFocused,
                onPlaceNameChange = { editPlaceName = it },
                onNameFocusChanged = { isPlaceNameFocused = it },
                onClearInputFocus = ::hideEditKeyboard,
                onAddressClick = {
                    hideEditKeyboard()
                    onNavigateToEditPlaceSearch(uiState.selectedDateKey)
                },
                onDismiss = ::dismissPlaceEdit,
                onSubmit = ::submitPlaceEdit,
                modifier = Modifier.zIndex(MainScreenOverlayZIndex.PlaceEdit)
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

private object MainScreenOverlayZIndex {
    const val Toast = 1f
    const val PlaceEdit = 2f
}

@Composable
private fun PlaceEditNameOverlay(
    place: VisitedPlace,
    placeName: String,
    roadAddress: String,
    latitude: Double,
    longitude: Double,
    isSubmitting: Boolean,
    isNameFocused: Boolean,
    onPlaceNameChange: (String) -> Unit,
    onNameFocusChanged: (Boolean) -> Unit,
    onClearInputFocus: () -> Unit,
    onAddressClick: () -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canSubmit = placeName.trim().isNotBlank() &&
        roadAddress.trim().isNotBlank() &&
        (
            placeName.trim() != place.placeName.trim() ||
                roadAddress.trim() != place.roadAddress.trim() ||
                latitude != place.latitude ||
                longitude != place.longitude
        ) &&
        !isSubmitting

    PassedPathBottomModal(
        onDimClick = onClearInputFocus,
        modifier = modifier,
        onBackPress = {
            if (isNameFocused) {
                onClearInputFocus()
            } else {
                onDismiss()
            }
        },
        floatingBottomContent = if (isNameFocused) {
            {
                BaseKeyboardInputBar(
                    title = stringResource(R.string.place_edit_keyboard_title),
                    value = placeName,
                    onValueChange = onPlaceNameChange,
                    onDismiss = onClearInputFocus,
                    onConfirmInput = onClearInputFocus,
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(bottom = 2.dp)
                )
            }
        } else {
            null
        }
    ) {
        PlaceEditNameBottomSheet(
            placeName = placeName,
            originalPlaceName = place.placeName,
            roadAddress = roadAddress,
            onPlaceNameChange = onPlaceNameChange,
            onNameFocusChanged = onNameFocusChanged,
            onClearInputFocus = onClearInputFocus,
            onAddressClick = onAddressClick,
            onDismiss = onDismiss,
            onSubmit = onSubmit,
            isSubmitting = isSubmitting,
            isSubmitEnabled = canSubmit
        )
    }
}
