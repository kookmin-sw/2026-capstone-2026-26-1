package com.example.passedpath.feature.place.data.remote.api

import com.example.passedpath.feature.place.data.remote.dto.PlaceAddRequestDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceAddResponseDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface PlaceApi {
    @POST("/api/day-routes/{date}/places")
    suspend fun addPlace(
        @Path("date") date: String,
        @Body request: PlaceAddRequestDto
    ): PlaceAddResponseDto
}
