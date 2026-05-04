package com.example.passedpath.feature.placebookmark.data.repository

import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.placebookmark.data.remote.api.PlaceBookmarkApi
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkCreateRequestDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkCreateResponseDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkListResponseDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkSummaryResponseDto
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
    fun `getPlaceBookmarks fetches and maps bookmark list`() = runTest {
        val fakeApi = FakePlaceBookmarkApi(
            listResponse = PlaceBookmarkListResponseDto(
                placeCount = 1,
                bookmarkPlaces = listOf(
                    PlaceBookmarkSummaryResponseDto(
                        placeId = 5L,
                        type = "COMPANY",
                        placeName = "Office",
                        roadAddress = "Seoul Gangnam-gu 123",
                        latitude = 37.4979,
                        longitude = 127.0276
                    )
                )
            )
        )
        val repository = PlaceBookmarkRepositoryImpl(placeBookmarkApi = fakeApi)

        val result = repository.getPlaceBookmarks()

        assertEquals(1, result.placeCount)
        assertEquals(1, result.bookmarkPlaces.size)
        assertEquals(5L, result.bookmarkPlaces.first().bookmarkPlaceId)
        assertEquals(BookmarkPlaceType.COMPANY, result.bookmarkPlaces.first().type)
        assertEquals("Office", result.bookmarkPlaces.first().placeName)
    }

    @Test
    fun `createPlaceBookmark forwards request body then maps created response`() = runTest {
        val fakeApi = FakePlaceBookmarkApi()
        val repository = PlaceBookmarkRepositoryImpl(placeBookmarkApi = fakeApi)

        val result = repository.createPlaceBookmark(
            placeBookmark = PlaceBookmark(
                type = BookmarkPlaceType.HOME,
                placeName = "Home",
                roadAddress = "Seoul Gangnam-gu 123",
                latitude = 37.498095,
                longitude = 127.02761
            )
        )

        assertEquals(
            PlaceBookmarkCreateRequestDto(
                type = "HOME",
                placeName = "Home",
                roadAddress = "Seoul Gangnam-gu 123",
                latitude = 37.498095,
                longitude = 127.02761
            ),
            fakeApi.receivedCreateRequest
        )
        assertEquals(21L, result.bookmarkPlaceId)
        assertEquals(BookmarkPlaceType.HOME, result.type)
        assertEquals("Home", result.placeName)
        assertEquals("Seoul Gangnam-gu 123", result.roadAddress)
    }

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
            listResponse: PlaceBookmarkListResponseDto = PlaceBookmarkListResponseDto(
                placeCount = 0,
                bookmarkPlaces = emptyList()
            ),
            deleteResponse: Response<Unit> = Response.success(Unit)
        ) {
            this.listResponse = listResponse
            this.deleteResponse = deleteResponse
        }

        private var listResponse: PlaceBookmarkListResponseDto = PlaceBookmarkListResponseDto(
            placeCount = 0,
            bookmarkPlaces = emptyList()
        )
        private var deleteResponse: Response<Unit> = Response.success(Unit)
        var receivedCreateRequest: PlaceBookmarkCreateRequestDto? = null
        var receivedBookmarkPlaceId: Long? = null
        var receivedRequest: PlaceBookmarkUpdateRequestDto? = null
        var deletedBookmarkPlaceId: Long? = null

        override suspend fun getPlaceBookmarks(): PlaceBookmarkListResponseDto {
            return listResponse
        }

        override suspend fun createPlaceBookmark(
            request: PlaceBookmarkCreateRequestDto
        ): PlaceBookmarkCreateResponseDto {
            receivedCreateRequest = request
            return PlaceBookmarkCreateResponseDto(
                bookmarkPlaceId = 21L,
                type = request.type,
                placeName = request.placeName,
                roadAddress = request.roadAddress,
                latitude = request.latitude,
                longitude = request.longitude
            )
        }

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
