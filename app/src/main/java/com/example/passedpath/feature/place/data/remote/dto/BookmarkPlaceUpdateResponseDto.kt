package com.example.passedpath.feature.place.data.remote.dto

data class BookmarkPlaceUpdateResponseDto(
    val type: String,
    val placeName: String,
    val roadAddress: String,
    val latitude: Double,
    val longitude: Double
)
