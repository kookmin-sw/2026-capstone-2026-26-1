package com.example.passedpath.feature.calendar.domain.model

import java.time.LocalDate

data class CalendarMonthlyRoute(
    val year: Int,
    val month: Int,
    val days: List<CalendarMonthlyDay>
)

data class CalendarMonthlyDay(
    val date: LocalDate,
    val hasManualData: Boolean,
    val hasLocationData: Boolean,
    val isBookmarked: Boolean
)
