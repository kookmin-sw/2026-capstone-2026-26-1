package com.example.passedpath.feature.summary.data.remote.mapper

import com.example.passedpath.feature.summary.data.remote.dto.DayRouteSummaryResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DayRouteSummaryRemoteMapperTest {

    @Test
    fun `toDayRouteSummary preserves KST offset time strings and maps numeric fields`() {
        val response = DayRouteSummaryResponseDto(
            outingTime = "2026-04-29T08:10:00+09:00",
            enterHomeTime = "2026-04-29T18:40:00+09:00",
            totalOutingCount = 2,
            totalOutingSeconds = 36_120L,
            totalOutingDurationText = "10\uC2DC\uAC04 2\uBD84",
            visitedDongName = listOf("\uC815\uB989\uB3D9", "\uC131\uBD81\uB3D9")
        )

        val result = response.toDayRouteSummary(dateKey = "2026-04-29")

        assertEquals("2026-04-29", result.dateKey)
        assertEquals("2026-04-29T08:10:00+09:00", result.outingTime)
        assertEquals("2026-04-29T18:40:00+09:00", result.enterHomeTime)
        assertEquals(2, result.totalOutingCount)
        assertEquals(36_120L, result.totalOutingSeconds)
        assertEquals("10\uC2DC\uAC04 2\uBD84", result.totalOutingDurationText)
        assertEquals(listOf("\uC815\uB989\uB3D9", "\uC131\uBD81\uB3D9"), result.visitedDongNames)
    }

    @Test
    fun `toDayRouteSummary preserves null missing metric values`() {
        val response = DayRouteSummaryResponseDto(
            outingTime = null,
            enterHomeTime = " ",
            totalOutingCount = null,
            totalOutingSeconds = null,
            totalOutingDurationText = null,
            visitedDongName = listOf(" ", "")
        )

        val result = response.toDayRouteSummary(dateKey = "2026-04-30")

        assertNull(result.outingTime)
        assertNull(result.enterHomeTime)
        assertNull(result.totalOutingCount)
        assertNull(result.totalOutingSeconds)
        assertNull(result.totalOutingDurationText)
        assertTrue(result.visitedDongNames.isEmpty())
    }

    @Test
    fun `toDayRouteSummary derives duration text from seconds when server text is blank`() {
        val response = DayRouteSummaryResponseDto(
            outingTime = null,
            enterHomeTime = null,
            totalOutingCount = 1,
            totalOutingSeconds = 3_660L,
            totalOutingDurationText = ""
        )

        val result = response.toDayRouteSummary(dateKey = "2026-05-01")

        assertEquals("1\uC2DC\uAC04 1\uBD84", result.totalOutingDurationText)
    }

    @Test
    fun `toDayRouteSummary derives zero duration text when seconds value is zero`() {
        val response = DayRouteSummaryResponseDto(
            outingTime = null,
            enterHomeTime = null,
            totalOutingCount = 0,
            totalOutingSeconds = 0L,
            totalOutingDurationText = null
        )

        val result = response.toDayRouteSummary(dateKey = "2026-05-01")

        assertEquals(0, result.totalOutingCount)
        assertEquals(0L, result.totalOutingSeconds)
        assertEquals("0\uBD84", result.totalOutingDurationText)
    }
}
