package com.example.passedpath.feature.place.data.remote.mapper

import com.example.passedpath.feature.place.data.remote.dto.PlaceListItemDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceListResponseDto
import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.place.domain.model.PlaceSourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceRemoteMapperTest {

    @Test
    fun `toVisitedPlaceList sorts items by orderIndex and maps source`() {
        val response = PlaceListResponseDto(
            placeCount = 2,
            places = listOf(
                PlaceListItemDto(
                    placeId = 2L,
                    placeName = "Cafe",
                    source = "MANUAL",
                    type = null,
                    roadAddress = "Seoul Forest 2-gil",
                    latitude = 37.5,
                    longitude = 127.5,
                    orderIndex = 2,
                    startTime = null,
                    endTime = null
                ),
                PlaceListItemDto(
                    placeId = 1L,
                    placeName = "Seoul Forest",
                    source = "AUTO",
                    type = "HOME",
                    roadAddress = "Ttukseom-ro",
                    latitude = 37.4,
                    longitude = 127.4,
                    orderIndex = 1,
                    startTime = "2026-04-23T18:26:07.620Z",
                    endTime = "2026-04-23T18:26:07.620Z"
                )
            )
        )

        val result = response.toVisitedPlaceList()

        assertEquals(2, result.placeCount)
        assertEquals(2, result.places.size)
        assertEquals(1L, result.places.first().placeId)
        assertEquals(PlaceSourceType.AUTO, result.places.first().source)
        assertEquals(BookmarkPlaceType.HOME, result.places.first().bookmarkType)
        assertEquals(2L, result.places.last().placeId)
    }

    @Test
    fun `toVisitedPlaceList drops invalid items`() {
        val response = PlaceListResponseDto(
            placeCount = 2,
            places = listOf(
                PlaceListItemDto(
                    placeId = 1L,
                    placeName = "Valid Place",
                    source = "AUTO",
                    type = null,
                    roadAddress = "Road",
                    latitude = 37.4,
                    longitude = 127.4,
                    orderIndex = 1,
                    startTime = null,
                    endTime = null
                ),
                PlaceListItemDto(
                    placeId = null,
                    placeName = "Broken Place",
                    source = "MANUAL",
                    type = null,
                    roadAddress = "Road",
                    latitude = 37.5,
                    longitude = 127.5,
                    orderIndex = 2,
                    startTime = null,
                    endTime = null
                )
            )
        )

        val result = response.toVisitedPlaceList()

        assertEquals(2, result.placeCount)
        assertEquals(1, result.places.size)
        assertTrue(result.places.all { it.placeId == 1L })
    }
}
