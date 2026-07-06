package com.example.passedpath.feature.care.presentation.screen

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.R
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.care.presentation.viewmodel.ProtectedPersonSummaryDetailViewModel
import com.example.passedpath.feature.care.presentation.viewmodel.ProtectedPersonSummaryDetailViewModelFactory
import com.example.passedpath.feature.care.presentation.viewmodel.ProtectedPersonVisitStatisticsViewModel
import com.example.passedpath.feature.care.presentation.viewmodel.ProtectedPersonVisitStatisticsViewModelFactory
import com.example.passedpath.feature.care.presentation.viewmodel.ProtectedPersonWeeklySummaryViewModel
import com.example.passedpath.feature.care.presentation.viewmodel.ProtectedPersonWeeklySummaryViewModelFactory
import com.example.passedpath.feature.summary.presentation.screen.SummaryDetailScreen
import com.example.passedpath.feature.summary.presentation.screen.VisitStatisticsDetailScreen
import com.example.passedpath.feature.summary.presentation.screen.WeeklySummaryScreen
import com.example.passedpath.feature.summary.presentation.state.DaySummaryNoDataText
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailChartBarUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailChartUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailDateRangeUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailMetric
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriod
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriodOptionUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailUiState
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProtectedPersonWeeklySummaryRoute(
    dependentUserId: Long,
    dependentNickname: String,
    onBackClick: () -> Unit,
    onMetricClick: (SummaryDetailMetric) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProtectedPersonWeeklySummaryViewModel = viewModel(
        factory = ProtectedPersonWeeklySummaryViewModelFactory(
            appContainer = LocalContext.current.appContainer,
            dependentUserId = dependentUserId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val highlightedTitlePrefix = dependentNickname.trim()
        .takeIf(String::isNotEmpty)
    val title = highlightedTitlePrefix
        ?.let { nickname -> "$nickname\uC758 \uC8FC\uAC04 \uC694\uC57D" }

    LaunchedEffect(viewModel) {
        viewModel.loadWeeklySummary()
    }

    WeeklySummaryScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetryClick = { viewModel.loadWeeklySummary(forceRefresh = true) },
        onMetricClick = onMetricClick,
        title = title,
        highlightedTitlePrefix = highlightedTitlePrefix,
        modifier = modifier
    )
}

@Composable
fun ProtectedPersonSummaryDetailRoute(
    dependentUserId: Long,
    metric: SummaryDetailMetric,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProtectedPersonSummaryDetailViewModel = viewModel(
        factory = ProtectedPersonSummaryDetailViewModelFactory(
            appContainer = LocalContext.current.appContainer,
            dependentUserId = dependentUserId
        )
    )
) {
    val viewModelState by viewModel.uiState.collectAsStateWithLifecycle()
    val periodOptions = rememberProtectedPersonStatisticsPeriodOptions()
    val averageLabel = stringResource(R.string.summary_detail_average)
    val emptyHighlightText = stringResource(R.string.summary_detail_highlight_empty)
    val anchorDate = remember { LocalDate.now(ZoneId.of(ProtectedPersonStatisticsZoneId)) }
    val contentState = viewModelState.content
        ?.takeIf { content -> content.metric == metric }
        ?.copy(
            selectedPeriod = viewModelState.selectedPeriod,
            periodOptions = periodOptions
        )
        ?: metric.toEmptyProtectedPersonSummaryDetailUiState(
            selectedPeriod = viewModelState.selectedPeriod,
            periodOptions = periodOptions,
            anchorDate = anchorDate,
            averageLabel = averageLabel
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
fun ProtectedPersonVisitStatisticsDetailRoute(
    dependentUserId: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProtectedPersonVisitStatisticsViewModel = viewModel(
        factory = ProtectedPersonVisitStatisticsViewModelFactory(
            appContainer = LocalContext.current.appContainer,
            dependentUserId = dependentUserId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val periodOptions = rememberProtectedPersonStatisticsPeriodOptions()

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
private fun rememberProtectedPersonStatisticsPeriodOptions(): List<SummaryDetailPeriodOptionUiState> {
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

private fun SummaryDetailMetric.toEmptyProtectedPersonSummaryDetailUiState(
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
            rangeText = anchorDate.toRangeText(selectedPeriod),
            canMovePrevious = false,
            canMoveNext = false
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

private const val ProtectedPersonStatisticsZoneId = "Asia/Seoul"
private val RangeStartFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREA)
private val RangeEndFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MM.dd", Locale.KOREA)
private val WeekdayLabels = listOf(
    "\uC6D4",
    "\uD654",
    "\uC218",
    "\uBAA9",
    "\uAE08",
    "\uD1A0",
    "\uC77C"
)
