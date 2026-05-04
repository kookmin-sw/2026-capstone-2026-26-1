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
                    placeName = "Cafe",
                    category = "Food",
                    roadAddress = "Road Address",
                    latitude = 37.1,
                    longitude = 127.1
                )
            )
        )

        val result = response.toPlaceSearchResults()

        assertEquals(1, result.size)
        assertEquals(null, result.first().id)
        assertEquals("Cafe", result.first().name)
        assertEquals("Food", result.first().category)
        assertEquals("Road Address", result.first().displayAddress)
        assertEquals(37.1, result.first().latitude, 0.0)
        assertEquals(127.1, result.first().longitude, 0.0)
    }

    @Test
    fun `toPlaceSearchResults keeps only leaf category`() {
        val response = PlaceSearchResponseDto(
            places = listOf(
                PlaceSearchItemDto(
                    placeName = "Theme Cafe",
                    category = "음식점 > 카페 > 테마카페",
                    roadAddress = "Road Address",
                    latitude = 37.1,
                    longitude = 127.1
                )
            )
        )

        val result = response.toPlaceSearchResults()

        assertEquals("테마카페", result.first().category)
    }

    @Test
    fun `toPlaceSearchResults drops invalid results when road address is missing`() {
        val response = PlaceSearchResponseDto(
            places = listOf(
                PlaceSearchItemDto(
                    placeName = "Library",
                    category = null,
                    roadAddress = "Road Address",
                    latitude = 37.2,
                    longitude = 127.2
                ),
                PlaceSearchItemDto(
                    placeName = "Broken",
                    category = "Etc",
                    roadAddress = "Road",
                    latitude = null,
                    longitude = 127.3
                ),
                PlaceSearchItemDto(
                    placeName = "No Road Address",
                    category = "Etc",
                    roadAddress = "",
                    latitude = 37.3,
                    longitude = 127.3
                )
            )
        )

        val result = response.toPlaceSearchResults()

        assertEquals(1, result.size)
        assertEquals(null, result.first().id)
        assertEquals("", result.first().category)
        assertEquals("Road Address", result.first().displayAddress)
    }
}
