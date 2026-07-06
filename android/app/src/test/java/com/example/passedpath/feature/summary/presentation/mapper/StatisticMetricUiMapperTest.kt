package com.example.passedpath.feature.summary.presentation.mapper

import com.example.passedpath.feature.summary.domain.model.HighlightMetricValue
import com.example.passedpath.feature.summary.domain.model.StatisticMetric
import com.example.passedpath.feature.summary.domain.model.StatisticMetricAverage
import com.example.passedpath.feature.summary.domain.model.StatisticMetricBarItem
import com.example.passedpath.feature.summary.domain.model.StatisticMetricHighlight
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import com.example.passedpath.feature.summary.presentation.state.DaySummaryNoDataText
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailMetric
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StatisticMetricUiMapperTest {

    @Test
    fun `toOutingTimeSummaryDetailUiState maps time chart and highlight`() {
        val metric = statisticMetric(
            period = StatisticsPeriod.WEEK,
            average = StatisticMetricAverage(
                value = 552.0,
                displayText = null,
                sampleSize = 5
            ),
            bars = listOf(
                timeBar("Mon", value = 540.0, hasValue = true),
                timeBar("Tue", value = null, hasValue = false),
                timeBar("Wed", value = 570.0, hasValue = true),
                timeBar("Thu", value = 600.0, hasValue = true),
                timeBar("Fri", value = 590.0, hasValue = true),
                timeBar("Sat", value = null, hasValue = false),
                timeBar("Sun", value = 552.0, hasValue = true)
            ),
            highlight = StatisticMetricHighlight(
                title = "Highlight",
                message = "Compared with previous",
                current = HighlightMetricValue(
                    label = "Current",
                    value = 510.0,
                    displayText = null,
                    sampleSize = 5
                ),
                previous = HighlightMetricValue(
                    label = "Previous",
                    value = 552.0,
                    displayText = "09:12",
                    sampleSize = 5
                )
            )
        )

        val result = metric.toOutingTimeSummaryDetailUiState()

        assertEquals(SummaryDetailMetric.OUTING_TIME, result.metric)
        assertEquals(SummaryDetailPeriod.WEEK, result.selectedPeriod)
        assertEquals("2026.05.14 ~ 05.20", result.dateRange.rangeText)
        assertFalse(result.dateRange.canMovePrevious)
        assertFalse(result.dateRange.canMoveNext)
        assertEquals("09:12", result.chart.averageValueText)
        assertTrue(result.chart.hasAverageData)
        assertEquals(listOf("10:00", "09:30", "09:00"), result.chart.yAxisLabels)
        assertEquals(7, result.chart.bars.size)
        assertTrue(result.chart.bars[0].hasData)
        assertEquals(0f, result.chart.bars[0].ratio, 0f)
        assertFalse(result.chart.bars[1].hasData)
        assertEquals(0.5f, result.chart.bars[2].ratio, 0f)
        assertEquals(1f, result.chart.bars[3].ratio, 0f)
        assertTrue(result.chart.bars.all { bar -> bar.showLabel })
        assertEquals("08:30", result.highlights.first().comparisons.first().valueText)
        assertEquals("09:12", result.highlights.first().comparisons.last().valueText)
    }

    @Test
    fun `toOutingTimeSummaryDetailUiState treats zero as valid midnight value`() {
        val metric = statisticMetric(
            period = StatisticsPeriod.WEEK,
            average = StatisticMetricAverage(
                value = 0.0,
                displayText = null,
                sampleSize = 1
            ),
            bars = listOf(timeBar("Mon", value = 0.0, hasValue = true))
        )

        val result = metric.toOutingTimeSummaryDetailUiState()

        assertEquals("00:00", result.chart.averageValueText)
        assertTrue(result.chart.hasAverageData)
        assertTrue(result.chart.bars.first().hasData)
        assertTrue(result.chart.bars.first().isZeroValue)
    }

    @Test
    fun `toOutingTimeSummaryDetailUiState maps missing average and month labels`() {
        val metric = statisticMetric(
            period = StatisticsPeriod.MONTH,
            average = StatisticMetricAverage(
                value = null,
                displayText = null,
                sampleSize = 0
            ),
            bars = (1..30).map { day ->
                timeBar(
                    label = day.toString(),
                    value = null,
                    hasValue = false
                )
            },
            highlight = StatisticMetricHighlight(
                title = "Highlight",
                message = "Not enough data",
                current = HighlightMetricValue(
                    label = "Current",
                    value = null,
                    displayText = null,
                    sampleSize = 0
                ),
                previous = HighlightMetricValue(
                    label = "Previous",
                    value = null,
                    displayText = null,
                    sampleSize = 0
                )
            )
        )

        val result = metric.toOutingTimeSummaryDetailUiState()

        assertEquals(DaySummaryNoDataText, result.chart.averageValueText)
        assertFalse(result.chart.hasAverageData)
        assertEquals(listOf("10:00", "09:30", "09:00"), result.chart.yAxisLabels)
        assertTrue(result.chart.bars.none { bar -> bar.hasData })
        assertEquals(
            listOf(0, 7, 14, 21, 29),
            result.chart.bars.mapIndexedNotNull { index, bar ->
                if (bar.showLabel) index else null
            }
        )
        assertEquals(DaySummaryNoDataText, result.highlights.first().comparisons.first().valueText)
        assertEquals(0f, result.highlights.first().comparisons.first().ratio, 0f)
    }

    @Test
    fun `toEnterHomeTimeSummaryDetailUiState maps time chart and highlight`() {
        val metric = statisticMetric(
            period = StatisticsPeriod.WEEK,
            average = StatisticMetricAverage(
                value = 1395.0,
                displayText = null,
                sampleSize = 5
            ),
            bars = listOf(
                timeBar("Mon", value = 1380.0, hasValue = true),
                timeBar("Tue", value = null, hasValue = false),
                timeBar("Wed", value = 1410.0, hasValue = true),
                timeBar("Thu", value = 1440.0, hasValue = true),
                timeBar("Fri", value = 1420.0, hasValue = true),
                timeBar("Sat", value = null, hasValue = false),
                timeBar("Sun", value = 1395.0, hasValue = true)
            ),
            highlight = StatisticMetricHighlight(
                title = "Highlight",
                message = "Compared with previous",
                current = HighlightMetricValue(
                    label = "Current",
                    value = 1395.0,
                    displayText = null,
                    sampleSize = 5
                ),
                previous = HighlightMetricValue(
                    label = "Previous",
                    value = 1410.0,
                    displayText = "23:30",
                    sampleSize = 5
                )
            )
        )

        val result = metric.toEnterHomeTimeSummaryDetailUiState()

        assertEquals(SummaryDetailMetric.ENTER_HOME_TIME, result.metric)
        assertEquals(SummaryDetailPeriod.WEEK, result.selectedPeriod)
        assertEquals("2026.05.14 ~ 05.20", result.dateRange.rangeText)
        assertFalse(result.dateRange.canMovePrevious)
        assertFalse(result.dateRange.canMoveNext)
        assertEquals("23:15", result.chart.averageValueText)
        assertTrue(result.chart.hasAverageData)
        assertEquals(listOf("24:00", "23:30", "23:00"), result.chart.yAxisLabels)
        assertEquals(7, result.chart.bars.size)
        assertTrue(result.chart.bars[0].hasData)
        assertEquals(0f, result.chart.bars[0].ratio, 0f)
        assertFalse(result.chart.bars[1].hasData)
        assertEquals(0.5f, result.chart.bars[2].ratio, 0f)
        assertEquals(1f, result.chart.bars[3].ratio, 0f)
        assertTrue(result.chart.bars.all { bar -> bar.showLabel })
        assertEquals("23:15", result.highlights.first().comparisons.first().valueText)
        assertEquals("23:30", result.highlights.first().comparisons.last().valueText)
    }

    @Test
    fun `toEnterHomeTimeSummaryDetailUiState treats zero as valid midnight value`() {
        val metric = statisticMetric(
            period = StatisticsPeriod.WEEK,
            average = StatisticMetricAverage(
                value = 0.0,
                displayText = null,
                sampleSize = 1
            ),
            bars = listOf(timeBar("Mon", value = 0.0, hasValue = true))
        )

        val result = metric.toEnterHomeTimeSummaryDetailUiState()

        assertEquals("00:00", result.chart.averageValueText)
        assertTrue(result.chart.hasAverageData)
        assertTrue(result.chart.bars.first().hasData)
        assertTrue(result.chart.bars.first().isZeroValue)
    }

    @Test
    fun `toEnterHomeTimeSummaryDetailUiState maps missing average and month labels`() {
        val metric = statisticMetric(
            period = StatisticsPeriod.MONTH,
            average = StatisticMetricAverage(
                value = null,
                displayText = null,
                sampleSize = 0
            ),
            bars = (1..30).map { day ->
                timeBar(
                    label = day.toString(),
                    value = null,
                    hasValue = false
                )
            },
            highlight = StatisticMetricHighlight(
                title = "Highlight",
                message = "Not enough data",
                current = HighlightMetricValue(
                    label = "Current",
                    value = null,
                    displayText = null,
                    sampleSize = 0
                ),
                previous = HighlightMetricValue(
                    label = "Previous",
                    value = null,
                    displayText = null,
                    sampleSize = 0
                )
            )
        )

        val result = metric.toEnterHomeTimeSummaryDetailUiState()

        assertEquals(DaySummaryNoDataText, result.chart.averageValueText)
        assertFalse(result.chart.hasAverageData)
        assertEquals(listOf("24:00", "23:30", "23:00"), result.chart.yAxisLabels)
        assertTrue(result.chart.bars.none { bar -> bar.hasData })
        assertEquals(
            listOf(0, 7, 14, 21, 29),
            result.chart.bars.mapIndexedNotNull { index, bar ->
                if (bar.showLabel) index else null
            }
        )
        assertEquals(DaySummaryNoDataText, result.highlights.first().comparisons.first().valueText)
        assertEquals(0f, result.highlights.first().comparisons.first().ratio, 0f)
    }

    @Test
    fun `toTotalOutingDurationSummaryDetailUiState maps duration chart and highlight`() {
        val metric = statisticMetric(
            period = StatisticsPeriod.WEEK,
            average = StatisticMetricAverage(
                value = 5400.0,
                displayText = "server average",
                sampleSize = 5
            ),
            bars = listOf(
                bar("Mon", value = 0.0, hasValue = true),
                bar("Tue", value = null, hasValue = false),
                bar("Wed", value = 7200.0, hasValue = true),
                bar("Thu", value = 3600.0, hasValue = true),
                bar("Fri", value = 1800.0, hasValue = true),
                bar("Sat", value = null, hasValue = false),
                bar("Sun", value = 60.0, hasValue = true)
            ),
            highlight = StatisticMetricHighlight(
                title = "Highlight",
                message = "Compared with previous",
                current = HighlightMetricValue(
                    label = "Current",
                    value = 3600.0,
                    displayText = "1h",
                    sampleSize = 5
                ),
                previous = HighlightMetricValue(
                    label = "Previous",
                    value = 7200.0,
                    displayText = "2h",
                    sampleSize = 5
                )
            )
        )

        val result = metric.toTotalOutingDurationSummaryDetailUiState()

        assertEquals(SummaryDetailMetric.TOTAL_OUTING_DURATION, result.metric)
        assertEquals(SummaryDetailPeriod.WEEK, result.selectedPeriod)
        assertEquals("2026.05.14 ~ 05.20", result.dateRange.rangeText)
        assertFalse(result.dateRange.canMovePrevious)
        assertFalse(result.dateRange.canMoveNext)
        assertEquals("server average", result.chart.averageValueText)
        assertTrue(result.chart.hasAverageData)
        assertEquals(listOf("2\uC2DC\uAC04", "1\uC2DC\uAC04", "0\uBD84"), result.chart.yAxisLabels)
        assertEquals(7, result.chart.bars.size)
        assertTrue(result.chart.bars.first().hasData)
        assertTrue(result.chart.bars.first().isZeroValue)
        assertEquals(0f, result.chart.bars.first().ratio, 0f)
        assertFalse(result.chart.bars[1].hasData)
        assertEquals(1f, result.chart.bars[2].ratio, 0f)
        assertTrue(result.chart.bars.all { bar -> bar.showLabel })
        assertEquals("Highlight", result.highlights.first().title)
        assertEquals("Compared with previous", result.highlights.first().description)
        assertEquals(0.5f, result.highlights.first().comparisons.first().ratio, 0f)
        assertEquals(1f, result.highlights.first().comparisons.last().ratio, 0f)
    }

    @Test
    fun `toTotalOutingDurationSummaryDetailUiState keeps month bars and thins labels`() {
        val metric = statisticMetric(
            period = StatisticsPeriod.MONTH,
            average = StatisticMetricAverage(
                value = 1800.0,
                displayText = null,
                sampleSize = 3
            ),
            bars = (1..30).map { day ->
                bar(day.toString(), value = 60.0 * day, hasValue = true)
            }
        )

        val result = metric.toTotalOutingDurationSummaryDetailUiState()

        assertEquals(SummaryDetailPeriod.MONTH, result.selectedPeriod)
        assertEquals(30, result.chart.bars.size)
        assertEquals(
            listOf(0, 7, 14, 21, 29),
            result.chart.bars.mapIndexedNotNull { index, bar ->
                if (bar.showLabel) index else null
            }
        )
        assertEquals("30\uBD84", result.chart.averageValueText)
    }

    @Test
    fun `toTotalOutingDurationSummaryDetailUiState maps missing average and bars as no data`() {
        val metric = statisticMetric(
            period = StatisticsPeriod.YEAR,
            average = StatisticMetricAverage(
                value = null,
                displayText = null,
                sampleSize = 0
            ),
            bars = listOf(
                bar("Jan", value = null, hasValue = false),
                bar("Feb", value = null, hasValue = false)
            ),
            highlight = StatisticMetricHighlight(
                title = "Highlight",
                message = "Not enough data",
                current = HighlightMetricValue(
                    label = "Current",
                    value = null,
                    displayText = null,
                    sampleSize = 0
                ),
                previous = HighlightMetricValue(
                    label = "Previous",
                    value = null,
                    displayText = null,
                    sampleSize = 0
                )
            )
        )

        val result = metric.toTotalOutingDurationSummaryDetailUiState()

        assertEquals(DaySummaryNoDataText, result.chart.averageValueText)
        assertFalse(result.chart.hasAverageData)
        assertEquals(listOf("1\uC2DC\uAC04", "30\uBD84", "0\uBD84"), result.chart.yAxisLabels)
        assertTrue(result.chart.bars.none { bar -> bar.hasData })
        assertEquals(DaySummaryNoDataText, result.highlights.first().comparisons.first().valueText)
        assertEquals(0f, result.highlights.first().comparisons.first().ratio, 0f)
    }

    @Test
    fun `toTotalOutingCountSummaryDetailUiState maps count chart and highlight`() {
        val metric = statisticMetric(
            period = StatisticsPeriod.WEEK,
            average = StatisticMetricAverage(
                value = 1.3,
                displayText = null,
                sampleSize = 5
            ),
            bars = listOf(
                countBar("Mon", value = 0.0, hasValue = true),
                countBar("Tue", value = null, hasValue = false),
                countBar("Wed", value = 1.0, hasValue = true),
                countBar("Thu", value = 2.0, hasValue = true),
                countBar("Fri", value = 0.5, hasValue = true),
                countBar("Sat", value = null, hasValue = false),
                countBar("Sun", value = 1.5, hasValue = true)
            ),
            highlight = StatisticMetricHighlight(
                title = "Highlight",
                message = "Compared with previous",
                current = HighlightMetricValue(
                    label = "Current",
                    value = 0.0,
                    displayText = null,
                    sampleSize = 5
                ),
                previous = HighlightMetricValue(
                    label = "Previous",
                    value = 2.0,
                    displayText = "2.0\uD68C",
                    sampleSize = 5
                )
            )
        )

        val result = metric.toTotalOutingCountSummaryDetailUiState()

        assertEquals(SummaryDetailMetric.TOTAL_OUTING_COUNT, result.metric)
        assertEquals(SummaryDetailPeriod.WEEK, result.selectedPeriod)
        assertEquals("2026.05.14 ~ 05.20", result.dateRange.rangeText)
        assertFalse(result.dateRange.canMovePrevious)
        assertFalse(result.dateRange.canMoveNext)
        assertEquals("1.3\uD68C", result.chart.averageValueText)
        assertTrue(result.chart.hasAverageData)
        assertEquals(listOf("2", "1", "0"), result.chart.yAxisLabels)
        assertEquals(7, result.chart.bars.size)
        assertTrue(result.chart.bars.first().hasData)
        assertTrue(result.chart.bars.first().isZeroValue)
        assertEquals(0f, result.chart.bars.first().ratio, 0f)
        assertFalse(result.chart.bars[1].hasData)
        assertEquals(1f, result.chart.bars[3].ratio, 0f)
        assertTrue(result.chart.bars.all { bar -> bar.showLabel })
        assertEquals("0.0\uD68C", result.highlights.first().comparisons.first().valueText)
        assertEquals(0f, result.highlights.first().comparisons.first().ratio, 0f)
        assertEquals("2.0\uD68C", result.highlights.first().comparisons.last().valueText)
        assertEquals(1f, result.highlights.first().comparisons.last().ratio, 0f)
    }

    @Test
    fun `toTotalOutingCountSummaryDetailUiState maps missing average and month labels`() {
        val metric = statisticMetric(
            period = StatisticsPeriod.MONTH,
            average = StatisticMetricAverage(
                value = null,
                displayText = null,
                sampleSize = 0
            ),
            bars = (1..30).map { day ->
                countBar(
                    label = day.toString(),
                    value = null,
                    hasValue = false
                )
            },
            highlight = StatisticMetricHighlight(
                title = "Highlight",
                message = "Not enough data",
                current = HighlightMetricValue(
                    label = "Current",
                    value = null,
                    displayText = null,
                    sampleSize = 0
                ),
                previous = HighlightMetricValue(
                    label = "Previous",
                    value = null,
                    displayText = null,
                    sampleSize = 0
                )
            )
        )

        val result = metric.toTotalOutingCountSummaryDetailUiState()

        assertEquals(DaySummaryNoDataText, result.chart.averageValueText)
        assertFalse(result.chart.hasAverageData)
        assertEquals(listOf("1", "0.5", "0"), result.chart.yAxisLabels)
        assertTrue(result.chart.bars.none { bar -> bar.hasData })
        assertEquals(
            listOf(0, 7, 14, 21, 29),
            result.chart.bars.mapIndexedNotNull { index, bar ->
                if (bar.showLabel) index else null
            }
        )
        assertEquals(DaySummaryNoDataText, result.highlights.first().comparisons.first().valueText)
        assertEquals(0f, result.highlights.first().comparisons.first().ratio, 0f)
    }

    private fun statisticMetric(
        period: StatisticsPeriod,
        average: StatisticMetricAverage,
        bars: List<StatisticMetricBarItem>,
        highlight: StatisticMetricHighlight = StatisticMetricHighlight(
            title = "Highlight",
            message = "Message",
            current = HighlightMetricValue("Current", 0.0, "0m", 1),
            previous = HighlightMetricValue("Previous", 0.0, "0m", 1)
        )
    ): StatisticMetric {
        return StatisticMetric(
            metricType = "TOTAL_OUTING_SECONDS",
            period = period,
            startDate = "2026-05-14",
            endDate = "2026-05-20",
            average = average,
            bars = bars,
            highlight = highlight
        )
    }

    private fun bar(
        label: String,
        value: Double?,
        hasValue: Boolean
    ): StatisticMetricBarItem {
        return StatisticMetricBarItem(
            label = label,
            startDate = "2026-05-20",
            endDate = "2026-05-20",
            value = value,
            displayText = value?.toLong()?.toString(),
            hasValue = hasValue,
            sampleSize = if (hasValue) 1 else 0
        )
    }

    private fun timeBar(
        label: String,
        value: Double?,
        hasValue: Boolean,
        displayText: String? = null
    ): StatisticMetricBarItem {
        return StatisticMetricBarItem(
            label = label,
            startDate = "2026-05-20",
            endDate = "2026-05-20",
            value = value,
            displayText = displayText,
            hasValue = hasValue,
            sampleSize = if (hasValue) 1 else 0
        )
    }

    private fun countBar(
        label: String,
        value: Double?,
        hasValue: Boolean,
        displayText: String? = null
    ): StatisticMetricBarItem {
        return StatisticMetricBarItem(
            label = label,
            startDate = "2026-05-20",
            endDate = "2026-05-20",
            value = value,
            displayText = displayText,
            hasValue = hasValue,
            sampleSize = if (hasValue) 1 else 0
        )
    }
}
