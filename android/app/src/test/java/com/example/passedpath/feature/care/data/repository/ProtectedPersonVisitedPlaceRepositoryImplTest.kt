package com.example.passedpath.feature.care.data.repository

import com.example.passedpath.feature.care.data.remote.api.CareDependentDayRoutePlacesApi
import com.example.passedpath.feature.care.data.remote.dto.CarePlaceItemDto
import com.example.passedpath.feature.care.data.remote.dto.CarePlaceListResponseDto
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonVisitedPlaceResult
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
class ProtectedPersonVisitedPlaceRepositoryImplTest {

    @Test
    fun `fetchVisitedPlaces forwards path values and maps success response`() = runTest {
        val fakeApi = FakeCareDependentDayRoutePlacesApi(
            response = CarePlaceListResponseDto(
                placeCount = 1,
                places = listOf(carePlaceItem())
            )
        )
        val repository = ProtectedPersonVisitedPlaceRepositoryImpl(
            careDependentDayRoutePlacesApi = fakeApi
        )

        val result = repository.fetchVisitedPlaces(
            dependentUserId = 123L,
            dateKey = "2026-04-29"
        )

        assertEquals(123L, fakeApi.receivedDependentUserId)
        assertEquals("2026-04-29", fakeApi.receivedDate)
        assertTrue(result is ProtectedPersonVisitedPlaceResult.Success)
        val placeList = (result as ProtectedPersonVisitedPlaceResult.Success).placeList
        assertEquals(1, placeList.placeCount)
        assertEquals(1L, placeList.places.first().placeId)
    }

    @Test
    fun `fetchVisitedPlaces keeps empty place list as success`() = runTest {
        val repository = ProtectedPersonVisitedPlaceRepositoryImpl(
            careDependentDayRoutePlacesApi = FakeCareDependentDayRoutePlacesApi(
                response = CarePlaceListResponseDto(
                    placeCount = 0,
                    places = emptyList()
                )
            )
        )

        val result = repository.fetchVisitedPlaces(
            dependentUserId = 123L,
            dateKey = "2026-04-29"
        )

        assertTrue(result is ProtectedPersonVisitedPlaceResult.Success)
        val placeList = (result as ProtectedPersonVisitedPlaceResult.Success).placeList
        assertEquals(0, placeList.placeCount)
        assertTrue(placeList.places.isEmpty())
    }

    @Test
    fun `fetchVisitedPlaces returns Empty when server returns day route not found`() = runTest {
        val repository = ProtectedPersonVisitedPlaceRepositoryImpl(
            careDependentDayRoutePlacesApi = FakeCareDependentDayRoutePlacesApi(
                throwable = httpException(
                    statusCode = 404,
                    code = "DAY_ROUTE_NOT_FOUND"
                )
            )
        )

        val result = repository.fetchVisitedPlaces(
            dependentUserId = 123L,
            dateKey = "2026-04-29"
        )

        assertEquals(ProtectedPersonVisitedPlaceResult.Empty, result)
    }

    @Test
    fun `fetchVisitedPlaces returns Error when server returns forbidden`() = runTest {
        val forbiddenException = httpException(
            statusCode = 403,
            code = "FORBIDDEN"
        )
        val repository = ProtectedPersonVisitedPlaceRepositoryImpl(
            careDependentDayRoutePlacesApi = FakeCareDependentDayRoutePlacesApi(
                throwable = forbiddenException
            )
        )

        val result = repository.fetchVisitedPlaces(
            dependentUserId = 123L,
            dateKey = "2026-04-29"
        )

        assertTrue(result is ProtectedPersonVisitedPlaceResult.Error)
        assertEquals(
            forbiddenException,
            (result as ProtectedPersonVisitedPlaceResult.Error).throwable
        )
    }

    private class FakeCareDependentDayRoutePlacesApi(
        private val response: CarePlaceListResponseDto = CarePlaceListResponseDto(
            placeCount = 0,
            places = emptyList()
        ),
        private val throwable: Throwable? = null
    ) : CareDependentDayRoutePlacesApi {
        var receivedDependentUserId: Long? = null
        var receivedDate: String? = null

        override suspend fun getDayRoutePlaces(
            dependentUserId: Long,
            date: String
        ): CarePlaceListResponseDto {
            receivedDependentUserId = dependentUserId
            receivedDate = date
            throwable?.let { throw it }
            return response
        }
    }

    private companion object {
        fun carePlaceItem(): CarePlaceItemDto {
            return CarePlaceItemDto(
                placeId = 1L,
                placeName = "Library",
                source = "AUTO",
                type = "SCHOOL",
                roadAddress = "Seoul Jongno 1",
                latitude = 37.1,
                longitude = 127.1,
                orderIndex = 1,
                startTime = "2026-04-29T09:00:00+09:00",
                endTime = "2026-04-29T10:00:00+09:00"
            )
        }

        fun httpException(
            statusCode: Int,
            code: String
        ): HttpException {
            val errorBody = """{"code":"$code","message":"error"}"""
                .toResponseBody("application/json".toMediaType())
            return HttpException(
                Response.error<CarePlaceListResponseDto>(
                    statusCode,
                    errorBody
                )
            )
        }
    }
}
