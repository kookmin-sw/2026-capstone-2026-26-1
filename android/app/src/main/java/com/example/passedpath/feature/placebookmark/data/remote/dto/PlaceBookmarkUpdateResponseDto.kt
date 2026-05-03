package com.example.passedpath.feature.placebookmark.data.remote.dto

data class PlaceBookmarkUpdateResponseDto(
    val type: String,
    val placeName: String,
    val roadAddress: String,
    val latitude: Double,
    val longitude: Double
)
