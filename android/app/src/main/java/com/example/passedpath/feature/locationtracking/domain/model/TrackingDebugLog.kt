package com.example.passedpath.feature.locationtracking.domain.model

data class TrackingDebugLog(
    val recordedAtEpochMillis: Long,
    val category: String,
    val dateKey: String?,
    val message: String
)
