package com.example.passedpath.feature.locationtracking.data.remote.mapper

import com.example.passedpath.feature.locationtracking.data.remote.dto.DayRouteDetailResponseDto
import com.example.passedpath.feature.locationtracking.data.remote.dto.PlaceItemDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DayRouteRemoteMapperTest {

    @Test
    fun `toDayRouteDetail decodes polyline and normalizes nullable fields`() {
        // 테스트로 사용할 입력 데이터 변수 생성
        val dto = DayRouteDetailResponseDto(
            date = null,
            totalDistance = null,
            title = null,
            memo = null,
            isBookmarked = null,
            encodedPath = "_p~iF~ps|U_ulLnnqC_mqNvxq`@",
            pathPointCount = null,
            places = listOf(
                PlaceItemDto(
                    placeId = 2L,
                    placeName = "Second",
                    roadAddress = null,
                    latitude = 38.5,
                    longitude = -120.2,
                    orderIndex = 2
                ),
                PlaceItemDto(
                    placeId = null,
                    placeName = null,
                    roadAddress = null,
                    latitude = 40.7,
                    longitude = -120.95,
                    orderIndex = 1
                ),
                PlaceItemDto(
                    placeId = 3L,
                    placeName = "Ignored",
                    roadAddress = "No coordinate",
                    latitude = null,
                    longitude = 10.0,
                    orderIndex = 3
                )
            )
        )

        val result = dto.toDayRouteDetail(requestedDateKey = "2026-03-29")

        assertEquals("2026-03-29", result.dateKey)
        assertEquals(0.0, result.totalDistanceKm, 0.0)
        assertEquals("", result.title)
        assertEquals("", result.memo)
        assertFalse(result.isBookmarked)
        assertEquals(3, result.polylinePoints.size)
        assertEquals(3, result.pathPointCount)
        assertEquals(38.5, result.polylinePoints[0].latitude, 0.00001)
        assertEquals(-120.2, result.polylinePoints[0].longitude, 0.00001)
        assertEquals(2, result.places.size)
        assertEquals(1, result.places[0].orderIndex)
        assertEquals("", result.places[0].placeName)
        assertEquals(0L, result.places[0].placeId)
        assertTrue(result.places[0].roadAddress.isEmpty())
    }
}
