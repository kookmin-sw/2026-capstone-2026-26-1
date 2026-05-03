package com.example.passedpath.feature.placebookmark.data.repository

import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.placebookmark.data.remote.api.PlaceBookmarkApi
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkUpdateRequestDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkUpdateResponseDto
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmark
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

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

    @Test
    fun `deletePlaceBookmark accepts successful no content response`() = runTest {
        val fakeApi = FakePlaceBookmarkApi(
            deleteResponse = Response.success(204, null as Unit?)
        )
        val repository = PlaceBookmarkRepositoryImpl(placeBookmarkApi = fakeApi)

        repository.deletePlaceBookmark(bookmarkPlaceId = 9L)

        assertEquals(9L, fakeApi.deletedBookmarkPlaceId)
    }

    @Test(expected = HttpException::class)
    fun `deletePlaceBookmark throws when response is unsuccessful`() = runTest {
        val fakeApi = FakePlaceBookmarkApi(
            deleteResponse = Response.error(
                403,
                """{"code":"FORBIDDEN"}""".toResponseBody("application/json".toMediaType())
            )
        )
        val repository = PlaceBookmarkRepositoryImpl(placeBookmarkApi = fakeApi)

        repository.deletePlaceBookmark(bookmarkPlaceId = 11L)
    }

    private class FakePlaceBookmarkApi : PlaceBookmarkApi {
        constructor(
            deleteResponse: Response<Unit> = Response.success(Unit)
        ) {
            this.deleteResponse = deleteResponse
        }

        private var deleteResponse: Response<Unit> = Response.success(Unit)
        var receivedBookmarkPlaceId: Long? = null
        var receivedRequest: PlaceBookmarkUpdateRequestDto? = null
        var deletedBookmarkPlaceId: Long? = null

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

        override suspend fun deletePlaceBookmark(bookmarkPlaceId: Long): Response<Unit> {
            deletedBookmarkPlaceId = bookmarkPlaceId
            return deleteResponse
        }
    }
}
