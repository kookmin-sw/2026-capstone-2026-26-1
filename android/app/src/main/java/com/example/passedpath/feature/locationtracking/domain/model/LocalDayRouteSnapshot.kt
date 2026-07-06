package com.example.passedpath.feature.locationtracking.domain.model

data class LocalDayRouteSnapshot(
    val dateKey: String,
    val points: List<TrackedLocation> = emptyList(),
    val totalDistanceMeters: Double = 0.0,
    val pathPointCount: Int = points.size
)
