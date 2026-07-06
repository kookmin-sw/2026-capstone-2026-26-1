package com.example.passedpath.feature.summary.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailChartBarUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailChartUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailComparisonBarUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailDateRangeUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailHighlightCardUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriod
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriodOptionUiState
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray200
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White
import androidx.compose.ui.text.style.TextAlign
import com.example.passedpath.ui.theme.Green300
import com.example.passedpath.ui.theme.Green400

@Composable
fun SummaryDetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    backContentDescription: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = backContentDescription,
                tint = Gray900,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleMedium,
            color = Gray900,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SummaryDetailPeriodSelector(
    options: List<SummaryDetailPeriodOptionUiState>,
    selectedPeriod: SummaryDetailPeriod,
    onPeriodSelected: (SummaryDetailPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(color = Gray100, shape = RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEach { option ->
            val selected = option.period == selectedPeriod
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (selected) White else Gray100,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = { onPeriodSelected(option.period) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) Gray900 else Gray400,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SummaryDetailDateRangeBar(
    dateRange: SummaryDetailDateRangeUiState,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        IconButton(
            onClick = onPreviousClick,
            enabled = dateRange.canMovePrevious,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_left),
                contentDescription = null,
                tint = if (dateRange.canMovePrevious) Gray400 else Gray200,
                modifier = Modifier.size(width = 7.dp, height = 12.dp)
            )
        }

        Text(
            text = dateRange.rangeText,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.bodyLarge,
            color = Gray500,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        IconButton(
            onClick = onNextClick,
            enabled = dateRange.canMoveNext,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = if (dateRange.canMoveNext) Gray400 else Gray200,
                modifier = Modifier.size(width = 7.dp, height = 12.dp)
            )
        }
    }
}

@Composable
fun SummaryDetailChart(
    chart: SummaryDetailChartUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = modifier.padding(bottom = 4.dp),
                text = chart.averageLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray400,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = chart.averageValueText,
                style = MaterialTheme.typography.headlineSmall,
                color = if (chart.hasAverageData) Gray900 else Gray400,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryDetailBarPlot(
                bars = chart.bars,
                modifier = Modifier.weight(1f)
            )
            SummaryDetailYAxisLabels(
                labels = chart.yAxisLabels,
                modifier = Modifier
                    .width(44.dp)
                    .height(146.dp)
            )
        }
    }
}

@Composable
fun SummaryDetailHighlightSection(
    title: String,
    highlights: List<SummaryDetailHighlightCardUiState>,
    modifier: Modifier = Modifier,
    emptyText: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Gray100)
            .padding(horizontal = 16.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = Gray900,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (highlights.isEmpty()) {
            if (emptyText != null) {
                SummaryDetailEmptyHighlightCard(emptyText = emptyText)
            }
        } else {
            highlights.forEach { highlight ->
                SummaryDetailHighlightCard(card = highlight)
            }
        }
    }
}

@Composable
fun SummaryDetailHighlightCard(
    card: SummaryDetailHighlightCardUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = White, shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color = White, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_clock),
                    contentDescription = null,
                    tint = Green500,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = card.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Green500,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = card.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Gray900,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            card.comparisons.forEach { comparison ->
                SummaryDetailComparisonBar(comparison = comparison)
            }
        }
    }
}

@Composable
private fun SummaryDetailEmptyHighlightCard(
    emptyText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = White, shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = emptyText,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray400,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SummaryDetailBarPlot(
    bars: List<SummaryDetailChartBarUiState>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxHeight()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(146.dp)
        ) {
            val plotMaxHeight = maxHeight

            SummaryDetailChartGrid(
                modifier = Modifier.matchParentSize()
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEach { bar ->
                    SummaryDetailChartBar(
                        bar = bar,
                        maxHeight = plotMaxHeight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Top
        ) {
            bars.forEach { bar ->
                Text(
                    text = if (bar.showLabel) bar.label else "",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SummaryDetailChartGrid(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f), 0f)
        val strokeWidth = 1.dp.toPx()
        val yPositions = listOf(0f, size.height / 2f, size.height)

        yPositions.forEach { y ->
            drawLine(
                color = Gray200,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth,
                pathEffect = pathEffect
            )
        }
    }
}

@Composable
private fun SummaryDetailChartBar(
    bar: SummaryDetailChartBarUiState,
    maxHeight: Dp,
    modifier: Modifier = Modifier
) {
    val rawBarHeight = (maxHeight.value * bar.ratio.coerceIn(0f, 1f)).dp
    val barHeight = when {
        !bar.hasData -> 0.dp
        bar.isZeroValue -> SummaryDetailZeroBarHeight
        rawBarHeight < SummaryDetailMinimumBarHeight -> SummaryDetailMinimumBarHeight
        else -> rawBarHeight
    }

    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (bar.hasData) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(barHeight)
                    .background(color = Green500, shape = SummaryDetailBarShape)
            )
        }
    }
}

@Composable
private fun SummaryDetailYAxisLabels(
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        labels.take(3).forEach { label ->
            Text(
                text = label,
                fontWeight = FontWeight.Light,
                color = Gray400,
                fontSize = 14.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SummaryDetailComparisonBar(
    comparison: SummaryDetailComparisonBarUiState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = comparison.label,
            modifier = Modifier.width(48.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (comparison.isPrimary) Green500 else Gray500,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .background(color = Gray100, shape = RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(comparison.ratio.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(
                        color = if (comparison.isPrimary) Green500 else Gray400,
                        shape = RoundedCornerShape(5.dp)
                    )
            )
            Text(
                text = comparison.valueText,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Green300,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun SummaryDetailComponentsPreview() {
    PassedPathTheme {
        Column(
            modifier = Modifier
                .background(White)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SummaryDetailPeriodSelector(
                options = previewPeriodOptions(),
                selectedPeriod = SummaryDetailPeriod.WEEK,
                onPeriodSelected = {}
            )
            SummaryDetailDateRangeBar(
                dateRange = SummaryDetailDateRangeUiState("2026.01.01 ~ 01.08"),
                onPreviousClick = {},
                onNextClick = {}
            )
            SummaryDetailChart(chart = previewChart())
            SummaryDetailHighlightSection(
                title = "Highlight",
                highlights = listOf(previewHighlightCard())
            )
        }
    }
}

private fun previewPeriodOptions(): List<SummaryDetailPeriodOptionUiState> {
    return listOf(
        SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.WEEK, "1w"),
        SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.MONTH, "1m"),
        SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.SIX_MONTHS, "6m"),
        SummaryDetailPeriodOptionUiState(SummaryDetailPeriod.YEAR, "1y")
    )
}

private fun previewChart(): SummaryDetailChartUiState {
    return SummaryDetailChartUiState(
        averageLabel = "Avg",
        averageValueText = "09:12",
        yAxisLabels = listOf("10:00", "09:30", "09:00"),
        bars = listOf(
            SummaryDetailChartBarUiState("M", 0.90f),
            SummaryDetailChartBarUiState("T", 0.66f),
            SummaryDetailChartBarUiState("W", 0.74f),
            SummaryDetailChartBarUiState("T", 0.42f),
            SummaryDetailChartBarUiState("F", 0.82f),
            SummaryDetailChartBarUiState("S", 0.48f),
            SummaryDetailChartBarUiState("S", 0.74f)
        )
    )
}

private fun previewHighlightCard(): SummaryDetailHighlightCardUiState {
    return SummaryDetailHighlightCardUiState(
        title = "This week outing",
        description = "Average outing time decreased from last week.",
        comparisons = listOf(
            SummaryDetailComparisonBarUiState("This", "08:30", 0.54f, true),
            SummaryDetailComparisonBarUiState("Last", "09:12", 0.70f, false)
        )
    )
}

private val SummaryDetailMinimumBarHeight = 4.dp
private val SummaryDetailZeroBarHeight = 2.dp
private val SummaryDetailBarShape = RoundedCornerShape(
    topStart = 5.dp,
    topEnd = 5.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)
