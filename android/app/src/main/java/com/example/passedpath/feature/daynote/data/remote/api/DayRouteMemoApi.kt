package com.example.passedpath.feature.daynote.data.remote.api

import com.example.passedpath.feature.daynote.data.remote.dto.DayRouteMemoRequestDto
import com.example.passedpath.feature.daynote.data.remote.dto.DayRouteMemoResponseDto
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path

interface DayRouteMemoApi {
    @PATCH("/api/day-routes/{date}/memo")
    suspend fun patchMemo(
        @Path("date") date: String,
        @Body request: DayRouteMemoRequestDto
    ): DayRouteMemoResponseDto
}
