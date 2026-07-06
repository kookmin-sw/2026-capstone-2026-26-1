package com.example.passedpath.feature.bookmark.data.remote.mapper

import com.example.passedpath.feature.bookmark.data.remote.dto.DayRouteBookmarkItemResponseDto
import com.example.passedpath.feature.bookmark.data.remote.dto.DayRouteBookmarkListResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DayRouteBookmarkListRemoteMapperTest {

    @Test
    fun `toDayRouteBookmarkList preserves null title and maps visited regions`() {
        val response = DayRouteBookmarkListResponseDto(
            dayRouteCount = 1,
            hasNext = true,
            nextCursorDate = "2026-05-07",
            dayRoutes = listOf(
                DayRouteBookmarkItemResponseDto(
                    date = "2026-05-09",
                    title = null,
                    visitedRegions = listOf("성북구", "강북구")
                )
            )
        )

        val result = response.toDayRouteBookmarkList()

        assertEquals(1, result.dayRouteCount)
        assertTrue(result.hasNext)
        assertEquals("2026-05-07", result.nextCursorDate)
        assertEquals("2026-05-09", result.dayRoutes.first().date)
        assertNull(result.dayRoutes.first().title)
        assertEquals(listOf("성북구", "강북구"), result.dayRoutes.first().visitedRegions)
    }

    @Test
    fun `toDayRouteBookmarkList maps null visited regions to empty list`() {
        val response = DayRouteBookmarkListResponseDto(
            dayRouteCount = null,
            hasNext = false,
            nextCursorDate = "2026-05-07",
            dayRoutes = listOf(
                DayRouteBookmarkItemResponseDto(
                    date = "2026-05-09",
                    title = "한강 산책",
                    visitedRegions = null
                )
            )
        )

        val result = response.toDayRouteBookmarkList()

        assertEquals(1, result.dayRouteCount)
        assertFalse(result.hasNext)
        assertNull(result.nextCursorDate)
        assertEquals(emptyList<String>(), result.dayRoutes.first().visitedRegions)
    }

    @Test
    fun `toDayRouteBookmarkList drops null or blank date items`() {
        val response = DayRouteBookmarkListResponseDto(
            dayRouteCount = null,
            hasNext = false,
            nextCursorDate = null,
            dayRoutes = listOf(
                DayRouteBookmarkItemResponseDto(
                    date = null,
                    title = "Broken",
                    visitedRegions = emptyList()
                ),
                DayRouteBookmarkItemResponseDto(
                    date = "",
                    title = "Blank",
                    visitedRegions = emptyList()
                ),
                DayRouteBookmarkItemResponseDto(
                    date = "2026-05-08",
                    title = "Valid",
                    visitedRegions = emptyList()
                )
            )
        )

        val result = response.toDayRouteBookmarkList()

        assertEquals(1, result.dayRouteCount)
        assertEquals(1, result.dayRoutes.size)
        assertEquals("2026-05-08", result.dayRoutes.first().date)
    }
}
