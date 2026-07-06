package com.example.passedpath.feature.care.data.repository

import com.example.passedpath.feature.care.data.remote.api.CareDependentDayRouteSummaryApi
import com.example.passedpath.feature.care.data.remote.dto.CareDayRouteSummaryResponseDto
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDaySummaryResult
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
class ProtectedPersonDaySummaryRepositoryImplTest {

    @Test
    fun `fetchDaySummary forwards path values and maps success response`() = runTest {
        val fakeApi = FakeCareDependentDayRouteSummaryApi(
            response = CareDayRouteSummaryResponseDto(
                outingTime = "2026-04-29T08:10:00+09:00",
                enterHomeTime = "2026-04-29T18:40:00+09:00",
                totalOutingCount = 2,
                totalOutingSeconds = 36_120L,
                totalOutingDurationText = "10\uC2DC\uAC04 2\uBD84",
                visitedDongName = listOf("\uC815\uB989\uB3D9")
            )
        )
        val repository = ProtectedPersonDaySummaryRepositoryImpl(
            careDependentDayRouteSummaryApi = fakeApi
        )

        val result = repository.fetchDaySummary(
            dependentUserId = 123L,
            dateKey = "2026-04-29"
        )

        assertEquals(123L, fakeApi.receivedDependentUserId)
        assertEquals("2026-04-29", fakeApi.receivedDate)
        assertTrue(result is ProtectedPersonDaySummaryResult.Success)
        val summary = (result as ProtectedPersonDaySummaryResult.Success).daySummary
        assertEquals("2026-04-29", summary.dateKey)
        assertEquals(2, summary.totalOutingCount)
        assertEquals(listOf("\uC815\uB989\uB3D9"), summary.visitedDongNames)
    }

    @Test
    fun `fetchDaySummary returns Empty when server returns day route not found`() = runTest {
        val repository = ProtectedPersonDaySummaryRepositoryImpl(
            careDependentDayRouteSummaryApi = FakeCareDependentDayRouteSummaryApi(
                throwable = httpException(
                    statusCode = 404,
                    code = "DAY_ROUTE_NOT_FOUND"
                )
            )
        )

        val result = repository.fetchDaySummary(
            dependentUserId = 123L,
            dateKey = "2026-04-29"
        )

        assertEquals(ProtectedPersonDaySummaryResult.Empty, result)
    }

    @Test
    fun `fetchDaySummary returns Error when server returns forbidden`() = runTest {
        val forbiddenException = httpException(
            statusCode = 403,
            code = "FORBIDDEN"
        )
        val repository = ProtectedPersonDaySummaryRepositoryImpl(
            careDependentDayRouteSummaryApi = FakeCareDependentDayRouteSummaryApi(
                throwable = forbiddenException
            )
        )

        val result = repository.fetchDaySummary(
            dependentUserId = 123L,
            dateKey = "2026-04-29"
        )

        assertTrue(result is ProtectedPersonDaySummaryResult.Error)
        assertEquals(
            forbiddenException,
            (result as ProtectedPersonDaySummaryResult.Error).throwable
        )
    }

    private class FakeCareDependentDayRouteSummaryApi(
        private val response: CareDayRouteSummaryResponseDto = CareDayRouteSummaryResponseDto(
            outingTime = null,
            enterHomeTime = null,
            totalOutingCount = null,
            totalOutingSeconds = null,
            totalOutingDurationText = null
        ),
        private val throwable: Throwable? = null
    ) : CareDependentDayRouteSummaryApi {
        var receivedDependentUserId: Long? = null
        var receivedDate: String? = null

        override suspend fun getDayRouteSummary(
            dependentUserId: Long,
            date: String
        ): CareDayRouteSummaryResponseDto {
            receivedDependentUserId = dependentUserId
            receivedDate = date
            throwable?.let { throw it }
            return response
        }
    }

    private companion object {
        fun httpException(
            statusCode: Int,
            code: String
        ): HttpException {
            val errorBody = """{"code":"$code","message":"error"}"""
                .toResponseBody("application/json".toMediaType())
            return HttpException(
                Response.error<CareDayRouteSummaryResponseDto>(
                    statusCode,
                    errorBody
                )
            )
        }
    }
}
