package com.example.passedpath.feature.summary.data.remote.api

import com.example.passedpath.feature.summary.data.remote.dto.VisitStatisticsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface VisitStatisticsApi {
    @GET("/api/statics/metrics/visits")
    suspend fun getVisitStatistics(
        @Query("period") period: String? = null
    ): VisitStatisticsResponseDto
}
