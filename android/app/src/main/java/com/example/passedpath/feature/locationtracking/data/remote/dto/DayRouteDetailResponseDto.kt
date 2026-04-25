package com.example.passedpath.feature.locationtracking.data.remote.dto

data class DayRouteDetailResponseDto(
    val date: String?,
    val totalDistance: Double?,
    val title: String?,
    val memo: String?,
    val isBookmarked: Boolean?,
    val pathPointCount: Int?,
    val gpsPoints: List<GpsPointItemDto>?,
    val places: List<PlaceItemDto>?
)

data class GpsPointItemDto(
    val recordedAt: String?,
    val latitude: Double?,
    val longitude: Double?
)

data class PlaceItemDto(
    val placeId: Long?,
    val placeName: String?,
    val roadAddress: String?,
    val latitude: Double?,
    val longitude: Double?,
    val orderIndex: Int?
)
