package com.example.passedpath.feature.calendar.domain.repository

import com.example.passedpath.feature.calendar.domain.model.CalendarMonthlyRoute

interface CalendarMonthlyRouteRepository {
    suspend fun getMonthlyDayRoutes(year: Int, month: Int): CalendarMonthlyRoute
}
