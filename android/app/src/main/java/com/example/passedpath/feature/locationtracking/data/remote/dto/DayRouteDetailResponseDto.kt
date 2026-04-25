package com.example.passedpath.feature.locationtracking.data.remote.dto

data class DayRouteDetailResponseDto(
    val date: String?,
    val totalDistance: Double?,
    val title: String?,
    val memo: String?,
    val isBookmarked: Boolean?,
    val encodedPath: String?,
    val pathPointCount: Int?,
    val places: List<PlaceItemDto>?
)

data class PlaceItemDto(
    val placeId: Long?,
    val placeName: String?,
    val roadAddress: String?,
    val latitude: Double?,
    val longitude: Double?,
    val orderIndex: Int?
)
