package com.example.passedpath.feature.summary.data.remote.dto

data class DayRouteSummaryResponseDto(
    val outingTime: String?,
    val enterHomeTime: String?,
    val totalOutingCount: Int?,
    val totalOutingSeconds: Long?,
    val totalOutingDurationText: String?,
    val visitedDongName: List<String>? = null
)
