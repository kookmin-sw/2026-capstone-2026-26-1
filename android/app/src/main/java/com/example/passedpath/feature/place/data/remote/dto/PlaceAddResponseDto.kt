package com.example.passedpath.feature.place.data.remote.dto

data class PlaceAddResponseDto(
    val placeId: Long,
    val placeName: String,
    val roadAddress: String,
    val latitude: Double,
    val longitude: Double,
    val orderIndex: Int
)
