package com.example.passedpath.feature.place.domain.model

data class PlaceSearchPage(
    val page: Int,
    val size: Int,
    val isEnd: Boolean,
    val pageableCount: Int,
    val places: List<PlaceSearchResult>
)
