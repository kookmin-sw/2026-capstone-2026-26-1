package com.example.passedpath.feature.place.domain.model

data class VisitedPlace(
    val placeId: Long,
    val placeName: String,
    val type: PlaceSourceType,
    val roadAddress: String,
    val latitude: Double,
    val longitude: Double,
    val orderIndex: Int
)
