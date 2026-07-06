package com.example.passedpath.feature.care.data.remote.mapper

import com.example.passedpath.feature.care.data.remote.dto.CareDayRouteSummaryResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtectedPersonDaySummaryRemoteMapperTest {

    @Test
    fun `toProtectedPersonDaySummary maps response fields`() {
        val response = CareDayRouteSummaryResponseDto(
            outingTime = "2026-04-29T08:10:00+09:00",
            enterHomeTime = "2026-04-29T18:40:00+09:00",
            totalOutingCount = 2,
            totalOutingSeconds = 36_120L,
            totalOutingDurationText = "10\uC2DC\uAC04 2\uBD84",
            visitedDongName = listOf("\uC815\uB989\uB3D9", "\uC131\uBD81\uB3D9")
        )

        val result = response.toProtectedPersonDaySummary(dateKey = "2026-04-29")

        assertEquals("2026-04-29", result.dateKey)
        assertEquals("2026-04-29T08:10:00+09:00", result.outingTime)
        assertEquals("2026-04-29T18:40:00+09:00", result.enterHomeTime)
        assertEquals(2, result.totalOutingCount)
        assertEquals(36_120L, result.totalOutingSeconds)
        assertEquals("10\uC2DC\uAC04 2\uBD84", result.totalOutingDurationText)
        assertEquals(listOf("\uC815\uB989\uB3D9", "\uC131\uBD81\uB3D9"), result.visitedDongNames)
    }

    @Test
    fun `toProtectedPersonDaySummary falls back null and blank fields safely`() {
        val response = CareDayRouteSummaryResponseDto(
            outingTime = " ",
            enterHomeTime = "",
            totalOutingCount = null,
            totalOutingSeconds = 7_500L,
            totalOutingDurationText = " ",
            visitedDongName = listOf(" ", "\uD61C\uD654\uB3D9", "")
        )

        val result = response.toProtectedPersonDaySummary(dateKey = "2026-04-30")

        assertEquals("2026-04-30", result.dateKey)
        assertNull(result.outingTime)
        assertNull(result.enterHomeTime)
        assertEquals(0, result.totalOutingCount)
        assertEquals(7_500L, result.totalOutingSeconds)
        assertEquals("2\uC2DC\uAC04 5\uBD84", result.totalOutingDurationText)
        assertEquals(listOf("\uD61C\uD654\uB3D9"), result.visitedDongNames)
    }

    @Test
    fun `toProtectedPersonDaySummary uses zero minute fallback for null seconds`() {
        val response = CareDayRouteSummaryResponseDto(
            outingTime = null,
            enterHomeTime = null,
            totalOutingCount = null,
            totalOutingSeconds = null,
            totalOutingDurationText = null,
            visitedDongName = null
        )

        val result = response.toProtectedPersonDaySummary(dateKey = "2026-05-01")

        assertEquals(0L, result.totalOutingSeconds)
        assertEquals("0\uBD84", result.totalOutingDurationText)
        assertTrue(result.visitedDongNames.isEmpty())
    }
}
