package com.example.passedpath.feature.summary.presentation.state

data class VisitStatisticsUiState(
    val selectedPeriod: SummaryDetailPeriod = SummaryDetailPeriod.WEEK,
    val content: VisitStatisticsContentUiState = VisitStatisticsContentUiState.Empty,
    val hasLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class VisitStatisticsContentUiState(
    val dateRangeText: String,
    val visitedRegions: VisitRegionDistributionUiState,
    val places: VisitPlaceRankingUiState
) {
    companion object {
        val Empty = VisitStatisticsContentUiState(
            dateRangeText = "",
            visitedRegions = VisitRegionDistributionUiState.Empty,
            places = VisitPlaceRankingUiState.Empty
        )
    }
}

data class VisitRegionDistributionUiState(
    val totalVisitCountText: String,
    val items: List<VisitRegionDistributionItemUiState>
) {
    companion object {
        val Empty = VisitRegionDistributionUiState(
            totalVisitCountText = "0회",
            items = emptyList()
        )
    }
}

data class VisitRegionDistributionItemUiState(
    val rankText: String,
    val regionName: String,
    val visitCountText: String,
    val displayRatio: String,
    val ratio: Float
)

data class VisitPlaceRankingUiState(
    val totalVisitCountText: String,
    val items: List<VisitPlaceRankingItemUiState>
) {
    companion object {
        val Empty = VisitPlaceRankingUiState(
            totalVisitCountText = "0회",
            items = emptyList()
        )
    }
}

data class VisitPlaceRankingItemUiState(
    val rankText: String,
    val placeName: String,
    val roadAddress: String,
    val displayVisitCount: String
)
