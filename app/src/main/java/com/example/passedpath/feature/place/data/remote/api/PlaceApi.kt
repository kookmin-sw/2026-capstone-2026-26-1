package com.example.passedpath.feature.place.data.remote.api

import com.example.passedpath.feature.place.data.remote.dto.BookmarkPlaceUpdateRequestDto
import com.example.passedpath.feature.place.data.remote.dto.BookmarkPlaceUpdateResponseDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceAddRequestDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceAddResponseDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceReorderRequestDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceUpdateRequestDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceUpdateResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PlaceApi {
    @POST("/api/day-routes/{date}/places")
    suspend fun addPlace(
        @Path("date") date: String,
        @Body request: PlaceAddRequestDto
    ): PlaceAddResponseDto

    @PUT("/api/day-routes/{date}/places/{placeId}")
    suspend fun updatePlace(
        @Path("date") date: String,
        @Path("placeId") placeId: Long,
        @Body request: PlaceUpdateRequestDto
    ): PlaceUpdateResponseDto

    @PUT("/api/day-routes/{date}/places:reorder")
    suspend fun reorderPlaces(
        @Path("date") date: String,
        @Body request: PlaceReorderRequestDto
    )

    @PUT("/api/bookmark-places/{bookmarkPlaceId}")
    suspend fun updateBookmarkPlace(
        @Path("bookmarkPlaceId") bookmarkPlaceId: Long,
        @Body request: BookmarkPlaceUpdateRequestDto
    ): BookmarkPlaceUpdateResponseDto

    @DELETE("/api/day-routes/{date}/places/{placeId}")
    suspend fun deletePlace(
        @Path("date") date: String,
        @Path("placeId") placeId: Long
    )
}
