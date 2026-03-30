package com.example.passedpath.feature.main.presentation.viewmodel

import com.example.passedpath.feature.locationtracking.domain.model.DayRouteDetail
import com.example.passedpath.feature.locationtracking.domain.model.DayRoutePlace
import com.example.passedpath.feature.locationtracking.domain.model.RoutePoint
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.RemoteDayRouteResult
import com.example.passedpath.feature.permission.data.manager.LocationPermissionStatusReader
import com.example.passedpath.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    fun `init loads remote day route into ui state`() = runTest {

        // 가짜 레포지토리 -> 가짜 데이터
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

        val viewModel = MainViewModel(
            locationPermissionStatusReader = FakeLocationPermissionStatusReader(backgroundGranted = true),
            dayRouteRepository = repository,
            initialDateKeyProvider = { "2026-03-29" }
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
        assertEquals(listOf("2026-03-29"), repository.requestedDates)
    }

    @Test
    fun `selectDate with empty result shows no location data state without error`() = runTest {
        val repository = FakeDayRouteRepository(
            resultByDate = mutableMapOf(
                "2026-03-29" to RemoteDayRouteResult.Empty,
                "2026-03-30" to RemoteDayRouteResult.Empty
            )
        )

        val viewModel = MainViewModel(
            locationPermissionStatusReader = FakeLocationPermissionStatusReader(),
            dayRouteRepository = repository,
            initialDateKeyProvider = { "2026-03-29" }
        )
        advanceUntilIdle()

        viewModel.selectDate("2026-03-30")
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals("2026-03-30", state.selectedDateKey)
        assertTrue(state.isRouteEmpty)
        assertFalse(state.selectedRoute.hasLocationData)
        assertEquals("No route data exists for this day.", state.routeEmptyMessage)
        assertNull(state.routeErrorMessage)
    }

    @Test
    fun `selectDate with error result exposes retryable error state`() = runTest {
        val repository = FakeDayRouteRepository(
            resultByDate = mutableMapOf(
                "2026-03-29" to RemoteDayRouteResult.Empty,
                "2026-03-31" to RemoteDayRouteResult.Error(IllegalStateException("boom"))
            )
        )

        val viewModel = MainViewModel(
            locationPermissionStatusReader = FakeLocationPermissionStatusReader(),
            dayRouteRepository = repository,
            initialDateKeyProvider = { "2026-03-29" }
        )
        advanceUntilIdle()

        viewModel.selectDate("2026-03-31")
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals("2026-03-31", state.selectedDateKey)
        assertFalse(state.isRouteEmpty)
        assertEquals("Failed to load the selected route.", state.routeErrorMessage)
        assertFalse(state.selectedRoute.hasLocationData)
    }

    private class FakeLocationPermissionStatusReader(
        private val foregroundGranted: Boolean = false,
        private val backgroundGranted: Boolean = false
    ) : LocationPermissionStatusReader {
        override fun isForegroundGranted(): Boolean = foregroundGranted
        override fun isBackgroundAlwaysGranted(): Boolean = backgroundGranted
    }

    private class FakeDayRouteRepository(
        private val resultByDate: MutableMap<String, RemoteDayRouteResult>
    ) : DayRouteRepository {
        val requestedDates = mutableListOf<String>()

        override suspend fun getLocalDayRoute(dateKey: String) = null

        override suspend fun markLocalDayRouteSynced(dateKey: String, syncedAtEpochMillis: Long) =
            Unit

        override suspend fun fetchRemoteDayRoute(dateKey: String): RemoteDayRouteResult {
            requestedDates += dateKey
            return resultByDate[dateKey]
                ?: error("No fake result prepared for $dateKey")
        }
    }
}
