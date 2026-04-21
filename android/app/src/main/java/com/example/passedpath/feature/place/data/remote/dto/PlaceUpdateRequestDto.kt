package com.example.passedpath.feature.place.data.remote.dto

data class PlaceUpdateRequestDto(
    val roadAddress: String,
    val placeName: String,
    val latitude: Double,
    val longitude: Double
)
