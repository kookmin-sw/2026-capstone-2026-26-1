package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.daynote.presentation.viewmodel.DayNoteViewModel
import com.example.passedpath.feature.daynote.presentation.viewmodel.DayNoteViewModelFactory
import com.example.passedpath.feature.main.presentation.coordinator.DateSelectionDecision
import com.example.passedpath.feature.main.presentation.coordinator.DateSelectionGuardCoordinator
import com.example.passedpath.feature.main.presentation.mapper.resolveMainMarkerPlaces
import com.example.passedpath.feature.main.presentation.viewmodel.MainViewModel
import com.example.passedpath.feature.main.presentation.viewmodel.MainViewModelFactory
import com.example.passedpath.feature.place.presentation.viewmodel.PlaceViewModel
import com.example.passedpath.feature.place.presentation.viewmodel.PlaceViewModelFactory
import com.example.passedpath.feature.permission.presentation.policy.PermissionActionTarget
import com.example.passedpath.feature.permission.presentation.policy.resolvePermissionActionTarget
import com.example.passedpath.util.AppSettingsNavigator

@Composable
fun MainRoute(
    mainTabReselectionEvent: Int = 0,
    placeCreatedEvent: Int = 0,
    onNavigateToAddPlace: (String) -> Unit = {},
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val context = LocalContext.current
    val appContainer = context.appContainer
    val uiState by viewModel.uiState.collectAsState()
    val dayNoteViewModel: DayNoteViewModel = viewModel(
        factory = DayNoteViewModelFactory(
            appContainer = appContainer,
            initialDateKey = uiState.selectedDateKey
        )
    )
    val dayNoteUiState by dayNoteViewModel.uiState.collectAsStateWithLifecycle()
    val placeViewModel: PlaceViewModel = viewModel(
        factory = PlaceViewModelFactory(
            appContainer = appContainer,
            initialDateKey = uiState.selectedDateKey
        )
    )
    val placeUiState by placeViewModel.uiState.collectAsStateWithLifecycle()
    val markerPlaces = resolveMainMarkerPlaces(
        placeListUiState = placeUiState.placeList,
        route = uiState.selectedRoute
    )
    val dateSelectionGuardCoordinator = remember { DateSelectionGuardCoordinator() }
    val dateSelectionGuardState by dateSelectionGuardCoordinator.state.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.selectedDateKey, uiState.selectedRoute.title, uiState.selectedRoute.memo) {
        dayNoteViewModel.syncSelectedDay(
            dateKey = uiState.selectedDateKey,
            title = uiState.selectedRoute.title,
            memo = uiState.selectedRoute.memo
        )
    }

    LaunchedEffect(dayNoteViewModel) {
        dayNoteViewModel.snapshotPatch.collect { patch ->
            viewModel.applyDayNoteSnapshotPatch(
                dateKey = patch.dateKey,
                title = patch.title,
                memo = patch.memo,
                shouldUpdateTitle = patch.shouldUpdateTitle,
                shouldUpdateMemo = patch.shouldUpdateMemo
            )
        }
    }

    LaunchedEffect(uiState.selectedDateKey) {
        placeViewModel.updateDateKey(uiState.selectedDateKey)
        placeViewModel.fetchVisitedPlaces(uiState.selectedDateKey)
    }

    LaunchedEffect(placeCreatedEvent) {
        if (placeCreatedEvent == 0) return@LaunchedEffect
        placeViewModel.fetchVisitedPlaces(uiState.selectedDateKey)
    }

    LaunchedEffect(
        dateSelectionGuardState.pendingDateSelection,
        dayNoteUiState.isSubmitting,
        dayNoteUiState.isDirty,
        dayNoteUiState.errorMessage,
        dayNoteUiState.successMessage
    ) {
        val targetDate = dateSelectionGuardCoordinator.consumeDateSelectionAfterSave(
            isSubmitting = dayNoteUiState.isSubmitting,
            hasUnsavedDayNoteChanges = dayNoteUiState.isDirty,
            hasSaveError = dayNoteUiState.errorMessage != null,
            hasSaveSuccessMessage = dayNoteUiState.successMessage != null
        ) ?: return@LaunchedEffect

        viewModel.selectDate(targetDate)
    }

    fun requestDateSelection(dateKey: String) {
        when (
            val decision = dateSelectionGuardCoordinator.requestDateSelection(
                currentDateKey = uiState.selectedDateKey,
                targetDateKey = dateKey,
                hasUnsavedDayNoteChanges = dayNoteUiState.isDirty
            )
        ) {
            DateSelectionDecision.Ignore -> Unit
            DateSelectionDecision.RequireConfirmation -> Unit
            is DateSelectionDecision.Proceed -> viewModel.selectDate(decision.dateKey)
        }
    }

    MainRouteEffects(
        permissionState = uiState.permissionState,
        isLocationServiceEnabled = uiState.isLocationServiceEnabled,
        currentLocation = uiState.currentLocation,
        isTrackingActive = uiState.isTrackingActive,
        onRefreshPermissionState = viewModel::refreshPermissionState,
        onRefreshLocationServiceState = viewModel::refreshLocationServiceState,
        onCurrentLocationUpdated = viewModel::updateCurrentLocation,
        locationTracker = appContainer.currentLocationTracker,
        trackingServiceStateReader = appContainer.locationTrackingServiceStateReader,
        startLocationTracking = { persistUserPreference ->
            appContainer.startLocationTrackingUseCase(persistUserPreference)
        },
        stopLocationTracking = { persistUserPreference ->
            appContainer.stopLocationTrackingUseCase(persistUserPreference)
        }
    )

    MainScreen(
        uiState = uiState,
        dayNoteUiState = dayNoteUiState,
        placeUiState = placeUiState,
        markerPlaces = markerPlaces,
        onCameraIntentConsumed = viewModel::consumeCameraIntent,
        onDateSelected = viewModel::selectDate,
        onDateSelectionRequested = ::requestDateSelection,
        onBookmarkClick = viewModel::toggleSelectedRouteBookmark,
        onRouteAction = viewModel::handleRouteAction,
        onDayNoteTitleChanged = dayNoteViewModel::updateTitle,
        onDayNoteMemoChanged = dayNoteViewModel::updateMemo,
        onDayNoteSaveClick = dayNoteViewModel::submitDayNote,
        onPlaceListRefreshRequested = placeViewModel::fetchVisitedPlaces,
        onNavigateToAddPlace = onNavigateToAddPlace,
        onReorderPlaces = placeViewModel::reorderPlaces,
        onCloseReorderGuideBanner = placeViewModel::dismissReorderGuideBanner,
        onTrackingPermissionDialogConfirm = {
            viewModel.dismissTrackingPermissionDialog()
            AppSettingsNavigator.openAppSettings(context)
        },
        onTrackingPermissionDialogDismiss = viewModel::dismissTrackingPermissionDialog,
        onPermissionActionClick = {
            when (
                resolvePermissionActionTarget(
                    permissionState = uiState.permissionState,
                    isLocationServiceEnabled = uiState.isLocationServiceEnabled
                )
            ) {
                PermissionActionTarget.OpenAppSettings -> AppSettingsNavigator.openAppSettings(context)
                PermissionActionTarget.OpenLocationSettings -> AppSettingsNavigator.openLocationSettings(context)
                PermissionActionTarget.None -> Unit
            }
        },
        mainTabReselectionEvent = mainTabReselectionEvent,
        showUnsavedDayNoteDialog = dateSelectionGuardState.pendingDateSelection != null,
        onDismissUnsavedDayNoteDialog = dateSelectionGuardCoordinator::dismissPendingDateSelection,
        onConfirmUnsavedDayNoteDialog = dayNoteViewModel::submitDayNote,
        debugActions = MainDebugActions(
            refreshSystemState = {
                viewModel.refreshPermissionState()
                viewModel.refreshLocationServiceState()
            },
            reloadRoute = {
                viewModel.selectDate(uiState.selectedDateKey)
            }
        )
    )
}
