package com.example.passedpath.feature.care.data.remote.api

import com.example.passedpath.feature.care.data.remote.dto.CareDependentUserListResponseDto
import retrofit2.http.GET

interface CareDependentApi {
    @GET("/api/care/dependents")
    suspend fun getDependents(): CareDependentUserListResponseDto
}
