package com.example.passedpath.feature.care.presentation.mapper

import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteListItem
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonRouteDateUiState
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun ProtectedPersonDayRouteListItem.toProtectedPersonRouteDateUiState():
    ProtectedPersonRouteDateUiState {
    return ProtectedPersonRouteDateUiState(
        dateKey = dateKey,
        dateText = dateKey.toRouteDateText(),
        outingTimeText = outingTime.toRouteHistoryTimeText(),
        enterHomeTimeText = enterHomeTime.toRouteHistoryTimeText(),
        outingCountText = "${totalOutingCount.coerceAtLeast(0)}$KoreanCount"
    )
}

private fun String.toRouteDateText(): String {
    return runCatching {
        val date = LocalDate.parse(this)
        "${date.monthValue}$KoreanMonth ${date.dayOfMonth}$KoreanDay"
    }.getOrDefault(this)
}

private fun String?.toRouteHistoryTimeText(): String {
    val timestamp = this?.trim()?.takeIf(String::isNotEmpty) ?: return EmptyValue

    return runCatching {
        OffsetDateTime.parse(timestamp).format(RouteHistoryTimeFormatter)
    }.getOrDefault(EmptyValue)
}

private val RouteHistoryTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale.KOREA)

private const val EmptyValue = "-"
private const val KoreanMonth = "\uC6D4"
private const val KoreanDay = "\uC77C"
private const val KoreanCount = "\uD68C"
