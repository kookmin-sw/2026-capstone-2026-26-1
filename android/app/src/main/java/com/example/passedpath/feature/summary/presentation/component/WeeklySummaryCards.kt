package com.example.passedpath.feature.summary.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.feature.summary.presentation.state.WeeklySummaryBarUiState
import com.example.passedpath.feature.summary.presentation.state.WeeklySummaryMetricCardUiState
import com.example.passedpath.feature.summary.presentation.state.WeeklySummaryVisitedRegionUiState
import com.example.passedpath.feature.summary.presentation.state.WeeklySummaryVisitedRegionsCardUiState
import com.example.passedpath.ui.component.loading.BaseSkeletonBlock
import com.example.passedpath.ui.component.loading.rememberBaseSkeletonBrush
import com.example.passedpath.ui.theme.Gray200
import com.example.passedpath.ui.theme.Gray300
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray50
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun WeeklySummaryMetricCard(
    card: WeeklySummaryMetricCardUiState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(WeeklySummaryCardHeight)
            .clip(cardShape)
            .background(color = Gray50, shape = cardShape)
            .weeklySummaryCardClickable(onClick)
            .padding(horizontal = WeeklySummaryCardHorizontalPadding)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .padding(end = WeeklySummaryMetricContentEndPadding),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Green500,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 4.dp),
                    text = card.prefixLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray400,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = card.valueText,
                    style = MaterialTheme.typography.titleLarge,
                    color = Gray900,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        WeeklySummaryMiniBarChart(
            bars = card.bars,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = WeeklySummaryChartBottomPadding)
                .height(48.dp)
        )
        SummaryArrowIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = WeeklySummaryArrowTopPadding)
        )
    }
}

private fun Modifier.weeklySummaryCardClickable(
    onClick: (() -> Unit)?
): Modifier {
    return if (onClick == null) {
        this
    } else {
        clickable(onClick = onClick)
    }
}

@Composable
fun WeeklySummaryVisitedRegionsCard(
    card: WeeklySummaryVisitedRegionsCardUiState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(WeeklySummaryCardHeight)
            .clip(cardShape)
            .background(color = Gray50, shape = cardShape)
            .weeklySummaryCardClickable(onClick)
            .padding(horizontal = WeeklySummaryCardHorizontalPadding)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .padding(end = WeeklySummaryVisitedContentEndPadding),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Green500,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (card.regions.isEmpty()) {
                Text(
                    text = EmptySummaryValue,
                    style = MaterialTheme.typography.titleLarge,
                    color = Gray900,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    card.regions.forEach { region ->
                        WeeklyVisitedRegionText(
                            region = region,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        SummaryArrowIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = WeeklySummaryArrowTopPadding)
        )
    }
}

@Composable
fun WeeklySummaryMetricCardSkeleton(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(WeeklySummaryCardHeight)
            .background(color = Gray50, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = WeeklySummaryCardHorizontalPadding)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .padding(end = WeeklySummaryMetricContentEndPadding),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            BaseSkeletonBlock(
                brush = shimmerBrush,
                modifier = Modifier
                    .fillMaxWidth(0.52f)
                    .height(16.dp)
            )
            BaseSkeletonBlock(
                brush = shimmerBrush,
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(24.dp),
                shape = RoundedCornerShape(10.dp)
            )
        }
        WeeklySummaryMiniBarSkeleton(
            brush = shimmerBrush,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = WeeklySummaryChartBottomPadding)
                .height(48.dp)
        )
        BaseSkeletonBlock(
            brush = shimmerBrush,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = WeeklySummaryArrowTopPadding)
                .size(width = 7.dp, height = 12.dp),
            shape = RoundedCornerShape(4.dp)
        )
    }
}

@Composable
fun WeeklySummaryVisitedRegionsCardSkeleton(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(WeeklySummaryCardHeight)
            .background(color = Gray50, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = WeeklySummaryCardHorizontalPadding)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .padding(end = WeeklySummaryVisitedContentEndPadding),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            BaseSkeletonBlock(
                brush = shimmerBrush,
                modifier = Modifier
                    .fillMaxWidth(0.48f)
                    .height(16.dp)
            )
            BaseSkeletonBlock(
                brush = shimmerBrush,
                modifier = Modifier
                    .fillMaxWidth(0.70f)
                    .height(24.dp),
                shape = RoundedCornerShape(10.dp)
            )
        }
        BaseSkeletonBlock(
            brush = shimmerBrush,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = WeeklySummaryArrowTopPadding)
                .size(width = 7.dp, height = 12.dp),
            shape = RoundedCornerShape(4.dp)
        )
    }
}

@Composable
private fun WeeklySummaryMiniBarChart(
    bars: List<WeeklySummaryBarUiState>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.width(WeeklySummaryChartWidth),
        horizontalArrangement = Arrangement.spacedBy(WeeklySummaryChartBarSpacing),
        verticalAlignment = Alignment.Bottom
    ) {
        bars.take(WeeklySummaryChartBarCount).forEach { bar ->
            if (bar.hasData) {
                val color = if (bar.isHighlighted) Green500 else Gray300
                Box(
                    modifier = Modifier
                        .width(WeeklySummaryChartBarWidth)
                        .fillMaxHeight(bar.ratio.coerceIn(0f, 1f))
                        .background(color = color, shape = WeeklySummaryChartBarShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(WeeklySummaryChartBarWidth)
                        .height(WeeklySummaryChartMissingDashHeight)
                        .background(color = Gray200, shape = RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

@Composable
private fun WeeklySummaryMiniBarSkeleton(
    brush: Brush,
    modifier: Modifier = Modifier
) {
    val ratios = listOf(0.92f, 0.64f, 0.74f, 0.42f, 0.82f, 0.52f, 0.72f)
    Row(
        modifier = modifier.width(WeeklySummaryChartWidth),
        horizontalArrangement = Arrangement.spacedBy(WeeklySummaryChartBarSpacing),
        verticalAlignment = Alignment.Bottom
    ) {
        ratios.take(WeeklySummaryChartBarCount).forEach { ratio ->
            BaseSkeletonBlock(
                brush = brush,
                modifier = Modifier
                    .width(WeeklySummaryChartBarWidth)
                    .fillMaxHeight(ratio),
                shape = RoundedCornerShape(3.dp)
            )
        }
    }
}

@Composable
private fun WeeklyVisitedRegionText(
    region: WeeklySummaryVisitedRegionUiState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            modifier = Modifier.padding(bottom = 4.dp),
            text = region.rankText,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray400,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Text(
            text = region.regionName,
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.titleLarge,
            color = Gray900,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SummaryArrowIcon(
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_arrow_right),
        contentDescription = null,
        tint = Gray400,
        modifier = modifier.size(width = 7.dp, height = 12.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun WeeklySummaryCardsPreview() {
    PassedPathTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            WeeklySummaryMetricCard(
                card = WeeklySummaryMetricCardUiState(
                    title = "주간 외출 시간",
                    valueText = "09:12",
                    bars = previewBars()
                )
            )
            WeeklySummaryVisitedRegionsCard(
                card = WeeklySummaryVisitedRegionsCardUiState(
                    title = "자주 방문한 동네",
                    regions = listOf(
                        WeeklySummaryVisitedRegionUiState("1위", "성북구"),
                        WeeklySummaryVisitedRegionUiState("2위", "강북구")
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun WeeklySummaryMissingDataCardPreview() {
    PassedPathTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            WeeklySummaryMetricCard(
                card = WeeklySummaryMetricCardUiState(
                    title = "주간 외출 시간",
                    valueText = "09:12",
                    bars = previewBarsWithMissingData()
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun WeeklySummaryCardSkeletonPreview() {
    PassedPathTheme {
        val skeletonBrush = rememberBaseSkeletonBrush()

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            WeeklySummaryMetricCardSkeleton(shimmerBrush = skeletonBrush)
            WeeklySummaryVisitedRegionsCardSkeleton(shimmerBrush = skeletonBrush)
        }
    }
}

private fun previewBars(): List<WeeklySummaryBarUiState> {
    return listOf(
        WeeklySummaryBarUiState(ratio = 0.92f, isHighlighted = false, hasData = true),
        WeeklySummaryBarUiState(ratio = 0.64f, isHighlighted = false, hasData = true),
        WeeklySummaryBarUiState(ratio = 0.74f, isHighlighted = false, hasData = true),
        WeeklySummaryBarUiState(ratio = 0.42f, isHighlighted = false, hasData = true),
        WeeklySummaryBarUiState(ratio = 0.82f, isHighlighted = false, hasData = true),
        WeeklySummaryBarUiState(ratio = 0.52f, isHighlighted = false, hasData = true),
        WeeklySummaryBarUiState(ratio = 0.72f, isHighlighted = true, hasData = true)
    )
}

private fun previewBarsWithMissingData(): List<WeeklySummaryBarUiState> {
    return listOf(
        WeeklySummaryBarUiState(ratio = 0.92f, isHighlighted = false, hasData = true),
        WeeklySummaryBarUiState(ratio = 0f, isHighlighted = false, hasData = false),
        WeeklySummaryBarUiState(ratio = 0.74f, isHighlighted = false, hasData = true),
        WeeklySummaryBarUiState(ratio = 0f, isHighlighted = false, hasData = false),
        WeeklySummaryBarUiState(ratio = 0.82f, isHighlighted = false, hasData = true),
        WeeklySummaryBarUiState(ratio = 0f, isHighlighted = false, hasData = false),
        WeeklySummaryBarUiState(ratio = 0.72f, isHighlighted = true, hasData = true)
    )
}

private const val EmptySummaryValue = "-"
private val WeeklySummaryCardHeight = 104.dp
private val WeeklySummaryCardHorizontalPadding = 20.dp
private val WeeklySummaryArrowTopPadding = 18.dp
private val WeeklySummaryChartBottomPadding = 14.dp
private val WeeklySummaryMetricContentEndPadding = 116.dp
private val WeeklySummaryVisitedContentEndPadding = 24.dp
private const val WeeklySummaryChartBarCount = 7
private const val WeeklySummaryChartBarWidthValue = 8
private const val WeeklySummaryChartBarSpacingValue = 6
private const val WeeklySummaryChartWidthExtra = 2
private val WeeklySummaryChartBarWidth = WeeklySummaryChartBarWidthValue.dp
private val WeeklySummaryChartBarSpacing = WeeklySummaryChartBarSpacingValue.dp
private val WeeklySummaryChartBarShape = RoundedCornerShape(
    topStart = 2.dp,
    topEnd = 2.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)
private val WeeklySummaryChartMissingDashHeight = 3.dp
private val WeeklySummaryChartWidth =
    (
        WeeklySummaryChartBarWidthValue * WeeklySummaryChartBarCount +
            WeeklySummaryChartBarSpacingValue * (WeeklySummaryChartBarCount - 1) +
            WeeklySummaryChartWidthExtra
        ).dp
