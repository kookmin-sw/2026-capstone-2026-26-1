package com.example.passedpath.feature.care.data.remote.mapper

import com.example.passedpath.feature.care.data.remote.dto.CareDayRouteDetailResponseDto
import com.example.passedpath.feature.care.data.remote.dto.CareDependentDayRouteItemDto
import com.example.passedpath.feature.care.data.remote.dto.CareDependentDayRouteListResponseDto
import com.example.passedpath.feature.care.data.remote.dto.CareGpsPointItemDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.OffsetDateTime

class ProtectedPersonDayRouteRemoteMapperTest {

    @Test
    fun `toProtectedPersonDayRouteList maps response metadata and valid items`() {
        val response = CareDependentDayRouteListResponseDto(
            dayRouteCount = 10,
            hasNext = true,
            nextCursorDate = "2026-05-07",
            dayRoutes = listOf(
                CareDependentDayRouteItemDto(
                    date = "2026-05-13",
                    outingTime = "2026-05-13T09:12:00+09:00",
                    enterHomeTime = "2026-05-13T23:15:00+09:00",
                    totalOutingCount = 2
                ),
                CareDependentDayRouteItemDto(
                    date = "invalid-date",
                    outingTime = "2026-05-12T09:12:00+09:00",
                    enterHomeTime = "2026-05-12T23:15:00+09:00",
                    totalOutingCount = 1
                )
            )
        )

        val result = response.toProtectedPersonDayRouteList()

        assertEquals(10, result.dayRouteCount)
        assertEquals(true, result.hasNext)
        assertEquals("2026-05-07", result.nextCursorDate)
        assertEquals(1, result.dayRoutes.size)
        val item = result.dayRoutes.first()
        assertEquals("2026-05-13", item.dateKey)
        assertEquals("2026-05-13T09:12:00+09:00", item.outingTime)
        assertEquals("2026-05-13T23:15:00+09:00", item.enterHomeTime)
        assertEquals(2, item.totalOutingCount)
    }

    @Test
    fun `toProtectedPersonDayRouteList falls back metadata and maps empty list`() {
        val response = CareDependentDayRouteListResponseDto(
            dayRouteCount = null,
            hasNext = null,
            nextCursorDate = "invalid-date",
            dayRoutes = listOf(
                CareDependentDayRouteItemDto(
                    date = "2026-05-13",
                    outingTime = null,
                    enterHomeTime = " ",
                    totalOutingCount = null
                ),
                CareDependentDayRouteItemDto(
                    date = null,
                    outingTime = "2026-05-12T09:12:00+09:00",
                    enterHomeTime = "2026-05-12T23:15:00+09:00",
                    totalOutingCount = 1
                )
            )
        )

        val result = response.toProtectedPersonDayRouteList()

        assertEquals(1, result.dayRouteCount)
        assertEquals(false, result.hasNext)
        assertNull(result.nextCursorDate)
        assertEquals(1, result.dayRoutes.size)
        assertEquals("2026-05-13", result.dayRoutes.first().dateKey)
        assertNull(result.dayRoutes.first().outingTime)
        assertNull(result.dayRoutes.first().enterHomeTime)
        assertEquals(0, result.dayRoutes.first().totalOutingCount)
    }

    @Test
    fun `toProtectedPersonDayRouteDetail maps response fields and recordedAt epoch millis`() {
        val recordedAt = "2026-04-29T13:32:43.059+09:00"
        val response = CareDayRouteDetailResponseDto(
            date = "2026-04-29",
            totalDistance = 3.25,
            gpsPoints = listOf(
                CareGpsPointItemDto(
                    recordedAt = recordedAt,
                    latitude = 37.498095,
                    longitude = 127.02761
                )
            )
        )

        val result = response.toProtectedPersonDayRouteDetail(
            requestedDateKey = "2026-04-30"
        )

        assertEquals("2026-04-29", result.dateKey)
        assertEquals(3.25, result.totalDistanceKm, 0.0)
        assertEquals(1, result.pathPointCount)
        assertEquals(1, result.routePoints.size)
        assertEquals(recordedAt, result.routePoints.first().recordedAt)
        assertEquals(
            OffsetDateTime.parse(recordedAt).toInstant().toEpochMilli(),
            result.routePoints.first().recordedAtEpochMillis
        )
        assertEquals(37.498095, result.routePoints.first().latitude, 0.0)
        assertEquals(127.02761, result.routePoints.first().longitude, 0.0)
    }

    @Test
    fun `toProtectedPersonDayRouteDetail falls back and keeps point when recordedAt is invalid`() {
        val response = CareDayRouteDetailResponseDto(
            date = null,
            totalDistance = null,
            gpsPoints = listOf(
                CareGpsPointItemDto(
                    recordedAt = "invalid-time",
                    latitude = 37.5,
                    longitude = 127.5
                ),
                CareGpsPointItemDto(
                    recordedAt = "2026-04-29T13:32:43.059+09:00",
                    latitude = null,
                    longitude = 127.6
                ),
                CareGpsPointItemDto(
                    recordedAt = "2026-04-29T13:32:43.059+09:00",
                    latitude = 37.6,
                    longitude = null
                )
            )
        )

        val result = response.toProtectedPersonDayRouteDetail(
            requestedDateKey = "2026-01-01"
        )

        assertEquals("2026-01-01", result.dateKey)
        assertEquals(0.0, result.totalDistanceKm, 0.0)
        assertEquals(1, result.pathPointCount)
        assertEquals(1, result.routePoints.size)
        assertEquals("invalid-time", result.routePoints.first().recordedAt)
        assertNull(result.routePoints.first().recordedAtEpochMillis)
    }
}
