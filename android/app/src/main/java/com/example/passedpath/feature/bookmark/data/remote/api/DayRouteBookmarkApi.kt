package com.example.passedpath.feature.bookmark.data.remote.api

import com.example.passedpath.feature.bookmark.data.remote.dto.DayRouteBookmarkResponseDto
import retrofit2.http.PATCH
import retrofit2.http.Path

interface DayRouteBookmarkApi {
    @PATCH("/api/day-routes/{date}/bookmark")
    suspend fun toggleBookmark(
        @Path("date") date: String
    ): DayRouteBookmarkResponseDto
}
