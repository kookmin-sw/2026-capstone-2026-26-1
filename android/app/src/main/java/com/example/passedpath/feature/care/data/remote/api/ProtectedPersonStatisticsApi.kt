package com.example.passedpath.feature.care.data.remote.api

import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricResponseDto
import com.example.passedpath.feature.summary.data.remote.dto.VisitStatisticsResponseDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyStatisticsResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProtectedPersonStatisticsApi {
    @GET("/api/care/dependents/{dependentUserId}/statics/weekly")
    suspend fun getWeeklyStatistics(
        @Path("dependentUserId") dependentUserId: Long
    ): WeeklyStatisticsResponseDto

    @GET("/api/care/dependents/{dependentUserId}/statics/metrics/visits")
    suspend fun getVisitStatistics(
        @Path("dependentUserId") dependentUserId: Long,
        @Query("period") period: String? = null
    ): VisitStatisticsResponseDto

    @GET("/api/care/dependents/{dependentUserId}/statics/metrics/outing-time")
    suspend fun getOutingTime(
        @Path("dependentUserId") dependentUserId: Long,
        @Query("period") period: String? = null
    ): StatisticMetricResponseDto

    @GET("/api/care/dependents/{dependentUserId}/statics/metrics/enter-home-time")
    suspend fun getEnterHomeTime(
        @Path("dependentUserId") dependentUserId: Long,
        @Query("period") period: String? = null
    ): StatisticMetricResponseDto

    @GET("/api/care/dependents/{dependentUserId}/statics/metrics/total-outing-seconds")
    suspend fun getTotalOutingSeconds(
        @Path("dependentUserId") dependentUserId: Long,
        @Query("period") period: String? = null
    ): StatisticMetricResponseDto

    @GET("/api/care/dependents/{dependentUserId}/statics/metrics/total-outing-count")
    suspend fun getTotalOutingCount(
        @Path("dependentUserId") dependentUserId: Long,
        @Query("period") period: String? = null
    ): StatisticMetricResponseDto
}
