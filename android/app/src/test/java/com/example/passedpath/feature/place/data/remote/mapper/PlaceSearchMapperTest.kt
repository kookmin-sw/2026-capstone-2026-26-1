package com.example.passedpath.feature.place.data.remote.mapper

import com.example.passedpath.feature.place.data.remote.dto.PlaceSearchItemDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceSearchResponseDto
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaceSearchMapperTest {

    @Test
    fun `toPlaceSearchResults maps category and address fields`() {
        val response = PlaceSearchResponseDto(
            places = listOf(
                PlaceSearchItemDto(
                    id = "naver-1",
                    name = "Cafe",
                    category = "Food",
                    roadAddress = "Road Address",
                    address = "Jibun Address",
                    latitude = 37.1,
                    longitude = 127.1
                )
            )
        )

        val result = response.toPlaceSearchResults()

        assertEquals(1, result.size)
        assertEquals("naver-1", result.first().id)
        assertEquals("Cafe", result.first().name)
        assertEquals("Food", result.first().category)
        assertEquals("Road Address", result.first().displayAddress)
        assertEquals(37.1, result.first().latitude, 0.0)
        assertEquals(127.1, result.first().longitude, 0.0)
    }

    @Test
    fun `toPlaceSearchResults drops invalid results and falls back to address`() {
        val response = PlaceSearchResponseDto(
            places = listOf(
                PlaceSearchItemDto(
                    id = "valid",
                    name = "Library",
                    category = null,
                    roadAddress = "",
                    address = "Fallback Address",
                    latitude = 37.2,
                    longitude = 127.2
                ),
                PlaceSearchItemDto(
                    id = "missing-coordinate",
                    name = "Broken",
                    category = "Etc",
                    roadAddress = "Road",
                    address = "Address",
                    latitude = null,
                    longitude = 127.3
                )
            )
        )

        val result = response.toPlaceSearchResults()

        assertEquals(1, result.size)
        assertEquals("valid", result.first().id)
        assertEquals("", result.first().category)
        assertEquals("Fallback Address", result.first().displayAddress)
    }
}
