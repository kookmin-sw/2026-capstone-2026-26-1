package com.example.passedpath.feature.place.domain.model

data class UpdatedPlace(
    val placeName: String,
    val roadAddress: String,
    val latitude: Double,
    val longitude: Double,
    val source: PlaceSourceType? = null,
    val bookmarkType: BookmarkPlaceType? = null,
    val startTime: String? = null,
    val endTime: String? = null
)
