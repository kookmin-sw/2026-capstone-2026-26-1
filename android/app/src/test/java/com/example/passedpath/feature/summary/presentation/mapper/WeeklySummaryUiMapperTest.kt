package com.example.passedpath.feature.summary.presentation.mapper

import com.example.passedpath.feature.summary.domain.model.WeeklyCountMetricAverage
import com.example.passedpath.feature.summary.domain.model.WeeklyCountMetricDailyItem
import com.example.passedpath.feature.summary.domain.model.WeeklyCountMetricSection
import com.example.passedpath.feature.summary.domain.model.WeeklyDurationMetricAverage
import com.example.passedpath.feature.summary.domain.model.WeeklyDurationMetricDailyItem
import com.example.passedpath.feature.summary.domain.model.WeeklyDurationMetricSection
import com.example.passedpath.feature.summary.domain.model.WeeklyStatistics
import com.example.passedpath.feature.summary.domain.model.WeeklyTimeMetricAverage
import com.example.passedpath.feature.summary.domain.model.WeeklyTimeMetricDailyItem
import com.example.passedpath.feature.summary.domain.model.WeeklyTimeMetricSection
import com.example.passedpath.feature.summary.domain.model.WeeklyVisitedRegionSummaryItem
import com.example.passedpath.feature.summary.domain.model.WeeklyVisitedRegionsSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklySummaryUiMapperTest {

    @Test
    fun `toWeeklySummaryContentUiState maps averages and chart bars`() {
        val result = weeklyStatistics().toWeeklySummaryContentUiState()

        assertEquals(4, result.metricCards.size)

        val outingTimeCard = result.metricCards[0]
        assertEquals("주간 외출 시간", outingTimeCard.title)
        assertEquals("평균", outingTimeCard.prefixLabel)
        assertEquals("09:12", outingTimeCard.valueText)
        assertEquals(3, outingTimeCard.bars.size)
        assertEquals(0.32f, outingTimeCard.bars[0].ratio, 0.001f)
        assertTrue(outingTimeCard.bars[0].hasData)
        assertEquals(0.18f, outingTimeCard.bars[1].ratio, 0.001f)
        assertFalse(outingTimeCard.bars[1].hasData)
        assertEquals(1f, outingTimeCard.bars[2].ratio, 0.001f)
        assertTrue(outingTimeCard.bars[2].isHighlighted)

        val countCard = result.metricCards[3]
        assertEquals("주간 외출 횟수", countCard.title)
        assertEquals("1.5회", countCard.valueText)
        assertEquals(0.5f, countCard.bars[0].ratio, 0.001f)
        assertEquals(1f, countCard.bars[1].ratio, 0.001f)
    }

    @Test
    fun `toWeeklySummaryContentUiState uses dash and safe empty bars for missing data`() {
        val result = weeklyStatistics(
            outingTime = WeeklyTimeMetricSection(
                average = WeeklyTimeMetricAverage(
                    value = null,
                    displayText = null,
                    sampleSize = 0
                ),
                sevenDays = listOf(
                    WeeklyTimeMetricDailyItem(
                        date = "2026-05-07",
                        hasDayRoute = false,
                        value = null,
                        displayText = null
                    )
                )
            )
        ).toWeeklySummaryContentUiState()

        val outingTimeCard = result.metricCards[0]
        assertEquals("-", outingTimeCard.valueText)
        assertEquals(0.18f, outingTimeCard.bars.first().ratio, 0.001f)
        assertFalse(outingTimeCard.bars.first().hasData)
        assertFalse(outingTimeCard.bars.first().isHighlighted)
    }

    @Test
    fun `toWeeklySummaryContentUiState maps top two visited regions by rank`() {
        val result = weeklyStatistics(
            visitedRegions = WeeklyVisitedRegionsSection(
                topRegions = listOf(
                    WeeklyVisitedRegionSummaryItem(rank = 3, regionName = "종로구"),
                    WeeklyVisitedRegionSummaryItem(rank = 1, regionName = "성북구"),
                    WeeklyVisitedRegionSummaryItem(rank = 2, regionName = "강북구")
                )
            )
        ).toWeeklySummaryContentUiState()

        assertEquals("자주 방문한 동네", result.visitedRegionsCard.title)
        assertEquals(2, result.visitedRegionsCard.regions.size)
        assertEquals("1위", result.visitedRegionsCard.regions[0].rankText)
        assertEquals("성북구", result.visitedRegionsCard.regions[0].regionName)
        assertEquals("2위", result.visitedRegionsCard.regions[1].rankText)
        assertEquals("강북구", result.visitedRegionsCard.regions[1].regionName)
    }

    private fun weeklyStatistics(
        outingTime: WeeklyTimeMetricSection = WeeklyTimeMetricSection(
            average = WeeklyTimeMetricAverage(
                value = 552,
                displayText = "09:12",
                sampleSize = 2
            ),
            sevenDays = listOf(
                WeeklyTimeMetricDailyItem(
                    date = "2026-05-07",
                    hasDayRoute = true,
                    value = 540,
                    displayText = "09:00"
                ),
                WeeklyTimeMetricDailyItem(
                    date = "2026-05-08",
                    hasDayRoute = false,
                    value = 600,
                    displayText = "10:00"
                ),
                WeeklyTimeMetricDailyItem(
                    date = "2026-05-09",
                    hasDayRoute = true,
                    value = 600,
                    displayText = "10:00"
                )
            )
        ),
        visitedRegions: WeeklyVisitedRegionsSection = WeeklyVisitedRegionsSection(
            topRegions = listOf(
                WeeklyVisitedRegionSummaryItem(rank = 1, regionName = "성북구")
            )
        )
    ): WeeklyStatistics {
        return WeeklyStatistics(
            startDate = "2026-05-07",
            endDate = "2026-05-13",
            outingTime = outingTime,
            enterHomeTime = WeeklyTimeMetricSection(
                average = WeeklyTimeMetricAverage(
                    value = 1_395,
                    displayText = "23:15",
                    sampleSize = 2
                ),
                sevenDays = emptyList()
            ),
            totalOutingCount = WeeklyCountMetricSection(
                average = WeeklyCountMetricAverage(
                    value = 1.5,
                    displayText = "1.5회",
                    sampleSize = 2
                ),
                sevenDays = listOf(
                    WeeklyCountMetricDailyItem(
                        date = "2026-05-07",
                        hasDayRoute = true,
                        value = 1.0,
                        displayText = "1회"
                    ),
                    WeeklyCountMetricDailyItem(
                        date = "2026-05-08",
                        hasDayRoute = true,
                        value = 2.0,
                        displayText = "2회"
                    )
                )
            ),
            totalOutingSeconds = WeeklyDurationMetricSection(
                average = WeeklyDurationMetricAverage(
                    value = 19_800.0,
                    displayText = "5시간 30분",
                    sampleSize = 2
                ),
                sevenDays = listOf(
                    WeeklyDurationMetricDailyItem(
                        date = "2026-05-07",
                        hasDayRoute = true,
                        value = 18_000L,
                        displayText = "5시간"
                    )
                )
            ),
            visitedRegions = visitedRegions
        )
    }
}
