package com.example.passedpath.feature.care.data.remote.api

import com.example.passedpath.feature.care.data.remote.dto.CareDayRouteDetailResponseDto
import com.example.passedpath.feature.care.data.remote.dto.CareDependentDayRouteListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CareDependentDayRouteApi {
    @GET("/api/care/dependents/{dependentUserId}/day-routes")
    suspend fun getDayRoutes(
        @Path("dependentUserId") dependentUserId: Long,
        @Query("cursorDate") cursorDate: String? = null,
        @Query("size") size: Int? = null
    ): CareDependentDayRouteListResponseDto

    @GET("/api/care/dependents/{dependentUserId}/day-routes/{date}")
    suspend fun getDayRoute(
        @Path("dependentUserId") dependentUserId: Long,
        @Path("date") date: String
    ): CareDayRouteDetailResponseDto
}
