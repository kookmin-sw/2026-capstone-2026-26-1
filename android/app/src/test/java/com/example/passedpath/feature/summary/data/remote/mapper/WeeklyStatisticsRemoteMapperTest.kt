package com.example.passedpath.feature.summary.data.remote.mapper

import com.example.passedpath.feature.summary.data.remote.dto.WeeklyCountMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyCountMetricDailyItemDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyCountMetricSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyDurationMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyDurationMetricDailyItemDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyDurationMetricSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyStatisticsResponseDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyTimeMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyTimeMetricDailyItemDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyTimeMetricSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyVisitedRegionSummaryItemDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyVisitedRegionsSectionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklyStatisticsRemoteMapperTest {

    @Test
    fun `toWeeklyStatistics maps weekly metric sections and visited regions`() {
        val response = WeeklyStatisticsResponseDto(
            startDate = "2026-05-07",
            endDate = "2026-05-13",
            outingTime = WeeklyTimeMetricSectionDto(
                average = WeeklyTimeMetricAverageDto(
                    value = 552,
                    displayText = "09:12",
                    sampleSize = 5
                ),
                sevenDays = listOf(
                    WeeklyTimeMetricDailyItemDto(
                        date = "2026-05-07",
                        hasDayRoute = true,
                        value = 540,
                        displayText = "09:00"
                    ),
                    WeeklyTimeMetricDailyItemDto(
                        date = "2026-05-08",
                        hasDayRoute = false,
                        value = 600,
                        displayText = "10:00"
                    )
                )
            ),
            enterHomeTime = WeeklyTimeMetricSectionDto(
                average = WeeklyTimeMetricAverageDto(
                    value = 1_120,
                    displayText = "18:40",
                    sampleSize = 5
                ),
                sevenDays = listOf(
                    WeeklyTimeMetricDailyItemDto(
                        date = "2026-05-07",
                        hasDayRoute = true,
                        value = 1_100,
                        displayText = "18:20"
                    )
                )
            ),
            totalOutingCount = WeeklyCountMetricSectionDto(
                average = WeeklyCountMetricAverageDto(
                    value = 1.6,
                    displayText = "1.6\uD68C",
                    sampleSize = 5
                ),
                sevenDays = listOf(
                    WeeklyCountMetricDailyItemDto(
                        date = "2026-05-07",
                        hasDayRoute = true,
                        value = 2.0,
                        displayText = "2\uD68C"
                    )
                )
            ),
            totalOutingSeconds = WeeklyDurationMetricSectionDto(
                average = WeeklyDurationMetricAverageDto(
                    value = 19_800.5,
                    displayText = "5\uC2DC\uAC04 30\uBD84",
                    sampleSize = 5
                ),
                sevenDays = listOf(
                    WeeklyDurationMetricDailyItemDto(
                        date = "2026-05-07",
                        hasDayRoute = true,
                        value = 18_000L,
                        displayText = "5\uC2DC\uAC04"
                    )
                )
            ),
            visitedRegions = WeeklyVisitedRegionsSectionDto(
                topRegions = listOf(
                    WeeklyVisitedRegionSummaryItemDto(
                        rank = 1,
                        regionName = " \uC131\uBD81\uAD6C "
                    ),
                    WeeklyVisitedRegionSummaryItemDto(
                        rank = 2,
                        regionName = "\uC885\uB85C\uAD6C"
                    )
                )
            )
        )

        val result = response.toWeeklyStatistics()

        assertEquals("2026-05-07", result.startDate)
        assertEquals("2026-05-13", result.endDate)
        assertEquals(552, result.outingTime.average.value)
        assertEquals("09:12", result.outingTime.average.displayText)
        assertEquals(5, result.outingTime.average.sampleSize)
        assertEquals("2026-05-07", result.outingTime.sevenDays.first().date)
        assertEquals(540, result.outingTime.sevenDays.first().value)

        val missingDay = result.outingTime.sevenDays[1]
        assertFalse(missingDay.hasDayRoute)
        assertNull(missingDay.value)
        assertNull(missingDay.displayText)

        assertEquals(1_120, result.enterHomeTime.average.value)
        assertEquals(1.6, result.totalOutingCount.average.value ?: -1.0, 0.0)
        assertEquals(19_800.5, result.totalOutingSeconds.average.value ?: -1.0, 0.0)
        assertEquals(18_000L, result.totalOutingSeconds.sevenDays.first().value)
        assertEquals("\uC131\uBD81\uAD6C", result.visitedRegions.topRegions.first().regionName)
        assertEquals(2, result.visitedRegions.topRegions.size)
    }

    @Test
    fun `toWeeklyStatistics keeps empty defaults for missing nullable sections`() {
        val response = WeeklyStatisticsResponseDto(
            startDate = " ",
            endDate = null,
            outingTime = null,
            enterHomeTime = null,
            totalOutingCount = null,
            totalOutingSeconds = null,
            visitedRegions = WeeklyVisitedRegionsSectionDto(
                topRegions = listOf(
                    WeeklyVisitedRegionSummaryItemDto(
                        rank = null,
                        regionName = " "
                    )
                )
            )
        )

        val result = response.toWeeklyStatistics()

        assertTrue(result.startDate.isEmpty())
        assertTrue(result.endDate.isEmpty())
        assertNull(result.outingTime.average.value)
        assertNull(result.outingTime.average.displayText)
        assertEquals(0, result.outingTime.average.sampleSize)
        assertTrue(result.outingTime.sevenDays.isEmpty())
        assertNull(result.totalOutingCount.average.value)
        assertTrue(result.visitedRegions.topRegions.isEmpty())
    }
}
