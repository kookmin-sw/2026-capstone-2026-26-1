package com.example.passedpath.feature.summary.presentation.state

enum class SummaryDetailMetric(
    val routeValue: String
) {
    OUTING_TIME("outing_time"),
    ENTER_HOME_TIME("enter_home_time"),
    TOTAL_OUTING_DURATION("total_outing_duration"),
    TOTAL_OUTING_COUNT("total_outing_count"),
    VISITS("visits");

    companion object {
        fun fromRouteValue(routeValue: String?): SummaryDetailMetric {
            return entries.firstOrNull { metric -> metric.routeValue == routeValue } ?: OUTING_TIME
        }

        fun fromMetricCardIndex(index: Int): SummaryDetailMetric? {
            return when (index) {
                0 -> OUTING_TIME
                1 -> ENTER_HOME_TIME
                2 -> TOTAL_OUTING_DURATION
                3 -> TOTAL_OUTING_COUNT
                else -> null
            }
        }
    }
}

enum class SummaryDetailPeriod {
    WEEK,
    MONTH,
    SIX_MONTHS,
    YEAR
}

data class SummaryDetailUiState(
    val metric: SummaryDetailMetric,
    val selectedPeriod: SummaryDetailPeriod,
    val periodOptions: List<SummaryDetailPeriodOptionUiState>,
    val dateRange: SummaryDetailDateRangeUiState,
    val chart: SummaryDetailChartUiState,
    val highlights: List<SummaryDetailHighlightCardUiState>
)

data class SummaryDetailPeriodOptionUiState(
    val period: SummaryDetailPeriod,
    val label: String
)

data class SummaryDetailDateRangeUiState(
    val rangeText: String,
    val canMovePrevious: Boolean = true,
    val canMoveNext: Boolean = true
)

data class SummaryDetailChartUiState(
    val averageLabel: String,
    val averageValueText: String,
    val hasAverageData: Boolean = true,
    val yAxisLabels: List<String>,
    val bars: List<SummaryDetailChartBarUiState>
)

data class SummaryDetailChartBarUiState(
    val label: String,
    val ratio: Float,
    val hasData: Boolean = true,
    val isZeroValue: Boolean = false,
    val showLabel: Boolean = true
)

data class SummaryDetailHighlightCardUiState(
    val title: String,
    val description: String,
    val comparisons: List<SummaryDetailComparisonBarUiState>
)

data class SummaryDetailComparisonBarUiState(
    val label: String,
    val valueText: String,
    val ratio: Float,
    val isPrimary: Boolean
)

data class SummaryDetailViewModelUiState(
    val metric: SummaryDetailMetric = SummaryDetailMetric.TOTAL_OUTING_DURATION,
    val selectedPeriod: SummaryDetailPeriod = SummaryDetailPeriod.WEEK,
    val content: SummaryDetailUiState? = null,
    val hasLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
