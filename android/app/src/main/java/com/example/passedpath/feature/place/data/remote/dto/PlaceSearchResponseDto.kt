package com.example.passedpath.feature.place.data.remote.dto

data class PlaceSearchResponseDto(
    val page: Int? = null,
    val size: Int? = null,
    val isEnd: Boolean? = null,
    val pageableCount: Int? = null,
    val places: List<PlaceSearchItemDto> = emptyList()
)

data class PlaceSearchItemDto(
    val placeName: String?,
    val category: String?,
    val roadAddress: String?,
    val latitude: Double?,
    val longitude: Double?
)
