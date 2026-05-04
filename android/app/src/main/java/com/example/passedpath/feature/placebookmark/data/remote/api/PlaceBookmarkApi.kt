package com.example.passedpath.feature.placebookmark.data.remote.api

import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkListResponseDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkUpdateRequestDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkUpdateResponseDto
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface PlaceBookmarkApi {
    @GET("/api/bookmark-places")
    suspend fun getPlaceBookmarks(): PlaceBookmarkListResponseDto

    @PUT("/api/bookmark-places/{bookmarkPlaceId}")
    suspend fun updatePlaceBookmark(
        @Path("bookmarkPlaceId") bookmarkPlaceId: Long,
        @Body request: PlaceBookmarkUpdateRequestDto
    ): PlaceBookmarkUpdateResponseDto

    @DELETE("/api/bookmark-places/{bookmarkPlaceId}")
    suspend fun deletePlaceBookmark(
        @Path("bookmarkPlaceId") bookmarkPlaceId: Long
    ): Response<Unit>
}
