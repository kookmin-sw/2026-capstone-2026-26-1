package com.example.passedpath.feature.care.data.remote.api

import com.example.passedpath.feature.care.data.remote.dto.CareDayRouteSummaryResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CareDependentDayRouteSummaryApi {
    @GET("/api/care/dependents/{dependentUserId}/day-routes/{date}/summary")
    suspend fun getDayRouteSummary(
        @Path("dependentUserId") dependentUserId: Long,
        @Path("date") date: String
    ): CareDayRouteSummaryResponseDto
}
