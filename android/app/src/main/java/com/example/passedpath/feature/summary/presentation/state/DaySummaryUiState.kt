package com.example.passedpath.feature.summary.presentation.state

data class DaySummaryUiState(
    val dateKey: String = "",
    val summary: DaySummaryContentUiState = DaySummaryContentUiState.Empty,
    val hasLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class DaySummaryContentUiState(
    val outingTimeText: String,
    val enterHomeTimeText: String,
    val totalOutingDurationText: String,
    val totalOutingCountText: String,
    val visitedDongNames: List<String> = emptyList(),
    val hasOutingTimeData: Boolean = true,
    val hasEnterHomeTimeData: Boolean = true,
    val hasTotalOutingDurationData: Boolean = true,
    val hasTotalOutingCountData: Boolean = true,
    val hasVisitedDongData: Boolean = true
) {
    companion object {
        val Empty = DaySummaryContentUiState(
            outingTimeText = DaySummaryNoDataText,
            enterHomeTimeText = DaySummaryNoDataText,
            totalOutingDurationText = DaySummaryNoDataText,
            totalOutingCountText = DaySummaryNoDataText,
            visitedDongNames = emptyList(),
            hasOutingTimeData = false,
            hasEnterHomeTimeData = false,
            hasTotalOutingDurationData = false,
            hasTotalOutingCountData = false,
            hasVisitedDongData = false
        )
    }
}

const val DaySummaryNoDataText = "\uB370\uC774\uD130 \uC5C6\uC74C"
