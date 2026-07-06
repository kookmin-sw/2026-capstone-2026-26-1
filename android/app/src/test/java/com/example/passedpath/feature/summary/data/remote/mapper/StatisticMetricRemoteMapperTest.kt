package com.example.passedpath.feature.summary.data.remote.mapper

import com.example.passedpath.feature.summary.data.remote.dto.HighlightMetricValueDto
import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricBarItemDto
import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricHighlightDto
import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricResponseDto
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StatisticMetricRemoteMapperTest {

    @Test
    fun `toStatisticMetric maps average bars and highlight`() {
        val response = StatisticMetricResponseDto(
            metricType = "TOTAL_OUTING_SECONDS",
            period = "SIX_MONTHS",
            startDate = "2025-12-01",
            endDate = "2026-05-20",
            average = StatisticMetricAverageDto(
                value = 3600.0,
                displayText = "1h",
                sampleSize = 6
            ),
            bars = listOf(
                StatisticMetricBarItemDto(
                    label = "Jan",
                    startDate = "2026-01-01",
                    endDate = "2026-01-31",
                    value = 0.0,
                    displayText = "0m",
                    hasValue = true,
                    sampleSize = 1
                ),
                StatisticMetricBarItemDto(
                    label = "Feb",
                    startDate = "2026-02-01",
                    endDate = "2026-02-28",
                    value = null,
                    displayText = null,
                    hasValue = false,
                    sampleSize = 0
                )
            ),
            highlight = StatisticMetricHighlightDto(
                title = "This period",
                message = "Shorter",
                current = HighlightMetricValueDto(
                    label = "Current",
                    value = 3000.0,
                    displayText = "50m",
                    sampleSize = 3
                ),
                previous = HighlightMetricValueDto(
                    label = "Previous",
                    value = 6000.0,
                    displayText = "1h 40m",
                    sampleSize = 3
                )
            )
        )

        val result = response.toStatisticMetric()

        assertEquals("TOTAL_OUTING_SECONDS", result.metricType)
        assertEquals(StatisticsPeriod.SIX_MONTHS, result.period)
        assertEquals("2025-12-01", result.startDate)
        assertEquals("2026-05-20", result.endDate)
        assertEquals(3600.0, result.average.value ?: -1.0, 0.0)
        assertEquals("1h", result.average.displayText)
        assertEquals(6, result.average.sampleSize)
        assertEquals(2, result.bars.size)
        assertTrue(result.bars.first().hasValue)
        assertEquals(0.0, result.bars.first().value ?: -1.0, 0.0)
        assertFalse(result.bars.last().hasValue)
        assertNull(result.bars.last().value)
        assertEquals("This period", result.highlight.title)
        assertEquals("Current", result.highlight.current.label)
        assertEquals(3000.0, result.highlight.current.value ?: -1.0, 0.0)
    }

    @Test
    fun `toStatisticMetric preserves missing values separately from zero`() {
        val response = StatisticMetricResponseDto(
            metricType = null,
            period = null,
            startDate = " ",
            endDate = null,
            average = null,
            bars = listOf(
                StatisticMetricBarItemDto(
                    label = "Missing",
                    startDate = null,
                    endDate = null,
                    value = 120.0,
                    displayText = "2m",
                    hasValue = false,
                    sampleSize = -1
                )
            ),
            highlight = StatisticMetricHighlightDto(
                title = null,
                message = null,
                current = HighlightMetricValueDto(
                    label = "Current",
                    value = null,
                    displayText = null,
                    sampleSize = 0
                ),
                previous = null
            )
        )

        val result = response.toStatisticMetric()

        assertEquals(StatisticsPeriod.WEEK, result.period)
        assertTrue(result.metricType.isEmpty())
        assertTrue(result.startDate.isEmpty())
        assertTrue(result.endDate.isEmpty())
        assertNull(result.average.value)
        assertNull(result.average.displayText)
        assertEquals(0, result.average.sampleSize)
        assertFalse(result.bars.first().hasValue)
        assertNull(result.bars.first().value)
        assertNull(result.bars.first().displayText)
        assertEquals(0, result.bars.first().sampleSize)
        assertNull(result.highlight.current.value)
        assertNull(result.highlight.previous.value)
    }
}
