package com.example.passedpath.feature.placebookmark.data.remote.dto

data class PlaceBookmarkListResponseDto(
    val placeCount: Int?,
    val bookmarkPlaces: List<PlaceBookmarkSummaryResponseDto>?
)

data class PlaceBookmarkSummaryResponseDto(
    val placeId: Long?,
    val type: String?,
    val placeName: String?,
    val roadAddress: String?,
    val latitude: Double?,
    val longitude: Double?
)
