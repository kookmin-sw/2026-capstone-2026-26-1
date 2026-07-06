package com.example.passedpath.feature.summary.data.remote.api

import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface StatisticMetricApi {
    @GET("/api/statics/metrics/outing-time")
    suspend fun getOutingTime(
        @Query("period") period: String? = null
    ): StatisticMetricResponseDto

    @GET("/api/statics/metrics/enter-home-time")
    suspend fun getEnterHomeTime(
        @Query("period") period: String? = null
    ): StatisticMetricResponseDto

    @GET("/api/statics/metrics/total-outing-seconds")
    suspend fun getTotalOutingSeconds(
        @Query("period") period: String? = null
    ): StatisticMetricResponseDto

    @GET("/api/statics/metrics/total-outing-count")
    suspend fun getTotalOutingCount(
        @Query("period") period: String? = null
    ): StatisticMetricResponseDto
}
