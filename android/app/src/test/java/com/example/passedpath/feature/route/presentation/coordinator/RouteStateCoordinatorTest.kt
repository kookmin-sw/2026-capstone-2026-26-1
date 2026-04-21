package com.example.passedpath.feature.route.presentation.coordinator

import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.DayRouteDetail
import com.example.passedpath.feature.locationtracking.domain.model.DayRoutePlace
import com.example.passedpath.feature.locationtracking.domain.model.RoutePoint
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.RemoteDayRouteResult
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RouteStateCoordinatorTest {

    @Test
    fun `createInitialState returns today mode for today date`() {
        val coordinator = RouteStateCoordinator(
            dayRouteRepository = FakeDayRouteRepository(),
            todayDateKeyProvider = { "2026-04-01" }
        )

        val state = coordinator.createInitialState("2026-04-01")

        assertTrue(state is MainRouteModeUiState.Today)
        assertEquals("2026-04-01", state.route.dateKey)
    }

    @Test
    fun `loadRoute emits loading then today data from local repository`() = runTest {
        val localFlow = MutableStateFlow<DailyPath?>(
            DailyPath(
                dateKey = "2026-04-01",
                points = listOf(
                    TrackedLocation(37.1, 127.1, 5f, 1L),
                    TrackedLocation(37.2, 127.2, 5f, 2L)
                ),
                totalDistanceMeters = 1200.0,
                pathPointCount = 2
            )
        )
        val repository = FakeDayRouteRepository(
            localRouteByDate = mutableMapOf("2026-04-01" to localFlow),
            resultByDate = mutableMapOf(
                "2026-04-01" to RemoteDayRouteResult.Success(
                    DayRouteDetail(
                        dateKey = "2026-04-01",
                        totalDistanceKm = 0.0,
                        title = "Today Title",
                        memo = "Today Memo",
                        places = listOf(
                            DayRoutePlace(1L, "Seed Place", "Road", 37.3, 127.3, 1)
                        )
                    )
                )
            )
        )
        val coordinator = RouteStateCoordinator(
            dayRouteRepository = repository,
            todayDateKeyProvider = { "2026-04-01" }
        )

        val states = coordinator.loadRoute("2026-04-01").take(2).toList()

        assertEquals(2, states.size)
        assertTrue(states[0].routeModeUiState.isRouteLoading)
        assertTrue(states[0].routeModeUiState is MainRouteModeUiState.Today)
        assertFalse(states[1].routeModeUiState.isRouteLoading)
        assertTrue(states[1].routeModeUiState is MainRouteModeUiState.Today)
        assertEquals(2, states[1].routeModeUiState.route.polylinePoints.size)
        assertEquals("Today Title", states[1].routeModeUiState.route.title)
        assertEquals(1, states[1].routeModeUiState.route.markerPlaces.size)
        assertEquals(listOf("2026-04-01"), repository.observedLocalDates)
        assertEquals(listOf("2026-04-01"), repository.requestedRemoteDates)
    }

    @Test
    fun `loadRoute emits today empty state when local route is null`() = runTest {
        val repository = FakeDayRouteRepository(
            localRouteByDate = mutableMapOf("2026-04-01" to MutableStateFlow<DailyPath?>(null))
        )
        val coordinator = RouteStateCoordinator(
            dayRouteRepository = repository,
            todayDateKeyProvider = { "2026-04-01" }
        )

        val states = coordinator.loadRoute("2026-04-01").take(2).toList()
        val finalState = states.last().routeModeUiState as MainRouteModeUiState.Today

        assertTrue(finalState.isRouteEmpty)
        assertEquals("오늘의 이동 경로가 기록되면 이곳에 표시됩니다.", finalState.routeEmptyMessage)
        assertEquals("2026-04-01", finalState.route.dateKey)
    }

    @Test
    fun `loadRoute emits loading then past remote success`() = runTest {
        val repository = FakeDayRouteRepository(
            resultByDate = mutableMapOf(
                "2026-03-31" to RemoteDayRouteResult.Success(
                    DayRouteDetail(
                        dateKey = "2026-03-31",
                        totalDistanceKm = 3.4,
                        pathPointCount = 2,
                        polylinePoints = listOf(
                            RoutePoint(37.1, 127.1),
                            RoutePoint(37.2, 127.2)
                        ),
                        places = listOf(
                            DayRoutePlace(1L, "장소", "도로명", 37.3, 127.3, 1)
                        )
                    )
                )
            )
        )
        val coordinator = RouteStateCoordinator(
            dayRouteRepository = repository,
            todayDateKeyProvider = { "2026-04-01" }
        )

        val states = coordinator.loadRoute("2026-03-31").toList()
        val finalState = states.last().routeModeUiState as MainRouteModeUiState.Past

        assertEquals(2, states.size)
        assertTrue(states.first().routeModeUiState.isRouteLoading)
        assertFalse(finalState.isRouteLoading)
        assertEquals(1, finalState.route.places.size)
        assertEquals(listOf("2026-03-31"), repository.requestedRemoteDates)
        assertTrue(repository.observedLocalDates.isEmpty())
    }

    @Test
    fun `loadRoute emits past empty state for empty remote result`() = runTest {
        val repository = FakeDayRouteRepository(
            resultByDate = mutableMapOf("2026-03-31" to RemoteDayRouteResult.Empty)
        )
        val coordinator = RouteStateCoordinator(
            dayRouteRepository = repository,
            todayDateKeyProvider = { "2026-04-01" }
        )

        val states = coordinator.loadRoute("2026-03-31").toList()
        val finalState = states.last().routeModeUiState as MainRouteModeUiState.Past

        assertTrue(finalState.isRouteEmpty)
        assertEquals("선택한 날짜에는 지도에 표시할 경로 데이터가 없습니다.", finalState.routeEmptyMessage)
        assertNull(finalState.routeErrorMessage)
    }

    @Test
    fun `loadRoute emits past error state for failed remote result`() = runTest {
        val repository = FakeDayRouteRepository(
            resultByDate = mutableMapOf(
                "2026-03-31" to RemoteDayRouteResult.Error(IllegalStateException("boom"))
            )
        )
        val coordinator = RouteStateCoordinator(
            dayRouteRepository = repository,
            todayDateKeyProvider = { "2026-04-01" }
        )

        val states = coordinator.loadRoute("2026-03-31").toList()
        val finalState = states.last().routeModeUiState as MainRouteModeUiState.Past

        assertFalse(finalState.isRouteEmpty)
        assertEquals("선택한 날짜의 경로를 불러오지 못했습니다.", finalState.routeErrorMessage)
    }

    @Test
    fun `loadRoute emits today read data even when local path is empty`() = runTest {
        val repository = FakeDayRouteRepository(
            localRouteByDate = mutableMapOf("2026-04-01" to MutableStateFlow<DailyPath?>(null)),
            resultByDate = mutableMapOf(
                "2026-04-01" to RemoteDayRouteResult.Success(
                    DayRouteDetail(
                        dateKey = "2026-04-01",
                        totalDistanceKm = 0.0,
                        title = "Today Title",
                        memo = "Today Memo",
                        places = listOf(
                            DayRoutePlace(1L, "Seed Place", "Road", 37.3, 127.3, 1)
                        )
                    )
                )
            )
        )
        val coordinator = RouteStateCoordinator(
            dayRouteRepository = repository,
            todayDateKeyProvider = { "2026-04-01" }
        )

        val states = coordinator.loadRoute("2026-04-01").take(2).toList()
        val finalState = states.last().routeModeUiState as MainRouteModeUiState.Today

        assertFalse(finalState.isRouteEmpty)
        assertEquals("Today Title", finalState.route.title)
        assertEquals("Today Memo", finalState.route.memo)
        assertEquals(1, finalState.route.markerPlaces.size)
        assertTrue(finalState.route.polylinePoints.isEmpty())
    }

    private class FakeDayRouteRepository(
        private val resultByDate: MutableMap<String, RemoteDayRouteResult> = mutableMapOf(),
        private val localRouteByDate: MutableMap<String, MutableStateFlow<DailyPath?>> = mutableMapOf()
    ) : DayRouteRepository {
        val requestedRemoteDates = mutableListOf<String>()
        val observedLocalDates = mutableListOf<String>()

        override fun observeLocalDayRoute(dateKey: String): Flow<DailyPath?> {
            observedLocalDates += dateKey
            return localRouteByDate.getOrPut(dateKey) { MutableStateFlow<DailyPath?>(null) }.asStateFlow()
        }

        override suspend fun getLocalDayRoute(dateKey: String): DailyPath? = localRouteByDate[dateKey]?.value

        override suspend fun markLocalDayRouteSynced(dateKey: String, syncedAtEpochMillis: Long) = Unit

        override suspend fun fetchRemoteDayRoute(dateKey: String): RemoteDayRouteResult {
            requestedRemoteDates += dateKey
            return resultByDate[dateKey] ?: RemoteDayRouteResult.Empty
        }
    }
}
