package com.example.passedpath.feature.place.data.remote.dto

data class PlaceListResponseDto(
    val placeCount: Int?,
    val places: List<PlaceListItemDto>?
)

data class PlaceListItemDto(
    val placeId: Long?,
    val placeName: String?,
    val type: String?,
    val roadAddress: String?,
    val latitude: Double?,
    val longitude: Double?,
    val orderIndex: Int?
)
