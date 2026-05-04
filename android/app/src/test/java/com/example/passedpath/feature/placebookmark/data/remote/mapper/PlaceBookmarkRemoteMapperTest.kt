package com.example.passedpath.feature.placebookmark.data.remote.mapper

import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkCreateResponseDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkListResponseDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkSummaryResponseDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkUpdateResponseDto
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmark
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaceBookmarkRemoteMapperTest {

    @Test
    fun `toPlaceBookmarkList maps valid items and preserves response count`() {
        val response = PlaceBookmarkListResponseDto(
            placeCount = 2,
            bookmarkPlaces = listOf(
                PlaceBookmarkSummaryResponseDto(
                    placeId = 1L,
                    type = "HOME",
                    placeName = "Home",
                    roadAddress = "Seoul Jung-gu 45",
                    latitude = 37.5665,
                    longitude = 126.978
                ),
                PlaceBookmarkSummaryResponseDto(
                    placeId = 2L,
                    type = "SCHOOL",
                    placeName = "Kookmin University",
                    roadAddress = "Seoul Seongbuk-gu 77",
                    latitude = 37.6109,
                    longitude = 126.997
                )
            )
        )

        val result = response.toPlaceBookmarkList()

        assertEquals(2, result.placeCount)
        assertEquals(2, result.bookmarkPlaces.size)
        assertEquals(1L, result.bookmarkPlaces.first().bookmarkPlaceId)
        assertEquals(BookmarkPlaceType.HOME, result.bookmarkPlaces.first().type)
        assertEquals("Home", result.bookmarkPlaces.first().placeName)
    }

    @Test
    fun `toPlaceBookmarkList drops invalid items and falls back to mapped size when count is null`() {
        val response = PlaceBookmarkListResponseDto(
            placeCount = null,
            bookmarkPlaces = listOf(
                PlaceBookmarkSummaryResponseDto(
                    placeId = 1L,
                    type = "ETC",
                    placeName = "Cafe",
                    roadAddress = "Road",
                    latitude = 37.5,
                    longitude = 127.5
                ),
                PlaceBookmarkSummaryResponseDto(
                    placeId = null,
                    type = "HOME",
                    placeName = "Broken",
                    roadAddress = "Road",
                    latitude = 37.6,
                    longitude = 127.6
                )
            )
        )

        val result = response.toPlaceBookmarkList()

        assertEquals(1, result.placeCount)
        assertEquals(1, result.bookmarkPlaces.size)
        assertEquals(BookmarkPlaceType.ETC, result.bookmarkPlaces.first().type)
    }

    @Test
    fun `toCreateRequestDto serializes bookmark type name`() {
        val placeBookmark = PlaceBookmark(
            type = BookmarkPlaceType.HOME,
            placeName = "Home",
            roadAddress = "Seoul Gangnam-gu 123",
            latitude = 37.498095,
            longitude = 127.02761
        )

        val request = placeBookmark.toCreateRequestDto()

        assertEquals("HOME", request.type)
        assertEquals("Home", request.placeName)
        assertEquals("Seoul Gangnam-gu 123", request.roadAddress)
        assertEquals(37.498095, request.latitude, 0.0)
        assertEquals(127.02761, request.longitude, 0.0)
    }

    @Test
    fun `toUpdateRequestDto serializes bookmark type name`() {
        val placeBookmark = PlaceBookmark(
            type = BookmarkPlaceType.COMPANY,
            placeName = "OpenAI Korea",
            roadAddress = "Seoul Gangnam-gu 123",
            latitude = 37.4979,
            longitude = 127.0276
        )

        val request = placeBookmark.toUpdateRequestDto()

        assertEquals("COMPANY", request.type)
        assertEquals("OpenAI Korea", request.placeName)
        assertEquals("Seoul Gangnam-gu 123", request.roadAddress)
        assertEquals(37.4979, request.latitude, 0.0)
        assertEquals(127.0276, request.longitude, 0.0)
    }

    @Test
    fun `toRegisteredPlaceBookmark maps create response fields`() {
        val response = PlaceBookmarkCreateResponseDto(
            bookmarkPlaceId = 10L,
            type = "ETC",
            placeName = "Cafe",
            roadAddress = "Seoul Mapo-gu 77",
            latitude = 37.5572,
            longitude = 126.9245
        )

        val result = response.toRegisteredPlaceBookmark()

        assertEquals(10L, result.bookmarkPlaceId)
        assertEquals(BookmarkPlaceType.ETC, result.type)
        assertEquals("Cafe", result.placeName)
        assertEquals("Seoul Mapo-gu 77", result.roadAddress)
        assertEquals(37.5572, result.latitude, 0.0)
        assertEquals(126.9245, result.longitude, 0.0)
    }

    @Test
    fun `toPlaceBookmark maps response fields`() {
        val response = PlaceBookmarkUpdateResponseDto(
            type = "HOME",
            placeName = "My Home",
            roadAddress = "Seoul Jung-gu 45",
            latitude = 37.5665,
            longitude = 126.978
        )

        val result = response.toPlaceBookmark()

        assertEquals(BookmarkPlaceType.HOME, result.type)
        assertEquals("My Home", result.placeName)
        assertEquals("Seoul Jung-gu 45", result.roadAddress)
        assertEquals(37.5665, result.latitude, 0.0)
        assertEquals(126.978, result.longitude, 0.0)
    }
}
