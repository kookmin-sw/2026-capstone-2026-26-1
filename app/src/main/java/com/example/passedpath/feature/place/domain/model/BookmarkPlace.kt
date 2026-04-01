package com.example.passedpath.feature.place.domain.model

data class BookmarkPlace(
    val type: BookmarkPlaceType,
    val placeName: String,
    val roadAddress: String,
    val latitude: Double,
    val longitude: Double
)
