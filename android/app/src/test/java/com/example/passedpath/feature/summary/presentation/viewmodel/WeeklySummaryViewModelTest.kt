package com.example.passedpath.feature.summary.presentation.viewmodel

import com.example.passedpath.feature.summary.domain.model.WeeklyCountMetricAverage
import com.example.passedpath.feature.summary.domain.model.WeeklyCountMetricSection
import com.example.passedpath.feature.summary.domain.model.WeeklyDurationMetricAverage
import com.example.passedpath.feature.summary.domain.model.WeeklyDurationMetricSection
import com.example.passedpath.feature.summary.domain.model.WeeklyStatistics
import com.example.passedpath.feature.summary.domain.model.WeeklyTimeMetricAverage
import com.example.passedpath.feature.summary.domain.model.WeeklyTimeMetricSection
import com.example.passedpath.feature.summary.domain.model.WeeklyVisitedRegionSummaryItem
import com.example.passedpath.feature.summary.domain.model.WeeklyVisitedRegionsSection
import com.example.passedpath.feature.summary.domain.repository.WeeklyStatisticsRepository
import com.example.passedpath.feature.summary.domain.usecase.GetWeeklyStatisticsUseCase
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
class WeeklySummaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadWeeklySummary exposes weekly summary ui state on success`() = runTest {
        val repository = FakeWeeklyStatisticsRepository()
        val viewModel = createViewModel(repository)

        viewModel.loadWeeklySummary()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(4, state.summary.metricCards.size)
        assertEquals(5, state.summary.metricCards.size + 1)
        assertEquals("주간 외출 시간", state.summary.metricCards.first().title)
        assertEquals("성북구", state.summary.visitedRegionsCard.regions.first().regionName)
    }

    @Test
    fun `loadWeeklySummary keeps default state and exposes error on failure`() = runTest {
        val repository = FakeWeeklyStatisticsRepository(
            failure = RuntimeException("boom")
        )
        val viewModel = createViewModel(repository)

        viewModel.loadWeeklySummary()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.hasLoaded)
        assertFalse(state.isLoading)
        assertTrue(state.summary.metricCards.isEmpty())
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `loadWeeklySummary skips duplicate request after loaded`() = runTest {
        val repository = FakeWeeklyStatisticsRepository()
        val viewModel = createViewModel(repository)

        viewModel.loadWeeklySummary()
        advanceUntilIdle()
        viewModel.loadWeeklySummary()
        advanceUntilIdle()

        assertEquals(1, repository.requestCount)
    }

    @Test
    fun `loadWeeklySummary fetches again with force refresh`() = runTest {
        val repository = FakeWeeklyStatisticsRepository()
        val viewModel = createViewModel(repository)

        viewModel.loadWeeklySummary()
        advanceUntilIdle()
        viewModel.loadWeeklySummary(forceRefresh = true)
        advanceUntilIdle()

        assertEquals(2, repository.requestCount)
    }

    private fun createViewModel(
        repository: WeeklyStatisticsRepository
    ): WeeklySummaryViewModel {
        return WeeklySummaryViewModel(
            getWeeklyStatisticsUseCase = GetWeeklyStatisticsUseCase(repository)
        )
    }
}

private class FakeWeeklyStatisticsRepository(
    private val failure: Throwable? = null
) : WeeklyStatisticsRepository {
    var requestCount: Int = 0

    override suspend fun getWeeklyStatistics(): WeeklyStatistics {
        requestCount += 1
        failure?.let { throw it }
        return WeeklyStatistics(
            startDate = "2026-05-07",
            endDate = "2026-05-13",
            outingTime = WeeklyTimeMetricSection(
                average = WeeklyTimeMetricAverage(
                    value = 552,
                    displayText = "09:12",
                    sampleSize = 5
                ),
                sevenDays = emptyList()
            ),
            enterHomeTime = WeeklyTimeMetricSection(
                average = WeeklyTimeMetricAverage(
                    value = 1_395,
                    displayText = "23:15",
                    sampleSize = 5
                ),
                sevenDays = emptyList()
            ),
            totalOutingCount = WeeklyCountMetricSection(
                average = WeeklyCountMetricAverage(
                    value = 1.3,
                    displayText = "1.3회",
                    sampleSize = 5
                ),
                sevenDays = emptyList()
            ),
            totalOutingSeconds = WeeklyDurationMetricSection(
                average = WeeklyDurationMetricAverage(
                    value = 19_800.0,
                    displayText = "5시간 30분",
                    sampleSize = 5
                ),
                sevenDays = emptyList()
            ),
            visitedRegions = WeeklyVisitedRegionsSection(
                topRegions = listOf(
                    WeeklyVisitedRegionSummaryItem(
                        rank = 1,
                        regionName = "성북구"
                    )
                )
            )
        )
    }
}
