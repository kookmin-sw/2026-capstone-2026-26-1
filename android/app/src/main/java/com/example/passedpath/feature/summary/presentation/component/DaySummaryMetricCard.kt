package com.example.passedpath.feature.summary.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.example.passedpath.ui.component.loading.BaseSkeletonBlock
import com.example.passedpath.ui.component.loading.rememberBaseSkeletonBrush
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray50
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun DaySummaryMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isNoDataValue: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val cardShape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(98.dp)
            .clip(cardShape)
            .background(color = Gray50, shape = cardShape)
            .summaryMetricClickable(onClick)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Green500,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = if (isNoDataValue) Gray400 else Gray900,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = Gray400,
            modifier = Modifier.size(width = 7.dp, height = 12.dp)
        )
    }
}

private fun Modifier.summaryMetricClickable(
    onClick: (() -> Unit)?
): Modifier {
    return if (onClick == null) {
        this
    } else {
        clickable(onClick = onClick)
    }
}

@Composable
fun DaySummaryMetricCardSkeleton(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(98.dp)
            .background(color = Gray50, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BaseSkeletonBlock(
                brush = shimmerBrush,
                modifier = Modifier
                    .fillMaxWidth(0.34f)
                    .height(16.dp)
            )
            BaseSkeletonBlock(
                brush = shimmerBrush,
                modifier = Modifier
                    .fillMaxWidth(0.48f)
                    .height(24.dp),
                shape = RoundedCornerShape(10.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        BaseSkeletonBlock(
            brush = shimmerBrush,
            modifier = Modifier.size(width = 7.dp, height = 12.dp),
            shape = RoundedCornerShape(4.dp)
        )
    }
}

@Composable
fun DaySummaryVisitedDongCard(
    label: String,
    visitedDongNames: List<String>,
    modifier: Modifier = Modifier,
    emptyValue: String = EmptySummaryValue,
    isEmptyValueNoData: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(color = Gray50, shape = cardShape)
            .summaryMetricClickable(onClick)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Green500,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (visitedDongNames.isEmpty()) {
            Text(
                text = emptyValue,
                style = MaterialTheme.typography.titleLarge,
                color = if (isEmptyValueNoData) Gray400 else Gray900,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                visitedDongNames.forEachIndexed { index, dongName ->
                    Text(
                        text = "${index + 1}. $dongName",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray900,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun DaySummaryVisitedDongCardSkeleton(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Gray50, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BaseSkeletonBlock(
            brush = shimmerBrush,
            modifier = Modifier
                .fillMaxWidth(0.32f)
                .height(16.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(3) { index ->
                BaseSkeletonBlock(
                    brush = shimmerBrush,
                    modifier = Modifier
                        .fillMaxWidth(
                            when (index) {
                                0 -> 0.42f
                                1 -> 0.58f
                                else -> 0.36f
                            }
                        )
                        .height(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun DaySummaryMetricCardPreview() {
    PassedPathTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DaySummaryMetricCard(label = "Outing time", value = "09:12")
            DaySummaryMetricCard(label = "Enter home time", value = "21:03")
            DaySummaryMetricCard(label = "Total outing time", value = "11h 51m")
            DaySummaryMetricCard(label = "Outing count", value = "3")
            DaySummaryVisitedDongCard(
                label = "Visited places",
                visitedDongNames = listOf("Jeongneung-dong", "Seongbuk-dong", "Hyehwa-dong")
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun DaySummaryMetricCardSkeletonPreview() {
    PassedPathTheme {
        val skeletonBrush = rememberBaseSkeletonBrush()

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            repeat(4) {
                DaySummaryMetricCardSkeleton(shimmerBrush = skeletonBrush)
            }
            DaySummaryVisitedDongCardSkeleton(shimmerBrush = skeletonBrush)
        }
    }
}

private const val EmptySummaryValue = "-"
