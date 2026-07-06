package com.example.passedpath.feature.summary.presentation.state

data class WeeklySummaryUiState(
    val summary: WeeklySummaryContentUiState = WeeklySummaryContentUiState.Empty,
    val hasLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class WeeklySummaryContentUiState(
    val metricCards: List<WeeklySummaryMetricCardUiState>,
    val visitedRegionsCard: WeeklySummaryVisitedRegionsCardUiState
) {
    companion object {
        val Empty = WeeklySummaryContentUiState(
            metricCards = emptyList(),
            visitedRegionsCard = WeeklySummaryVisitedRegionsCardUiState.Empty
        )
    }
}

data class WeeklySummaryMetricCardUiState(
    val title: String,
    val prefixLabel: String = DefaultPrefixLabel,
    val valueText: String,
    val bars: List<WeeklySummaryBarUiState>
)

data class WeeklySummaryBarUiState(
    val ratio: Float,
    val isHighlighted: Boolean,
    val hasData: Boolean
)

data class WeeklySummaryVisitedRegionsCardUiState(
    val title: String,
    val regions: List<WeeklySummaryVisitedRegionUiState>
) {
    companion object {
        val Empty = WeeklySummaryVisitedRegionsCardUiState(
            title = "",
            regions = emptyList()
        )
    }
}

data class WeeklySummaryVisitedRegionUiState(
    val rankText: String,
    val regionName: String
)

private const val DefaultPrefixLabel = "평균"
