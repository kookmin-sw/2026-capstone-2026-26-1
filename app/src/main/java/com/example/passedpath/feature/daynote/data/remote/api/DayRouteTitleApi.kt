package com.example.passedpath.feature.daynote.data.remote.api

import com.example.passedpath.feature.daynote.data.remote.dto.DayRouteTitleRequestDto
import com.example.passedpath.feature.daynote.data.remote.dto.DayRouteTitleResponseDto
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path

interface DayRouteTitleApi {
    @PATCH("/api/day-routes/{date}/title")
    suspend fun patchTitle(
        @Path("date") date: String,
        @Body request: DayRouteTitleRequestDto
    ): DayRouteTitleResponseDto
}
