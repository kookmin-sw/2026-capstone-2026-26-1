package com.example.passedpath.feature.calendar.data.remote.mapper

import com.example.passedpath.feature.calendar.data.remote.dto.CalendarMonthlyDayResponseDto
import com.example.passedpath.feature.calendar.data.remote.dto.CalendarMonthlyRouteResponseDto
import com.example.passedpath.feature.calendar.domain.model.CalendarMonthlyDay
import com.example.passedpath.feature.calendar.domain.model.CalendarMonthlyRoute
import java.time.LocalDate

internal fun CalendarMonthlyRouteResponseDto.toCalendarMonthlyRoute(
    requestedYear: Int,
    requestedMonth: Int
): CalendarMonthlyRoute {
    return CalendarMonthlyRoute(
        year = year ?: requestedYear,
        month = month ?: requestedMonth,
        days = days.orEmpty().mapNotNull { day ->
            day.toCalendarMonthlyDay()
        }
    )
}

private fun CalendarMonthlyDayResponseDto.toCalendarMonthlyDay(): CalendarMonthlyDay? {
    val parsedDate = date?.let { dateText ->
        runCatching { LocalDate.parse(dateText) }.getOrNull()
    } ?: return null

    val route = dayRoute
    val hasExistingRoute = dayRouteExists == true && route != null
    return CalendarMonthlyDay(
        date = parsedDate,
        hasManualData = hasExistingRoute && route?.hasDetails == true,
        hasLocationData = hasExistingRoute && route?.hasPolyline == true,
        isBookmarked = hasExistingRoute && route?.isBookmarked == true
    )
}
