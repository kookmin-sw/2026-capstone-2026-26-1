package com.example.passedpath.feature.place.data.remote.dto

data class PlaceSearchResponseDto(
    val places: List<PlaceSearchItemDto> = emptyList()
)

data class PlaceSearchItemDto(
    val id: String?,
    val name: String?,
    val category: String?,
    val roadAddress: String?,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?
)
