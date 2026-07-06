package com.example.passedpath.feature.summary.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.R
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.summary.presentation.component.SummaryDetailDateRangeBar
import com.example.passedpath.feature.summary.presentation.component.SummaryDetailPeriodSelector
import com.example.passedpath.feature.summary.presentation.component.SummaryDetailTopBar
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailDateRangeUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriod
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriodOptionUiState
import com.example.passedpath.feature.summary.presentation.state.VisitPlaceRankingItemUiState
import com.example.passedpath.feature.summary.presentation.state.VisitPlaceRankingUiState
import com.example.passedpath.feature.summary.presentation.state.VisitRegionDistributionItemUiState
import com.example.passedpath.feature.summary.presentation.state.VisitRegionDistributionUiState
import com.example.passedpath.feature.summary.presentation.state.VisitStatisticsContentUiState
import com.example.passedpath.feature.summary.presentation.state.VisitStatisticsUiState
import com.example.passedpath.feature.summary.presentation.viewmodel.VisitStatisticsViewModel
import com.example.passedpath.feature.summary.presentation.viewmodel.VisitStatisticsViewModelFactory
import com.example.passedpath.ui.component.feedback.NetworkFailureBanner
import com.example.passedpath.ui.component.loading.BaseSkeletonBlock
import com.example.passedpath.ui.component.loading.rememberBaseSkeletonBrush
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray200
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray50
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White

@Composable
fun VisitStatisticsDetailRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VisitStatisticsViewModel = viewModel(
        factory = VisitStatisticsViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val periodOptions = rememberVisitStatisticsPeriodOptions()

    LaunchedEffect(viewModel) {
        viewModel.loadVisitStatistics()
    }

    VisitStatisticsDetailScreen(
        uiState = uiState,
        periodOptions = periodOptions,
        onBackClick = onBackClick,
        onPeriodSelected = viewModel::selectPeriod,
        onRetryClick = { viewModel.loadVisitStatistics(forceRefresh = true) },
        modifier = modifier
    )
}

@Composable
internal fun VisitStatisticsDetailScreen(
    uiState: VisitStatisticsUiState,
    periodOptions: List<SummaryDetailPeriodOptionUiState>,
    onBackClick: () -> Unit,
    onPeriodSelected: (SummaryDetailPeriod) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
            .statusBarsPadding()
    ) {
        SummaryDetailTopBar(
            title = stringResource(R.string.day_summary_visited_dong_title),
            onBackClick = onBackClick,
            backContentDescription = stringResource(R.string.calendar_back)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item(key = "visit_statistics_filter") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryDetailPeriodSelector(
                        options = periodOptions,
                        selectedPeriod = uiState.selectedPeriod,
                        onPeriodSelected = onPeriodSelected
                    )
                    SummaryDetailDateRangeBar(
                        dateRange = SummaryDetailDateRangeUiState(
                            rangeText = uiState.content.dateRangeText,
                            canMovePrevious = false,
                            canMoveNext = false
                        ),
                        onPreviousClick = {},
                        onNextClick = {}
                    )
                }
            }

            if (uiState.errorMessage != null) {
                item(key = "visit_statistics_error") {
                    NetworkFailureBanner(
                        retryText = stringResource(R.string.route_retry),
                        onRetryClick = onRetryClick,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            when {
                !uiState.hasLoaded && uiState.errorMessage == null -> {
                    item(key = "visit_statistics_skeleton") {
                        VisitStatisticsSkeleton(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                uiState.hasLoaded -> {
                    item(key = "visit_statistics_regions") {
                        VisitRegionDistributionSection(
                            section = uiState.content.visitedRegions,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    item(key = "visit_statistics_places") {
                        VisitPlaceRankingSection(
                            section = uiState.content.places,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberVisitStatisticsPeriodOptions(): List<SummaryDetailPeriodOptionUiState> {
    val week = stringResource(R.string.summary_detail_period_week)
    val month = stringResource(R.string.summary_detail_period_month)
    val sixMonths = stringResource(R.string.summary_detail_period_six_months)
    val year = stringResource(R.string.summary_detail_period_year)

    return remember(week, month, sixMonths, year) {
        listOf(
            SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.WEEK, week),
            SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.MONTH, month),
            SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.SIX_MONTHS, sixMonths),
            SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.YEAR, year)
        )
    }
}

@Composable
private fun VisitRegionDistributionSection(
    section: VisitRegionDistributionUiState,
    modifier: Modifier = Modifier
) {
    VisitStatisticsSectionContainer(
        title = stringResource(R.string.visit_statistics_regions_title),
        caption = stringResource(
            R.string.visit_statistics_total_count,
            section.totalVisitCountText
        ),
        modifier = modifier
    ) {
        if (section.items.isEmpty()) {
            VisitStatisticsEmptyText(text = stringResource(R.string.visit_statistics_regions_empty))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                section.items.forEach { item ->
                    VisitRegionDistributionRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun VisitRegionDistributionRow(
    item: VisitRegionDistributionItemUiState
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = item.rankText,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray400,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = item.regionName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = Gray900,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.visitCountText,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = item.displayRatio,
                style = MaterialTheme.typography.bodyMedium,
                color = Green500,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(color = Gray100, shape = RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(item.ratio)
                    .fillMaxHeight()
                    .background(color = Green500, shape = RoundedCornerShape(5.dp))
            )
        }
    }
}

@Composable
private fun VisitPlaceRankingSection(
    section: VisitPlaceRankingUiState,
    modifier: Modifier = Modifier
) {
    VisitStatisticsSectionContainer(
        title = stringResource(R.string.visit_statistics_places_title),
        caption = stringResource(
            R.string.visit_statistics_total_count,
            section.totalVisitCountText
        ),
        modifier = modifier
    ) {
        if (section.items.isEmpty()) {
            VisitStatisticsEmptyText(text = stringResource(R.string.visit_statistics_places_empty))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                section.items.forEach { item ->
                    VisitPlaceRankingRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun VisitPlaceRankingRow(
    item: VisitPlaceRankingItemUiState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = item.rankText,
            modifier = Modifier.width(32.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Green500,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.placeName,
                style = MaterialTheme.typography.bodyLarge,
                color = Gray900,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.roadAddress.isNotBlank()) {
                Text(
                    text = item.roadAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray400,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = item.displayVisitCount,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun VisitStatisticsSectionContainer(
    title: String,
    caption: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Gray50, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Gray900,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray400,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        content()
    }
}

@Composable
private fun VisitStatisticsEmptyText(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Gray400,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun VisitStatisticsSkeleton(
    modifier: Modifier = Modifier
) {
    val skeletonBrush = rememberBaseSkeletonBrush()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        repeat(2) {
            VisitStatisticsSkeletonSection(brush = skeletonBrush)
        }
    }
}

@Composable
private fun VisitStatisticsSkeletonSection(
    brush: Brush
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Gray50, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BaseSkeletonBlock(
            brush = brush,
            modifier = Modifier
                .fillMaxWidth(0.38f)
                .height(20.dp)
        )
        repeat(3) {
            BaseSkeletonBlock(
                brush = brush,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun VisitStatisticsDetailScreenPreview() {
    PassedPathTheme {
        VisitStatisticsDetailScreen(
            uiState = VisitStatisticsUiState(
                hasLoaded = true,
                content = previewVisitStatisticsContent()
            ),
            periodOptions = listOf(
                SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.WEEK, "1w"),
                SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.MONTH, "1m"),
                SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.SIX_MONTHS, "6m"),
                SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.YEAR, "1y")
            ),
            onBackClick = {},
            onPeriodSelected = {},
            onRetryClick = {}
        )
    }
}

private fun previewVisitStatisticsContent(): VisitStatisticsContentUiState {
    return VisitStatisticsContentUiState(
        dateRangeText = "2026.05.14 ~ 05.20",
        visitedRegions = VisitRegionDistributionUiState(
            totalVisitCountText = "21회",
            items = listOf(
                VisitRegionDistributionItemUiState("1위", "수유동", "9회", "43%", 0.43f),
                VisitRegionDistributionItemUiState("2위", "정릉동", "7회", "33%", 0.33f),
                VisitRegionDistributionItemUiState("3위", "그 외", "5회", "24%", 0.24f)
            )
        ),
        places = VisitPlaceRankingUiState(
            totalVisitCountText = "32회",
            items = listOf(
                VisitPlaceRankingItemUiState(
                    rankText = "1위",
                    placeName = "스타벅스 수유역점",
                    roadAddress = "서울특별시 성북구 정릉로 77",
                    displayVisitCount = "8회"
                ),
                VisitPlaceRankingItemUiState(
                    rankText = "2위",
                    placeName = "정릉시장",
                    roadAddress = "서울특별시 성북구 보국문로",
                    displayVisitCount = "5회"
                )
            )
        )
    )
}
