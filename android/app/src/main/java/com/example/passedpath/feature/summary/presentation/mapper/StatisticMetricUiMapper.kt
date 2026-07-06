package com.example.passedpath.feature.summary.presentation.mapper

import com.example.passedpath.feature.summary.domain.model.HighlightMetricValue
import com.example.passedpath.feature.summary.domain.model.StatisticMetric
import com.example.passedpath.feature.summary.domain.model.StatisticMetricBarItem
import com.example.passedpath.feature.summary.domain.model.StatisticMetricHighlight
import com.example.passedpath.feature.summary.presentation.state.DaySummaryNoDataText
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailChartBarUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailChartUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailComparisonBarUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailDateRangeUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailHighlightCardUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailMetric
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriod
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriodOptionUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToLong

internal fun StatisticMetric.toOutingTimeSummaryDetailUiState(): SummaryDetailUiState {
    return toTimeSummaryDetailUiState(
        metric = SummaryDetailMetric.OUTING_TIME,
        defaultAxisRange = TimeAxisRange(
            minMinutes = DefaultOutingTimeAxisMin,
            maxMinutes = DefaultOutingTimeAxisMax
        )
    )
}

internal fun StatisticMetric.toEnterHomeTimeSummaryDetailUiState(): SummaryDetailUiState {
    return toTimeSummaryDetailUiState(
        metric = SummaryDetailMetric.ENTER_HOME_TIME,
        defaultAxisRange = TimeAxisRange(
            minMinutes = DefaultEnterHomeTimeAxisMin,
            maxMinutes = DefaultEnterHomeTimeAxisMax
        )
    )
}

private fun StatisticMetric.toTimeSummaryDetailUiState(
    metric: SummaryDetailMetric,
    defaultAxisRange: TimeAxisRange
): SummaryDetailUiState {
    val selectedPeriod = period.toSummaryDetailPeriod()
    val yAxisRange = calculateTimeAxisRange(defaultAxisRange = defaultAxisRange)

    return SummaryDetailUiState(
        metric = metric,
        selectedPeriod = selectedPeriod,
        periodOptions = DefaultSummaryDetailPeriodOptions,
        dateRange = SummaryDetailDateRangeUiState(
            rangeText = formatMetricDateRange(startDate, endDate),
            canMovePrevious = false,
            canMoveNext = false
        ),
        chart = SummaryDetailChartUiState(
            averageLabel = DefaultAverageLabel,
            averageValueText = average.displayText.normalizedOrNull()
                ?: average.value?.roundToLong()?.formatTimeText()
                ?: DaySummaryNoDataText,
            hasAverageData = average.value != null,
            yAxisLabels = yAxisRange.toTimeAxisLabels(),
            bars = bars.mapIndexed { index, bar ->
                bar.toTimeChartBarUiState(
                    period = selectedPeriod,
                    index = index,
                    lastIndex = bars.lastIndex,
                    yAxisRange = yAxisRange
                )
            }
        ),
        highlights = highlight.toTimeHighlightUiStates(selectedPeriod)
    )
}

internal fun StatisticMetric.toTotalOutingDurationSummaryDetailUiState(): SummaryDetailUiState {
    val selectedPeriod = period.toSummaryDetailPeriod()
    val yAxisMaxSeconds = calculateDurationAxisMaxSeconds()

    return SummaryDetailUiState(
        metric = SummaryDetailMetric.TOTAL_OUTING_DURATION,
        selectedPeriod = selectedPeriod,
        periodOptions = DefaultSummaryDetailPeriodOptions,
        dateRange = SummaryDetailDateRangeUiState(
            rangeText = formatMetricDateRange(startDate, endDate),
            canMovePrevious = false,
            canMoveNext = false
        ),
        chart = SummaryDetailChartUiState(
            averageLabel = DefaultAverageLabel,
            averageValueText = average.displayText.normalizedOrNull()
                ?: average.value?.roundToLong()?.formatDurationText()
                ?: DaySummaryNoDataText,
            hasAverageData = average.value != null,
            yAxisLabels = yAxisMaxSeconds.toDurationAxisLabels(),
            bars = bars.mapIndexed { index, bar ->
                bar.toDurationChartBarUiState(
                    period = selectedPeriod,
                    index = index,
                    lastIndex = bars.lastIndex,
                    yAxisMaxSeconds = yAxisMaxSeconds
                )
            }
        ),
        highlights = highlight.toDurationHighlightUiStates(selectedPeriod)
    )
}

internal fun StatisticMetric.toTotalOutingCountSummaryDetailUiState(): SummaryDetailUiState {
    val selectedPeriod = period.toSummaryDetailPeriod()
    val yAxisMaxCount = calculateCountAxisMax()

    return SummaryDetailUiState(
        metric = SummaryDetailMetric.TOTAL_OUTING_COUNT,
        selectedPeriod = selectedPeriod,
        periodOptions = DefaultSummaryDetailPeriodOptions,
        dateRange = SummaryDetailDateRangeUiState(
            rangeText = formatMetricDateRange(startDate, endDate),
            canMovePrevious = false,
            canMoveNext = false
        ),
        chart = SummaryDetailChartUiState(
            averageLabel = DefaultAverageLabel,
            averageValueText = average.displayText.normalizedOrNull()
                ?: average.value?.formatCountText()
                ?: DaySummaryNoDataText,
            hasAverageData = average.value != null,
            yAxisLabels = yAxisMaxCount.toCountAxisLabels(),
            bars = bars.mapIndexed { index, bar ->
                bar.toCountChartBarUiState(
                    period = selectedPeriod,
                    index = index,
                    lastIndex = bars.lastIndex,
                    yAxisMaxCount = yAxisMaxCount
                )
            }
        ),
        highlights = highlight.toCountHighlightUiStates(selectedPeriod)
    )
}

private fun StatisticMetric.calculateDurationAxisMaxSeconds(): Long {
    val values = bars.asSequence()
        .filter { bar -> bar.hasValue }
        .mapNotNull { bar -> bar.value }
        .toList() + listOfNotNull(average.value)
    val maxValue = (values.maxOrNull() ?: 0.0).coerceAtLeast(0.0)

    return maxValue.toNiceDurationAxisMaxSeconds()
}

private fun Double.toNiceDurationAxisMaxSeconds(): Long {
    val seconds = ceil(this).toLong()
    val candidates = listOf(
        3_600L,
        7_200L,
        14_400L,
        21_600L,
        28_800L,
        43_200L,
        64_800L,
        86_400L
    )

    candidates.firstOrNull { candidate -> seconds <= candidate }?.let { return it }

    val sixHours = 21_600L
    return ceil(seconds.toDouble() / sixHours).toLong() * sixHours
}

private fun StatisticMetric.calculateCountAxisMax(): Double {
    val values = bars.asSequence()
        .filter { bar -> bar.hasValue }
        .mapNotNull { bar -> bar.value }
        .toList() + listOfNotNull(average.value)
    val maxValue = (values.maxOrNull() ?: 0.0).coerceAtLeast(0.0)

    return ceil(maxValue).coerceAtLeast(MinimumCountAxisMax)
}

private fun StatisticMetric.calculateTimeAxisRange(defaultAxisRange: TimeAxisRange): TimeAxisRange {
    val values = bars.asSequence()
        .filter { bar -> bar.hasValue }
        .mapNotNull { bar -> bar.value }
        .toList() + listOfNotNull(average.value)

    if (values.isEmpty()) {
        return defaultAxisRange
    }

    val minValue = values.minOrNull()?.coerceAtLeast(0.0) ?: defaultAxisRange.minMinutes.toDouble()
    val maxValue = values.maxOrNull()?.coerceAtLeast(0.0) ?: defaultAxisRange.maxMinutes.toDouble()
    val axisMin = floor(minValue / TimeAxisStepMinutes).toLong() * TimeAxisStepMinutes
    var axisMax = ceil(maxValue / TimeAxisStepMinutes).toLong() * TimeAxisStepMinutes

    if (axisMax - axisMin < MinimumTimeAxisRangeMinutes) {
        axisMax = axisMin + MinimumTimeAxisRangeMinutes
    }

    return TimeAxisRange(
        minMinutes = axisMin.coerceAtLeast(0L),
        maxMinutes = axisMax.coerceAtLeast(MinimumTimeAxisRangeMinutes)
    )
}

private fun Long.toDurationAxisLabels(): List<String> {
    return listOf(
        formatDurationText(),
        (this / 2L).formatDurationText(),
        ZeroDurationText
    )
}

private fun StatisticMetricBarItem.toDurationChartBarUiState(
    period: SummaryDetailPeriod,
    index: Int,
    lastIndex: Int,
    yAxisMaxSeconds: Long
): SummaryDetailChartBarUiState {
    val hasBarData = hasValue && value != null
    val safeValue = value?.coerceAtLeast(0.0) ?: 0.0

    return SummaryDetailChartBarUiState(
        label = label,
        ratio = if (hasBarData && yAxisMaxSeconds > 0L) {
            (safeValue / yAxisMaxSeconds.toDouble()).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        },
        hasData = hasBarData,
        isZeroValue = hasBarData && safeValue == 0.0,
        showLabel = shouldShowBarLabel(period = period, index = index, lastIndex = lastIndex)
    )
}

private fun StatisticMetricBarItem.toCountChartBarUiState(
    period: SummaryDetailPeriod,
    index: Int,
    lastIndex: Int,
    yAxisMaxCount: Double
): SummaryDetailChartBarUiState {
    val hasBarData = hasValue && value != null
    val safeValue = value?.coerceAtLeast(0.0) ?: 0.0

    return SummaryDetailChartBarUiState(
        label = label,
        ratio = if (hasBarData && yAxisMaxCount > 0.0) {
            (safeValue / yAxisMaxCount).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        },
        hasData = hasBarData,
        isZeroValue = hasBarData && safeValue == 0.0,
        showLabel = shouldShowBarLabel(period = period, index = index, lastIndex = lastIndex)
    )
}

private fun StatisticMetricBarItem.toTimeChartBarUiState(
    period: SummaryDetailPeriod,
    index: Int,
    lastIndex: Int,
    yAxisRange: TimeAxisRange
): SummaryDetailChartBarUiState {
    val hasBarData = hasValue && value != null
    val safeValue = value?.coerceAtLeast(0.0) ?: 0.0

    return SummaryDetailChartBarUiState(
        label = label,
        ratio = if (hasBarData) {
            yAxisRange.toRatio(value = safeValue)
        } else {
            0f
        },
        hasData = hasBarData,
        isZeroValue = hasBarData && safeValue == 0.0,
        showLabel = shouldShowBarLabel(period = period, index = index, lastIndex = lastIndex)
    )
}

private fun shouldShowBarLabel(
    period: SummaryDetailPeriod,
    index: Int,
    lastIndex: Int
): Boolean {
    return when (period) {
        SummaryDetailPeriod.MONTH -> index == 0 ||
            index == lastIndex ||
            (index % 7 == 0 && index + 7 <= lastIndex)
        SummaryDetailPeriod.WEEK,
        SummaryDetailPeriod.SIX_MONTHS,
        SummaryDetailPeriod.YEAR -> true
    }
}

private fun StatisticMetricHighlight.toDurationHighlightUiStates(
    period: SummaryDetailPeriod
): List<SummaryDetailHighlightCardUiState> {
    if (title.isBlank() && message.isBlank() && current.label.isBlank() && previous.label.isBlank()) {
        return emptyList()
    }

    val maxValue = listOfNotNull(current.value, previous.value)
        .maxOrNull()
        ?.coerceAtLeast(0.0)
        ?: 0.0

    return listOf(
        SummaryDetailHighlightCardUiState(
            title = title.ifBlank { period.currentHighlightLabel() },
            description = message,
            comparisons = listOf(
                current.toDurationComparisonBarUiState(
                    fallbackLabel = period.currentHighlightLabel(),
                    maxValue = maxValue,
                    isPrimary = true
                ),
                previous.toDurationComparisonBarUiState(
                    fallbackLabel = period.previousHighlightLabel(),
                    maxValue = maxValue,
                    isPrimary = false
                )
            )
        )
    )
}

private fun StatisticMetricHighlight.toCountHighlightUiStates(
    period: SummaryDetailPeriod
): List<SummaryDetailHighlightCardUiState> {
    if (title.isBlank() && message.isBlank() && current.label.isBlank() && previous.label.isBlank()) {
        return emptyList()
    }

    val maxValue = listOfNotNull(current.value, previous.value)
        .maxOrNull()
        ?.coerceAtLeast(0.0)
        ?: 0.0

    return listOf(
        SummaryDetailHighlightCardUiState(
            title = title.ifBlank { period.currentHighlightLabel() },
            description = message,
            comparisons = listOf(
                current.toCountComparisonBarUiState(
                    fallbackLabel = period.currentHighlightLabel(),
                    maxValue = maxValue,
                    isPrimary = true
                ),
                previous.toCountComparisonBarUiState(
                    fallbackLabel = period.previousHighlightLabel(),
                    maxValue = maxValue,
                    isPrimary = false
                )
            )
        )
    )
}

private fun StatisticMetricHighlight.toTimeHighlightUiStates(
    period: SummaryDetailPeriod
): List<SummaryDetailHighlightCardUiState> {
    if (title.isBlank() && message.isBlank() && current.label.isBlank() && previous.label.isBlank()) {
        return emptyList()
    }

    val maxValue = listOfNotNull(current.value, previous.value)
        .maxOrNull()
        ?.coerceAtLeast(0.0)
        ?: 0.0

    return listOf(
        SummaryDetailHighlightCardUiState(
            title = title.ifBlank { period.currentHighlightLabel() },
            description = message,
            comparisons = listOf(
                current.toTimeComparisonBarUiState(
                    fallbackLabel = period.currentHighlightLabel(),
                    maxValue = maxValue,
                    isPrimary = true
                ),
                previous.toTimeComparisonBarUiState(
                    fallbackLabel = period.previousHighlightLabel(),
                    maxValue = maxValue,
                    isPrimary = false
                )
            )
        )
    )
}

private fun HighlightMetricValue.toDurationComparisonBarUiState(
    fallbackLabel: String,
    maxValue: Double,
    isPrimary: Boolean
): SummaryDetailComparisonBarUiState {
    val safeValue = value?.coerceAtLeast(0.0)
    return SummaryDetailComparisonBarUiState(
        label = label.ifBlank { fallbackLabel },
        valueText = displayText.normalizedOrNull()
            ?: safeValue?.roundToLong()?.formatDurationText()
            ?: DaySummaryNoDataText,
        ratio = if (safeValue != null && maxValue > 0.0) {
            (safeValue / maxValue).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        },
        isPrimary = isPrimary
    )
}

private fun HighlightMetricValue.toCountComparisonBarUiState(
    fallbackLabel: String,
    maxValue: Double,
    isPrimary: Boolean
): SummaryDetailComparisonBarUiState {
    val safeValue = value?.coerceAtLeast(0.0)
    return SummaryDetailComparisonBarUiState(
        label = label.ifBlank { fallbackLabel },
        valueText = displayText.normalizedOrNull()
            ?: safeValue?.formatCountText()
            ?: DaySummaryNoDataText,
        ratio = if (safeValue != null && maxValue > 0.0) {
            (safeValue / maxValue).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        },
        isPrimary = isPrimary
    )
}

private fun HighlightMetricValue.toTimeComparisonBarUiState(
    fallbackLabel: String,
    maxValue: Double,
    isPrimary: Boolean
): SummaryDetailComparisonBarUiState {
    val safeValue = value?.coerceAtLeast(0.0)
    return SummaryDetailComparisonBarUiState(
        label = label.ifBlank { fallbackLabel },
        valueText = displayText.normalizedOrNull()
            ?: safeValue?.roundToLong()?.formatTimeText()
            ?: DaySummaryNoDataText,
        ratio = if (safeValue != null && maxValue > 0.0) {
            (safeValue / maxValue).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        },
        isPrimary = isPrimary
    )
}

private fun SummaryDetailPeriod.currentHighlightLabel(): String {
    return when (this) {
        SummaryDetailPeriod.WEEK -> "\uC774\uBC88 \uC8FC"
        SummaryDetailPeriod.MONTH -> "\uC774\uBC88 \uB2EC"
        SummaryDetailPeriod.SIX_MONTHS -> "\uCD5C\uADFC 6\uAC1C\uC6D4"
        SummaryDetailPeriod.YEAR -> "\uCD5C\uADFC 1\uB144"
    }
}

private fun SummaryDetailPeriod.previousHighlightLabel(): String {
    return when (this) {
        SummaryDetailPeriod.WEEK -> "\uC9C0\uB09C\uC8FC"
        SummaryDetailPeriod.MONTH -> "\uC9C0\uB09C\uB2EC"
        SummaryDetailPeriod.SIX_MONTHS -> "\uC774\uC804 6\uAC1C\uC6D4"
        SummaryDetailPeriod.YEAR -> "\uC774\uC804 1\uB144"
    }
}

private fun String?.normalizedOrNull(): String? {
    return this?.trim()?.takeIf(String::isNotEmpty)
}

private fun Long.formatDurationText(): String {
    val totalMinutes = (this / 60L).coerceAtLeast(0L)
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L

    return when {
        hours <= 0L -> "${totalMinutes}\uBD84"
        minutes == 0L -> "${hours}\uC2DC\uAC04"
        else -> "${hours}\uC2DC\uAC04 ${minutes}\uBD84"
    }
}

private fun Double.formatCountText(): String {
    return String.format(Locale.KOREA, "%.1f\uD68C", coerceAtLeast(0.0))
}

private fun Double.toCountAxisLabels(): List<String> {
    return listOf(
        formatCountAxisLabel(),
        (this / 2.0).formatCountAxisLabel(),
        ZeroCountText
    )
}

private fun Double.formatCountAxisLabel(): String {
    val safeValue = coerceAtLeast(0.0)
    return if (safeValue % 1.0 == 0.0) {
        safeValue.toInt().toString()
    } else {
        String.format(Locale.KOREA, "%.1f", safeValue)
    }
}

private fun Long.formatTimeText(): String {
    val normalizedMinutes = coerceAtLeast(0L)
    val hours = normalizedMinutes / 60L
    val minutes = normalizedMinutes % 60L
    return String.format(Locale.KOREA, "%02d:%02d", hours, minutes)
}

private fun TimeAxisRange.toTimeAxisLabels(): List<String> {
    val middle = minMinutes + ((maxMinutes - minMinutes) / 2L)
    return listOf(
        maxMinutes.formatTimeText(),
        middle.formatTimeText(),
        minMinutes.formatTimeText()
    )
}

private fun TimeAxisRange.toRatio(value: Double): Float {
    val denominator = (maxMinutes - minMinutes).coerceAtLeast(1L).toDouble()
    return ((value - minMinutes.toDouble()) / denominator).toFloat().coerceIn(0f, 1f)
}

private fun formatMetricDateRange(
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

private val DefaultSummaryDetailPeriodOptions = listOf(
    SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.WEEK, "1\uC8FC"),
    SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.MONTH, "1\uAC1C\uC6D4"),
    SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.SIX_MONTHS, "6\uAC1C\uC6D4"),
    SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.YEAR, "1\uB144")
)
private const val DefaultAverageLabel = "\uD3C9\uADE0"
private const val ZeroDurationText = "0\uBD84"
private const val ZeroCountText = "0"
private const val MinimumCountAxisMax = 1.0
private const val TimeAxisStepMinutes = 30L
private const val MinimumTimeAxisRangeMinutes = 60L
private const val DefaultOutingTimeAxisMin = 540L
private const val DefaultOutingTimeAxisMax = 600L
private const val DefaultEnterHomeTimeAxisMin = 1_380L
private const val DefaultEnterHomeTimeAxisMax = 1_440L
private data class TimeAxisRange(
    val minMinutes: Long,
    val maxMinutes: Long
)
private val DateRangeStartFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREA)
private val DateRangeEndFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MM.dd", Locale.KOREA)
