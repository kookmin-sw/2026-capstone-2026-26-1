package com.example.passedpath.feature.main.presentation.viewmodel

import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmark
import com.example.passedpath.feature.bookmark.domain.repository.DayRouteBookmarkRepository
import com.example.passedpath.feature.bookmark.domain.usecase.ToggleDayRouteBookmarkUseCase
import com.example.passedpath.feature.locationtracking.data.manager.LocationTrackingServiceStateReader
import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.DayRouteDetail
import com.example.passedpath.feature.locationtracking.domain.model.DayRoutePlace
import com.example.passedpath.feature.locationtracking.domain.model.RoutePoint
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.RemoteDayRouteResult
import com.example.passedpath.feature.locationtracking.domain.repository.TrackingDebugLogRepository
import com.example.passedpath.feature.locationtracking.domain.usecase.ObserveRecentTrackingEventsUseCase
import com.example.passedpath.feature.main.presentation.state.MainCameraIntent
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.permission.presentation.mapper.createPermissionOverlayUiModel
import com.example.passedpath.feature.place.domain.model.PlaceSourceType
import com.example.passedpath.feature.place.domain.model.VisitedPlace
import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.permission.data.manager.LocationPermissionStatusReader
import com.example.passedpath.feature.permission.data.manager.LocationServiceStatusReader
import com.example.passedpath.feature.route.presentation.coordinator.RouteStateCoordinator
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.testutil.MainDispatcherRule
import com.example.passedpath.ui.state.ApiFailureMessage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init loads remote day route into ui state for past date`() = runTest {
        val repository = FakeDayRouteRepository(
            resultByDate = mutableMapOf(
                "2026-03-29" to RemoteDayRouteResult.Success(
                    routeDetail = DayRouteDetail(
                        dateKey = "2026-03-29",
                        totalDistanceKm = 12.3,
                        title = "Han River",
                        memo = "Windy evening walk",
                        pathPointCount = 3,
                        polylinePoints = listOf(
                            RoutePoint(37.1, 127.1),
                            RoutePoint(37.2, 127.2),
                            RoutePoint(37.3, 127.3)
                        ),
                        places = listOf(
                            DayRoutePlace(1L, "A", "Road", 37.1, 127.1, 1)
                        )
                    )
                )
            )
        )

        val viewModel = createViewModel(
            repository = repository,
            initialDateKey = "2026-03-29",
            todayDateKey = "2026-03-31",
            backgroundGranted = true
        )

        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals("2026-03-29", state.selectedDateKey)
        assertFalse(state.isRouteLoading)
        assertFalse(state.isRouteEmpty)
        assertNull(state.routeErrorMessage)
        assertTrue(state.selectedRoute.hasLocationData)
        assertEquals("Han River", state.selectedRoute.title)
        assertEquals("Windy evening walk", state.selectedRoute.memo)
        assertEquals(3, state.selectedRoute.polylinePoints.size)
        assertTrue(state.mapPlaces.isEmpty())
        assertEquals(1, state.selectedRoute.places.size)
        assertEquals(listOf("2026-03-29"), repository.requestedRemoteDates)
        assertTrue(repository.observedLocalDates.isEmpty())
    }

    @Test
    fun `init observes local day route for today date`() = runTest {
        val repository = FakeDayRouteRepository(
            localRouteByDate = mutableMapOf(
                "2026-03-31" to MutableStateFlow(
                    DailyPath(
                        dateKey = "2026-03-31",
                        points = listOf(
                            TrackedLocation(37.1, 127.1, 5f, 1L),
                            TrackedLocation(37.2, 127.2, 5f, 2L)
                        ),
                        totalDistanceMeters = 1500.0,
                        pathPointCount = 2
                    )
                )
            )
        )

        val viewModel = createViewModel(
            repository = repository,
            initialDateKey = "2026-03-31",
            todayDateKey = "2026-03-31",
            backgroundGranted = true
        )

        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals("2026-03-31", state.selectedDateKey)
        assertFalse(state.isRouteLoading)
        assertFalse(state.isRouteEmpty)
        assertNull(state.routeErrorMessage)
        assertEquals(2, state.selectedRoute.polylinePoints.size)
        assertEquals(1.5, state.selectedRoute.totalDistanceKm, 0.0)
        assertEquals(listOf("2026-03-31"), repository.observedLocalDates)
        assertEquals(listOf("2026-03-31"), repository.requestedRemoteDates)
    }

    @Test
    fun `today route updates when local flow emits new data`() = runTest {
        val localFlow = MutableStateFlow<DailyPath?>(null)
        val repository = FakeDayRouteRepository(
            localRouteByDate = mutableMapOf("2026-03-31" to localFlow)
        )

        val viewModel = createViewModel(
            repository = repository,
            initialDateKey = "2026-03-31",
            todayDateKey = "2026-03-31"
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isRouteEmpty)

        localFlow.value = DailyPath(
            dateKey = "2026-03-31",
            points = listOf(
                TrackedLocation(37.1, 127.1, 5f, 1L),
                TrackedLocation(37.2, 127.2, 5f, 2L),
                TrackedLocation(37.3, 127.3, 5f, 3L)
            ),
            totalDistanceMeters = 2300.0,
            pathPointCount = 3
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRouteEmpty)
        assertNull(state.routeErrorMessage)
        assertEquals(3, state.selectedRoute.polylinePoints.size)
        assertEquals(2.3, state.selectedRoute.totalDistanceKm, 0.0)
    }

    @Test
    fun `first current location requests current location camera intent when route is empty`() = runTest {
        val viewModel = createViewModel(
            repository = FakeDayRouteRepository(),
            initialDateKey = "2026-03-31",
            todayDateKey = "2026-03-31",
            backgroundGranted = true
        )
        advanceUntilIdle()

        viewModel.updateCurrentLocation(
            MainCoordinateUiState(latitude = 37.1, longitude = 127.1)
        )

        assertEquals(
            MainCameraIntent.CenterCurrentLocation,
            viewModel.uiState.value.pendingCameraIntent
        )
    }

    @Test
    fun `consumed camera intent is not recreated by later today route updates on same date`() = runTest {
        val localFlow = MutableStateFlow<DailyPath?>(null)
        val viewModel = createViewModel(
            repository = FakeDayRouteRepository(
                localRouteByDate = mutableMapOf("2026-03-31" to localFlow)
            ),
            initialDateKey = "2026-03-31",
            todayDateKey = "2026-03-31",
            backgroundGranted = true
        )
        advanceUntilIdle()

        viewModel.updateCurrentLocation(
            MainCoordinateUiState(latitude = 37.1, longitude = 127.1)
        )
        viewModel.consumeCameraIntent()

        localFlow.value = DailyPath(
            dateKey = "2026-03-31",
            points = listOf(
                TrackedLocation(37.1, 127.1, 5f, 1L),
                TrackedLocation(37.2, 127.2, 5f, 2L)
            ),
            totalDistanceMeters = 1500.0,
            pathPointCount = 2
        )
        advanceUntilIdle()

        assertEquals(MainCameraIntent.FitRoute, viewModel.uiState.value.pendingCameraIntent)
        viewModel.consumeCameraIntent()

        localFlow.value = DailyPath(
            dateKey = "2026-03-31",
            points = listOf(
                TrackedLocation(37.1, 127.1, 5f, 1L),
                TrackedLocation(37.2, 127.2, 5f, 2L),
                TrackedLocation(37.3, 127.3, 5f, 3L)
            ),
            totalDistanceMeters = 2300.0,
            pathPointCount = 3
        )
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingCameraIntent)
    }

    @Test
    fun `tracking state updates today route mode`() = runTest {
        val trackingState = MutableStateFlow(false)
        val viewModel = createViewModel(
            repository = FakeDayRouteRepository(),
            initialDateKey = "2026-03-31",
            todayDateKey = "2026-03-31",
            trackingState = trackingState
        )
        advanceUntilIdle()

        assertFalse((viewModel.uiState.value.routeModeUiState as MainRouteModeUiState.Today).isTrackingEnabled)
        assertFalse(viewModel.uiState.value.isTrackingActive)

        trackingState.value = true
        advanceUntilIdle()

        assertTrue((viewModel.uiState.value.routeModeUiState as MainRouteModeUiState.Today).isTrackingEnabled)
        assertTrue(viewModel.uiState.value.isTrackingActive)
    }

    @Test
    fun `foreground only permission shows overlay`() = runTest {
        val permissionReader = MutableLocationPermissionStatusReader(
            foregroundGranted = true,
            backgroundGranted = false
        )
        val viewModel = createViewModel(
            repository = FakeDayRouteRepository(),
            initialDateKey = "2026-03-31",
            todayDateKey = "2026-03-31",
            permissionReader = permissionReader
        )
        advanceUntilIdle()

        assertEquals(LocationPermissionUiState.FOREGROUND_ONLY, viewModel.uiState.value.permissionState)
        assertNotNull(createPermissionOverlayUiModel(viewModel.uiState.value.permissionState, viewModel.uiState.value.isLocationServiceEnabled))
    }

    @Test
    fun `denied permission shows overlay and clears current location`() = runTest {
        val permissionReader = MutableLocationPermissionStatusReader(
            foregroundGranted = false,
            backgroundGranted = false
        )
        val viewModel = createViewModel(
            repository = FakeDayRouteRepository(),
            initialDateKey = "2026-03-31",
            todayDateKey = "2026-03-31",
            permissionReader = permissionReader
        )
        advanceUntilIdle()

        assertEquals(LocationPermissionUiState.DENIED, viewModel.uiState.value.permissionState)
        assertNotNull(createPermissionOverlayUiModel(viewModel.uiState.value.permissionState, viewModel.uiState.value.isLocationServiceEnabled))
        assertNull(viewModel.uiState.value.currentLocation)
    }

    @Test
    fun `gps off shows overlay even with always permission`() = runTest {
        val viewModel = createViewModel(
            repository = FakeDayRouteRepository(),
            initialDateKey = "2026-03-31",
            todayDateKey = "2026-03-31",
            backgroundGranted = true,
            isLocationServiceEnabled = false
        )
        advanceUntilIdle()

        assertEquals(LocationPermissionUiState.ALWAYS, viewModel.uiState.value.permissionState)
        assertFalse(viewModel.uiState.value.isLocationServiceEnabled)
        assertNotNull(createPermissionOverlayUiModel(viewModel.uiState.value.permissionState, viewModel.uiState.value.isLocationServiceEnabled))
    }

    @Test
    fun `refresh location service state updates gps flag`() = runTest {
        val locationServiceReader = MutableLocationServiceStatusReader(isEnabled = false)
        val viewModel = createViewModel(
            repository = FakeDayRouteRepository(),
            initialDateKey = "2026-03-31",
            todayDateKey = "2026-03-31",
            backgroundGranted = true,
            locationServiceReader = locationServiceReader
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLocationServiceEnabled)

        locationServiceReader.isEnabled = true
        viewModel.refreshLocationServiceState()

        assertTrue(viewModel.uiState.value.isLocationServiceEnabled)
        assertNull(createPermissionOverlayUiModel(viewModel.uiState.value.permissionState, viewModel.uiState.value.isLocationServiceEnabled))
    }

    @Test
    fun `toggle tracking starts service when today is not tracking and permission is always granted`() = runTest {
        var startCalls = 0
        var stopCalls = 0
        val viewModel = createViewModel(
            repository = FakeDayRouteRepository(),
            initialDateKey = "2026-03-31",
            todayDateKey = "2026-03-31",
            backgroundGranted = true,
            onStartTracking = { startCalls += 1 },
            onStopTracking = { stopCalls += 1 }
        )
        advanceUntilIdle()

        viewModel.handleRouteAction(RouteUiAction.ToggleTracking)

        assertEquals(1, startCalls)
        assertEquals(0, stopCalls)
        assertFalse(viewModel.uiState.value.showTrackingPermissionDialog)
    }

    @Test
    fun `toggle tracking stops service when today is already tracking`() = runTest {
        var startCalls = 0
        var stopCalls = 0
        val trackingState = MutableStateFlow(true)
        val viewModel = createViewModel(
            repository = FakeDayRouteRepository(),
            initialDateKey = "2026-03-31",
            todayDateKey = "2026-03-31",
            backgroundGranted = true,
            trackingState = trackingState,
            onStartTracking = { startCalls += 1 },
            onStopTracking = { stopCalls += 1 }
        )
        advanceUntilIdle()

        viewModel.handleRouteAction(RouteUiAction.ToggleTracking)

        assertEquals(0, startCalls)
        assertEquals(1, stopCalls)
        assertFalse(viewModel.uiState.value.showTrackingPermissionDialog)
    }

    @Test
    fun `toggle tracking without always permission keeps off state and opens settings dialog`() = runTest {
        var startCalls = 0
        var stopCalls = 0
        val trackingState = MutableStateFlow(false)
        val viewModel = createViewModel(
            repository = FakeDayRouteRepository(),
            initialDateKey = "2026-03-31",
            todayDateKey = "2026-03-31",
            trackingState = trackingState,
            onStartTracking = { startCalls += 1 },
            onStopTracking = { stopCalls += 1 }
        )
        advanceUntilIdle()

        viewModel.handleRouteAction(RouteUiAction.ToggleTracking)
        advanceUntilIdle()

        assertEquals(0, startCalls)
        assertEquals(0, stopCalls)
        assertFalse((viewModel.uiState.value.routeModeUiState as MainRouteModeUiState.Today).isTrackingEnabled)
        assertTrue(viewModel.uiState.value.showTrackingPermissionDialog)
    }

    @Test
    fun `dismiss tracking permission dialog clears dialog state`() = runTest {
        val viewModel = createViewModel(
            repository = FakeDayRouteRepository(),
            initialDateKey = "2026-03-31",
            todayDateKey = "2026-03-31"
        )
        advanceUntilIdle()

        viewModel.handleRouteAction(RouteUiAction.ToggleTracking)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showTrackingPermissionDialog)

        viewModel.dismissTrackingPermissionDialog()

        assertFalse(viewModel.uiState.value.showTrackingPermissionDialog)
    }

    @Test
    fun `selectDate with empty remote result shows no location data state without error`() = runTest {
        val repository = FakeDayRouteRepository(
            resultByDate = mutableMapOf(
                "2026-03-29" to RemoteDayRouteResult.Empty,
                "2026-03-30" to RemoteDayRouteResult.Empty
            )
        )

        val viewModel = createViewModel(
            repository = repository,
            initialDateKey = "2026-03-29",
            todayDateKey = "2026-03-31"
        )
        advanceUntilIdle()

        viewModel.selectDate("2026-03-30")
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals("2026-03-30", state.selectedDateKey)
        assertTrue(state.isRouteEmpty)
        assertFalse(state.selectedRoute.hasLocationData)
        assertNotNull(state.routeEmptyMessage)
        assertNull(state.routeErrorMessage)
    }

    @Test
    fun `selectDate with error remote result exposes retryable error state`() = runTest {
        val repository = FakeDayRouteRepository(
            resultByDate = mutableMapOf(
                "2026-03-29" to RemoteDayRouteResult.Empty,
                "2026-03-31" to RemoteDayRouteResult.Error(IllegalStateException("boom"))
            )
        )

        val viewModel = createViewModel(
            repository = repository,
            initialDateKey = "2026-03-29",
            todayDateKey = "2026-04-01"
        )
        advanceUntilIdle()

        viewModel.selectDate("2026-03-31")
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals("2026-03-31", state.selectedDateKey)
        assertFalse(state.isRouteEmpty)
        assertNotNull(state.routeErrorMessage)
        assertFalse(state.selectedRoute.hasLocationData)
    }

    @Test
    fun `retry past route action reloads selected date`() = runTest {
        val repository = FakeDayRouteRepository(
            resultByDate = mutableMapOf(
                "2026-03-30" to RemoteDayRouteResult.Error(IllegalStateException("boom"))
            )
        )

        val viewModel = createViewModel(
            repository = repository,
            initialDateKey = "2026-03-30",
            todayDateKey = "2026-03-31"
        )
        advanceUntilIdle()

        viewModel.handleRouteAction(RouteUiAction.RetryPastRoute)
        advanceUntilIdle()

        assertEquals(listOf("2026-03-30", "2026-03-30"), repository.requestedRemoteDates)
    }

    @Test
    fun `toggleSelectedRouteBookmark updates selected route bookmark state`() = runTest {
        val bookmarkRepository = FakeDayRouteBookmarkRepository(
            resultByDate = mutableMapOf("2026-03-29" to true)
        )
        val repository = FakeDayRouteRepository(
            resultByDate = mutableMapOf(
                "2026-03-29" to RemoteDayRouteResult.Success(
                    routeDetail = DayRouteDetail(
                        dateKey = "2026-03-29",
                        totalDistanceKm = 1.2,
                        isBookmarked = false
                    )
                )
            )
        )
        val viewModel = createViewModel(
            repository = repository,
            initialDateKey = "2026-03-29",
            todayDateKey = "2026-03-31",
            bookmarkRepository = bookmarkRepository
        )
        advanceUntilIdle()

        viewModel.toggleSelectedRouteBookmark()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.selectedRoute.isBookmarked)
        assertEquals(listOf("2026-03-29"), bookmarkRepository.toggledDates)
    }

    @Test
    fun `toggleSelectedRouteBookmark failure keeps bookmark state and exposes feedback`() = runTest {
        val bookmarkRepository = FakeDayRouteBookmarkRepository(
            throwOnToggle = IllegalStateException("boom")
        )
        val repository = FakeDayRouteRepository(
            resultByDate = mutableMapOf(
                "2026-03-29" to RemoteDayRouteResult.Success(
                    routeDetail = DayRouteDetail(
                        dateKey = "2026-03-29",
                        totalDistanceKm = 1.2,
                        isBookmarked = false
                    )
                )
            )
        )
        val viewModel = createViewModel(
            repository = repository,
            initialDateKey = "2026-03-29",
            todayDateKey = "2026-03-31",
            bookmarkRepository = bookmarkRepository
        )
        advanceUntilIdle()

        viewModel.toggleSelectedRouteBookmark()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.selectedRoute.isBookmarked)
        assertNull(state.bookmarkToggleUiState.updatingDateKey)
        assertEquals(ApiFailureMessage.NETWORK_REQUEST_FAILED, state.bookmarkToggleUiState.feedbackMessage)
        assertEquals(1L, state.bookmarkToggleUiState.feedbackEventId)
    }

    @Test
    fun `toggleSelectedRouteBookmark ignores duplicate clicks while request is in flight`() = runTest {
        val toggleGate = CompletableDeferred<Unit>()
        val bookmarkRepository = FakeDayRouteBookmarkRepository(
            resultByDate = mutableMapOf("2026-03-29" to true),
            toggleGate = toggleGate
        )
        val repository = FakeDayRouteRepository(
            resultByDate = mutableMapOf(
                "2026-03-29" to RemoteDayRouteResult.Success(
                    routeDetail = DayRouteDetail(
                        dateKey = "2026-03-29",
                        totalDistanceKm = 1.2,
                        isBookmarked = false
                    )
                )
            )
        )
        val viewModel = createViewModel(
            repository = repository,
            initialDateKey = "2026-03-29",
            todayDateKey = "2026-03-31",
            bookmarkRepository = bookmarkRepository
        )
        advanceUntilIdle()

        viewModel.toggleSelectedRouteBookmark()
        viewModel.toggleSelectedRouteBookmark()
        assertTrue(viewModel.uiState.value.bookmarkToggleUiState.isUpdating("2026-03-29"))

        toggleGate.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf("2026-03-29"), bookmarkRepository.toggledDates)
        assertTrue(viewModel.uiState.value.selectedRoute.isBookmarked)
    }

    @Test
    fun `switching from past error date to today clears stale error and starts local observation`() = runTest {
        val localFlow = MutableStateFlow<DailyPath?>(
            DailyPath(
                dateKey = "2026-03-31",
                points = listOf(TrackedLocation(37.1, 127.1, 5f, 1L)),
                totalDistanceMeters = 0.0,
                pathPointCount = 1
            )
        )
        val repository = FakeDayRouteRepository(
            resultByDate = mutableMapOf(
                "2026-03-30" to RemoteDayRouteResult.Error(IllegalStateException("boom"))
            ),
            localRouteByDate = mutableMapOf("2026-03-31" to localFlow)
        )

        val viewModel = createViewModel(
            repository = repository,
            initialDateKey = "2026-03-30",
            todayDateKey = "2026-03-31"
        )
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.routeErrorMessage)

        viewModel.selectDate("2026-03-31")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("2026-03-31", state.selectedDateKey)
        assertNull(state.routeErrorMessage)
        assertFalse(state.isRouteEmpty)
        assertEquals(1, state.selectedRoute.polylinePoints.size)
        assertEquals(listOf("2026-03-31"), repository.observedLocalDates)
    }

    @Test
    fun `updateFetchedMapPlaces overrides route seed markers with fetched places`() = runTest {
        val repository = FakeDayRouteRepository(
            resultByDate = mutableMapOf(
                "2026-03-29" to RemoteDayRouteResult.Success(
                    routeDetail = DayRouteDetail(
                        dateKey = "2026-03-29",
                        totalDistanceKm = 12.3,
                        title = "Han River",
                        memo = "Windy evening walk",
                        pathPointCount = 3,
                        polylinePoints = listOf(
                            RoutePoint(37.1, 127.1),
                            RoutePoint(37.2, 127.2),
                            RoutePoint(37.3, 127.3)
                        ),
                        places = listOf(
                            DayRoutePlace(1L, "Seed Place", "Road", 37.1, 127.1, 1)
                        )
                    )
                )
            )
        )
        val viewModel = createViewModel(
            repository = repository,
            initialDateKey = "2026-03-29",
            todayDateKey = "2026-03-31",
            backgroundGranted = true
        )
        advanceUntilIdle()

        viewModel.updateFetchedMapPlaces(
            dateKey = "2026-03-29",
            places = listOf(
                VisitedPlace(
                    placeId = 4L,
                    placeName = "Fetched B",
                    type = PlaceSourceType.MANUAL,
                    roadAddress = "Address B",
                    latitude = 37.4,
                    longitude = 127.4,
                    orderIndex = 2
                ),
                VisitedPlace(
                    placeId = 3L,
                    placeName = "Fetched A",
                    type = PlaceSourceType.AUTO,
                    roadAddress = "Address A",
                    latitude = 37.3,
                    longitude = 127.3,
                    orderIndex = 1
                )
            )
        )

        val state = viewModel.uiState.value
        assertEquals(2, state.mapPlaces.size)
        assertEquals(listOf(3L, 4L), state.mapPlaces.map { it.placeId })
        assertEquals("Fetched A", state.mapPlaces.first().placeName)
    }

    @Test
    fun `selectDate clears fetched markers until places are fetched again`() = runTest {
        val repository = FakeDayRouteRepository(
            resultByDate = mutableMapOf(
                "2026-03-29" to RemoteDayRouteResult.Success(
                    routeDetail = DayRouteDetail(
                        dateKey = "2026-03-29",
                        totalDistanceKm = 1.0,
                        title = "",
                        memo = "",
                        pathPointCount = 1,
                        polylinePoints = listOf(RoutePoint(37.1, 127.1)),
                        places = listOf(
                            DayRoutePlace(1L, "Seed A", "Road A", 37.1, 127.1, 1)
                        )
                    )
                ),
                "2026-03-30" to RemoteDayRouteResult.Success(
                    routeDetail = DayRouteDetail(
                        dateKey = "2026-03-30",
                        totalDistanceKm = 2.0,
                        title = "",
                        memo = "",
                        pathPointCount = 1,
                        polylinePoints = listOf(RoutePoint(37.2, 127.2)),
                        places = listOf(
                            DayRoutePlace(2L, "Seed B", "Road B", 37.2, 127.2, 1)
                        )
                    )
                )
            )
        )
        val viewModel = createViewModel(
            repository = repository,
            initialDateKey = "2026-03-29",
            todayDateKey = "2026-03-31",
            backgroundGranted = true
        )
        advanceUntilIdle()

        viewModel.updateFetchedMapPlaces(
            dateKey = "2026-03-29",
            places = listOf(
                VisitedPlace(
                    placeId = 9L,
                    placeName = "Fetched Place",
                    type = PlaceSourceType.MANUAL,
                    roadAddress = "Fetched Road",
                    latitude = 37.9,
                    longitude = 127.9,
                    orderIndex = 1
                )
            )
        )
        assertEquals(listOf(9L), viewModel.uiState.value.mapPlaces.map { it.placeId })

        viewModel.selectDate("2026-03-30")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("2026-03-30", state.selectedDateKey)
        assertTrue(state.mapPlaces.isEmpty())
        assertNull(state.fetchedMapPlaces)
    }

    private fun createViewModel(
        repository: FakeDayRouteRepository,
        initialDateKey: String,
        todayDateKey: String,
        backgroundGranted: Boolean = false,
        isLocationServiceEnabled: Boolean = true,
        trackingState: MutableStateFlow<Boolean> = MutableStateFlow(false),
        onStartTracking: () -> Unit = {},
        onStopTracking: () -> Unit = {},
        permissionReader: LocationPermissionStatusReader =
            FakeLocationPermissionStatusReader(backgroundGranted = backgroundGranted),
        locationServiceReader: LocationServiceStatusReader =
            MutableLocationServiceStatusReader(isEnabled = isLocationServiceEnabled),
        bookmarkRepository: DayRouteBookmarkRepository = FakeDayRouteBookmarkRepository()
    ): MainViewModel {
        return MainViewModel(
            locationPermissionStatusReader = permissionReader,
            locationServiceStatusReader = locationServiceReader,
            initialDateKeyProvider = { initialDateKey },
            routeStateCoordinator = RouteStateCoordinator(
                dayRouteRepository = repository,
                todayDateKeyProvider = { todayDateKey }
            ),
            toggleDayRouteBookmarkUseCase = ToggleDayRouteBookmarkUseCase(bookmarkRepository),
            observeRecentTrackingEvents = FakeObserveRecentTrackingEventsUseCase(),
            trackingServiceStateReader = FakeLocationTrackingServiceStateReader(trackingState),
            startTracking = onStartTracking,
            stopTracking = onStopTracking
        )
    }

    private class FakeLocationPermissionStatusReader(
        private val foregroundGranted: Boolean = false,
        private val backgroundGranted: Boolean = false
    ) : LocationPermissionStatusReader {
        override fun isForegroundGranted(): Boolean = foregroundGranted
        override fun isBackgroundAlwaysGranted(): Boolean = backgroundGranted
    }

    private class MutableLocationPermissionStatusReader(
        var foregroundGranted: Boolean = false,
        var backgroundGranted: Boolean = false
    ) : LocationPermissionStatusReader {
        override fun isForegroundGranted(): Boolean = foregroundGranted
        override fun isBackgroundAlwaysGranted(): Boolean = backgroundGranted
    }

    private class MutableLocationServiceStatusReader(
        var isEnabled: Boolean = true
    ) : LocationServiceStatusReader {
        override fun isLocationServiceEnabled(): Boolean = isEnabled
    }

    private class FakeLocationTrackingServiceStateReader(
        override val isTracking: StateFlow<Boolean>
    ) : LocationTrackingServiceStateReader {
        override fun isTrackingEnabledByUser(): Boolean = true
    }

    private class FakeDayRouteRepository(
        private val resultByDate: MutableMap<String, RemoteDayRouteResult> = mutableMapOf(),
        private val localRouteByDate: MutableMap<String, MutableStateFlow<DailyPath?>> = mutableMapOf()
    ) : DayRouteRepository {
        val requestedRemoteDates = mutableListOf<String>()
        val observedLocalDates = mutableListOf<String>()

        override fun observeLocalDayRoute(dateKey: String): Flow<DailyPath?> {
            observedLocalDates += dateKey
            return localRouteByDate.getOrPut(dateKey) { MutableStateFlow(null) }.asStateFlow()
        }

        override suspend fun getLocalDayRoute(dateKey: String): DailyPath? {
            return localRouteByDate[dateKey]?.value
        }

        override suspend fun markLocalDayRouteSynced(dateKey: String, syncedAtEpochMillis: Long) = Unit

        override suspend fun fetchRemoteDayRoute(dateKey: String): RemoteDayRouteResult {
            requestedRemoteDates += dateKey
            return resultByDate[dateKey]
                ?: RemoteDayRouteResult.Empty
        }
    }

    private class FakeObserveRecentTrackingEventsUseCase : ObserveRecentTrackingEventsUseCase(
        trackingDebugLogRepository = FakeTrackingDebugLogRepository()
    ) {
        override fun invoke(limit: Int): Flow<List<String>> = flowOf(emptyList())
    }

    private class FakeDayRouteBookmarkRepository(
        private val resultByDate: MutableMap<String, Boolean> = mutableMapOf(),
        private val throwOnToggle: Throwable? = null,
        private val toggleGate: CompletableDeferred<Unit>? = null
    ) : DayRouteBookmarkRepository {
        val toggledDates = mutableListOf<String>()

        override suspend fun toggleBookmark(dateKey: String): DayRouteBookmark {
            toggleGate?.await()
            toggledDates += dateKey
            throwOnToggle?.let { throw it }
            val nextValue = resultByDate[dateKey] ?: true
            resultByDate[dateKey] = nextValue
            return DayRouteBookmark(isBookmarked = nextValue)
        }
    }

    private class FakeTrackingDebugLogRepository : TrackingDebugLogRepository {
        override suspend fun append(category: String, message: String, dateKey: String?) = Unit

        override fun observeRecent(limit: Int): Flow<List<com.example.passedpath.feature.locationtracking.domain.model.TrackingDebugLog>> {
            return flowOf(emptyList())
        }
    }
}




