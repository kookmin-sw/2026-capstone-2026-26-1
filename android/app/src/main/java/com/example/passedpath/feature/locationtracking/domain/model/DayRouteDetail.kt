package com.example.passedpath.feature.locationtracking.domain.model

data class RoutePoint(
    val latitude: Double,
    val longitude: Double
)

data class DayRoutePlace(
    val placeId: Long,
    val placeName: String,
    val roadAddress: String,
    val latitude: Double,
    val longitude: Double,
    val orderIndex: Int
)

data class DayRouteDetail(
    val dateKey: String,
    val totalDistanceKm: Double,
    val title: String = "",
    val memo: String = "",
    val isBookmarked: Boolean = false,
    val encodedPath: String = "",
    val pathPointCount: Int = 0,
    val polylinePoints: List<RoutePoint> = emptyList(),
    val places: List<DayRoutePlace> = emptyList()
)
