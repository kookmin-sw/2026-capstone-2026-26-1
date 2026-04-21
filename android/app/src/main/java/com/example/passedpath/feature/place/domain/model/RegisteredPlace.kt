package com.example.passedpath.feature.place.domain.model

data class RegisteredPlace(
    val placeId: Long,
    val placeName: String,
    val roadAddress: String,
    val latitude: Double,
    val longitude: Double,
    val orderIndex: Int
)
