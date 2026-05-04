package com.example.passedpath.feature.place.data.repository

import com.example.passedpath.feature.place.data.remote.api.PlaceApi
import com.example.passedpath.feature.place.data.remote.dto.BookmarkPlaceUpdateRequestDto
import com.example.passedpath.feature.place.data.remote.dto.BookmarkPlaceUpdateResponseDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceAddRequestDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceAddResponseDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceListResponseDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceReorderRequestDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceUpdateRequestDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceUpdateResponseDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceRepositoryImplTest {

    @Test
    fun `getPlaces returns empty list when server responds with DAY_ROUTE_NOT_FOUND`() = runTest {
        val repository = PlaceRepositoryImpl(
            placeApi = FakePlaceApi(
                throwable = HttpException(
                    Response.error<PlaceListResponseDto>(
                        404,
                        """{"code":"DAY_ROUTE_NOT_FOUND","message":"not found"}"""
                            .toResponseBody("application/json".toMediaType())
                    )
                )
            )
        )

        val result = repository.getPlaces("2026-04-03")

        assertEquals(0, result.placeCount)
        assertTrue(result.places.isEmpty())
    }

    @Test
    fun `deletePlace accepts successful no content response`() = runTest {
        val repository = PlaceRepositoryImpl(
            placeApi = FakePlaceApi(
                deleteResponse = Response.success(204, null as Unit?)
            )
        )

        repository.deletePlace(dateKey = "2026-04-30", placeId = 33L)
    }

    @Test
    fun `reorderPlaces accepts successful no content response`() = runTest {
        val repository = PlaceRepositoryImpl(
            placeApi = FakePlaceApi(
                reorderResponse = Response.success(204, null as Unit?)
            )
        )

        repository.reorderPlaces(dateKey = "2026-05-04", placeIds = listOf(3L, 1L, 2L))
    }

    @Test
    fun `reorderPlaces throws HttpException when server responds with failure`() = runTest {
        val repository = PlaceRepositoryImpl(
            placeApi = FakePlaceApi(
                reorderResponse = Response.error(
                    500,
                    """{"message":"server error"}"""
                        .toResponseBody("application/json".toMediaType())
                )
            )
        )

        try {
            repository.reorderPlaces(dateKey = "2026-05-04", placeIds = listOf(3L, 1L, 2L))
        } catch (exception: HttpException) {
            assertEquals(500, exception.code())
            return@runTest
        }

        error("Expected HttpException")
    }

    private class FakePlaceApi(
        private val throwable: Throwable? = null,
        private val reorderResponse: Response<Unit> = Response.success(Unit),
        private val deleteResponse: Response<Unit> = Response.success(Unit)
    ) : PlaceApi {
        override suspend fun getPlaces(date: String): PlaceListResponseDto {
            throwable?.let { throw it }
            return PlaceListResponseDto(placeCount = 0, places = emptyList())
        }

        override suspend fun addPlace(date: String, request: PlaceAddRequestDto): PlaceAddResponseDto {
            error("Not needed in test")
        }

        override suspend fun updatePlace(
            date: String,
            placeId: Long,
            request: PlaceUpdateRequestDto
        ): PlaceUpdateResponseDto {
            error("Not needed in test")
        }

        override suspend fun reorderPlaces(date: String, request: PlaceReorderRequestDto): Response<Unit> {
            return reorderResponse
        }

        override suspend fun updateBookmarkPlace(
            bookmarkPlaceId: Long,
            request: BookmarkPlaceUpdateRequestDto
        ): BookmarkPlaceUpdateResponseDto {
            error("Not needed in test")
        }

        override suspend fun deletePlace(date: String, placeId: Long): Response<Unit> {
            return deleteResponse
        }
    }
}
