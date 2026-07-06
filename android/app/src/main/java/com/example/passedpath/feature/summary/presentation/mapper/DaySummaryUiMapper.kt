package com.example.passedpath.feature.summary.presentation.mapper

import com.example.passedpath.feature.summary.domain.model.DayRouteSummary
import com.example.passedpath.feature.summary.presentation.state.DaySummaryContentUiState
import com.example.passedpath.feature.summary.presentation.state.DaySummaryNoDataText
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun DayRouteSummary.toDaySummaryContentUiState(): DaySummaryContentUiState {
    val outingTimeText = outingTime.toSummaryTimeTextOrNull()
    val enterHomeTimeText = enterHomeTime.toSummaryTimeTextOrNull()
    val outingDurationText = totalOutingDurationText.normalizedOrNull()
        ?: totalOutingSeconds?.toDurationText()
    val outingCountText = totalOutingCount?.let { count -> "${count}${KoreanCount}" }

    return DaySummaryContentUiState(
        outingTimeText = outingTimeText ?: DaySummaryNoDataText,
        enterHomeTimeText = enterHomeTimeText ?: DaySummaryNoDataText,
        totalOutingDurationText = outingDurationText ?: DaySummaryNoDataText,
        totalOutingCountText = outingCountText ?: DaySummaryNoDataText,
        visitedDongNames = visitedDongNames,
        hasOutingTimeData = outingTimeText != null,
        hasEnterHomeTimeData = enterHomeTimeText != null,
        hasTotalOutingDurationData = outingDurationText != null,
        hasTotalOutingCountData = outingCountText != null,
        hasVisitedDongData = visitedDongNames.isNotEmpty()
    )
}

internal fun String?.toSummaryTimeText(): String {
    return toSummaryTimeTextOrNull() ?: EmptySummaryValue
}

private fun String?.toSummaryTimeTextOrNull(): String? {
    val timestamp = this?.trim()?.takeIf(String::isNotEmpty) ?: return null
    return runCatching {
        OffsetDateTime.parse(timestamp).format(SummaryTimeFormatter)
    }.getOrNull()
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

private val SummaryTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale.KOREA)

private const val SecondsPerHour = 3_600L
private const val SecondsPerMinute = 60L
private const val EmptySummaryValue = "-"
private const val KoreanHour = "\uC2DC\uAC04"
private const val KoreanMinute = "\uBD84"
private const val KoreanCount = "\uD68C"
