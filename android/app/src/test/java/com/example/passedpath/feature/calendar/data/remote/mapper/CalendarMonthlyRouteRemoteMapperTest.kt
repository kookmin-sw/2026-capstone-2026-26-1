package com.example.passedpath.feature.calendar.data.remote.mapper

import com.example.passedpath.feature.calendar.data.remote.dto.CalendarMonthlyDayResponseDto
import com.example.passedpath.feature.calendar.data.remote.dto.CalendarMonthlyDayRouteResponseDto
import com.example.passedpath.feature.calendar.data.remote.dto.CalendarMonthlyRouteResponseDto
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarMonthlyRouteRemoteMapperTest {

    @Test
    fun `toCalendarMonthlyRoute maps route flags to calendar statuses`() {
        val response = CalendarMonthlyRouteResponseDto(
            year = 2026,
            month = 1,
            days = listOf(
                CalendarMonthlyDayResponseDto(
                    date = "2026-01-13",
                    dayRouteExists = true,
                    dayRoute = CalendarMonthlyDayRouteResponseDto(
                        hasPolyline = true,
                        hasDetails = true,
                        isBookmarked = true
                    )
                )
            )
        )

        val result = response.toCalendarMonthlyRoute(
            requestedYear = 2026,
            requestedMonth = 1
        )

        val day = result.days.single()
        assertEquals(LocalDate.of(2026, 1, 13), day.date)
        assertTrue(day.hasLocationData)
        assertTrue(day.hasManualData)
        assertTrue(day.isBookmarked)
    }

    @Test
    fun `toCalendarMonthlyRoute treats missing route data as empty status`() {
        val response = CalendarMonthlyRouteResponseDto(
            year = 2026,
            month = 1,
            days = listOf(
                CalendarMonthlyDayResponseDto(
                    date = "2026-01-14",
                    dayRouteExists = false,
                    dayRoute = null
                )
            )
        )

        val result = response.toCalendarMonthlyRoute(
            requestedYear = 2026,
            requestedMonth = 1
        )

        val day = result.days.single()
        assertFalse(day.hasLocationData)
        assertFalse(day.hasManualData)
        assertFalse(day.isBookmarked)
    }

    @Test
    fun `toCalendarMonthlyRoute ignores invalid day date`() {
        val response = CalendarMonthlyRouteResponseDto(
            year = null,
            month = null,
            days = listOf(
                CalendarMonthlyDayResponseDto(
                    date = "not-a-date",
                    dayRouteExists = true,
                    dayRoute = CalendarMonthlyDayRouteResponseDto(
                        hasPolyline = true,
                        hasDetails = true,
                        isBookmarked = true
                    )
                )
            )
        )

        val result = response.toCalendarMonthlyRoute(
            requestedYear = 2026,
            requestedMonth = 1
        )

        assertEquals(2026, result.year)
        assertEquals(1, result.month)
        assertTrue(result.days.isEmpty())
    }
}
