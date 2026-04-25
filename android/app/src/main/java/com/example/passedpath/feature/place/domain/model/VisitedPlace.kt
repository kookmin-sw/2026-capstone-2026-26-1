package com.example.passedpath.feature.place.domain.model

data class VisitedPlace(
    val placeId: Long,
    val placeName: String,
    val source: PlaceSourceType,
    val bookmarkType: BookmarkPlaceType? = null,
    val roadAddress: String,
    val latitude: Double,
    val longitude: Double,
    val orderIndex: Int,
    val startTime: String? = null,
    val endTime: String? = null
)
