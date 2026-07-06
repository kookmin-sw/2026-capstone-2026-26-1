package com.example.passedpath.feature.care.data.remote.mapper

import com.example.passedpath.feature.care.data.remote.dto.CareDayRouteSummaryResponseDto
import com.example.passedpath.feature.care.domain.model.ProtectedPersonDaySummary

internal fun CareDayRouteSummaryResponseDto.toProtectedPersonDaySummary(
    dateKey: String
): ProtectedPersonDaySummary {
    val seconds = totalOutingSeconds ?: 0L

    return ProtectedPersonDaySummary(
        dateKey = dateKey,
        outingTime = outingTime.normalizedOrNull(),
        enterHomeTime = enterHomeTime.normalizedOrNull(),
        totalOutingCount = totalOutingCount ?: 0,
        totalOutingSeconds = seconds,
        totalOutingDurationText = totalOutingDurationText.normalizedOrNull()
            ?: seconds.toDurationText(),
        visitedDongNames = visitedDongName.orEmpty()
            .map(String::trim)
            .filter(String::isNotEmpty)
    )
}

private fun String?.normalizedOrNull(): String? {
    return this?.trim()?.takeIf(String::isNotEmpty)
}

private fun Long.toDurationText(): String {
    val safeSeconds = coerceAtLeast(0L)
    val hours = safeSeconds / SecondsPerHour
    val minutes = (safeSeconds % SecondsPerHour) / SecondsPerMinute

    return when {
        hours > 0L && minutes > 0L -> "${hours}${KoreanHour} ${minutes}${KoreanMinute}"
        hours > 0L -> "${hours}${KoreanHour}"
        minutes > 0L -> "${minutes}${KoreanMinute}"
        else -> "0${KoreanMinute}"
    }
}

private const val SecondsPerHour = 3_600L
private const val SecondsPerMinute = 60L
private const val KoreanHour = "\uC2DC\uAC04"
private const val KoreanMinute = "\uBD84"
