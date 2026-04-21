package com.example.passedpath.feature.route.presentation.policy

import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val GapRenderMinTimeMillis = 10 * 60 * 1000L
private const val GapRenderMinDistanceMeters = 1000.0
private const val EarthRadiusMeters = 6_371_000.0

internal fun shouldRenderGapAsDashed(
    start: MainCoordinateUiState,
    end: MainCoordinateUiState
): Boolean {
    val startRecordedAt = start.recordedAtEpochMillis ?: return false
    val endRecordedAt = end.recordedAtEpochMillis ?: return false
    val timeGapMillis = endRecordedAt - startRecordedAt
    if (timeGapMillis < GapRenderMinTimeMillis) return false

    val distanceMeters = distanceBetweenMeters(start = start, end = end)
    return distanceMeters >= GapRenderMinDistanceMeters
}

private fun distanceBetweenMeters(
    start: MainCoordinateUiState,
    end: MainCoordinateUiState
): Double {
    val startLatitudeRadians = Math.toRadians(start.latitude)
    val endLatitudeRadians = Math.toRadians(end.latitude)
    val deltaLatitudeRadians = Math.toRadians(end.latitude - start.latitude)
    val deltaLongitudeRadians = Math.toRadians(end.longitude - start.longitude)

    val haversine =
        sin(deltaLatitudeRadians / 2).pow(2) +
            cos(startLatitudeRadians) * cos(endLatitudeRadians) *
            sin(deltaLongitudeRadians / 2).pow(2)
    val angularDistance = 2 * atan2(sqrt(haversine), sqrt(1 - haversine))
    return EarthRadiusMeters * angularDistance
}
