package com.example.passedpath.feature.care.presentation.mapper

import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteDetail
import com.example.passedpath.feature.care.domain.model.ProtectedPersonRoutePoint
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonRouteMapUiState
import com.example.passedpath.feature.locationtracking.domain.policy.MapPolylineSimplificationPolicy
import com.example.passedpath.ui.state.CoordinateUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun ProtectedPersonDayRouteDetail.toProtectedPersonRouteMapUiState():
    ProtectedPersonRouteMapUiState {
    return ProtectedPersonRouteMapUiState(
        dateKey = dateKey,
        dateText = dateKey.toProtectedPersonRouteDetailDateText(),
        totalDistanceText = totalDistanceKm.toRouteDetailDistanceText(),
        totalDistanceKm = totalDistanceKm,
        pathPointCount = pathPointCount,
        mapPolylinePoints = routePoints.toRouteDetailPolylinePoints()
    )
}

internal fun String.toProtectedPersonRouteDetailDateText(): String {
    return runCatching {
        LocalDate.parse(this).format(RouteDetailDateFormatter)
    }.getOrDefault(this)
}

internal fun Double.toRouteDetailDistanceText(): String {
    return String.format(Locale.US, "%.1fkm", coerceAtLeast(0.0))
}

private fun List<ProtectedPersonRoutePoint>.toRouteDetailPolylinePoints():
    List<CoordinateUiState> {
    val indexes = MapPolylineSimplificationPolicy.simplifyIndexes(
        size = size,
        latitudeAt = { index -> this[index].latitude },
        longitudeAt = { index -> this[index].longitude }
    )

    return indexes.map { index ->
        val point = this[index]
        CoordinateUiState(
            latitude = point.latitude,
            longitude = point.longitude,
            recordedAtEpochMillis = point.recordedAtEpochMillis
        )
    }
}

private val RouteDetailDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd. EEE", Locale.KOREAN)
