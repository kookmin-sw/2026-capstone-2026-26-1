package com.example.passedpath.feature.locationtracking.data.remote.api

import com.example.passedpath.feature.locationtracking.data.remote.dto.DayRouteDetailResponseDto
import com.example.passedpath.feature.locationtracking.data.remote.dto.GpsPointBatchUploadRequestDto
import com.example.passedpath.feature.locationtracking.data.remote.dto.GpsPointBatchUploadResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DayRouteApi {
    @POST("/api/day-routes/{date}/gps-points:batch")
    suspend fun uploadGpsPointsBatch(
        @Path("date") date: String,
        @Body request: GpsPointBatchUploadRequestDto
    ): GpsPointBatchUploadResponseDto

    @GET("/api/day-routes/{date}")
    suspend fun getDayRoute(
        @Path("date") date: String
    ): DayRouteDetailResponseDto
}
