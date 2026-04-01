package com.example.passedpath.feature.place.data.remote.dto

data class PlaceUpdateResponseDto(
    val roadAddress: String,
    val placeName: String,
    val latitude: Double,
    val longitude: Double
)
