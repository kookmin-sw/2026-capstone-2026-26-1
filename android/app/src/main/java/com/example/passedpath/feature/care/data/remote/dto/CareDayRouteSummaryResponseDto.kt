package com.example.passedpath.feature.care.data.remote.dto

data class CareDayRouteSummaryResponseDto(
    val outingTime: String?,
    val enterHomeTime: String?,
    val totalOutingCount: Int?,
    val totalOutingSeconds: Long?,
    val totalOutingDurationText: String?,
    val visitedDongName: List<String>? = null
)
