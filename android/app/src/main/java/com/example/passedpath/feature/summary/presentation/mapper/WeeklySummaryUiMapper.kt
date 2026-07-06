package com.example.passedpath.feature.summary.presentation.mapper

import com.example.passedpath.feature.summary.domain.model.WeeklyCountMetricDailyItem
import com.example.passedpath.feature.summary.domain.model.WeeklyCountMetricSection
import com.example.passedpath.feature.summary.domain.model.WeeklyDurationMetricDailyItem
import com.example.passedpath.feature.summary.domain.model.WeeklyDurationMetricSection
import com.example.passedpath.feature.summary.domain.model.WeeklyStatistics
import com.example.passedpath.feature.summary.domain.model.WeeklyTimeMetricDailyItem
import com.example.passedpath.feature.summary.domain.model.WeeklyTimeMetricSection
import com.example.passedpath.feature.summary.presentation.state.WeeklySummaryBarUiState
import com.example.passedpath.feature.summary.presentation.state.WeeklySummaryContentUiState
import com.example.passedpath.feature.summary.presentation.state.WeeklySummaryMetricCardUiState
import com.example.passedpath.feature.summary.presentation.state.WeeklySummaryVisitedRegionUiState
import com.example.passedpath.feature.summary.presentation.state.WeeklySummaryVisitedRegionsCardUiState

internal fun WeeklyStatistics.toWeeklySummaryContentUiState(): WeeklySummaryContentUiState {
    return WeeklySummaryContentUiState(
        metricCards = listOf(
            outingTime.toWeeklyTimeMetricCard(
                title = WeeklyOutingTimeTitle
            ),
            enterHomeTime.toWeeklyTimeMetricCard(
                title = WeeklyEnterHomeTimeTitle
            ),
            totalOutingSeconds.toWeeklyDurationMetricCard(
                title = WeeklyTotalOutingDurationTitle
            ),
            totalOutingCount.toWeeklyCountMetricCard(
                title = WeeklyOutingCountTitle
            )
        ),
        visitedRegionsCard = WeeklySummaryVisitedRegionsCardUiState(
            title = WeeklyVisitedRegionsTitle,
            regions = visitedRegions.topRegions
                .sortedBy { region -> region.rank }
                .take(MaxVisitedRegionCount)
                .map { region ->
                    WeeklySummaryVisitedRegionUiState(
                        rankText = "${region.rank}위",
                        regionName = region.regionName
                    )
                }
        )
    )
}

private fun WeeklyTimeMetricSection.toWeeklyTimeMetricCard(
    title: String
): WeeklySummaryMetricCardUiState {
    return WeeklySummaryMetricCardUiState(
        title = title,
        valueText = average.displayText.toSummaryValueText(),
        bars = sevenDays.take(MaxBarCount).toTimeBars()
    )
}

private fun WeeklyDurationMetricSection.toWeeklyDurationMetricCard(
    title: String
): WeeklySummaryMetricCardUiState {
    return WeeklySummaryMetricCardUiState(
        title = title,
        valueText = average.displayText.toSummaryValueText(),
        bars = sevenDays.take(MaxBarCount).toZeroBasedBars { item ->
            item.value?.toDouble().takeIf { item.hasDayRoute }
        }
    )
}

private fun WeeklyCountMetricSection.toWeeklyCountMetricCard(
    title: String
): WeeklySummaryMetricCardUiState {
    return WeeklySummaryMetricCardUiState(
        title = title,
        valueText = average.displayText.toSummaryValueText(),
        bars = sevenDays.take(MaxBarCount).toZeroBasedBars { item ->
            item.value.takeIf { item.hasDayRoute }
        }
    )
}

private fun List<WeeklyTimeMetricDailyItem>.toTimeBars(): List<WeeklySummaryBarUiState> {
    val values = map { item ->
        item.value?.toDouble().takeIf { item.hasDayRoute }
    }
    val availableValues = values.filterNotNull()
    val minValue = availableValues.minOrNull()
    val maxValue = availableValues.maxOrNull()
    val highlightedIndex = values.indexOfLast { value -> value != null }

    return values.mapIndexed { index, value ->
        WeeklySummaryBarUiState(
            ratio = value.toRangeRatio(minValue, maxValue),
            isHighlighted = index == highlightedIndex && value != null,
            hasData = value != null
        )
    }
}

private fun <T> List<T>.toZeroBasedBars(
    valueSelector: (T) -> Double?
): List<WeeklySummaryBarUiState> {
    val values = map(valueSelector)
    val maxValue = values.filterNotNull().maxOrNull()
    val highlightedIndex = values.indexOfLast { value -> value != null }

    return values.mapIndexed { index, value ->
        WeeklySummaryBarUiState(
            ratio = value.toZeroBasedRatio(maxValue),
            isHighlighted = index == highlightedIndex && value != null,
            hasData = value != null
        )
    }
}

private fun Double?.toRangeRatio(
    minValue: Double?,
    maxValue: Double?
): Float {
    val value = this ?: return EmptyBarRatio
    if (minValue == null || maxValue == null) return DataBarMinRatio
    val range = maxValue - minValue
    if (range <= 0.0) return FullBarRatio

    return (DataBarMinRatio + ((value - minValue) / range).toFloat() * DataBarRatioRange)
        .coerceIn(DataBarMinRatio, FullBarRatio)
}

private fun Double?.toZeroBasedRatio(maxValue: Double?): Float {
    val value = this ?: return EmptyBarRatio
    if (maxValue == null || maxValue <= 0.0) return DataBarMinRatio

    return (value / maxValue).toFloat()
        .coerceIn(DataBarMinRatio, FullBarRatio)
}

private fun String?.toSummaryValueText(): String {
    return this?.trim()?.takeIf(String::isNotEmpty) ?: EmptySummaryValue
}

private const val WeeklyOutingTimeTitle = "주간 외출 시간"
private const val WeeklyEnterHomeTimeTitle = "주간 귀가 시간"
private const val WeeklyTotalOutingDurationTitle = "주간 총 외출 시간"
private const val WeeklyOutingCountTitle = "주간 외출 횟수"
private const val WeeklyVisitedRegionsTitle = "자주 방문한 동네"
private const val EmptySummaryValue = "-"
private const val MaxBarCount = 7
private const val MaxVisitedRegionCount = 2
private const val EmptyBarRatio = 0.18f
private const val DataBarMinRatio = 0.32f
private const val FullBarRatio = 1f
private const val DataBarRatioRange = FullBarRatio - DataBarMinRatio
