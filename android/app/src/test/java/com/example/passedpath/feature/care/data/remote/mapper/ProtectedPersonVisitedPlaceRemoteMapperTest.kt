package com.example.passedpath.feature.care.data.remote.mapper

import com.example.passedpath.feature.care.data.remote.dto.CarePlaceItemDto
import com.example.passedpath.feature.care.data.remote.dto.CarePlaceListResponseDto
import com.example.passedpath.feature.care.domain.model.ProtectedPersonBookmarkPlaceType
import com.example.passedpath.feature.care.domain.model.ProtectedPersonPlaceSourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtectedPersonVisitedPlaceRemoteMapperTest {

    @Test
    fun `toProtectedPersonVisitedPlaceList maps and sorts response fields`() {
        val response = CarePlaceListResponseDto(
            placeCount = 2,
            places = listOf(
                carePlaceItem(
                    placeId = 2L,
                    placeName = "Cafe",
                    source = "MANUAL",
                    type = null,
                    roadAddress = "Seoul Jongno 2",
                    latitude = 37.2,
                    longitude = 127.2,
                    orderIndex = 2,
                    startTime = null,
                    endTime = null
                ),
                carePlaceItem(
                    placeId = 1L,
                    placeName = "Library",
                    source = "AUTO",
                    type = "SCHOOL",
                    roadAddress = "Seoul Jongno 1",
                    latitude = 37.1,
                    longitude = 127.1,
                    orderIndex = 1,
                    startTime = "2026-04-29T13:32:43.059+09:00",
                    endTime = "2026-04-29T14:32:43.059+09:00"
                )
            )
        )

        val result = response.toProtectedPersonVisitedPlaceList()

        assertEquals(2, result.placeCount)
        assertEquals(2, result.places.size)
        val firstPlace = result.places.first()
        assertEquals(1L, firstPlace.placeId)
        assertEquals("Library", firstPlace.placeName)
        assertEquals(ProtectedPersonPlaceSourceType.AUTO, firstPlace.source)
        assertEquals(ProtectedPersonBookmarkPlaceType.SCHOOL, firstPlace.bookmarkType)
        assertEquals("Seoul Jongno 1", firstPlace.roadAddress)
        assertEquals(37.1, firstPlace.latitude, 0.0)
        assertEquals(127.1, firstPlace.longitude, 0.0)
        assertEquals(1, firstPlace.orderIndex)
        assertEquals("2026-04-29T13:32:43.059+09:00", firstPlace.startTime)
        assertEquals("2026-04-29T14:32:43.059+09:00", firstPlace.endTime)
    }

    @Test
    fun `toProtectedPersonVisitedPlaceList keeps empty places as empty list`() {
        val response = CarePlaceListResponseDto(
            placeCount = 0,
            places = emptyList()
        )

        val result = response.toProtectedPersonVisitedPlaceList()

        assertEquals(0, result.placeCount)
        assertTrue(result.places.isEmpty())
    }

    @Test
    fun `toProtectedPersonVisitedPlaceList filters invalid items and falls back count`() {
        val response = CarePlaceListResponseDto(
            placeCount = null,
            places = listOf(
                carePlaceItem(placeId = null),
                carePlaceItem(source = "UNKNOWN"),
                carePlaceItem(latitude = null),
                carePlaceItem(type = "UNKNOWN")
            )
        )

        val result = response.toProtectedPersonVisitedPlaceList()

        assertEquals(1, result.placeCount)
        assertEquals(1, result.places.size)
        assertEquals(10L, result.places.first().placeId)
        assertNull(result.places.first().bookmarkType)
    }

    private companion object {
        fun carePlaceItem(
            placeId: Long? = 10L,
            placeName: String? = "Place",
            source: String? = "AUTO",
            type: String? = "HOME",
            roadAddress: String? = "Road",
            latitude: Double? = 37.0,
            longitude: Double? = 127.0,
            orderIndex: Int? = 1,
            startTime: String? = "2026-04-29T09:00:00+09:00",
            endTime: String? = "2026-04-29T10:00:00+09:00"
        ): CarePlaceItemDto {
            return CarePlaceItemDto(
                placeId = placeId,
                placeName = placeName,
                source = source,
                type = type,
                roadAddress = roadAddress,
                latitude = latitude,
                longitude = longitude,
                orderIndex = orderIndex,
                startTime = startTime,
                endTime = endTime
            )
        }
    }
}
