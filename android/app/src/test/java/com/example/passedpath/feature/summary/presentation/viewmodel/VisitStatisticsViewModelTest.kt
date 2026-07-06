package com.example.passedpath.feature.summary.presentation.viewmodel

import com.example.passedpath.feature.summary.domain.model.PlaceStatisticsItem
import com.example.passedpath.feature.summary.domain.model.PlaceStatisticsSection
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import com.example.passedpath.feature.summary.domain.model.VisitStatistics
import com.example.passedpath.feature.summary.domain.model.VisitedRegionStatisticsItem
import com.example.passedpath.feature.summary.domain.model.VisitedRegionStatisticsSection
import com.example.passedpath.feature.summary.domain.repository.VisitStatisticsRepository
import com.example.passedpath.feature.summary.domain.usecase.GetVisitStatisticsUseCase
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriod
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
class VisitStatisticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadVisitStatistics exposes visit statistics ui state on success`() = runTest {
        val repository = FakeVisitStatisticsRepository()
        val viewModel = createViewModel(repository)

        viewModel.loadVisitStatistics()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(SummaryDetailPeriod.WEEK, state.selectedPeriod)
        assertEquals("2026.05.14 ~ 05.20", state.content.dateRangeText)
        assertEquals("수유동", state.content.visitedRegions.items.first().regionName)
        assertEquals("스타벅스 수유역점", state.content.places.items.first().placeName)
    }

    @Test
    fun `selectPeriod fetches selected period`() = runTest {
        val repository = FakeVisitStatisticsRepository()
        val viewModel = createViewModel(repository)

        viewModel.selectPeriod(SummaryDetailPeriod.MONTH)
        advanceUntilIdle()

        assertEquals(listOf(StatisticsPeriod.MONTH), repository.requestedPeriods)
        assertEquals(SummaryDetailPeriod.MONTH, viewModel.uiState.value.selectedPeriod)
    }

    @Test
    fun `loadVisitStatistics exposes error on failure`() = runTest {
        val repository = FakeVisitStatisticsRepository(
            failure = RuntimeException("boom")
        )
        val viewModel = createViewModel(repository)

        viewModel.loadVisitStatistics()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    private fun createViewModel(
        repository: VisitStatisticsRepository
    ): VisitStatisticsViewModel {
        return VisitStatisticsViewModel(
            getVisitStatisticsUseCase = GetVisitStatisticsUseCase(repository)
        )
    }
}

private class FakeVisitStatisticsRepository(
    private val failure: Throwable? = null
) : VisitStatisticsRepository {
    val requestedPeriods = mutableListOf<StatisticsPeriod>()

    override suspend fun getVisitStatistics(period: StatisticsPeriod): VisitStatistics {
        requestedPeriods += period
        failure?.let { throw it }
        return VisitStatistics(
            period = period,
            startDate = "2026-05-14",
            endDate = "2026-05-20",
            visitedRegions = VisitedRegionStatisticsSection(
                totalVisitCount = 21,
                items = listOf(
                    VisitedRegionStatisticsItem(
                        rank = 1,
                        regionName = "수유동",
                        visitCount = 9,
                        ratio = 42.9,
                        displayRatio = "43%"
                    )
                )
            ),
            places = PlaceStatisticsSection(
                totalVisitCount = 32,
                items = listOf(
                    PlaceStatisticsItem(
                        rank = 1,
                        placeName = "스타벅스 수유역점",
                        roadAddress = "서울특별시 성북구 정릉로 77",
                        visitCount = 8,
                        displayVisitCount = "8회"
                    )
                )
            )
        )
    }
}
