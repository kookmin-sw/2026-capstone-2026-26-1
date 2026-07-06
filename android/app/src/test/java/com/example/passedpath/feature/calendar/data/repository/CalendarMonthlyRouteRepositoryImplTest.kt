package com.example.passedpath.feature.calendar.data.repository

import com.example.passedpath.feature.calendar.data.remote.api.CalendarMonthlyRouteApi
import com.example.passedpath.feature.calendar.data.remote.dto.CalendarMonthlyDayResponseDto
import com.example.passedpath.feature.calendar.data.remote.dto.CalendarMonthlyDayRouteResponseDto
import com.example.passedpath.feature.calendar.data.remote.dto.CalendarMonthlyRouteResponseDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarMonthlyRouteRepositoryImplTest {

    @Test
    fun `getMonthlyDayRoutes fetches requested month and maps response`() = runTest {
        val fakeApi = FakeCalendarMonthlyRouteApi(
            response = CalendarMonthlyRouteResponseDto(
                year = 2026,
                month = 1,
                days = listOf(
                    CalendarMonthlyDayResponseDto(
                        date = "2026-01-20",
                        dayRouteExists = true,
                        dayRoute = CalendarMonthlyDayRouteResponseDto(
                            hasPolyline = true,
                            hasDetails = false,
                            isBookmarked = true
                        )
                    )
                )
            )
        )
        val repository = CalendarMonthlyRouteRepositoryImpl(
            calendarMonthlyRouteApi = fakeApi
        )

        val result = repository.getMonthlyDayRoutes(year = 2026, month = 1)

        assertEquals(2026, fakeApi.requestedYear)
        assertEquals(1, fakeApi.requestedMonth)
        assertEquals(2026, result.year)
        assertEquals(1, result.month)
        assertEquals(1, result.days.size)
        assertTrue(result.days.first().hasLocationData)
        assertTrue(result.days.first().isBookmarked)
    }

    @Test(expected = RuntimeException::class)
    fun `getMonthlyDayRoutes throws api failures`() = runTest {
        val repository = CalendarMonthlyRouteRepositoryImpl(
            calendarMonthlyRouteApi = FakeCalendarMonthlyRouteApi(
                throwable = RuntimeException("boom")
            )
        )

        repository.getMonthlyDayRoutes(year = 2026, month = 1)
    }

    private class FakeCalendarMonthlyRouteApi(
        private val response: CalendarMonthlyRouteResponseDto = CalendarMonthlyRouteResponseDto(
            year = 2026,
            month = 1,
            days = emptyList()
        ),
        private val throwable: Throwable? = null
    ) : CalendarMonthlyRouteApi {
        var requestedYear: Int? = null
        var requestedMonth: Int? = null

        override suspend fun getMonthlyDayRoutes(
            year: Int,
            month: Int
        ): CalendarMonthlyRouteResponseDto {
            requestedYear = year
            requestedMonth = month
            throwable?.let { throw it }
            return response
        }
    }
}
