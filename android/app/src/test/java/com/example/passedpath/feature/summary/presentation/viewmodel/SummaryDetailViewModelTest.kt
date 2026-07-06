package com.example.passedpath.feature.summary.presentation.viewmodel

import com.example.passedpath.feature.summary.domain.model.HighlightMetricValue
import com.example.passedpath.feature.summary.domain.model.StatisticMetric
import com.example.passedpath.feature.summary.domain.model.StatisticMetricAverage
import com.example.passedpath.feature.summary.domain.model.StatisticMetricHighlight
import com.example.passedpath.feature.summary.domain.repository.StatisticMetricRepository
import com.example.passedpath.feature.summary.domain.usecase.GetEnterHomeTimeStatisticsUseCase
import com.example.passedpath.feature.summary.domain.usecase.GetOutingTimeStatisticsUseCase
import com.example.passedpath.feature.summary.domain.usecase.GetTotalOutingCountStatisticsUseCase
import com.example.passedpath.feature.summary.domain.usecase.GetTotalOutingSecondsStatisticsUseCase
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailMetric
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriod
import com.example.passedpath.feature.summary.domain.model.StatisticMetricBarItem
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
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
class SummaryDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadSummaryDetail requests outing time week and exposes ui state on success`() = runTest {
        val repository = FakeStatisticMetricRepository()
        val viewModel = createViewModel(repository)

        viewModel.loadSummaryDetail(metric = SummaryDetailMetric.OUTING_TIME)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(StatisticsPeriod.WEEK), repository.requestedPeriods)
        assertEquals(listOf(SummaryDetailMetric.OUTING_TIME), repository.requestedMetrics)
        assertTrue(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(SummaryDetailMetric.OUTING_TIME, state.metric)
        assertEquals(SummaryDetailMetric.OUTING_TIME, state.content?.metric)
    }

    @Test
    fun `loadSummaryDetail requests enter home time week and exposes ui state on success`() = runTest {
        val repository = FakeStatisticMetricRepository()
        val viewModel = createViewModel(repository)

        viewModel.loadSummaryDetail(metric = SummaryDetailMetric.ENTER_HOME_TIME)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(StatisticsPeriod.WEEK), repository.requestedPeriods)
        assertEquals(listOf(SummaryDetailMetric.ENTER_HOME_TIME), repository.requestedMetrics)
        assertTrue(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(SummaryDetailMetric.ENTER_HOME_TIME, state.metric)
        assertEquals(SummaryDetailMetric.ENTER_HOME_TIME, state.content?.metric)
    }

    @Test
    fun `loadTotalOutingDuration requests week and exposes ui state on success`() = runTest {
        val repository = FakeStatisticMetricRepository()
        val viewModel = createViewModel(repository)

        viewModel.loadTotalOutingDuration()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(StatisticsPeriod.WEEK), repository.requestedPeriods)
        assertEquals(listOf(SummaryDetailMetric.TOTAL_OUTING_DURATION), repository.requestedMetrics)
        assertTrue(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(SummaryDetailPeriod.WEEK, state.selectedPeriod)
        assertEquals(SummaryDetailMetric.TOTAL_OUTING_DURATION, state.content?.metric)
    }

    @Test
    fun `loadSummaryDetail requests total outing count week and exposes ui state on success`() = runTest {
        val repository = FakeStatisticMetricRepository()
        val viewModel = createViewModel(repository)

        viewModel.loadSummaryDetail(metric = SummaryDetailMetric.TOTAL_OUTING_COUNT)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(StatisticsPeriod.WEEK), repository.requestedPeriods)
        assertEquals(listOf(SummaryDetailMetric.TOTAL_OUTING_COUNT), repository.requestedMetrics)
        assertTrue(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(SummaryDetailMetric.TOTAL_OUTING_COUNT, state.metric)
        assertEquals(SummaryDetailMetric.TOTAL_OUTING_COUNT, state.content?.metric)
    }

    @Test
    fun `selectPeriod fetches selected period for outing time`() = runTest {
        val repository = FakeStatisticMetricRepository()
        val viewModel = createViewModel(repository)

        viewModel.selectPeriod(
            metric = SummaryDetailMetric.OUTING_TIME,
            period = SummaryDetailPeriod.MONTH
        )
        advanceUntilIdle()

        assertEquals(listOf(StatisticsPeriod.MONTH), repository.requestedPeriods)
        assertEquals(listOf(SummaryDetailMetric.OUTING_TIME), repository.requestedMetrics)
        assertEquals(SummaryDetailPeriod.MONTH, viewModel.uiState.value.selectedPeriod)
        assertEquals(SummaryDetailMetric.OUTING_TIME, viewModel.uiState.value.metric)
    }

    @Test
    fun `selectPeriod fetches selected period for enter home time`() = runTest {
        val repository = FakeStatisticMetricRepository()
        val viewModel = createViewModel(repository)

        viewModel.selectPeriod(
            metric = SummaryDetailMetric.ENTER_HOME_TIME,
            period = SummaryDetailPeriod.MONTH
        )
        advanceUntilIdle()

        assertEquals(listOf(StatisticsPeriod.MONTH), repository.requestedPeriods)
        assertEquals(listOf(SummaryDetailMetric.ENTER_HOME_TIME), repository.requestedMetrics)
        assertEquals(SummaryDetailPeriod.MONTH, viewModel.uiState.value.selectedPeriod)
        assertEquals(SummaryDetailMetric.ENTER_HOME_TIME, viewModel.uiState.value.metric)
    }

    @Test
    fun `selectPeriod fetches selected period for total outing duration`() = runTest {
        val repository = FakeStatisticMetricRepository()
        val viewModel = createViewModel(repository)

        viewModel.selectPeriod(SummaryDetailPeriod.MONTH)
        advanceUntilIdle()

        assertEquals(listOf(StatisticsPeriod.MONTH), repository.requestedPeriods)
        assertEquals(listOf(SummaryDetailMetric.TOTAL_OUTING_DURATION), repository.requestedMetrics)
        assertEquals(SummaryDetailPeriod.MONTH, viewModel.uiState.value.selectedPeriod)
    }

    @Test
    fun `selectPeriod fetches selected period for total outing count`() = runTest {
        val repository = FakeStatisticMetricRepository()
        val viewModel = createViewModel(repository)

        viewModel.selectPeriod(
            metric = SummaryDetailMetric.TOTAL_OUTING_COUNT,
            period = SummaryDetailPeriod.MONTH
        )
        advanceUntilIdle()

        assertEquals(listOf(StatisticsPeriod.MONTH), repository.requestedPeriods)
        assertEquals(listOf(SummaryDetailMetric.TOTAL_OUTING_COUNT), repository.requestedMetrics)
        assertEquals(SummaryDetailPeriod.MONTH, viewModel.uiState.value.selectedPeriod)
        assertEquals(SummaryDetailMetric.TOTAL_OUTING_COUNT, viewModel.uiState.value.metric)
    }

    @Test
    fun `loadTotalOutingDuration forceRefresh requests again`() = runTest {
        val repository = FakeStatisticMetricRepository()
        val viewModel = createViewModel(repository)

        viewModel.loadTotalOutingDuration()
        advanceUntilIdle()
        viewModel.loadTotalOutingDuration(forceRefresh = true)
        advanceUntilIdle()

        assertEquals(
            listOf(StatisticsPeriod.WEEK, StatisticsPeriod.WEEK),
            repository.requestedPeriods
        )
    }

    @Test
    fun `loadSummaryDetail forceRefresh requests enter home time again`() = runTest {
        val repository = FakeStatisticMetricRepository()
        val viewModel = createViewModel(repository)

        viewModel.loadSummaryDetail(metric = SummaryDetailMetric.ENTER_HOME_TIME)
        advanceUntilIdle()
        viewModel.loadSummaryDetail(
            metric = SummaryDetailMetric.ENTER_HOME_TIME,
            forceRefresh = true
        )
        advanceUntilIdle()

        assertEquals(
            listOf(StatisticsPeriod.WEEK, StatisticsPeriod.WEEK),
            repository.requestedPeriods
        )
        assertEquals(
            listOf(SummaryDetailMetric.ENTER_HOME_TIME, SummaryDetailMetric.ENTER_HOME_TIME),
            repository.requestedMetrics
        )
    }

    @Test
    fun `loadTotalOutingDuration exposes error on failure`() = runTest {
        val repository = FakeStatisticMetricRepository(
            failure = RuntimeException("boom")
        )
        val viewModel = createViewModel(repository)

        viewModel.loadTotalOutingDuration()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `loadSummaryDetail exposes enter home time error on failure`() = runTest {
        val repository = FakeStatisticMetricRepository(
            failure = RuntimeException("boom")
        )
        val viewModel = createViewModel(repository)

        viewModel.loadSummaryDetail(metric = SummaryDetailMetric.ENTER_HOME_TIME)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.hasLoaded)
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertEquals(SummaryDetailMetric.ENTER_HOME_TIME, state.metric)
    }

    private fun createViewModel(
        repository: StatisticMetricRepository
    ): SummaryDetailViewModel {
        return SummaryDetailViewModel(
            getOutingTimeStatisticsUseCase = GetOutingTimeStatisticsUseCase(repository),
            getEnterHomeTimeStatisticsUseCase = GetEnterHomeTimeStatisticsUseCase(repository),
            getTotalOutingSecondsStatisticsUseCase = GetTotalOutingSecondsStatisticsUseCase(repository),
            getTotalOutingCountStatisticsUseCase = GetTotalOutingCountStatisticsUseCase(repository)
        )
    }
}

private class FakeStatisticMetricRepository(
    private val failure: Throwable? = null
) : StatisticMetricRepository {
    val requestedPeriods = mutableListOf<StatisticsPeriod>()
    val requestedMetrics = mutableListOf<SummaryDetailMetric>()

    override suspend fun getOutingTime(period: StatisticsPeriod): StatisticMetric {
        requestedMetrics += SummaryDetailMetric.OUTING_TIME
        requestedPeriods += period
        failure?.let { throw it }
        return statisticMetric(
            metricType = "OUTING_TIME",
            period = period,
            value = 552.0,
            displayText = "09:12"
        )
    }

    override suspend fun getEnterHomeTime(period: StatisticsPeriod): StatisticMetric {
        requestedMetrics += SummaryDetailMetric.ENTER_HOME_TIME
        requestedPeriods += period
        failure?.let { throw it }
        return statisticMetric(
            metricType = "ENTER_HOME_TIME",
            period = period,
            value = 1395.0,
            displayText = "23:15"
        )
    }

    override suspend fun getTotalOutingSeconds(period: StatisticsPeriod): StatisticMetric {
        requestedMetrics += SummaryDetailMetric.TOTAL_OUTING_DURATION
        requestedPeriods += period
        failure?.let { throw it }
        return statisticMetric(
            metricType = "TOTAL_OUTING_SECONDS",
            period = period,
            value = 3600.0,
            displayText = "1h"
        )
    }

    override suspend fun getTotalOutingCount(period: StatisticsPeriod): StatisticMetric {
        requestedMetrics += SummaryDetailMetric.TOTAL_OUTING_COUNT
        requestedPeriods += period
        failure?.let { throw it }
        return statisticMetric(
            metricType = "TOTAL_OUTING_COUNT",
            period = period,
            value = 2.0,
            displayText = "2.0\uD68C"
        )
    }

    private fun statisticMetric(
        metricType: String,
        period: StatisticsPeriod,
        value: Double,
        displayText: String
    ): StatisticMetric {
        return StatisticMetric(
            metricType = metricType,
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
}
