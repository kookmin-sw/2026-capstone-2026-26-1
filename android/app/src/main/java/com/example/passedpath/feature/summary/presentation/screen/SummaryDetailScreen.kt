package com.example.passedpath.feature.summary.presentation.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.R
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.summary.presentation.component.SummaryDetailChart
import com.example.passedpath.feature.summary.presentation.component.SummaryDetailDateRangeBar
import com.example.passedpath.feature.summary.presentation.component.SummaryDetailHighlightSection
import com.example.passedpath.feature.summary.presentation.component.SummaryDetailPeriodSelector
import com.example.passedpath.feature.summary.presentation.component.SummaryDetailTopBar
import com.example.passedpath.feature.summary.presentation.state.DaySummaryNoDataText
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailChartBarUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailChartUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailDateRangeUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailMetric
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriod
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriodOptionUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailUiState
import com.example.passedpath.feature.summary.presentation.viewmodel.SummaryDetailViewModel
import com.example.passedpath.feature.summary.presentation.viewmodel.SummaryDetailViewModelFactory
import com.example.passedpath.ui.component.feedback.NetworkFailureBanner
import com.example.passedpath.ui.component.loading.BaseSkeletonBlock
import com.example.passedpath.ui.component.loading.rememberBaseSkeletonBrush
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SummaryDetailRoute(
    metric: SummaryDetailMetric,
    dateKey: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (metric.isApiBackedSummaryDetailMetric()) {
        ApiBackedSummaryDetailRoute(
            metric = metric,
            dateKey = dateKey,
            onBackClick = onBackClick,
            modifier = modifier
        )
        return
    }

    var selectedPeriod by rememberSaveable(metric) {
        mutableStateOf(SummaryDetailPeriod.WEEK)
    }
    var anchorDateKey by rememberSaveable(metric, dateKey) {
        mutableStateOf(dateKey)
    }
    val anchorDate = remember(anchorDateKey) {
        parseSummaryDetailDateOrToday(anchorDateKey)
    }
    val periodOptions = rememberSummaryDetailPeriodOptions()
    val averageLabel = stringResource(R.string.summary_detail_average)
    val emptyHighlightText = stringResource(R.string.summary_detail_highlight_empty)
    val uiState = remember(metric, selectedPeriod, anchorDate, periodOptions, averageLabel) {
        metric.toEmptySummaryDetailUiState(
            selectedPeriod = selectedPeriod,
            periodOptions = periodOptions,
            anchorDate = anchorDate,
            averageLabel = averageLabel
        )
    }

    SummaryDetailScreen(
        title = stringResource(metric.titleResId()),
        uiState = uiState,
        highlightTitle = stringResource(R.string.summary_detail_highlight),
        emptyHighlightText = emptyHighlightText,
        onBackClick = onBackClick,
        onPeriodSelected = { period -> selectedPeriod = period },
        onPreviousRangeClick = {
            anchorDateKey = anchorDate.shiftByPeriod(selectedPeriod, direction = -1).toString()
        },
        onNextRangeClick = {
            anchorDateKey = anchorDate.shiftByPeriod(selectedPeriod, direction = 1).toString()
        },
        modifier = modifier
    )
}

@Composable
private fun ApiBackedSummaryDetailRoute(
    metric: SummaryDetailMetric,
    dateKey: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SummaryDetailViewModel = viewModel(
        factory = SummaryDetailViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val viewModelState by viewModel.uiState.collectAsStateWithLifecycle()
    val periodOptions = rememberSummaryDetailPeriodOptions()
    val averageLabel = stringResource(R.string.summary_detail_average)
    val emptyHighlightText = stringResource(R.string.summary_detail_highlight_empty)
    val anchorDate = remember(dateKey) {
        parseSummaryDetailDateOrToday(dateKey)
    }
    val contentState = viewModelState.content
        ?.takeIf { content -> content.metric == metric }
        ?.copy(
            selectedPeriod = viewModelState.selectedPeriod,
            periodOptions = periodOptions
        )
        ?: metric.toEmptySummaryDetailUiState(
            selectedPeriod = viewModelState.selectedPeriod,
            periodOptions = periodOptions,
            anchorDate = anchorDate,
            averageLabel = averageLabel
        ).copy(
            dateRange = SummaryDetailDateRangeUiState(
                rangeText = anchorDate.toRangeText(viewModelState.selectedPeriod),
                canMovePrevious = false,
                canMoveNext = false
            )
        )

    LaunchedEffect(viewModel, metric) {
        viewModel.loadSummaryDetail(metric = metric)
    }

    SummaryDetailScreen(
        title = stringResource(metric.titleResId()),
        uiState = contentState,
        highlightTitle = stringResource(R.string.summary_detail_highlight),
        emptyHighlightText = emptyHighlightText,
        isLoading = viewModelState.isLoading && !viewModelState.hasLoaded,
        errorMessage = viewModelState.errorMessage,
        onRetryClick = { viewModel.loadSummaryDetail(metric = metric, forceRefresh = true) },
        onBackClick = onBackClick,
        onPeriodSelected = { period -> viewModel.selectPeriod(metric = metric, period = period) },
        onPreviousRangeClick = {},
        onNextRangeClick = {},
        modifier = modifier
    )
}

@Composable
internal fun SummaryDetailScreen(
    title: String,
    uiState: SummaryDetailUiState,
    highlightTitle: String,
    emptyHighlightText: String,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetryClick: () -> Unit = {},
    onBackClick: () -> Unit,
    onPeriodSelected: (SummaryDetailPeriod) -> Unit,
    onPreviousRangeClick: () -> Unit,
    onNextRangeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
            .statusBarsPadding()
    ) {
        SummaryDetailTopBar(
            title = title,
            onBackClick = onBackClick,
            backContentDescription = stringResource(R.string.calendar_back)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item(key = "summary_detail_chart") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryDetailPeriodSelector(
                        options = uiState.periodOptions,
                        selectedPeriod = uiState.selectedPeriod,
                        onPeriodSelected = onPeriodSelected
                    )
                    SummaryDetailDateRangeBar(
                        dateRange = uiState.dateRange,
                        onPreviousClick = onPreviousRangeClick,
                        onNextClick = onNextRangeClick
                    )
                    if (isLoading) {
                        SummaryDetailChartSkeleton()
                    } else {
                        SummaryDetailChart(chart = uiState.chart)
                    }
                }
            }

            if (errorMessage != null) {
                item(key = "summary_detail_error") {
                    NetworkFailureBanner(
                        retryText = stringResource(R.string.route_retry),
                        onRetryClick = onRetryClick,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            item(key = "summary_detail_highlight") {
                SummaryDetailHighlightSection(
                    title = highlightTitle,
                    highlights = uiState.highlights,
                    emptyText = emptyHighlightText
                )
            }
        }
    }
}

@Composable
private fun SummaryDetailChartSkeleton(
    modifier: Modifier = Modifier
) {
    val skeletonBrush = rememberBaseSkeletonBrush()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BaseSkeletonBlock(
            brush = skeletonBrush,
            modifier = Modifier
                .fillMaxWidth(0.34f)
                .height(36.dp),
            shape = RoundedCornerShape(10.dp)
        )
        BaseSkeletonBlock(
            brush = skeletonBrush,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun rememberSummaryDetailPeriodOptions(): List<SummaryDetailPeriodOptionUiState> {
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

private fun SummaryDetailMetric.toEmptySummaryDetailUiState(
    selectedPeriod: SummaryDetailPeriod,
    periodOptions: List<SummaryDetailPeriodOptionUiState>,
    anchorDate: LocalDate,
    averageLabel: String
): SummaryDetailUiState {
    return SummaryDetailUiState(
        metric = this,
        selectedPeriod = selectedPeriod,
        periodOptions = periodOptions,
        dateRange = SummaryDetailDateRangeUiState(
            rangeText = anchorDate.toRangeText(selectedPeriod)
        ),
        chart = SummaryDetailChartUiState(
            averageLabel = averageLabel,
            averageValueText = DaySummaryNoDataText,
            hasAverageData = false,
            yAxisLabels = yAxisLabels(),
            bars = WeekdayLabels.map { weekday ->
                SummaryDetailChartBarUiState(
                    label = weekday,
                    ratio = 0f,
                    hasData = false
                )
            }
        ),
        highlights = emptyList()
    )
}

private fun SummaryDetailMetric.isApiBackedSummaryDetailMetric(): Boolean {
    return this == SummaryDetailMetric.OUTING_TIME ||
        this == SummaryDetailMetric.ENTER_HOME_TIME ||
        this == SummaryDetailMetric.TOTAL_OUTING_DURATION ||
        this == SummaryDetailMetric.TOTAL_OUTING_COUNT
}

@StringRes
private fun SummaryDetailMetric.titleResId(): Int {
    return when (this) {
        SummaryDetailMetric.OUTING_TIME -> R.string.day_summary_outing_time
        SummaryDetailMetric.ENTER_HOME_TIME -> R.string.day_summary_enter_home_time
        SummaryDetailMetric.TOTAL_OUTING_DURATION -> R.string.day_summary_total_outing_duration
        SummaryDetailMetric.TOTAL_OUTING_COUNT -> R.string.day_summary_total_outing_count
        SummaryDetailMetric.VISITS -> R.string.day_summary_visited_dong_title
    }
}

private fun SummaryDetailMetric.yAxisLabels(): List<String> {
    return when (this) {
        SummaryDetailMetric.OUTING_TIME -> listOf("10:00", "09:30", "09:00")
        SummaryDetailMetric.ENTER_HOME_TIME -> listOf("24:00", "23:30", "23:00")
        SummaryDetailMetric.TOTAL_OUTING_DURATION -> listOf("8h", "4h", "0h")
        SummaryDetailMetric.TOTAL_OUTING_COUNT -> listOf("3", "1.5", "0")
        SummaryDetailMetric.VISITS -> emptyList()
    }
}

private fun LocalDate.toRangeText(period: SummaryDetailPeriod): String {
    val startDate = when (period) {
        SummaryDetailPeriod.WEEK -> minusDays(6)
        SummaryDetailPeriod.MONTH -> minusMonths(1).plusDays(1)
        SummaryDetailPeriod.SIX_MONTHS -> minusMonths(6).plusDays(1)
        SummaryDetailPeriod.YEAR -> minusYears(1).plusDays(1)
    }

    return "${startDate.format(RangeStartFormatter)} ~ ${format(RangeEndFormatter)}"
}

private fun LocalDate.shiftByPeriod(
    period: SummaryDetailPeriod,
    direction: Long
): LocalDate {
    return when (period) {
        SummaryDetailPeriod.WEEK -> plusDays(7 * direction)
        SummaryDetailPeriod.MONTH -> plusMonths(direction)
        SummaryDetailPeriod.SIX_MONTHS -> plusMonths(6 * direction)
        SummaryDetailPeriod.YEAR -> plusYears(direction)
    }
}

private fun parseSummaryDetailDateOrToday(dateKey: String): LocalDate {
    return runCatching { LocalDate.parse(dateKey) }
        .getOrDefault(LocalDate.now())
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun SummaryDetailScreenPreview() {
    PassedPathTheme {
        val periodOptions = listOf(
            SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.WEEK, "1w"),
            SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.MONTH, "1m"),
            SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.SIX_MONTHS, "6m"),
            SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.YEAR, "1y")
        )
        SummaryDetailScreen(
            title = "Outing time",
            uiState = SummaryDetailMetric.OUTING_TIME.toEmptySummaryDetailUiState(
                selectedPeriod = SummaryDetailPeriod.WEEK,
                periodOptions = periodOptions,
                anchorDate = LocalDate.of(2026, 1, 8),
                averageLabel = "Avg"
            ),
            highlightTitle = "Highlight",
            emptyHighlightText = "No highlight data",
            onBackClick = {},
            onPeriodSelected = {},
            onPreviousRangeClick = {},
            onNextRangeClick = {}
        )
    }
}

private val RangeStartFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREA)
private val RangeEndFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MM.dd", Locale.KOREA)
private val WeekdayLabels = listOf(
    "\uc6d4",
    "\ud654",
    "\uc218",
    "\ubaa9",
    "\uae08",
    "\ud1a0",
    "\uc77c"
)
