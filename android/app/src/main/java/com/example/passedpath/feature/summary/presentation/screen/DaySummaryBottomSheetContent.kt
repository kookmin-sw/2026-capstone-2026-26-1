package com.example.passedpath.feature.summary.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.summary.presentation.component.DaySummaryMetricCard
import com.example.passedpath.feature.summary.presentation.component.DaySummaryMetricCardSkeleton
import com.example.passedpath.feature.summary.presentation.component.DaySummaryVisitedDongCard
import com.example.passedpath.feature.summary.presentation.component.DaySummaryVisitedDongCardSkeleton
import com.example.passedpath.feature.summary.presentation.state.DaySummaryContentUiState
import com.example.passedpath.feature.summary.presentation.state.DaySummaryNoDataText
import com.example.passedpath.feature.summary.presentation.state.DaySummaryUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailMetric
import com.example.passedpath.ui.component.feedback.NetworkFailureBanner
import com.example.passedpath.ui.component.loading.rememberBaseSkeletonBrush
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun DaySummaryBottomSheetContent(
    selectedDateKey: String,
    uiState: DaySummaryUiState,
    onLoadSummary: (String) -> Unit,
    onRetryClick: () -> Unit,
    onScrollStateChanged: (Boolean) -> Unit,
    onMetricClick: (SummaryDetailMetric) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val isContentScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 0
        }
    }

    LaunchedEffect(selectedDateKey, uiState.dateKey, uiState.hasLoaded, uiState.isLoading) {
        if (uiState.dateKey != selectedDateKey ||
            (!uiState.hasLoaded && !uiState.isLoading && uiState.errorMessage == null)
        ) {
            onLoadSummary(selectedDateKey)
        }
    }

    LaunchedEffect(isContentScrolled) {
        onScrollStateChanged(isContentScrolled)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(2.dp))
        }

        when {
            uiState.errorMessage != null -> {
                item {
                    DaySummaryErrorNotice(
                        onRetryClick = onRetryClick
                    )
                }
            }

            !uiState.hasLoaded -> {
                item(key = "summary_skeleton") {
                    DaySummarySkeletonList()
                }
            }

            else -> {
                item {
                    DaySummaryMetricCard(
                        label = stringResource(R.string.day_summary_outing_time),
                        value = uiState.summary.outingTimeText,
                        isNoDataValue = !uiState.summary.hasOutingTimeData,
                        onClick = { onMetricClick(SummaryDetailMetric.OUTING_TIME) }
                    )
                }
                item {
                    DaySummaryMetricCard(
                        label = stringResource(R.string.day_summary_enter_home_time),
                        value = uiState.summary.enterHomeTimeText,
                        isNoDataValue = !uiState.summary.hasEnterHomeTimeData,
                        onClick = { onMetricClick(SummaryDetailMetric.ENTER_HOME_TIME) }
                    )
                }
                item {
                    DaySummaryMetricCard(
                        label = stringResource(R.string.day_summary_total_outing_duration),
                        value = uiState.summary.totalOutingDurationText,
                        isNoDataValue = !uiState.summary.hasTotalOutingDurationData,
                        onClick = { onMetricClick(SummaryDetailMetric.TOTAL_OUTING_DURATION) }
                    )
                }
                item {
                    DaySummaryMetricCard(
                        label = stringResource(R.string.day_summary_total_outing_count),
                        value = uiState.summary.totalOutingCountText,
                        isNoDataValue = !uiState.summary.hasTotalOutingCountData,
                        onClick = { onMetricClick(SummaryDetailMetric.TOTAL_OUTING_COUNT) }
                    )
                }
                item {
                    DaySummaryVisitedDongCard(
                        label = stringResource(R.string.day_summary_visited_dong_title),
                        visitedDongNames = uiState.summary.visitedDongNames,
                        emptyValue = DaySummaryNoDataText,
                        isEmptyValueNoData = !uiState.summary.hasVisitedDongData,
                        onClick = { onMetricClick(SummaryDetailMetric.VISITS) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DaySummarySkeletonList() {
    val skeletonBrush = rememberBaseSkeletonBrush()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(4) {
            DaySummaryMetricCardSkeleton(shimmerBrush = skeletonBrush)
        }
        DaySummaryVisitedDongCardSkeleton(shimmerBrush = skeletonBrush)
    }
}

@Composable
private fun DaySummaryErrorNotice(
    onRetryClick: () -> Unit
) {
    NetworkFailureBanner(
        retryText = stringResource(R.string.route_retry),
        onRetryClick = onRetryClick
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun DaySummaryBottomSheetContentPreview() {
    PassedPathTheme {
        DaySummaryBottomSheetContent(
            selectedDateKey = "2026-05-05",
            uiState = DaySummaryUiState(
                dateKey = "2026-05-05",
                hasLoaded = true,
                summary = DaySummaryContentUiState(
                    outingTimeText = "09:12",
                    enterHomeTimeText = "21:03",
                    totalOutingDurationText = "11h 51m",
                    totalOutingCountText = "3",
                    visitedDongNames = listOf("Jeongneung-dong", "Seongbuk-dong", "Hyehwa-dong")
                )
            ),
            onLoadSummary = {},
            onRetryClick = {},
            onScrollStateChanged = {}
        )
    }
}
