package com.example.passedpath.feature.placebookmark.data.remote.mapper

import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkUpdateResponseDto
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmark
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaceBookmarkRemoteMapperTest {

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
