package com.example.passedpath.feature.placebookmark.domain.model

import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType

data class PlaceBookmarkSummary(
    val bookmarkPlaceId: Long,
    val type: BookmarkPlaceType,
    val placeName: String,
    val roadAddress: String,
    val latitude: Double,
    val longitude: Double
)
