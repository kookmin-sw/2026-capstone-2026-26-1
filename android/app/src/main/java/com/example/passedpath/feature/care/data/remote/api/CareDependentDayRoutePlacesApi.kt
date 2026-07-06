package com.example.passedpath.feature.care.data.remote.api

import com.example.passedpath.feature.care.data.remote.dto.CarePlaceListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CareDependentDayRoutePlacesApi {
    @GET("/api/care/dependents/{dependentUserId}/day-routes/{date}/places")
    suspend fun getDayRoutePlaces(
        @Path("dependentUserId") dependentUserId: Long,
        @Path("date") date: String
    ): CarePlaceListResponseDto
}
