package com.example.passedpath.feature.summary.data.repository

import com.example.passedpath.feature.summary.data.remote.api.DayRouteSummaryApi
import com.example.passedpath.feature.summary.data.remote.dto.DayRouteSummaryResponseDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class DayRouteSummaryRepositoryImplTest {

    @Test
    fun `getDayRouteSummary returns default summary when server responds with DAY_ROUTE_NOT_FOUND`() = runTest {
        val repository = DayRouteSummaryRepositoryImpl(
            dayRouteSummaryApi = FakeDayRouteSummaryApi(
                throwable = HttpException(
                    Response.error<DayRouteSummaryResponseDto>(
                        404,
                        """{"code":"DAY_ROUTE_NOT_FOUND","message":"not found"}"""
                            .toResponseBody(JsonMediaType)
                    )
                )
            )
        )

        val result = repository.getDayRouteSummary("2026-05-06")

        assertEquals("2026-05-06", result.dateKey)
        assertNull(result.outingTime)
        assertNull(result.enterHomeTime)
        assertNull(result.totalOutingCount)
        assertNull(result.totalOutingSeconds)
        assertNull(result.totalOutingDurationText)
        assertTrue(result.visitedDongNames.isEmpty())
    }

    @Test
    fun `getDayRouteSummary throws when 404 has different error code`() = runTest {
        val repository = DayRouteSummaryRepositoryImpl(
            dayRouteSummaryApi = FakeDayRouteSummaryApi(
                throwable = HttpException(
                    Response.error<DayRouteSummaryResponseDto>(
                        404,
                        """{"code":"OTHER_NOT_FOUND","message":"not found"}"""
                            .toResponseBody(JsonMediaType)
                    )
                )
            )
        )

        try {
            repository.getDayRouteSummary("2026-05-06")
        } catch (exception: HttpException) {
            assertEquals(404, exception.code())
            return@runTest
        }

        error("Expected HttpException")
    }

    @Test
    fun `getDayRouteSummary throws when server responds with non not found failure`() = runTest {
        val repository = DayRouteSummaryRepositoryImpl(
            dayRouteSummaryApi = FakeDayRouteSummaryApi(
                throwable = HttpException(
                    Response.error<DayRouteSummaryResponseDto>(
                        500,
                        """{"message":"server error"}"""
                            .toResponseBody(JsonMediaType)
                    )
                )
            )
        )

        try {
            repository.getDayRouteSummary("2026-05-06")
        } catch (exception: HttpException) {
            assertEquals(500, exception.code())
            return@runTest
        }

        error("Expected HttpException")
    }

    private class FakeDayRouteSummaryApi(
        private val throwable: Throwable? = null
    ) : DayRouteSummaryApi {
        override suspend fun getDayRouteSummary(date: String): DayRouteSummaryResponseDto {
            throwable?.let { throw it }
            return DayRouteSummaryResponseDto(
                outingTime = "2026-05-06T09:00:00+09:00",
                enterHomeTime = "2026-05-06T18:00:00+09:00",
                totalOutingCount = 1,
                totalOutingSeconds = 3_600L,
                totalOutingDurationText = "1\uC2DC\uAC04"
            )
        }
    }
}

private val JsonMediaType = "application/json".toMediaType()
