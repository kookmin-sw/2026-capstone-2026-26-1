package com.example.passedpath.feature.bookmark.data.remote.api

import com.example.passedpath.feature.bookmark.data.remote.dto.DayRouteBookmarkBatchRequestDto
import com.example.passedpath.feature.bookmark.data.remote.dto.DayRouteBookmarkListResponseDto
import com.example.passedpath.feature.bookmark.data.remote.dto.DayRouteBookmarkResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface DayRouteBookmarkApi {
    @GET("/api/day-routes/bookmarks")
    suspend fun getBookmarkedDayRoutes(
        @Query("cursorDate") cursorDate: String? = null,
        @Query("size") size: Int = 10
    ): DayRouteBookmarkListResponseDto

    @PATCH("/api/day-routes/{date}/bookmark")
    suspend fun toggleBookmark(
        @Path("date") date: String
    ): DayRouteBookmarkResponseDto

    @PATCH("/api/day-routes/bookmarks:batch")
    suspend fun toggleBookmarks(
        @Body request: DayRouteBookmarkBatchRequestDto
    ): Response<Unit>
}
