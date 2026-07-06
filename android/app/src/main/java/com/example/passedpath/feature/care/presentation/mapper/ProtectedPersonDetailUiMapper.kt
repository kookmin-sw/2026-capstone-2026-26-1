package com.example.passedpath.feature.care.presentation.mapper

import com.example.passedpath.feature.care.domain.model.ProtectedPersonDaySummary
import com.example.passedpath.feature.care.domain.model.ProtectedPersonVisitedPlace
import com.example.passedpath.feature.care.domain.model.ProtectedPersonVisitedPlaceList
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonPlaceListUiState
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonPlaceUiState
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonSummaryContentUiState
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun ProtectedPersonVisitedPlaceList.toProtectedPersonPlaceListUiState():
    ProtectedPersonPlaceListUiState {
    val sortedPlaces = places
        .sortedBy(ProtectedPersonVisitedPlace::orderIndex)
        .map(ProtectedPersonVisitedPlace::toProtectedPersonPlaceUiState)

    return ProtectedPersonPlaceListUiState(
        places = sortedPlaces,
        placeCount = placeCount,
        hasLoaded = true
    )
}

fun ProtectedPersonDaySummary.toProtectedPersonSummaryContentUiState():
    ProtectedPersonSummaryContentUiState {
    return ProtectedPersonSummaryContentUiState(
        outingTimeText = outingTime.toSummaryTimeText(),
        enterHomeTimeText = enterHomeTime.toSummaryTimeText(),
        totalOutingDurationText = totalOutingDurationText.ifBlank { EmptyValue },
        totalOutingCountText = "$totalOutingCount$KoreanCount",
        visitedDongNames = visitedDongNames
    )
}

private fun ProtectedPersonVisitedPlace.toProtectedPersonPlaceUiState():
    ProtectedPersonPlaceUiState {
    return ProtectedPersonPlaceUiState(
        placeId = placeId,
        placeName = placeName,
        roadAddress = roadAddress,
        latitude = latitude,
        longitude = longitude,
        orderIndex = orderIndex,
        startTimeText = startTime.toPlaceCardTimeText(),
        endTimeText = endTime.toPlaceCardTimeText(),
        isFavoritePlace = bookmarkType != null
    )
}

private fun String?.toPlaceCardTimeText(): String? {
    val timestamp = this?.trim()?.takeIf(String::isNotEmpty) ?: return null
    return runCatching {
        OffsetDateTime.parse(timestamp).format(PlaceCardTimeFormatter)
    }.getOrNull()
}

private fun String?.toSummaryTimeText(): String {
    val timestamp = this?.trim()?.takeIf(String::isNotEmpty) ?: return EmptyValue
    return runCatching {
        OffsetDateTime.parse(timestamp).format(SummaryTimeFormatter)
    }.getOrDefault(EmptyValue)
}

private val PlaceCardTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA)

private val SummaryTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale.KOREA)

private const val EmptyValue = "-"
private const val KoreanCount = "\uD68C"
