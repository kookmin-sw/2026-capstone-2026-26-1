package com.example.passedpath.feature.placebookmark.data.remote.api

import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkUpdateRequestDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkUpdateResponseDto
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

interface PlaceBookmarkApi {
    @PUT("/api/bookmark-places/{bookmarkPlaceId}")
    suspend fun updatePlaceBookmark(
        @Path("bookmarkPlaceId") bookmarkPlaceId: Long,
        @Body request: PlaceBookmarkUpdateRequestDto
    ): PlaceBookmarkUpdateResponseDto
}
