package com.example.passedpath.feature.summary.data.remote.mapper

import com.example.passedpath.feature.summary.data.remote.dto.PlaceStatisticsItemDto
import com.example.passedpath.feature.summary.data.remote.dto.PlaceStatisticsSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.VisitStatisticsResponseDto
import com.example.passedpath.feature.summary.data.remote.dto.VisitedRegionStatisticsItemDto
import com.example.passedpath.feature.summary.data.remote.dto.VisitedRegionStatisticsSectionDto
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisitStatisticsRemoteMapperTest {

    @Test
    fun `toVisitStatistics maps visited regions and places`() {
        val response = VisitStatisticsResponseDto(
            period = "MONTH",
            startDate = "2026-04-21",
            endDate = "2026-05-20",
            visitedRegions = VisitedRegionStatisticsSectionDto(
                totalVisitCount = 21,
                items = listOf(
                    VisitedRegionStatisticsItemDto(
                        rank = 1,
                        regionName = " 수유동 ",
                        visitCount = 9,
                        ratio = 42.9,
                        displayRatio = "43%"
                    )
                )
            ),
            places = PlaceStatisticsSectionDto(
                totalVisitCount = 32,
                items = listOf(
                    PlaceStatisticsItemDto(
                        rank = 1,
                        placeName = "스타벅스 수유역점",
                        roadAddress = "서울특별시 성북구 정릉로 77",
                        visitCount = 8,
                        displayVisitCount = "8회"
                    )
                )
            )
        )

        val result = response.toVisitStatistics()

        assertEquals(StatisticsPeriod.MONTH, result.period)
        assertEquals("2026-04-21", result.startDate)
        assertEquals("2026-05-20", result.endDate)
        assertEquals(21, result.visitedRegions.totalVisitCount)
        assertEquals("수유동", result.visitedRegions.items.first().regionName)
        assertEquals(42.9, result.visitedRegions.items.first().ratio, 0.0)
        assertEquals("43%", result.visitedRegions.items.first().displayRatio)
        assertEquals(32, result.places.totalVisitCount)
        assertEquals("스타벅스 수유역점", result.places.items.first().placeName)
        assertEquals("8회", result.places.items.first().displayVisitCount)
    }

    @Test
    fun `toVisitStatistics keeps safe defaults for missing nullable sections`() {
        val response = VisitStatisticsResponseDto(
            period = null,
            startDate = " ",
            endDate = null,
            visitedRegions = null,
            places = PlaceStatisticsSectionDto(
                totalVisitCount = -1,
                items = listOf(
                    PlaceStatisticsItemDto(
                        rank = null,
                        placeName = " ",
                        roadAddress = null,
                        visitCount = -3,
                        displayVisitCount = null
                    )
                )
            )
        )

        val result = response.toVisitStatistics()

        assertEquals(StatisticsPeriod.WEEK, result.period)
        assertTrue(result.startDate.isEmpty())
        assertTrue(result.endDate.isEmpty())
        assertEquals(0, result.visitedRegions.totalVisitCount)
        assertTrue(result.visitedRegions.items.isEmpty())
        assertEquals(0, result.places.totalVisitCount)
        assertTrue(result.places.items.isEmpty())
    }
}
