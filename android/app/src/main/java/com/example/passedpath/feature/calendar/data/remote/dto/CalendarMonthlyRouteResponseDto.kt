package com.example.passedpath.feature.calendar.data.remote.dto

data class CalendarMonthlyRouteResponseDto(
    val year: Int?,
    val month: Int?,
    val days: List<CalendarMonthlyDayResponseDto>?
)

data class CalendarMonthlyDayResponseDto(
    val date: String?,
    val dayRouteExists: Boolean?,
    val dayRoute: CalendarMonthlyDayRouteResponseDto?
)

data class CalendarMonthlyDayRouteResponseDto(
    val hasPolyline: Boolean?,
    val hasDetails: Boolean?,
    val isBookmarked: Boolean?
)
