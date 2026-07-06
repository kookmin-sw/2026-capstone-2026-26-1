package com.example.passedpath.feature.calendar.data.repository

import com.example.passedpath.feature.calendar.data.remote.api.CalendarMonthlyRouteApi
import com.example.passedpath.feature.calendar.data.remote.mapper.toCalendarMonthlyRoute
import com.example.passedpath.feature.calendar.domain.model.CalendarMonthlyRoute
import com.example.passedpath.feature.calendar.domain.repository.CalendarMonthlyRouteRepository

class CalendarMonthlyRouteRepositoryImpl(
    private val calendarMonthlyRouteApi: CalendarMonthlyRouteApi
) : CalendarMonthlyRouteRepository {
    override suspend fun getMonthlyDayRoutes(year: Int, month: Int): CalendarMonthlyRoute {
        return calendarMonthlyRouteApi.getMonthlyDayRoutes(
            year = year,
            month = month
        ).toCalendarMonthlyRoute(
            requestedYear = year,
            requestedMonth = month
        )
    }
}
