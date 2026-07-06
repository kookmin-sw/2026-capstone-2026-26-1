package com.example.passedpath.feature.calendar.data.remote.api

import com.example.passedpath.feature.calendar.data.remote.dto.CalendarMonthlyRouteResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CalendarMonthlyRouteApi {
    @GET("/api/day-routes")
    suspend fun getMonthlyDayRoutes(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): CalendarMonthlyRouteResponseDto
}
