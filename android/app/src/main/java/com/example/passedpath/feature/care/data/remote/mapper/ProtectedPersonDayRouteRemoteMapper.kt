package com.example.passedpath.feature.care.data.remote.mapper

import com.example.passedpath.feature.care.data.remote.dto.CareDayRouteDetailResponseDto
import com.example.passedpath.feature.care.data.remote.dto.CareDependentDayRouteItemDto
import com.example.passedpath.feature.care.data.remote.dto.CareDependentDayRouteListResponseDto
import com.example.passedpath.feature.care.data.remote.dto.CareGpsPointItemDto
import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteDetail
import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteList
import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteListItem
import com.example.passedpath.feature.care.domain.model.ProtectedPersonRoutePoint
import java.time.LocalDate
import java.time.OffsetDateTime

internal fun CareDependentDayRouteListResponseDto.toProtectedPersonDayRouteList():
    ProtectedPersonDayRouteList {
    val routeItems = dayRoutes.orEmpty()
        .mapNotNull(CareDependentDayRouteItemDto::toProtectedPersonDayRouteListItemOrNull)

    return ProtectedPersonDayRouteList(
        dayRouteCount = dayRouteCount ?: routeItems.size,
        hasNext = hasNext ?: false,
        nextCursorDate = nextCursorDate.toDateKeyOrNull(),
        dayRoutes = routeItems
    )
}

internal fun CareDayRouteDetailResponseDto.toProtectedPersonDayRouteDetail(
    requestedDateKey: String
): ProtectedPersonDayRouteDetail {
    val routePoints = gpsPoints.orEmpty()
        .mapNotNull(CareGpsPointItemDto::toProtectedPersonRoutePointOrNull)

    return ProtectedPersonDayRouteDetail(
        dateKey = date ?: requestedDateKey,
        totalDistanceKm = totalDistance ?: 0.0,
        pathPointCount = routePoints.size,
        routePoints = routePoints
    )
}

private fun CareDependentDayRouteItemDto.toProtectedPersonDayRouteListItemOrNull():
    ProtectedPersonDayRouteListItem? {
    val parsedDateKey = date.toDateKeyOrNull() ?: return null

    return ProtectedPersonDayRouteListItem(
        dateKey = parsedDateKey,
        outingTime = outingTime.nonBlankOrNull(),
        enterHomeTime = enterHomeTime.nonBlankOrNull(),
        totalOutingCount = totalOutingCount ?: 0
    )
}

private fun CareGpsPointItemDto.toProtectedPersonRoutePointOrNull(): ProtectedPersonRoutePoint? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null

    return ProtectedPersonRoutePoint(
        recordedAt = recordedAt,
        recordedAtEpochMillis = recordedAt.toEpochMillisOrNull(),
        latitude = lat,
        longitude = lng
    )
}

private fun String?.toDateKeyOrNull(): String? {
    if (isNullOrBlank()) return null

    return runCatching {
        LocalDate.parse(trim()).toString()
    }.getOrNull()
}

private fun String?.nonBlankOrNull(): String? {
    return this?.takeIf { value -> value.isNotBlank() }
}

private fun String?.toEpochMillisOrNull(): Long? {
    if (isNullOrBlank()) return null

    return runCatching {
        OffsetDateTime.parse(this)
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}
