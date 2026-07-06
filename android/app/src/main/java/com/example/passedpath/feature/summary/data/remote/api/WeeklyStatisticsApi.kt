package com.example.passedpath.feature.summary.data.remote.api

import com.example.passedpath.feature.summary.data.remote.dto.WeeklyStatisticsResponseDto
import retrofit2.http.GET

interface WeeklyStatisticsApi {
    @GET("/api/statics/weekly")
    suspend fun getWeeklyStatistics(): WeeklyStatisticsResponseDto
}
