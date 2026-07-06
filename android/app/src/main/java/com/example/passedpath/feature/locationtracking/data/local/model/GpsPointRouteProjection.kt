package com.example.passedpath.feature.locationtracking.data.local.model

data class GpsPointRouteProjection(
    val recordedAtEpochMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?
)
