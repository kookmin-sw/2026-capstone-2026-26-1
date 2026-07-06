package com.example.passedpath.feature.calendar.domain.usecase

import com.example.passedpath.feature.calendar.domain.model.CalendarMonthlyRoute
import com.example.passedpath.feature.calendar.domain.repository.CalendarMonthlyRouteRepository

class GetCalendarMonthlyRouteUseCase(
    private val calendarMonthlyRouteRepository: CalendarMonthlyRouteRepository
) {
    suspend operator fun invoke(year: Int, month: Int): CalendarMonthlyRoute {
        require(year in MinYear..MaxYear) { "year must be between $MinYear and $MaxYear" }
        require(month in MinMonth..MaxMonth) { "month must be between $MinMonth and $MaxMonth" }
        return calendarMonthlyRouteRepository.getMonthlyDayRoutes(year = year, month = month)
    }
}

private const val MinYear = 2000
private const val MaxYear = 3000
private const val MinMonth = 1
private const val MaxMonth = 12
