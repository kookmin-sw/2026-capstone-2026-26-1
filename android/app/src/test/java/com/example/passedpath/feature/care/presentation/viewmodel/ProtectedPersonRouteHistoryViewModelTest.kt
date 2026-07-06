package com.example.passedpath.feature.care.presentation.viewmodel

import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteDetail
import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteList
import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteListItem
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteListResult
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteResult
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonDayRouteListUseCase
import com.example.passedpath.testutil.MainDispatcherRule
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
class ProtectedPersonRouteHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `first load success fills route date cards`() = runTest {
        val repository = FakeProtectedPersonDayRouteRepository(
            results = mutableListOf(
                routeListResult(
                    dayRoutes = listOf(
                        routeItem(
                            dateKey = "2026-04-03",
                            outingTime = "2026-04-03T09:12:00+09:00",
                            enterHomeTime = "2026-04-03T23:40:00+09:00",
                            totalOutingCount = 3
                        )
                    )
                )
            )
        )
        val viewModel = createViewModel(repository = repository)

        viewModel.fetchRoutes()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("\uB538\uD61C\uC6D0", state.dependentNickname)
        assertTrue(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(listOf("2026-04-03"), state.dayRoutes.map { it.dateKey })
        assertEquals("4\uC6D4 3\uC77C", state.dayRoutes.first().dateText)
        assertEquals("09:12", state.dayRoutes.first().outingTimeText)
        assertEquals("23:40", state.dayRoutes.first().enterHomeTimeText)
        assertEquals("3\uD68C", state.dayRoutes.first().outingCountText)
        assertEquals(listOf(Request(dependentUserId = 7L)), repository.requests)
    }

    @Test
    fun `first load error exposes retryable error message`() = runTest {
        val repository = FakeProtectedPersonDayRouteRepository(
            results = mutableListOf(
                ProtectedPersonDayRouteListResult.Error(
                    IllegalStateException("network failed")
                )
            )
        )
        val viewModel = createViewModel(repository = repository)

        viewModel.fetchRoutes()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertTrue(state.dayRoutes.isEmpty())
    }

    @Test
    fun `force refresh reloads after first loaded state`() = runTest {
        val repository = FakeProtectedPersonDayRouteRepository(
            results = mutableListOf(
                routeListResult(
                    dayRoutes = listOf(routeItem(dateKey = "2026-04-03"))
                ),
                routeListResult(
                    dayRoutes = listOf(routeItem(dateKey = "2026-03-20"))
                )
            )
        )
        val viewModel = createViewModel(repository = repository)

        viewModel.fetchRoutes()
        advanceUntilIdle()
        viewModel.fetchRoutes()
        advanceUntilIdle()
        viewModel.fetchRoutes(forceRefresh = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, repository.requests.size)
        assertEquals(listOf("2026-03-20"), state.dayRoutes.map { it.dateKey })
    }

    @Test
    fun `next page uses cursor and removes duplicate dates`() = runTest {
        val repository = FakeProtectedPersonDayRouteRepository(
            results = mutableListOf(
                routeListResult(
                    dayRoutes = listOf(routeItem(dateKey = "2026-04-03")),
                    hasNext = true,
                    nextCursorDate = "2026-03-20"
                ),
                routeListResult(
                    dayRoutes = listOf(
                        routeItem(dateKey = "2026-04-03"),
                        routeItem(dateKey = "2026-03-20")
                    ),
                    hasNext = false,
                    nextCursorDate = null
                )
            )
        )
        val viewModel = createViewModel(repository = repository)

        viewModel.fetchRoutes()
        advanceUntilIdle()
        viewModel.fetchNextRoutes()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(
            listOf(
                Request(dependentUserId = 7L),
                Request(dependentUserId = 7L, cursorDate = "2026-03-20")
            ),
            repository.requests
        )
        assertEquals(
            listOf("2026-04-03", "2026-03-20"),
            state.dayRoutes.map { it.dateKey }
        )
        assertFalse(state.hasNext)
        assertNull(state.nextCursorDate)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `next page error keeps existing routes and exposes error`() = runTest {
        val repository = FakeProtectedPersonDayRouteRepository(
            results = mutableListOf(
                routeListResult(
                    dayRoutes = listOf(routeItem(dateKey = "2026-04-03")),
                    hasNext = true,
                    nextCursorDate = "2026-03-20"
                ),
                ProtectedPersonDayRouteListResult.Error(
                    IllegalStateException("network failed")
                )
            )
        )
        val viewModel = createViewModel(repository = repository)

        viewModel.fetchRoutes()
        advanceUntilIdle()
        viewModel.fetchNextRoutes()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("2026-04-03"), state.dayRoutes.map { it.dateKey })
        assertTrue(state.hasNext)
        assertEquals("2026-03-20", state.nextCursorDate)
        assertFalse(state.isLoadingMore)
        assertNotNull(state.errorMessage)
    }

    private fun createViewModel(
        repository: ProtectedPersonDayRouteRepository
    ): ProtectedPersonRouteHistoryViewModel {
        return ProtectedPersonRouteHistoryViewModel(
            dependentUserId = 7L,
            dependentNickname = "\uB538\uD61C\uC6D0",
            getProtectedPersonDayRouteListUseCase =
                GetProtectedPersonDayRouteListUseCase(repository)
        )
    }

    private class FakeProtectedPersonDayRouteRepository(
        private val results: MutableList<ProtectedPersonDayRouteListResult>
    ) : ProtectedPersonDayRouteRepository {
        val requests = mutableListOf<Request>()

        override suspend fun fetchDayRoutes(
            dependentUserId: Long,
            cursorDate: String?,
            size: Int?
        ): ProtectedPersonDayRouteListResult {
            requests += Request(
                dependentUserId = dependentUserId,
                cursorDate = cursorDate,
                size = size
            )
            return results.removeAt(0)
        }

        override suspend fun fetchDayRoute(
            dependentUserId: Long,
            dateKey: String
        ): ProtectedPersonDayRouteResult {
            return ProtectedPersonDayRouteResult.Success(
                ProtectedPersonDayRouteDetail(
                    dateKey = dateKey,
                    totalDistanceKm = 0.0,
                    pathPointCount = 0
                )
            )
        }
    }

    private data class Request(
        val dependentUserId: Long,
        val cursorDate: String? = null,
        val size: Int? = null
    )
}

private fun routeListResult(
    dayRoutes: List<ProtectedPersonDayRouteListItem>,
    hasNext: Boolean = false,
    nextCursorDate: String? = null
): ProtectedPersonDayRouteListResult.Success {
    return ProtectedPersonDayRouteListResult.Success(
        ProtectedPersonDayRouteList(
            dayRouteCount = dayRoutes.size,
            hasNext = hasNext,
            nextCursorDate = nextCursorDate,
            dayRoutes = dayRoutes
        )
    )
}

private fun routeItem(
    dateKey: String,
    outingTime: String? = "2026-04-03T09:12:00+09:00",
    enterHomeTime: String? = "2026-04-03T23:40:00+09:00",
    totalOutingCount: Int = 1
): ProtectedPersonDayRouteListItem {
    return ProtectedPersonDayRouteListItem(
        dateKey = dateKey,
        outingTime = outingTime,
        enterHomeTime = enterHomeTime,
        totalOutingCount = totalOutingCount
    )
}
