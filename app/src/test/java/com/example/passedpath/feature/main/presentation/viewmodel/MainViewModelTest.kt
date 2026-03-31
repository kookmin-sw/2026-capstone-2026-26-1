package com.example.passedpath.feature.main.presentation.viewmodel

import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.DayRouteDetail
import com.example.passedpath.feature.locationtracking.domain.model.DayRoutePlace
import com.example.passedpath.feature.locationtracking.domain.model.RoutePoint
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.RemoteDayRouteResult
import com.example.passedpath.feature.permission.data.manager.LocationPermissionStatusReader
import com.example.passedpath.feature.route.presentation.coordinator.RouteStateCoordinator
import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import com.example.passedpath.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals(3, state.selectedRoute.polylinePoints.size)
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
        assertTrue(repository.requestedRemoteDates.isEmpty())
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
        assertEquals("선택한 날짜에는 지도에 표시할 경로 데이터가 없습니다.", state.routeEmptyMessage)
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
        assertEquals("선택한 날짜의 경로를 불러오지 못했습니다.", state.routeErrorMessage)
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
        assertEquals("선택한 날짜의 경로를 불러오지 못했습니다.", viewModel.uiState.value.routeErrorMessage)

        viewModel.selectDate("2026-03-31")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("2026-03-31", state.selectedDateKey)
        assertNull(state.routeErrorMessage)
        assertFalse(state.isRouteEmpty)
        assertEquals(1, state.selectedRoute.polylinePoints.size)
        assertEquals(listOf("2026-03-31"), repository.observedLocalDates)
    }

    private fun createViewModel(
        repository: FakeDayRouteRepository,
        initialDateKey: String,
        todayDateKey: String,
        backgroundGranted: Boolean = false
    ): MainViewModel {
        return MainViewModel(
            locationPermissionStatusReader = FakeLocationPermissionStatusReader(backgroundGranted = backgroundGranted),
            initialDateKeyProvider = { initialDateKey },
            routeStateCoordinator = RouteStateCoordinator(
                dayRouteRepository = repository,
                todayDateKeyProvider = { todayDateKey }
            )
        )
    }

    private class FakeLocationPermissionStatusReader(
        private val foregroundGranted: Boolean = false,
        private val backgroundGranted: Boolean = false
    ) : LocationPermissionStatusReader {
        override fun isForegroundGranted(): Boolean = foregroundGranted
        override fun isBackgroundAlwaysGranted(): Boolean = backgroundGranted
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
                ?: error("No fake result prepared for $dateKey")
        }
    }
}
