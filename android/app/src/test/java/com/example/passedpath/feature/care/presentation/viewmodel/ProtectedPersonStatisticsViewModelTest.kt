package com.example.passedpath.feature.care.presentation.viewmodel

import com.example.passedpath.feature.care.domain.repository.ProtectedPersonStatisticsRepository
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonStatisticMetricUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonVisitStatisticsUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonWeeklyStatisticsUseCase
import com.example.passedpath.feature.summary.domain.model.HighlightMetricValue
import com.example.passedpath.feature.summary.domain.model.PlaceStatisticsSection
import com.example.passedpath.feature.summary.domain.model.StatisticMetric
import com.example.passedpath.feature.summary.domain.model.StatisticMetricAverage
import com.example.passedpath.feature.summary.domain.model.StatisticMetricBarItem
import com.example.passedpath.feature.summary.domain.model.StatisticMetricHighlight
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import com.example.passedpath.feature.summary.domain.model.VisitStatistics
import com.example.passedpath.feature.summary.domain.model.VisitedRegionStatisticsSection
import com.example.passedpath.feature.summary.domain.model.WeeklyCountMetricAverage
import com.example.passedpath.feature.summary.domain.model.WeeklyCountMetricSection
import com.example.passedpath.feature.summary.domain.model.WeeklyDurationMetricAverage
import com.example.passedpath.feature.summary.domain.model.WeeklyDurationMetricSection
import com.example.passedpath.feature.summary.domain.model.WeeklyStatistics
import com.example.passedpath.feature.summary.domain.model.WeeklyTimeMetricAverage
import com.example.passedpath.feature.summary.domain.model.WeeklyTimeMetricSection
import com.example.passedpath.feature.summary.domain.model.WeeklyVisitedRegionsSection
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailMetric
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriod
import com.example.passedpath.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProtectedPersonStatisticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `weekly summary loads protected person statistics`() = runTest {
        val repository = FakeProtectedPersonStatisticsRepository()
        val viewModel = createWeeklyViewModel(repository)

        viewModel.loadWeeklySummary()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(DependentUserId), repository.weeklyRequests)
        assertTrue(state.hasLoaded)
        assertFalse(state.isLoading)
        assertEquals("09:12", state.summary.metricCards.first().valueText)
    }

    @Test
    fun `weekly summary forceRefresh requests again`() = runTest {
        val repository = FakeProtectedPersonStatisticsRepository()
        val viewModel = createWeeklyViewModel(repository)

        viewModel.loadWeeklySummary()
        advanceUntilIdle()
        viewModel.loadWeeklySummary(forceRefresh = true)
        advanceUntilIdle()

        assertEquals(listOf(DependentUserId, DependentUserId), repository.weeklyRequests)
    }

    @Test
    fun `weekly summary exposes error on failure`() = runTest {
        val repository = FakeProtectedPersonStatisticsRepository(
            failure = RuntimeException("boom")
        )
        val viewModel = createWeeklyViewModel(repository)

        viewModel.loadWeeklySummary()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `visit statistics loads selected protected person period`() = runTest {
        val repository = FakeProtectedPersonStatisticsRepository()
        val viewModel = createVisitViewModel(repository)

        viewModel.selectPeriod(SummaryDetailPeriod.SIX_MONTHS)
        advanceUntilIdle()

        assertEquals(
            listOf(ProtectedPersonPeriodRequest(DependentUserId, StatisticsPeriod.SIX_MONTHS)),
            repository.visitRequests
        )
        assertTrue(viewModel.uiState.value.hasLoaded)
        assertEquals(SummaryDetailPeriod.SIX_MONTHS, viewModel.uiState.value.selectedPeriod)
    }

    @Test
    fun `summary detail loads all protected person metric APIs`() = runTest {
        val metrics = listOf(
            SummaryDetailMetric.OUTING_TIME,
            SummaryDetailMetric.ENTER_HOME_TIME,
            SummaryDetailMetric.TOTAL_OUTING_DURATION,
            SummaryDetailMetric.TOTAL_OUTING_COUNT
        )

        metrics.forEach { metric ->
            val repository = FakeProtectedPersonStatisticsRepository()
            val viewModel = createSummaryDetailViewModel(repository)

            viewModel.loadSummaryDetail(metric = metric)
            advanceUntilIdle()

            assertEquals(
                listOf(
                    ProtectedPersonMetricRequest(
                        dependentUserId = DependentUserId,
                        metric = metric,
                        period = StatisticsPeriod.WEEK
                    )
                ),
                repository.metricRequests
            )
            assertTrue(viewModel.uiState.value.hasLoaded)
            assertEquals(metric, viewModel.uiState.value.content?.metric)
        }
    }

    @Test
    fun `summary detail period change uses selected protected person period`() = runTest {
        val repository = FakeProtectedPersonStatisticsRepository()
        val viewModel = createSummaryDetailViewModel(repository)

        viewModel.selectPeriod(
            metric = SummaryDetailMetric.TOTAL_OUTING_COUNT,
            period = SummaryDetailPeriod.MONTH
        )
        advanceUntilIdle()

        assertEquals(
            listOf(
                ProtectedPersonMetricRequest(
                    dependentUserId = DependentUserId,
                    metric = SummaryDetailMetric.TOTAL_OUTING_COUNT,
                    period = StatisticsPeriod.MONTH
                )
            ),
            repository.metricRequests
        )
        assertEquals(SummaryDetailPeriod.MONTH, viewModel.uiState.value.selectedPeriod)
    }

    @Test
    fun `summary detail exposes error on failure`() = runTest {
        val repository = FakeProtectedPersonStatisticsRepository(
            failure = RuntimeException("boom")
        )
        val viewModel = createSummaryDetailViewModel(repository)

        viewModel.loadSummaryDetail(metric = SummaryDetailMetric.OUTING_TIME)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertEquals(SummaryDetailMetric.OUTING_TIME, state.metric)
    }

    private fun createWeeklyViewModel(
        repository: ProtectedPersonStatisticsRepository
    ): ProtectedPersonWeeklySummaryViewModel {
        return ProtectedPersonWeeklySummaryViewModel(
            dependentUserId = DependentUserId,
            getProtectedPersonWeeklyStatisticsUseCase =
                GetProtectedPersonWeeklyStatisticsUseCase(repository)
        )
    }

    private fun createVisitViewModel(
        repository: ProtectedPersonStatisticsRepository
    ): ProtectedPersonVisitStatisticsViewModel {
        return ProtectedPersonVisitStatisticsViewModel(
            dependentUserId = DependentUserId,
            getProtectedPersonVisitStatisticsUseCase =
                GetProtectedPersonVisitStatisticsUseCase(repository)
        )
    }

    private fun createSummaryDetailViewModel(
        repository: ProtectedPersonStatisticsRepository
    ): ProtectedPersonSummaryDetailViewModel {
        return ProtectedPersonSummaryDetailViewModel(
            dependentUserId = DependentUserId,
            getProtectedPersonStatisticMetricUseCase =
                GetProtectedPersonStatisticMetricUseCase(repository)
        )
    }

    private companion object {
        const val DependentUserId = 7L
    }
}

private class FakeProtectedPersonStatisticsRepository(
    private val failure: Throwable? = null
) : ProtectedPersonStatisticsRepository {
    val weeklyRequests = mutableListOf<Long>()
    val visitRequests = mutableListOf<ProtectedPersonPeriodRequest>()
    val metricRequests = mutableListOf<ProtectedPersonMetricRequest>()

    override suspend fun getWeeklyStatistics(dependentUserId: Long): WeeklyStatistics {
        weeklyRequests += dependentUserId
        failure?.let { throw it }
        return weeklyStatistics()
    }

    override suspend fun getVisitStatistics(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): VisitStatistics {
        visitRequests += ProtectedPersonPeriodRequest(dependentUserId, period)
        failure?.let { throw it }
        return VisitStatistics(
            period = period,
            startDate = "2026-05-14",
            endDate = "2026-05-20",
            visitedRegions = VisitedRegionStatisticsSection(
                totalVisitCount = 0,
                items = emptyList()
            ),
            places = PlaceStatisticsSection(
                totalVisitCount = 0,
                items = emptyList()
            )
        )
    }

    override suspend fun getOutingTime(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric {
        return statisticMetric(
            dependentUserId = dependentUserId,
            metric = SummaryDetailMetric.OUTING_TIME,
            period = period,
            value = 552.0,
            displayText = "09:12"
        )
    }

    override suspend fun getEnterHomeTime(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric {
        return statisticMetric(
            dependentUserId = dependentUserId,
            metric = SummaryDetailMetric.ENTER_HOME_TIME,
            period = period,
            value = 1395.0,
            displayText = "23:15"
        )
    }

    override suspend fun getTotalOutingSeconds(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric {
        return statisticMetric(
            dependentUserId = dependentUserId,
            metric = SummaryDetailMetric.TOTAL_OUTING_DURATION,
            period = period,
            value = 3600.0,
            displayText = "1\uC2DC\uAC04"
        )
    }

    override suspend fun getTotalOutingCount(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric {
        return statisticMetric(
            dependentUserId = dependentUserId,
            metric = SummaryDetailMetric.TOTAL_OUTING_COUNT,
            period = period,
            value = 2.0,
            displayText = "2.0\uD68C"
        )
    }

    private fun statisticMetric(
        dependentUserId: Long,
        metric: SummaryDetailMetric,
        period: StatisticsPeriod,
        value: Double,
        displayText: String
    ): StatisticMetric {
        metricRequests += ProtectedPersonMetricRequest(
            dependentUserId = dependentUserId,
            metric = metric,
            period = period
        )
        failure?.let { throw it }
        return StatisticMetric(
            metricType = metric.routeValue,
            period = period,
            startDate = "2026-05-14",
            endDate = "2026-05-20",
            average = StatisticMetricAverage(
                value = value,
                displayText = displayText,
                sampleSize = 1
            ),
            bars = listOf(
                StatisticMetricBarItem(
                    label = "Mon",
                    startDate = "2026-05-20",
                    endDate = "2026-05-20",
                    value = value,
                    displayText = displayText,
                    hasValue = true,
                    sampleSize = 1
                )
            ),
            highlight = StatisticMetricHighlight(
                title = "Highlight",
                message = "Message",
                current = HighlightMetricValue("Current", value, displayText, 1),
                previous = HighlightMetricValue("Previous", null, null, 0)
            )
        )
    }

    private fun weeklyStatistics(): WeeklyStatistics {
        return WeeklyStatistics(
            startDate = "2026-05-14",
            endDate = "2026-05-20",
            outingTime = WeeklyTimeMetricSection(
                average = WeeklyTimeMetricAverage(
                    value = 552,
                    displayText = "09:12",
                    sampleSize = 1
                ),
                sevenDays = emptyList()
            ),
            enterHomeTime = WeeklyTimeMetricSection(
                average = WeeklyTimeMetricAverage(
                    value = 1395,
                    displayText = "23:15",
                    sampleSize = 1
                ),
                sevenDays = emptyList()
            ),
            totalOutingCount = WeeklyCountMetricSection(
                average = WeeklyCountMetricAverage(
                    value = 2.0,
                    displayText = "2.0\uD68C",
                    sampleSize = 1
                ),
                sevenDays = emptyList()
            ),
            totalOutingSeconds = WeeklyDurationMetricSection(
                average = WeeklyDurationMetricAverage(
                    value = 3600.0,
                    displayText = "1\uC2DC\uAC04",
                    sampleSize = 1
                ),
                sevenDays = emptyList()
            ),
            visitedRegions = WeeklyVisitedRegionsSection(topRegions = emptyList())
        )
    }
}

private data class ProtectedPersonPeriodRequest(
    val dependentUserId: Long,
    val period: StatisticsPeriod
)

private data class ProtectedPersonMetricRequest(
    val dependentUserId: Long,
    val metric: SummaryDetailMetric,
    val period: StatisticsPeriod
)
