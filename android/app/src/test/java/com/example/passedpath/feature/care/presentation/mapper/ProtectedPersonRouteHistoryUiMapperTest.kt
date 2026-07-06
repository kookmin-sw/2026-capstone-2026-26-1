package com.example.passedpath.feature.care.presentation.mapper

import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteListItem
import org.junit.Assert.assertEquals
import org.junit.Test

class ProtectedPersonRouteHistoryUiMapperTest {

    @Test
    fun `route item maps to date card ui state`() {
        val item = ProtectedPersonDayRouteListItem(
            dateKey = "2026-04-03",
            outingTime = "2026-04-03T09:12:00+09:00",
            enterHomeTime = "2026-04-03T23:40:00+09:00",
            totalOutingCount = 3
        )

        val uiState = item.toProtectedPersonRouteDateUiState()

        assertEquals("2026-04-03", uiState.dateKey)
        assertEquals("4\uC6D4 3\uC77C", uiState.dateText)
        assertEquals("09:12", uiState.outingTimeText)
        assertEquals("23:40", uiState.enterHomeTimeText)
        assertEquals("3\uD68C", uiState.outingCountText)
    }

    @Test
    fun `invalid or missing time maps to empty value`() {
        val item = ProtectedPersonDayRouteListItem(
            dateKey = "2026-03-20",
            outingTime = null,
            enterHomeTime = "not-a-time",
            totalOutingCount = 1
        )

        val uiState = item.toProtectedPersonRouteDateUiState()

        assertEquals("-", uiState.outingTimeText)
        assertEquals("-", uiState.enterHomeTimeText)
    }

    @Test
    fun `invalid date keeps original date key text`() {
        val item = ProtectedPersonDayRouteListItem(
            dateKey = "invalid-date",
            outingTime = "2026-03-20T09:12:00+09:00",
            enterHomeTime = "2026-03-20T23:40:00+09:00",
            totalOutingCount = 1
        )

        val uiState = item.toProtectedPersonRouteDateUiState()

        assertEquals("invalid-date", uiState.dateText)
    }
}
