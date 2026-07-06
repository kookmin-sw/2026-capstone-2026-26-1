package com.example.passedpath.feature.care.presentation.viewmodel

import com.example.passedpath.feature.care.domain.model.CareDependentLocationStreamEvent
import com.example.passedpath.feature.care.domain.model.CareDependentUser
import com.example.passedpath.feature.care.domain.model.CareDependentUserList
import com.example.passedpath.feature.care.domain.model.CareLatestGpsPoint
import com.example.passedpath.feature.care.domain.model.CareRelationshipInviteLink
import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteDetail
import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteList
import com.example.passedpath.feature.care.domain.model.ProtectedPersonDaySummary
import com.example.passedpath.feature.care.domain.model.ProtectedPersonPlaceSourceType
import com.example.passedpath.feature.care.domain.model.ProtectedPersonRoutePoint
import com.example.passedpath.feature.care.domain.model.ProtectedPersonVisitedPlace
import com.example.passedpath.feature.care.domain.model.ProtectedPersonVisitedPlaceList
import com.example.passedpath.feature.care.domain.repository.CareDependentLocationStreamRepository
import com.example.passedpath.feature.care.domain.repository.CareDependentRepository
import com.example.passedpath.feature.care.domain.repository.CareGuideRepository
import com.example.passedpath.feature.care.domain.repository.CareRelationshipInviteRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteListResult
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteResult
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDaySummaryRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDaySummaryResult
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonVisitedPlaceRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonVisitedPlaceResult
import com.example.passedpath.feature.care.domain.usecase.CreateCareRelationshipInviteLinkUseCase
import com.example.passedpath.feature.care.domain.usecase.GetCareDependentsUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonDayRouteUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonDaySummaryUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonVisitedPlacesUseCase
import com.example.passedpath.feature.care.domain.usecase.ObserveCareDependentLocationStreamUseCase
import com.example.passedpath.feature.care.presentation.model.ProtectedPersonBottomSheetTab
import com.example.passedpath.testutil.MainDispatcherRule
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
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
class CareViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial load stores dependent list on success`() = runTest {
        val repository = FakeCareDependentRepository(
            result = CareDependentUserList(
                dependentUserCount = 2,
                dependentUsers = listOf(
                    dependentUser(dependentUserId = 1L, nickname = "Jiyeon"),
                    dependentUser(dependentUserId = 2L, nickname = "Seulgi")
                )
            )
        )
        val viewModel = createViewModel(dependentRepository = repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, repository.requestCount)
        assertTrue(state.hasLoaded)
        assertFalse(state.isLoading)
        assertEquals(listOf(1L, 2L), state.dependents.map { it.dependentUserId })
        assertNull(state.errorMessage)
    }

    @Test
    fun `initial load exposes empty loaded state`() = runTest {
        val repository = FakeCareDependentRepository(
            result = CareDependentUserList(
                dependentUserCount = 0,
                dependentUsers = emptyList()
            )
        )
        val viewModel = createViewModel(dependentRepository = repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasLoaded)
        assertFalse(state.isLoading)
        assertTrue(state.dependents.isEmpty())
        assertNull(state.errorMessage)
    }

    @Test
    fun `initial load exposes error state on failure`() = runTest {
        val viewModel = createViewModel(
            dependentRepository = FakeCareDependentRepository(
                throwable = IllegalStateException("network failed")
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasLoaded)
        assertFalse(state.isLoading)
        assertTrue(state.dependents.isEmpty())
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `selectDependent changes selected dependent and null returns to all`() = runTest {
        val viewModel = createViewModel(
            dependentRepository = FakeCareDependentRepository(
                result = CareDependentUserList(
                    dependentUserCount = 1,
                    dependentUsers = listOf(dependentUser(dependentUserId = 7L))
                )
            )
        )
        advanceUntilIdle()

        viewModel.selectDependent(7L)
        assertEquals(7L, viewModel.uiState.value.selectedDependentUserId)

        viewModel.selectDependent(null)
        val state = viewModel.uiState.value
        assertNull(state.selectedDependentUserId)
        assertEquals(BaseBottomSheetValue.HIDDEN, state.requestedSheetValue)
        assertFalse(state.hasRouteLoaded)
        assertTrue(state.route.mapPolylinePoints.isEmpty())
        assertTrue(state.placeListUiState.places.isEmpty())
    }

    @Test
    fun `mapMarkers excludes dependents without latest location`() = runTest {
        val viewModel = createViewModel(
            dependentRepository = FakeCareDependentRepository(
                result = CareDependentUserList(
                    dependentUserCount = 2,
                    dependentUsers = listOf(
                        dependentUser(dependentUserId = 1L, latestGpsPoint = latestGpsPoint()),
                        dependentUser(dependentUserId = 2L, latestGpsPoint = null)
                    )
                )
            )
        )
        advanceUntilIdle()

        assertEquals(
            listOf(1L),
            viewModel.uiState.value.mapMarkers.map { it.dependentUserId }
        )
        assertEquals(
            listOf(1L, 2L),
            viewModel.uiState.value.dependents.map { it.dependentUserId }
        )
    }

    @Test
    fun `selectDependent loads today route places and summary`() = runTest {
        val routeRepository = FakeProtectedPersonDayRouteRepository(
            result = ProtectedPersonDayRouteResult.Success(routeDetail())
        )
        val placeRepository = FakeProtectedPersonVisitedPlaceRepository(
            result = ProtectedPersonVisitedPlaceResult.Success(
                ProtectedPersonVisitedPlaceList(
                    placeCount = 2,
                    places = listOf(
                        visitedPlace(placeId = 2L, orderIndex = 2),
                        visitedPlace(placeId = 1L, orderIndex = 1)
                    )
                )
            )
        )
        val summaryRepository = FakeProtectedPersonDaySummaryRepository(
            result = ProtectedPersonDaySummaryResult.Success(daySummary())
        )
        val viewModel = createLoadedViewModel(
            routeRepository = routeRepository,
            placeRepository = placeRepository,
            summaryRepository = summaryRepository
        )

        viewModel.selectDependent(7L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(7L, state.selectedDependentUserId)
        assertEquals("2026-05-18", state.selectedDateKey)
        assertEquals(BaseBottomSheetValue.MIDDLE, state.requestedSheetValue)
        assertTrue(state.hasRouteLoaded)
        assertFalse(state.isRouteEmpty)
        assertEquals(2, state.route.mapPolylinePoints.size)
        assertEquals(listOf(1L, 2L), state.placeListUiState.places.map { it.placeId })
        assertEquals(listOf(1, 2), state.visitedPlaceMarkers.map { it.displayOrderIndex })
        assertEquals("09:12", state.summaryUiState.summary.outingTimeText)
        assertEquals(listOf(7L to "2026-05-18"), routeRepository.detailRequests)
        assertEquals(listOf(7L to "2026-05-18"), placeRepository.requests)
        assertEquals(listOf(7L to "2026-05-18"), summaryRepository.requests)
    }

    @Test
    fun `selectDependent handles route empty without clearing places or summary`() = runTest {
        val viewModel = createLoadedViewModel(
            routeRepository = FakeProtectedPersonDayRouteRepository(
                result = ProtectedPersonDayRouteResult.Empty
            )
        )

        viewModel.selectDependent(7L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasRouteLoaded)
        assertTrue(state.isRouteEmpty)
        assertNull(state.routeErrorMessage)
        assertTrue(state.placeListUiState.hasLoaded)
        assertTrue(state.summaryUiState.hasLoaded)
    }

    @Test
    fun `route error preserves selected details and exposes route error only`() = runTest {
        val viewModel = createLoadedViewModel(
            routeRepository = FakeProtectedPersonDayRouteRepository(
                result = ProtectedPersonDayRouteResult.Error(
                    IllegalStateException("route failed")
                )
            )
        )

        viewModel.selectDependent(7L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasRouteLoaded)
        assertNotNull(state.routeErrorMessage)
        assertTrue(state.placeListUiState.hasLoaded)
        assertTrue(state.summaryUiState.hasLoaded)
    }

    @Test
    fun `selectDependent shows place guide banner while places are loading`() = runTest {
        val viewModel = createLoadedViewModel(
            placeRepository = FakeProtectedPersonVisitedPlaceRepository(delayMillis = 1_000L)
        )

        viewModel.selectDependent(7L)

        val state = viewModel.uiState.value
        assertTrue(state.placeListUiState.isLoading)
        assertTrue(state.placeListUiState.isPlaceGuideBannerVisible)

        advanceTimeBy(1_000L)
        advanceUntilIdle()
    }

    @Test
    fun `dismissProtectedPersonPlaceGuideBanner hides banner and persists dismissal`() = runTest {
        val guideRepository = FakeCareGuideRepository()
        val viewModel = createLoadedViewModel(careGuideRepository = guideRepository)

        viewModel.selectDependent(7L)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.placeListUiState.isPlaceGuideBannerVisible)

        viewModel.dismissProtectedPersonPlaceGuideBanner()
        advanceUntilIdle()

        assertTrue(guideRepository.dismissRequested)
        assertFalse(viewModel.uiState.value.placeListUiState.isPlaceGuideBannerVisible)

        viewModel.retryProtectedPersonPlaces()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.placeListUiState.isPlaceGuideBannerVisible)
    }

    @Test
    fun `place guide banner stays hidden when repository has dismissed state`() = runTest {
        val viewModel = createLoadedViewModel(
            careGuideRepository = FakeCareGuideRepository(initialDismissed = true)
        )

        viewModel.selectDependent(7L)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.placeListUiState.isPlaceGuideBannerVisible)
    }

    @Test
    fun `selectDependent handles day route empty details as loaded empty state`() = runTest {
        val viewModel = createLoadedViewModel(
            placeRepository = FakeProtectedPersonVisitedPlaceRepository(
                result = ProtectedPersonVisitedPlaceResult.Empty
            ),
            summaryRepository = FakeProtectedPersonDaySummaryRepository(
                result = ProtectedPersonDaySummaryResult.Empty
            )
        )

        viewModel.selectDependent(7L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.placeListUiState.hasLoaded)
        assertTrue(state.placeListUiState.places.isEmpty())
        assertNull(state.placeListUiState.errorMessage)
        assertTrue(state.summaryUiState.hasLoaded)
        assertNull(state.summaryUiState.errorMessage)
    }

    @Test
    fun `place marker and card reducers mirror main interaction direction`() = runTest {
        val viewModel = createLoadedViewModel()
        viewModel.selectDependent(7L)
        advanceUntilIdle()

        viewModel.onPlaceMarkerClick(3L)
        var state = viewModel.uiState.value
        assertEquals(ProtectedPersonBottomSheetTab.PLACE, state.selectedBottomSheetTab)
        assertEquals(3L, state.selectedPlaceId)
        assertEquals(BaseBottomSheetValue.EXPANDED, state.requestedSheetValue)

        viewModel.onPlaceCardClick(3L)
        state = viewModel.uiState.value
        assertNull(state.selectedPlaceId)
        assertEquals(3L, state.focusedPlaceId)
        assertEquals(BaseBottomSheetValue.HIDDEN, state.requestedSheetValue)
    }

    @Test
    fun `openInviteModal opens modal and stores created link`() = runTest {
        val inviteRepository = FakeCareRelationshipInviteRepository(
            result = CareRelationshipInviteLink("https://passedpath.site/invite?inviteCode=abc")
        )
        val viewModel = createViewModel(
            dependentRepository = FakeCareDependentRepository(),
            inviteRepository = inviteRepository
        )
        advanceUntilIdle()

        viewModel.openInviteModal()
        advanceUntilIdle()

        val state = viewModel.uiState.value.inviteUiState
        assertEquals(1, inviteRepository.createRequestCount)
        assertTrue(state.isVisible)
        assertFalse(state.isLoading)
        assertEquals("https://passedpath.site/invite?inviteCode=abc", state.inviteLink)
        assertNull(state.errorMessage)
    }

    @Test
    fun `openInviteModal exposes retryable error on failure`() = runTest {
        val inviteRepository = FakeCareRelationshipInviteRepository(
            throwable = IllegalStateException("network failed")
        )
        val viewModel = createViewModel(
            dependentRepository = FakeCareDependentRepository(),
            inviteRepository = inviteRepository
        )
        advanceUntilIdle()

        viewModel.openInviteModal()
        advanceUntilIdle()

        val state = viewModel.uiState.value.inviteUiState
        assertTrue(state.isVisible)
        assertFalse(state.isLoading)
        assertNull(state.inviteLink)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `retryCreateInviteLink requests link again in same modal`() = runTest {
        val inviteRepository = FakeCareRelationshipInviteRepository(
            throwable = IllegalStateException("network failed")
        )
        val viewModel = createViewModel(
            dependentRepository = FakeCareDependentRepository(),
            inviteRepository = inviteRepository
        )
        advanceUntilIdle()

        viewModel.openInviteModal()
        advanceUntilIdle()
        inviteRepository.throwable = null
        inviteRepository.result = CareRelationshipInviteLink("https://passedpath.site/retry")

        viewModel.retryCreateInviteLink()
        advanceUntilIdle()

        val state = viewModel.uiState.value.inviteUiState
        assertEquals(2, inviteRepository.createRequestCount)
        assertEquals("https://passedpath.site/retry", state.inviteLink)
        assertNull(state.errorMessage)
    }

    @Test
    fun `dismissInviteModal resets invite state`() = runTest {
        val viewModel = createViewModel(
            dependentRepository = FakeCareDependentRepository(),
            inviteRepository = FakeCareRelationshipInviteRepository()
        )
        advanceUntilIdle()

        viewModel.openInviteModal()
        advanceUntilIdle()
        viewModel.onInviteLinkCopied()
        viewModel.dismissInviteModal()

        val state = viewModel.uiState.value.inviteUiState
        assertFalse(state.isVisible)
        assertFalse(state.isLoading)
        assertNull(state.inviteLink)
        assertNull(state.errorMessage)
        assertEquals(0L, state.copyFeedbackEventId)
    }

    @Test
    fun `invite state changes do not clear selected dependent details`() = runTest {
        val viewModel = createLoadedViewModel(
            inviteRepository = FakeCareRelationshipInviteRepository()
        )
        viewModel.selectDependent(7L)
        advanceUntilIdle()

        viewModel.openInviteModal()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(7L, state.selectedDependentUserId)
        assertTrue(state.placeListUiState.hasLoaded)
        assertTrue(state.summaryUiState.hasLoaded)
        assertTrue(state.inviteUiState.isVisible)
    }

    @Test
    fun `location stream updates existing dependent and map marker`() = runTest {
        val streamRepository = FakeCareDependentLocationStreamRepository()
        val viewModel = createLoadedViewModel(
            locationStreamRepository = streamRepository
        )

        viewModel.startLocationStream()
        advanceUntilIdle()
        streamRepository.emitEvent(
            CareDependentLocationStreamEvent.LocationUpdated(
                dependentUserId = 7L,
                latestGpsPoint = latestGpsPoint(
                    latitude = 37.7,
                    longitude = 127.1,
                    recordedAt = "2026-05-19T10:00:00+09:00"
                )
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(37.7, state.dependents.first().latestLatitude ?: 0.0, CoordinateDelta)
        assertEquals(127.1, state.dependents.first().latestLongitude ?: 0.0, CoordinateDelta)
        assertEquals("2026-05-19T10:00:00+09:00", state.dependents.first().latestRecordedAt)
        assertEquals(37.7, state.mapMarkers.first().latitude, CoordinateDelta)
        assertEquals(127.1, state.mapMarkers.first().longitude, CoordinateDelta)
        assertNull(state.locationStreamErrorMessage)
        viewModel.stopLocationStream()
    }

    @Test
    fun `location stream adds marker for dependent without latest location`() = runTest {
        val streamRepository = FakeCareDependentLocationStreamRepository()
        val viewModel = createViewModel(
            dependentRepository = FakeCareDependentRepository(
                result = CareDependentUserList(
                    dependentUserCount = 1,
                    dependentUsers = listOf(
                        dependentUser(dependentUserId = 9L, latestGpsPoint = null)
                    )
                )
            ),
            locationStreamRepository = streamRepository
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.mapMarkers.isEmpty())

        viewModel.startLocationStream()
        advanceUntilIdle()
        streamRepository.emitEvent(
            CareDependentLocationStreamEvent.LocationUpdated(
                dependentUserId = 9L,
                latestGpsPoint = latestGpsPoint(latitude = 37.9, longitude = 127.3)
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(9L), state.mapMarkers.map { it.dependentUserId })
        assertEquals(37.9, state.mapMarkers.first().latitude, CoordinateDelta)
        assertEquals(127.3, state.mapMarkers.first().longitude, CoordinateDelta)
        viewModel.stopLocationStream()
    }

    @Test
    fun `location stream ignores unknown dependent`() = runTest {
        val streamRepository = FakeCareDependentLocationStreamRepository()
        val viewModel = createLoadedViewModel(
            locationStreamRepository = streamRepository
        )

        viewModel.startLocationStream()
        advanceUntilIdle()
        streamRepository.emitEvent(
            CareDependentLocationStreamEvent.LocationUpdated(
                dependentUserId = 999L,
                latestGpsPoint = latestGpsPoint(latitude = 38.0, longitude = 128.0)
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(7L), state.dependents.map { it.dependentUserId })
        assertEquals(37.5665, state.mapMarkers.first().latitude, CoordinateDelta)
        assertEquals(126.978, state.mapMarkers.first().longitude, CoordinateDelta)
        viewModel.stopLocationStream()
    }

    @Test
    fun `location stream error keeps existing markers and exposes stream error`() = runTest {
        val streamRepository = FakeCareDependentLocationStreamRepository()
        val viewModel = createLoadedViewModel(
            locationStreamRepository = streamRepository
        )

        viewModel.startLocationStream()
        advanceUntilIdle()
        streamRepository.emitEvent(
            CareDependentLocationStreamEvent.Error(IllegalStateException("stream failed"))
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(7L), state.mapMarkers.map { it.dependentUserId })
        assertNotNull(state.locationStreamErrorMessage)
        viewModel.stopLocationStream()
    }

    @Test
    fun `stopLocationStream prevents later events from changing state`() = runTest {
        val streamRepository = FakeCareDependentLocationStreamRepository()
        val viewModel = createLoadedViewModel(
            locationStreamRepository = streamRepository
        )

        viewModel.startLocationStream()
        advanceUntilIdle()
        viewModel.stopLocationStream()
        advanceUntilIdle()
        streamRepository.emitEvent(
            CareDependentLocationStreamEvent.LocationUpdated(
                dependentUserId = 7L,
                latestGpsPoint = latestGpsPoint(latitude = 37.8, longitude = 127.2)
            )
        )
        advanceUntilIdle()

        assertEquals(
            37.5665,
            viewModel.uiState.value.mapMarkers.first().latitude,
            CoordinateDelta
        )
        assertEquals(0, streamRepository.activeCollectors)
    }

    @Test
    fun `startLocationStream does not create duplicate collectors`() = runTest {
        val streamRepository = FakeCareDependentLocationStreamRepository()
        val viewModel = createLoadedViewModel(
            locationStreamRepository = streamRepository
        )

        viewModel.startLocationStream()
        viewModel.startLocationStream()
        advanceUntilIdle()

        assertEquals(1, streamRepository.observeRequestCount)
        assertEquals(1, streamRepository.activeCollectors)
        viewModel.stopLocationStream()
    }

    private fun TestScope.createLoadedViewModel(
        routeRepository: ProtectedPersonDayRouteRepository =
            FakeProtectedPersonDayRouteRepository(),
        placeRepository: ProtectedPersonVisitedPlaceRepository =
            FakeProtectedPersonVisitedPlaceRepository(),
        summaryRepository: ProtectedPersonDaySummaryRepository =
            FakeProtectedPersonDaySummaryRepository(),
        inviteRepository: CareRelationshipInviteRepository =
            FakeCareRelationshipInviteRepository(),
        locationStreamRepository: CareDependentLocationStreamRepository =
            FakeCareDependentLocationStreamRepository(),
        careGuideRepository: CareGuideRepository = FakeCareGuideRepository()
    ): CareViewModel {
        val viewModel = createViewModel(
            dependentRepository = FakeCareDependentRepository(
                result = CareDependentUserList(
                    dependentUserCount = 1,
                    dependentUsers = listOf(dependentUser(dependentUserId = 7L))
                )
            ),
            routeRepository = routeRepository,
            placeRepository = placeRepository,
            summaryRepository = summaryRepository,
            inviteRepository = inviteRepository,
            locationStreamRepository = locationStreamRepository,
            careGuideRepository = careGuideRepository
        )
        advanceUntilIdle()
        return viewModel
    }

    private fun createViewModel(
        dependentRepository: CareDependentRepository,
        routeRepository: ProtectedPersonDayRouteRepository =
            FakeProtectedPersonDayRouteRepository(),
        placeRepository: ProtectedPersonVisitedPlaceRepository =
            FakeProtectedPersonVisitedPlaceRepository(),
        summaryRepository: ProtectedPersonDaySummaryRepository =
            FakeProtectedPersonDaySummaryRepository(),
        inviteRepository: CareRelationshipInviteRepository =
            FakeCareRelationshipInviteRepository(),
        locationStreamRepository: CareDependentLocationStreamRepository =
            FakeCareDependentLocationStreamRepository(),
        careGuideRepository: CareGuideRepository = FakeCareGuideRepository()
    ): CareViewModel {
        return CareViewModel(
            getCareDependentsUseCase = GetCareDependentsUseCase(dependentRepository),
            getProtectedPersonDayRouteUseCase =
                GetProtectedPersonDayRouteUseCase(routeRepository),
            getProtectedPersonVisitedPlacesUseCase =
                GetProtectedPersonVisitedPlacesUseCase(placeRepository),
            getProtectedPersonDaySummaryUseCase =
                GetProtectedPersonDaySummaryUseCase(summaryRepository),
            createCareRelationshipInviteLinkUseCase =
                CreateCareRelationshipInviteLinkUseCase(inviteRepository),
            observeCareDependentLocationStreamUseCase =
                ObserveCareDependentLocationStreamUseCase(locationStreamRepository),
            careGuideRepository = careGuideRepository,
            todayDateKeyProvider = { "2026-05-18" }
        )
    }

    private class FakeCareGuideRepository(
        initialDismissed: Boolean = false
    ) : CareGuideRepository {
        private val dismissed = MutableStateFlow(initialDismissed)
        var dismissRequested: Boolean = false

        override val isProtectedPersonPlaceGuideBannerDismissed: Flow<Boolean> = dismissed

        override suspend fun dismissProtectedPersonPlaceGuideBanner() {
            dismissRequested = true
            dismissed.value = true
        }
    }

    private class FakeCareDependentRepository(
        private val result: CareDependentUserList = CareDependentUserList(
            dependentUserCount = 0,
            dependentUsers = emptyList()
        ),
        private val throwable: Throwable? = null
    ) : CareDependentRepository {
        var requestCount: Int = 0

        override suspend fun getDependents(): CareDependentUserList {
            requestCount++
            throwable?.let { throw it }
            return result
        }
    }

    private class FakeProtectedPersonDayRouteRepository(
        private val result: ProtectedPersonDayRouteResult =
            ProtectedPersonDayRouteResult.Success(routeDetail())
    ) : ProtectedPersonDayRouteRepository {
        val detailRequests = mutableListOf<Pair<Long, String>>()

        override suspend fun fetchDayRoutes(
            dependentUserId: Long,
            cursorDate: String?,
            size: Int?
        ): ProtectedPersonDayRouteListResult {
            return ProtectedPersonDayRouteListResult.Success(
                ProtectedPersonDayRouteList(
                    dayRouteCount = 0,
                    hasNext = false,
                    nextCursorDate = null,
                    dayRoutes = emptyList()
                )
            )
        }

        override suspend fun fetchDayRoute(
            dependentUserId: Long,
            dateKey: String
        ): ProtectedPersonDayRouteResult {
            detailRequests += dependentUserId to dateKey
            return result
        }
    }

    private class FakeProtectedPersonVisitedPlaceRepository(
        private val result: ProtectedPersonVisitedPlaceResult =
            ProtectedPersonVisitedPlaceResult.Success(
                ProtectedPersonVisitedPlaceList(
                    placeCount = 1,
                    places = listOf(visitedPlace(placeId = 3L, orderIndex = 1))
                )
            ),
        private val delayMillis: Long = 0L
    ) : ProtectedPersonVisitedPlaceRepository {
        val requests = mutableListOf<Pair<Long, String>>()

        override suspend fun fetchVisitedPlaces(
            dependentUserId: Long,
            dateKey: String
        ): ProtectedPersonVisitedPlaceResult {
            requests += dependentUserId to dateKey
            if (delayMillis > 0L) {
                delay(delayMillis)
            }
            return result
        }
    }

    private class FakeProtectedPersonDaySummaryRepository(
        private val result: ProtectedPersonDaySummaryResult =
            ProtectedPersonDaySummaryResult.Success(daySummary())
    ) : ProtectedPersonDaySummaryRepository {
        val requests = mutableListOf<Pair<Long, String>>()

        override suspend fun fetchDaySummary(
            dependentUserId: Long,
            dateKey: String
        ): ProtectedPersonDaySummaryResult {
            requests += dependentUserId to dateKey
            return result
        }
    }

    private class FakeCareRelationshipInviteRepository(
        var result: CareRelationshipInviteLink = CareRelationshipInviteLink(
            inviteLink = "https://passedpath.site/care-relationship/invite?inviteCode=T5rfCFFy9j"
        ),
        var throwable: Throwable? = null
    ) : CareRelationshipInviteRepository {
        var createRequestCount: Int = 0

        override suspend fun createInviteLink(): CareRelationshipInviteLink {
            createRequestCount++
            throwable?.let { throw it }
            return result
        }

        override suspend fun acceptInvite(inviteCode: String) = Unit
    }

    private class FakeCareDependentLocationStreamRepository :
        CareDependentLocationStreamRepository {
        private val events = MutableSharedFlow<CareDependentLocationStreamEvent>(
            extraBufferCapacity = 16
        )
        var observeRequestCount: Int = 0
        var activeCollectors: Int = 0

        override fun observeLocationStream(): Flow<CareDependentLocationStreamEvent> {
            return events
                .onStart {
                    observeRequestCount++
                    activeCollectors++
                }
                .onCompletion {
                    activeCollectors--
                }
        }

        fun emitEvent(event: CareDependentLocationStreamEvent) {
            events.tryEmit(event)
        }
    }
}

private fun dependentUser(
    dependentUserId: Long,
    nickname: String = "Jiyeon",
    latestGpsPoint: CareLatestGpsPoint? = latestGpsPoint()
): CareDependentUser {
    return CareDependentUser(
        dependentUserId = dependentUserId,
        nickname = nickname,
        profileImageUrl = "https://example.com/profile.png",
        latestGpsPoint = latestGpsPoint
    )
}

private fun latestGpsPoint(
    latitude: Double = 37.5665,
    longitude: Double = 126.978,
    recordedAt: String = "2026-05-11T09:00:00+09:00"
): CareLatestGpsPoint {
    return CareLatestGpsPoint(
        latitude = latitude,
        longitude = longitude,
        recordedAt = recordedAt,
        recordedAtEpochMillis = 1_746_925_200_000L
    )
}

private fun routeDetail(): ProtectedPersonDayRouteDetail {
    return ProtectedPersonDayRouteDetail(
        dateKey = "2026-05-18",
        totalDistanceKm = 3.4,
        pathPointCount = 2,
        routePoints = listOf(
            routePoint(latitude = 37.5665, longitude = 126.978),
            routePoint(latitude = 37.5765, longitude = 126.988)
        )
    )
}

private fun routePoint(
    latitude: Double,
    longitude: Double
): ProtectedPersonRoutePoint {
    return ProtectedPersonRoutePoint(
        recordedAt = "2026-05-18T09:00:00+09:00",
        recordedAtEpochMillis = null,
        latitude = latitude,
        longitude = longitude
    )
}

private fun visitedPlace(
    placeId: Long,
    orderIndex: Int
): ProtectedPersonVisitedPlace {
    return ProtectedPersonVisitedPlace(
        placeId = placeId,
        placeName = "Place $placeId",
        source = ProtectedPersonPlaceSourceType.AUTO,
        roadAddress = "Seoul",
        latitude = 37.5665 + orderIndex,
        longitude = 126.978 + orderIndex,
        orderIndex = orderIndex,
        startTime = "2026-05-18T09:12:00+09:00",
        endTime = "2026-05-18T10:05:00+09:00"
    )
}

private fun daySummary(): ProtectedPersonDaySummary {
    return ProtectedPersonDaySummary(
        dateKey = "2026-05-18",
        outingTime = "2026-05-18T09:12:00+09:00",
        enterHomeTime = "2026-05-18T21:03:00+09:00",
        totalOutingCount = 3,
        totalOutingSeconds = 42_660,
        totalOutingDurationText = "11시간 51분",
        visitedDongNames = listOf("혜화동", "정릉동")
    )
}

private const val CoordinateDelta = 0.000001
