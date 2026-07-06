package com.example.passedpath.feature.care.data.remote.mapper

import com.example.passedpath.feature.care.data.remote.dto.CareDependentLocationConnectedEventDto
import com.example.passedpath.feature.care.data.remote.dto.CareDependentLocationUpdatedEventDto
import com.example.passedpath.feature.care.data.remote.dto.LatestGpsPointItemDto
import com.example.passedpath.feature.care.domain.model.CareDependentLocationStreamEvent
import com.example.passedpath.feature.care.domain.model.CareLatestGpsPoint
import com.google.gson.Gson
import java.time.OffsetDateTime

internal fun String.toCareDependentLocationStreamEventOrNull(
    eventType: String?,
    gson: Gson = Gson()
): CareDependentLocationStreamEvent? {
    return when (eventType) {
        ConnectedEvent -> toConnectedEvent(gson)
        HeartbeatEvent -> null
        LocationUpdatedEvent -> toLocationUpdatedEventOrNull(gson)
        else -> null
    }
}

private fun String.toConnectedEvent(gson: Gson): CareDependentLocationStreamEvent.Connected {
    val message = runCatching {
        gson.fromJson(this, CareDependentLocationConnectedEventDto::class.java)
            ?.message
    }.getOrNull()

    return CareDependentLocationStreamEvent.Connected(message = message)
}

private fun String.toLocationUpdatedEventOrNull(
    gson: Gson
): CareDependentLocationStreamEvent.LocationUpdated? {
    val event = runCatching {
        gson.fromJson(this, CareDependentLocationUpdatedEventDto::class.java)
    }.getOrNull() ?: return null
    val dependentUserId = event.dependentUserId ?: return null
    val latestGpsPoint = event.latestGpsPoint?.toCareLatestGpsPointOrNull() ?: return null

    return CareDependentLocationStreamEvent.LocationUpdated(
        dependentUserId = dependentUserId,
        latestGpsPoint = latestGpsPoint
    )
}

private fun LatestGpsPointItemDto.toCareLatestGpsPointOrNull(): CareLatestGpsPoint? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null

    return CareLatestGpsPoint(
        latitude = lat,
        longitude = lng,
        recordedAt = recordedAt,
        recordedAtEpochMillis = recordedAt.toEpochMillisOrNull()
    )
}

private fun String?.toEpochMillisOrNull(): Long? {
    if (isNullOrBlank()) return null

    return runCatching {
        OffsetDateTime.parse(this)
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

private const val ConnectedEvent = "connected"
private const val HeartbeatEvent = "heartbeat"
private const val LocationUpdatedEvent = "location-updated"
