package com.example.passedpath.feature.place.data.remote.dto

data class PlaceUpdateResponseDto(
    val roadAddress: String? = null,
    val placeName: String? = null,
    val source: String? = null,
    val type: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val startTime: String? = null,
    val endTime: String? = null
)
