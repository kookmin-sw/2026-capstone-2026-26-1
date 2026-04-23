package com.example.passedpath.feature.locationtracking.data.remote.mapper

import com.example.passedpath.feature.locationtracking.data.remote.dto.DayRouteDetailResponseDto
import com.example.passedpath.feature.locationtracking.data.remote.dto.GpsPointItemDto
import com.example.passedpath.feature.locationtracking.data.remote.dto.PlaceItemDto
import com.example.passedpath.feature.locationtracking.domain.model.DayRouteDetail
import com.example.passedpath.feature.locationtracking.domain.model.DayRoutePlace
import com.example.passedpath.feature.locationtracking.domain.model.RoutePoint

fun DayRouteDetailResponseDto.toDayRouteDetail(requestedDateKey: String): DayRouteDetail {
    val routePoints = gpsPoints.orEmpty().mapNotNull(GpsPointItemDto::toRoutePointOrNull)

    return DayRouteDetail(
        dateKey = date ?: requestedDateKey,
        totalDistanceKm = totalDistance ?: 0.0,
        title = title.orEmpty(),
        memo = memo.orEmpty(),
        isBookmarked = isBookmarked ?: false,
        pathPointCount = pathPointCount ?: routePoints.size,
        polylinePoints = routePoints,
        places = places.orEmpty()
            .mapNotNull(PlaceItemDto::toDayRoutePlaceOrNull)
            .sortedBy(DayRoutePlace::orderIndex)
    )
}

private fun GpsPointItemDto.toRoutePointOrNull(): RoutePoint? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null

    return RoutePoint(
        latitude = lat,
        longitude = lng
    )
}

private fun PlaceItemDto.toDayRoutePlaceOrNull(): DayRoutePlace? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    val index = orderIndex ?: return null

    return DayRoutePlace(
        placeId = placeId ?: 0L,
        placeName = placeName.orEmpty(),
        roadAddress = roadAddress.orEmpty(),
        latitude = lat,
        longitude = lng,
        orderIndex = index
    )
}
