package com.example.passedpath.feature.summary.presentation.mapper

import com.example.passedpath.feature.summary.domain.model.DayRouteSummary
import com.example.passedpath.feature.summary.presentation.state.DaySummaryNoDataText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DaySummaryUiMapperTest {

    @Test
    fun `toDaySummaryContentUiState formats offset time without timezone conversion`() {
        val summary = dayRouteSummary(
            outingTime = "2026-04-29T08:10:00+09:00",
            enterHomeTime = "2026-04-29T21:03:00+09:00",
            totalOutingCount = 3,
            totalOutingDurationText = "11\uC2DC\uAC04 51\uBD84",
            visitedDongNames = listOf("\uC815\uB989\uB3D9", "\uC131\uBD81\uB3D9")
        )

        val result = summary.toDaySummaryContentUiState()

        assertEquals("08:10", result.outingTimeText)
        assertEquals("21:03", result.enterHomeTimeText)
        assertEquals("11\uC2DC\uAC04 51\uBD84", result.totalOutingDurationText)
        assertEquals("3\uD68C", result.totalOutingCountText)
        assertEquals(listOf("\uC815\uB989\uB3D9", "\uC131\uBD81\uB3D9"), result.visitedDongNames)
        assertTrue(result.hasOutingTimeData)
        assertTrue(result.hasEnterHomeTimeData)
        assertTrue(result.hasTotalOutingDurationData)
        assertTrue(result.hasTotalOutingCountData)
        assertTrue(result.hasVisitedDongData)
    }

    @Test
    fun `toDaySummaryContentUiState displays no data for missing metric values`() {
        val summary = dayRouteSummary(
            outingTime = null,
            enterHomeTime = "invalid",
            totalOutingCount = null,
            totalOutingSeconds = null,
            totalOutingDurationText = null,
            visitedDongNames = emptyList()
        )

        val result = summary.toDaySummaryContentUiState()

        assertEquals(DaySummaryNoDataText, result.outingTimeText)
        assertEquals(DaySummaryNoDataText, result.enterHomeTimeText)
        assertEquals(DaySummaryNoDataText, result.totalOutingDurationText)
        assertEquals(DaySummaryNoDataText, result.totalOutingCountText)
        assertFalse(result.hasOutingTimeData)
        assertFalse(result.hasEnterHomeTimeData)
        assertFalse(result.hasTotalOutingDurationData)
        assertFalse(result.hasTotalOutingCountData)
        assertFalse(result.hasVisitedDongData)
    }

    @Test
    fun `toDaySummaryContentUiState keeps actual zero values as data`() {
        val summary = dayRouteSummary(
            totalOutingCount = 0,
            totalOutingSeconds = 0L,
            totalOutingDurationText = null
        )

        val result = summary.toDaySummaryContentUiState()

        assertEquals("0\uBD84", result.totalOutingDurationText)
        assertEquals("0\uD68C", result.totalOutingCountText)
        assertTrue(result.hasTotalOutingDurationData)
        assertTrue(result.hasTotalOutingCountData)
    }

    private fun dayRouteSummary(
        outingTime: String? = null,
        enterHomeTime: String? = null,
        totalOutingCount: Int? = 0,
        totalOutingSeconds: Long? = 0L,
        totalOutingDurationText: String? = "0\uBD84",
        visitedDongNames: List<String> = emptyList()
    ): DayRouteSummary {
        return DayRouteSummary(
            dateKey = "2026-04-29",
            outingTime = outingTime,
            enterHomeTime = enterHomeTime,
            totalOutingCount = totalOutingCount,
            totalOutingSeconds = totalOutingSeconds,
            totalOutingDurationText = totalOutingDurationText,
            visitedDongNames = visitedDongNames
        )
    }
}
