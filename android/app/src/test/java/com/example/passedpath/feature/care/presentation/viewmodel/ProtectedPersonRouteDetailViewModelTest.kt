package com.example.passedpath.feature.care.presentation.viewmodel

import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteDetail
import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteList
import com.example.passedpath.feature.care.domain.model.ProtectedPersonRoutePoint
import com.example.passedpath.feature.care.domain.model.ProtectedPersonDaySummary
import com.example.passedpath.feature.care.domain.model.ProtectedPersonPlaceSourceType
import com.example.passedpath.feature.care.domain.model.ProtectedPersonVisitedPlace
import com.example.passedpath.feature.care.domain.model.ProtectedPersonVisitedPlaceList
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteListResult
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteResult
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDaySummaryRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDaySummaryResult
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonVisitedPlaceRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonVisitedPlaceResult
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonDayRouteUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonDaySummaryUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonVisitedPlacesUseCase
import com.example.passedpath.feature.care.presentation.model.ProtectedPersonBottomSheetTab
import com.example.passedpath.testutil.MainDispatcherRule
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ProtectedPersonRouteDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `load success fills route places and summary state`() = runTest {
        val routeRepository = FakeProtectedPersonDayRouteRepository(
            routeResults = mutableListOf(
                ProtectedPersonDayRouteResult.Success(routeDetail())
            )
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
        val viewModel = createViewModel(
            routeRepository = routeRepository,
            placeRepository = placeRepository,
            summaryRepository = summaryRepository
        )

        viewModel.load()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("\uB538\uD61C\uC6D0", state.dependentNickname)
        assertEquals("2026-01-20", state.dateKey)
        assertFalse(state.isRouteLoading)
        assertFalse(state.isRouteEmpty)
        assertNull(state.routeErrorMessage)
        assertEquals("3.4km", state.route.totalDistanceText)
        assertEquals(2, state.route.mapPolylinePoints.size)
        assertEquals(listOf(1L, 2L), state.placeListUiState.places.map { it.placeId })
        assertEquals(listOf(1, 2), state.markerPlaces.map { it.orderIndex })
        assertEquals("09:12", state.summaryUiState.summary.outingTimeText)
        assertEquals(listOf(7L to "2026-01-20"), routeRepository.detailRequests)
        assertEquals(listOf(7L to "2026-01-20"), placeRepository.requests)
        assertEquals(listOf(7L to "2026-01-20"), summaryRepository.requests)
    }

    @Test
    fun `route empty keeps sheet requests independent`() = runTest {
        val viewModel = createViewModel(
            routeRepository = FakeProtectedPersonDayRouteRepository(
                routeResults = mutableListOf(ProtectedPersonDayRouteResult.Empty)
            )
        )

        viewModel.load()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isRouteEmpty)
        assertFalse(state.isRouteLoading)
        assertNull(state.routeErrorMessage)
        assertTrue(state.route.mapPolylinePoints.isEmpty())
        assertTrue(state.placeListUiState.hasLoaded)
        assertTrue(state.summaryUiState.hasLoaded)
    }

    @Test
    fun `route error can retry without clearing loaded sheet state`() = runTest {
        val routeRepository = FakeProtectedPersonDayRouteRepository(
            routeResults = mutableListOf(
                ProtectedPersonDayRouteResult.Error(IllegalStateException("route failed")),
                ProtectedPersonDayRouteResult.Success(routeDetail())
            )
        )
        val viewModel = createViewModel(routeRepository = routeRepository)

        viewModel.load()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.routeErrorMessage)
        assertTrue(viewModel.uiState.value.placeListUiState.hasLoaded)

        viewModel.retryRoute()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.routeErrorMessage)
        assertEquals("3.4km", state.route.totalDistanceText)
        assertTrue(state.placeListUiState.hasLoaded)
        assertEquals(2, routeRepository.detailRequests.size)
    }

    @Test
    fun `place and summary errors do not fail route map`() = runTest {
        val viewModel = createViewModel(
            routeRepository = FakeProtectedPersonDayRouteRepository(
                routeResults = mutableListOf(
                    ProtectedPersonDayRouteResult.Success(routeDetail())
                )
            ),
            placeRepository = FakeProtectedPersonVisitedPlaceRepository(
                result = ProtectedPersonVisitedPlaceResult.Error(
                    IllegalStateException("place failed")
                )
            ),
            summaryRepository = FakeProtectedPersonDaySummaryRepository(
                result = ProtectedPersonDaySummaryResult.Error(
                    IllegalStateException("summary failed")
                )
            )
        )

        viewModel.load()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.routeErrorMessage)
        assertNotNull(state.placeListUiState.errorMessage)
        assertNotNull(state.summaryUiState.errorMessage)
    }

    @Test
    fun `marker and card interactions update sheet and map focus state`() = runTest {
        val viewModel = createViewModel()
        viewModel.load()
        advanceUntilIdle()

        viewModel.onPlaceMarkerClick(1L)
        var state = viewModel.uiState.value
        assertEquals(ProtectedPersonBottomSheetTab.PLACE, state.selectedBottomSheetTab)
        assertEquals(1L, state.selectedPlaceId)
        assertEquals(BaseBottomSheetValue.EXPANDED, state.requestedSheetValue)

        viewModel.onPlaceCardClick(1L)
        state = viewModel.uiState.value
        assertNull(state.selectedPlaceId)
        assertEquals(1L, state.focusedPlaceId)
        assertEquals(BaseBottomSheetValue.HIDDEN, state.requestedSheetValue)

        viewModel.onFocusedPlaceHandled()
        assertNull(viewModel.uiState.value.focusedPlaceId)
    }

    private fun createViewModel(
        routeRepository: ProtectedPersonDayRouteRepository =
            FakeProtectedPersonDayRouteRepository(),
        placeRepository: ProtectedPersonVisitedPlaceRepository =
            FakeProtectedPersonVisitedPlaceRepository(),
        summaryRepository: ProtectedPersonDaySummaryRepository =
            FakeProtectedPersonDaySummaryRepository()
    ): ProtectedPersonRouteDetailViewModel {
        return ProtectedPersonRouteDetailViewModel(
            dependentUserId = 7L,
            dependentNickname = "\uB538\uD61C\uC6D0",
            dateKey = "2026-01-20",
            getProtectedPersonDayRouteUseCase =
                GetProtectedPersonDayRouteUseCase(routeRepository),
            getProtectedPersonVisitedPlacesUseCase =
                GetProtectedPersonVisitedPlacesUseCase(placeRepository),
            getProtectedPersonDaySummaryUseCase =
                GetProtectedPersonDaySummaryUseCase(summaryRepository)
        )
    }

    private class FakeProtectedPersonDayRouteRepository(
        private val routeResults: MutableList<ProtectedPersonDayRouteResult> = mutableListOf(
            ProtectedPersonDayRouteResult.Success(routeDetail())
        )
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
            return routeResults.removeAt(0)
        }
    }

    private class FakeProtectedPersonVisitedPlaceRepository(
        private val result: ProtectedPersonVisitedPlaceResult =
            ProtectedPersonVisitedPlaceResult.Success(
                ProtectedPersonVisitedPlaceList(
                    placeCount = 1,
                    places = listOf(visitedPlace(placeId = 1L, orderIndex = 1))
                )
            )
    ) : ProtectedPersonVisitedPlaceRepository {
        val requests = mutableListOf<Pair<Long, String>>()

        override suspend fun fetchVisitedPlaces(
            dependentUserId: Long,
            dateKey: String
        ): ProtectedPersonVisitedPlaceResult {
            requests += dependentUserId to dateKey
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
}

private fun routeDetail(): ProtectedPersonDayRouteDetail {
    return ProtectedPersonDayRouteDetail(
        dateKey = "2026-01-20",
        totalDistanceKm = 3.42,
        pathPointCount = 2,
        routePoints = listOf(
            routePoint(latitude = 37.1, longitude = 127.1),
            routePoint(latitude = 37.2, longitude = 127.2)
        )
    )
}

private fun routePoint(
    latitude: Double,
    longitude: Double
): ProtectedPersonRoutePoint {
    return ProtectedPersonRoutePoint(
        recordedAt = "2026-01-20T09:00:00+09:00",
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
        startTime = "2026-01-20T09:12:00+09:00",
        endTime = "2026-01-20T10:05:00+09:00"
    )
}

private fun daySummary(): ProtectedPersonDaySummary {
    return ProtectedPersonDaySummary(
        dateKey = "2026-01-20",
        outingTime = "2026-01-20T09:12:00+09:00",
        enterHomeTime = "2026-01-20T21:03:00+09:00",
        totalOutingCount = 3,
        totalOutingSeconds = 42_660,
        totalOutingDurationText = "11\uC2DC\uAC04 51\uBD84",
        visitedDongNames = listOf("\uC815\uB989\uB3D9")
    )
}
