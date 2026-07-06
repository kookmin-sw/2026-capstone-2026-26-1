package com.example.passedpath.feature.care.domain.model

data class ProtectedPersonDaySummary(
    val dateKey: String,
    val outingTime: String?,
    val enterHomeTime: String?,
    val totalOutingCount: Int,
    val totalOutingSeconds: Long,
    val totalOutingDurationText: String,
    val visitedDongNames: List<String> = emptyList()
)
