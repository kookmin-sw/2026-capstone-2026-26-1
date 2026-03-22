package com.example.passedpath.feature.locationtracking.data.local.mapper

import android.location.Location
import com.example.passedpath.feature.locationtracking.data.local.entity.DayRouteEntity
import com.example.passedpath.feature.locationtracking.data.local.entity.GpsPointEntity
import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DateKeyFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun TrackedLocation.toGpsPointEntity(dateKey: String): GpsPointEntity {
    return GpsPointEntity(
        dateKey = dateKey,
        recordedAtEpochMillis = recordedAtEpochMillis,
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        isUploaded = false
    )
}

fun GpsPointEntity.toTrackedLocation(): TrackedLocation {
    return TrackedLocation(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        recordedAtEpochMillis = recordedAtEpochMillis
    )
}

fun List<GpsPointEntity>.toDailyPath(
    dateKey: String,
    existingRoute: DayRouteEntity? = null
): DailyPath {
    val trackedPoints = map(GpsPointEntity::toTrackedLocation)
    val totalDistanceMeters = existingRoute?.totalDistanceMeters ?: trackedPoints.calculateTotalDistanceMeters()

    return DailyPath(
        dateKey = dateKey,
        points = trackedPoints,
        totalDistanceMeters = totalDistanceMeters,
        pathPointCount = trackedPoints.size
    )
}

fun List<GpsPointEntity>.toDayRouteEntity(
    dateKey: String,
    previousRoute: DayRouteEntity? = null
): DayRouteEntity {
    val trackedPoints = map(GpsPointEntity::toTrackedLocation)

    return DayRouteEntity(
        dateKey = dateKey,
        totalDistanceMeters = trackedPoints.calculateTotalDistanceMeters(),
        pathPointCount = trackedPoints.size,
        lastRecordedAtEpochMillis = lastOrNull()?.recordedAtEpochMillis,
        lastSyncedAtEpochMillis = previousRoute?.lastSyncedAtEpochMillis,
        encodedPath = previousRoute?.encodedPath
    )
}

fun epochMillisToDateKey(
    epochMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault()
): String {
    return Instant.ofEpochMilli(epochMillis)
        .atZone(zoneId)
        .toLocalDate()
        .format(DateKeyFormatter)
}

private fun List<TrackedLocation>.calculateTotalDistanceMeters(): Double {
    if (size < 2) return 0.0

    var distanceMeters = 0.0
    for (index in 1 until size) {
        val previous = this[index - 1]
        val current = this[index]
        distanceMeters += distanceBetweenMeters(previous, current)
    }

    return distanceMeters
}

private fun distanceBetweenMeters(
    start: TrackedLocation,
    end: TrackedLocation
): Double {
    val results = FloatArray(1)
    Location.distanceBetween(
        start.latitude,
        start.longitude,
        end.latitude,
        end.longitude,
        results
    )
    return results.first().toDouble()
}
