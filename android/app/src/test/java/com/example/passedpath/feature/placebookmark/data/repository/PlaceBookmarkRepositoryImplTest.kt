package com.example.passedpath.feature.placebookmark.data.repository

import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.placebookmark.data.remote.api.PlaceBookmarkApi
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkUpdateRequestDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkUpdateResponseDto
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmark
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceBookmarkRepositoryImplTest {

    @Test
    fun `updatePlaceBookmark forwards path and request body then maps response`() = runTest {
        val fakeApi = FakePlaceBookmarkApi()
        val repository = PlaceBookmarkRepositoryImpl(placeBookmarkApi = fakeApi)

        val result = repository.updatePlaceBookmark(
            bookmarkPlaceId = 7L,
            placeBookmark = PlaceBookmark(
                type = BookmarkPlaceType.SCHOOL,
                placeName = "Kookmin University",
                roadAddress = "Seoul Seongbuk-gu 77",
                latitude = 37.6109,
                longitude = 126.997
            )
        )

        assertEquals(7L, fakeApi.receivedBookmarkPlaceId)
        assertEquals(
            PlaceBookmarkUpdateRequestDto(
                type = "SCHOOL",
                placeName = "Kookmin University",
                roadAddress = "Seoul Seongbuk-gu 77",
                latitude = 37.6109,
                longitude = 126.997
            ),
            fakeApi.receivedRequest
        )
        assertEquals(BookmarkPlaceType.SCHOOL, result.type)
        assertEquals("Kookmin University", result.placeName)
        assertEquals("Seoul Seongbuk-gu 77", result.roadAddress)
    }

    private class FakePlaceBookmarkApi : PlaceBookmarkApi {
        var receivedBookmarkPlaceId: Long? = null
        var receivedRequest: PlaceBookmarkUpdateRequestDto? = null

        override suspend fun updatePlaceBookmark(
            bookmarkPlaceId: Long,
            request: PlaceBookmarkUpdateRequestDto
        ): PlaceBookmarkUpdateResponseDto {
            receivedBookmarkPlaceId = bookmarkPlaceId
            receivedRequest = request
            return PlaceBookmarkUpdateResponseDto(
                type = request.type,
                placeName = request.placeName,
                roadAddress = request.roadAddress,
                latitude = request.latitude,
                longitude = request.longitude
            )
        }
    }
}
