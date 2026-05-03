package com.example.passedpath.feature.place.data.remote.api

import com.example.passedpath.feature.place.data.remote.dto.PlaceSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceSearchApi {
    @GET("/api/places/search")
    suspend fun searchPlaces(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): PlaceSearchResponseDto
}
