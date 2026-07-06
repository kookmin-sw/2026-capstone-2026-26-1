package com.example.passedpath.feature.summary.presentation.mapper

import com.example.passedpath.feature.summary.domain.model.PlaceStatisticsItem
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import com.example.passedpath.feature.summary.domain.model.VisitStatistics
import com.example.passedpath.feature.summary.domain.model.VisitedRegionStatisticsItem
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriod
import com.example.passedpath.feature.summary.presentation.state.VisitPlaceRankingItemUiState
import com.example.passedpath.feature.summary.presentation.state.VisitPlaceRankingUiState
import com.example.passedpath.feature.summary.presentation.state.VisitRegionDistributionItemUiState
import com.example.passedpath.feature.summary.presentation.state.VisitRegionDistributionUiState
import com.example.passedpath.feature.summary.presentation.state.VisitStatisticsContentUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun VisitStatistics.toVisitStatisticsContentUiState(): VisitStatisticsContentUiState {
    return VisitStatisticsContentUiState(
        dateRangeText = formatDateRange(startDate, endDate),
        visitedRegions = VisitRegionDistributionUiState(
            totalVisitCountText = visitedRegions.totalVisitCount.toVisitCountText(),
            items = visitedRegions.items.map { item -> item.toVisitRegionDistributionItemUiState() }
        ),
        places = VisitPlaceRankingUiState(
            totalVisitCountText = places.totalVisitCount.toVisitCountText(),
            items = places.items.map { item -> item.toVisitPlaceRankingItemUiState() }
        )
    )
}

internal fun SummaryDetailPeriod.toStatisticsPeriod(): StatisticsPeriod {
    return when (this) {
        SummaryDetailPeriod.WEEK -> StatisticsPeriod.WEEK
        SummaryDetailPeriod.MONTH -> StatisticsPeriod.MONTH
        SummaryDetailPeriod.SIX_MONTHS -> StatisticsPeriod.SIX_MONTHS
        SummaryDetailPeriod.YEAR -> StatisticsPeriod.YEAR
    }
}

internal fun StatisticsPeriod.toSummaryDetailPeriod(): SummaryDetailPeriod {
    return when (this) {
        StatisticsPeriod.WEEK -> SummaryDetailPeriod.WEEK
        StatisticsPeriod.MONTH -> SummaryDetailPeriod.MONTH
        StatisticsPeriod.SIX_MONTHS -> SummaryDetailPeriod.SIX_MONTHS
        StatisticsPeriod.YEAR -> SummaryDetailPeriod.YEAR
    }
}

private fun VisitedRegionStatisticsItem.toVisitRegionDistributionItemUiState(): VisitRegionDistributionItemUiState {
    return VisitRegionDistributionItemUiState(
        rankText = "${rank}위",
        regionName = regionName,
        visitCountText = visitCount.toVisitCountText(),
        displayRatio = displayRatio,
        ratio = (ratio / 100.0).toFloat().coerceIn(0f, 1f)
    )
}

private fun PlaceStatisticsItem.toVisitPlaceRankingItemUiState(): VisitPlaceRankingItemUiState {
    return VisitPlaceRankingItemUiState(
        rankText = "${rank}위",
        placeName = placeName,
        roadAddress = roadAddress,
        displayVisitCount = displayVisitCount
    )
}

private fun Int.toVisitCountText(): String {
    return "${coerceAtLeast(0)}회"
}

private fun formatDateRange(
    startDateText: String,
    endDateText: String
): String {
    val startDate = parseDateOrNull(startDateText)
    val endDate = parseDateOrNull(endDateText)
    if (startDate == null || endDate == null) return ""

    return if (startDate.year == endDate.year) {
        "${startDate.format(DateRangeStartFormatter)} ~ ${endDate.format(DateRangeEndFormatter)}"
    } else {
        "${startDate.format(DateRangeStartFormatter)} ~ ${endDate.format(DateRangeStartFormatter)}"
    }
}

private fun parseDateOrNull(value: String): LocalDate? {
    return runCatching { LocalDate.parse(value) }.getOrNull()
}

private val DateRangeStartFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREA)
private val DateRangeEndFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MM.dd", Locale.KOREA)
