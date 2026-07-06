package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.daynote.presentation.state.DayNoteUiState
import com.example.passedpath.feature.daynote.presentation.viewmodel.DayNoteViewModel
import com.example.passedpath.feature.daynote.presentation.viewmodel.DayNoteViewModelFactory
import com.example.passedpath.feature.main.presentation.coordinator.DateSelectionDecision
import com.example.passedpath.feature.main.presentation.coordinator.DateSelectionGuardCoordinator
import com.example.passedpath.feature.main.presentation.mapper.resolveMainMarkerPlaces
import com.example.passedpath.feature.main.presentation.viewmodel.MainViewModel
import com.example.passedpath.feature.main.presentation.viewmodel.MainViewModelFactory
import com.example.passedpath.feature.place.presentation.viewmodel.PlaceViewModel
import com.example.passedpath.feature.place.presentation.viewmodel.PlaceViewModelFactory
import com.example.passedpath.feature.placebookmark.presentation.viewmodel.PlaceBookmarkMapMarkerViewModel
import com.example.passedpath.feature.placebookmark.presentation.viewmodel.PlaceBookmarkMapMarkerViewModelFactory
import com.example.passedpath.feature.permission.presentation.policy.PermissionActionTarget
import com.example.passedpath.feature.permission.presentation.policy.resolvePermissionActionTarget
import com.example.passedpath.feature.summary.presentation.viewmodel.DaySummaryViewModel
import com.example.passedpath.feature.summary.presentation.viewmodel.DaySummaryViewModelFactory
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailMetric
import com.example.passedpath.util.AppSettingsNavigator

@Composable
fun MainRoute(
    mainTabReselectionEvent: Int = 0,
    placeCreatedEvent: PlaceCreatedEvent? = null,
    placeBookmarkChangedEvent: PlaceBookmarkChangedEvent? = null,
    calendarDateSelectedEvent: CalendarDateSelectedEvent? = null,
    onPlaceCreatedEventConsumed: (Int) -> Unit = {},
    onPlaceBookmarkChangedEventConsumed: (Int) -> Unit = {},
    onCalendarDateSelectedEventConsumed: (Int) -> Unit = {},
    onNavigateToAddPlace: (String) -> Unit = {},
    onNavigateToPlaceBookmarks: () -> Unit = {},
    onNavigateToCalendar: (String) -> Unit = {},
    onNavigateToWeeklySummary: () -> Unit = {},
    onNavigateToSummaryDetail: (SummaryDetailMetric, String) -> Unit = { _, _ -> },
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val context = LocalContext.current
    val appContainer = context.appContainer
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentLocationState = viewModel.currentLocationState.collectAsStateWithLifecycle()
    val dayNoteViewModel: DayNoteViewModel = viewModel(
        factory = DayNoteViewModelFactory(
            appContainer = appContainer,
            initialDateKey = uiState.selectedDateKey
        )
    )
    val dayNoteUiState by dayNoteViewModel.uiState.collectAsStateWithLifecycle()
    val daySummaryViewModel: DaySummaryViewModel = viewModel(
        factory = DaySummaryViewModelFactory(appContainer = appContainer)
    )
    val daySummaryUiState by daySummaryViewModel.uiState.collectAsStateWithLifecycle()
    val placeViewModel: PlaceViewModel = viewModel(
        factory = PlaceViewModelFactory(
            appContainer = appContainer,
            initialDateKey = uiState.selectedDateKey
        )
    )
    val placeUiState by placeViewModel.uiState.collectAsStateWithLifecycle()
    val placeBookmarkMapMarkerViewModel: PlaceBookmarkMapMarkerViewModel = viewModel(
        factory = PlaceBookmarkMapMarkerViewModelFactory(appContainer)
    )
    val placeBookmarkMapMarkerUiState by placeBookmarkMapMarkerViewModel.uiState.collectAsStateWithLifecycle()
    val placeListUiState = placeUiState.placeList
    val routeMarkerPlaces = uiState.selectedRoute.markerPlaces
    val markerPlaces = remember(
        placeListUiState.hasLoaded,
        placeListUiState.places,
        routeMarkerPlaces
    ) {
        resolveMainMarkerPlaces(
            placeListUiState = placeListUiState,
            route = uiState.selectedRoute
        )
    }
    val dateSelectionGuardCoordinator = remember { DateSelectionGuardCoordinator() }
    val dateSelectionGuardState by dateSelectionGuardCoordinator.state.collectAsStateWithLifecycle()

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

    MainDayNoteRouteEffects(
        selectedDateKey = uiState.selectedDateKey,
        selectedRouteTitle = uiState.selectedRoute.title,
        selectedRouteMemo = uiState.selectedRoute.memo,
        dayNoteViewModel = dayNoteViewModel,
        onApplyDayNoteSnapshotPatch = viewModel::applyDayNoteSnapshotPatch
    )

    MainPlaceRouteEffects(
        selectedDateKey = uiState.selectedDateKey,
        placeCreatedEvent = placeCreatedEvent,
        onUpdateDateKey = placeViewModel::updateDateKey,
        onFetchVisitedPlaces = placeViewModel::fetchVisitedPlaces
    )

    MainBookmarkMarkerRouteEffects(
        placeBookmarkChangedEvent = placeBookmarkChangedEvent,
        placeBookmarkMapMarkerViewModel = placeBookmarkMapMarkerViewModel,
        onPlaceBookmarkChangedEventConsumed = onPlaceBookmarkChangedEventConsumed
    )

    MainDateSelectionGuardEffect(
        pendingDateSelection = dateSelectionGuardState.pendingDateSelection,
        dayNoteUiState = dayNoteUiState,
        dateSelectionGuardCoordinator = dateSelectionGuardCoordinator,
        onDateSelected = viewModel::selectDate
    )

    MainCalendarDateSelectionEventEffect(
        calendarDateSelectedEvent = calendarDateSelectedEvent,
        onDateSelectionRequested = ::requestDateSelection,
        onCalendarDateSelectedEventConsumed = onCalendarDateSelectedEventConsumed
    )

    MainRouteEffects(
        permissionState = uiState.permissionState,
        isLocationServiceEnabled = uiState.isLocationServiceEnabled,
        currentLocationState = currentLocationState,
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
        daySummaryUiState = daySummaryUiState,
        placeUiState = placeUiState,
        currentLocationState = currentLocationState,
        markerPlaces = markerPlaces,
        bookmarkMarkers = placeBookmarkMapMarkerUiState.bookmarkPlaces,
        onCameraIntentConsumed = viewModel::consumeCameraIntent,
        onDateSelected = viewModel::selectDate,
        onDateSelectionRequested = ::requestDateSelection,
        onBookmarkClick = viewModel::toggleSelectedRouteBookmark,
        onRouteAction = viewModel::handleRouteAction,
        onDayNoteTitleChanged = dayNoteViewModel::updateTitle,
        onDayNoteMemoChanged = dayNoteViewModel::updateMemo,
        onDayNoteSaveClick = dayNoteViewModel::submitDayNote,
        onDaySummaryLoadRequest = { dateKey ->
            daySummaryViewModel.loadSummary(dateKey = dateKey)
        },
        onDaySummaryRetryClick = {
            daySummaryViewModel.loadSummary(
                dateKey = uiState.selectedDateKey,
                forceRefresh = true
            )
        },
        onDayNoteFeedbackDismissed = dayNoteViewModel::consumeFeedback,
        onPlaceListRefreshRequested = placeViewModel::fetchVisitedPlaces,
        onNavigateToAddPlace = onNavigateToAddPlace,
        onNavigateToPlaceBookmarks = onNavigateToPlaceBookmarks,
        onNavigateToCalendar = onNavigateToCalendar,
        onNavigateToWeeklySummary = onNavigateToWeeklySummary,
        onNavigateToSummaryDetail = onNavigateToSummaryDetail,
        onReorderPlaces = placeViewModel::reorderPlaces,
        onCloseReorderGuideBanner = placeViewModel::dismissReorderGuideBanner,
        onUpdatePlace = placeViewModel::updatePlace,
        onConfirmDeletePlace = placeViewModel::deletePlace,
        onPlaceFeedbackDismissed = placeViewModel::consumeFeedback,
        onBookmarkFeedbackDismissed = viewModel::consumeBookmarkFeedback,
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
        placeCreatedEvent = placeCreatedEvent,
        onPlaceCreatedEventHandled = onPlaceCreatedEventConsumed,
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

@Composable
private fun MainDayNoteRouteEffects(
    selectedDateKey: String,
    selectedRouteTitle: String,
    selectedRouteMemo: String,
    dayNoteViewModel: DayNoteViewModel,
    onApplyDayNoteSnapshotPatch: (
        dateKey: String,
        title: String?,
        memo: String?,
        shouldUpdateTitle: Boolean,
        shouldUpdateMemo: Boolean
    ) -> Unit
) {
    LaunchedEffect(selectedDateKey, selectedRouteTitle, selectedRouteMemo) {
        dayNoteViewModel.syncSelectedDay(
            dateKey = selectedDateKey,
            title = selectedRouteTitle,
            memo = selectedRouteMemo
        )
    }

    LaunchedEffect(dayNoteViewModel) {
        dayNoteViewModel.snapshotPatch.collect { patch ->
            onApplyDayNoteSnapshotPatch(
                patch.dateKey,
                patch.title,
                patch.memo,
                patch.shouldUpdateTitle,
                patch.shouldUpdateMemo
            )
        }
    }
}

@Composable
private fun MainPlaceRouteEffects(
    selectedDateKey: String,
    placeCreatedEvent: PlaceCreatedEvent?,
    onUpdateDateKey: (String) -> Unit,
    onFetchVisitedPlaces: (String) -> Unit
) {
    LaunchedEffect(selectedDateKey) {
        onUpdateDateKey(selectedDateKey)
        onFetchVisitedPlaces(selectedDateKey)
    }

    LaunchedEffect(placeCreatedEvent?.id) {
        if (placeCreatedEvent == null) return@LaunchedEffect
        onFetchVisitedPlaces(selectedDateKey)
    }
}

@Composable
private fun MainBookmarkMarkerRouteEffects(
    placeBookmarkChangedEvent: PlaceBookmarkChangedEvent?,
    placeBookmarkMapMarkerViewModel: PlaceBookmarkMapMarkerViewModel,
    onPlaceBookmarkChangedEventConsumed: (Int) -> Unit
) {
    LaunchedEffect(placeBookmarkMapMarkerViewModel) {
        placeBookmarkMapMarkerViewModel.fetchPlaceBookmarkMarkers()
    }

    LaunchedEffect(placeBookmarkChangedEvent?.id) {
        val event = placeBookmarkChangedEvent ?: return@LaunchedEffect
        placeBookmarkMapMarkerViewModel.fetchPlaceBookmarkMarkers()
        onPlaceBookmarkChangedEventConsumed(event.id)
    }
}

@Composable
private fun MainDateSelectionGuardEffect(
    pendingDateSelection: String?,
    dayNoteUiState: DayNoteUiState,
    dateSelectionGuardCoordinator: DateSelectionGuardCoordinator,
    onDateSelected: (String) -> Unit
) {
    LaunchedEffect(
        pendingDateSelection,
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

        onDateSelected(targetDate)
    }
}

@Composable
private fun MainCalendarDateSelectionEventEffect(
    calendarDateSelectedEvent: CalendarDateSelectedEvent?,
    onDateSelectionRequested: (String) -> Unit,
    onCalendarDateSelectedEventConsumed: (Int) -> Unit
) {
    LaunchedEffect(calendarDateSelectedEvent?.id) {
        val event = calendarDateSelectedEvent ?: return@LaunchedEffect
        onDateSelectionRequested(event.dateKey)
        onCalendarDateSelectedEventConsumed(event.id)
    }
}
