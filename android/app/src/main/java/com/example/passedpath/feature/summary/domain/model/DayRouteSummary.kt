package com.example.passedpath.feature.summary.domain.model

data class DayRouteSummary(
    val dateKey: String,
    val outingTime: String?,
    val enterHomeTime: String?,
    val totalOutingCount: Int?,
    val totalOutingSeconds: Long?,
    val totalOutingDurationText: String?,
    val visitedDongNames: List<String> = emptyList()
)
