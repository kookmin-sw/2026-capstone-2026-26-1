package com.example.passedpath.feature.calendar.presentation.model

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class CalendarDayStatus(
    val hasManualData: Boolean = false,
    val hasLocationData: Boolean = false,
    val isBookmarked: Boolean = false
)

data class CalendarMonthCell(
    val date: LocalDate?,
    val status: CalendarDayStatus = CalendarDayStatus()
)

internal fun buildCalendarMonthCells(
    visibleMonth: YearMonth,
    dayStatuses: Map<LocalDate, CalendarDayStatus> = emptyMap()
): List<CalendarMonthCell> {
    val firstDate = visibleMonth.atDay(1)
    val leadingBlankCount = firstDate.dayOfWeek.value % DaysPerWeek
    val dayCells = (1..visibleMonth.lengthOfMonth()).map { dayOfMonth ->
        val date = visibleMonth.atDay(dayOfMonth)
        CalendarMonthCell(
            date = date,
            status = dayStatuses[date] ?: CalendarDayStatus()
        )
    }
    val totalCellCount = leadingBlankCount + dayCells.size
    val trailingBlankCount = (DaysPerWeek - (totalCellCount % DaysPerWeek)) % DaysPerWeek

    return buildList {
        repeat(leadingBlankCount) { add(CalendarMonthCell(date = null)) }
        addAll(dayCells)
        repeat(trailingBlankCount) { add(CalendarMonthCell(date = null)) }
    }
}

internal fun LocalDate.toCalendarDateKey(): String {
    return format(CalendarDateKeyFormatter)
}

internal fun toggleCalendarSelectedDateKey(
    currentSelectedDateKey: String?,
    clickedDate: LocalDate
): String? {
    val clickedDateKey = clickedDate.toCalendarDateKey()
    return if (currentSelectedDateKey == clickedDateKey) {
        null
    } else {
        clickedDateKey
    }
}

internal val CalendarDateKeyFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd")

private const val DaysPerWeek = 7
