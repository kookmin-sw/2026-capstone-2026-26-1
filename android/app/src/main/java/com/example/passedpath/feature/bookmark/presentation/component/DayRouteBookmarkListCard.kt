package com.example.passedpath.feature.bookmark.presentation.component

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmarkItem
import com.example.passedpath.ui.theme.Gray200
import com.example.passedpath.ui.theme.Gray300
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray50
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DayRouteBookmarkListCard(
    bookmark: DayRouteBookmarkItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val hasTitle = bookmark.title?.isNotBlank() == true
    val titleText = bookmark.title
        ?.takeIf { it.isNotBlank() }
        ?: EmptyTitleText
    val visitedRegionText = remember(bookmark.visitedRegions) {
        bookmark.visitedRegions.joinToString(VisitedRegionSeparator)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            },
        shape = CardShape,
        color = White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 28.dp,
                vertical = 16.dp
            )
        ) {
            Text(
                text = bookmark.date.toBookmarkDateLabel(),
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                color = if (hasTitle) Green500 else Gray400,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (visitedRegionText.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                HorizontalDivider(
                    thickness = 0.6.dp,
                    color = Gray200
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_marker_empty),
                        contentDescription = null,
                        tint = Gray300,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = visitedRegionText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun String.toBookmarkDateLabel(): String {
    return runCatching {
        LocalDate.parse(this).format(BookmarkDateFormatter)
    }.getOrDefault(this)
}

private val CardShape = RoundedCornerShape(20.dp)
private val BookmarkDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd (E)", Locale.KOREAN)
private const val EmptyTitleText = "제목 없음"
private const val VisitedRegionSeparator = " · "

@Preview(
    name = "Day Route Bookmark Card Content",
    group = "Bookmark",
    showBackground = true,
    widthDp = 393
)
@Composable
private fun DayRouteBookmarkListCardPreview() {
    PassedPathTheme {
        Column(
            modifier = Modifier
                .background(White)
                .padding(horizontal = 30.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            DayRouteBookmarkListCard(
                bookmark = DayRouteBookmarkItem(
                    date = "2026-01-20",
                    title = "수업 듣고 지연이 만나러 혜화 간 날",
                    visitedRegions = listOf("성북구", "종로구", "혜화동")
                )
            )
            DayRouteBookmarkListCard(
                bookmark = DayRouteBookmarkItem(
                    date = "2026-01-26",
                    title = null,
                    visitedRegions = listOf("하루 요약의 방문 동네 리스트")
                )
            )
            DayRouteBookmarkListCard(
                bookmark = DayRouteBookmarkItem(
                    date = "2026-01-26",
                    title = "",
                    visitedRegions = emptyList()
                )
            )
            DayRouteBookmarkListCard(
                bookmark = DayRouteBookmarkItem(
                    date = "2026-01-20",
                    title = "아주 긴 제목이 들어와도 한 줄에서 자연스럽게 말줄임 처리되는지 확인하는 카드",
                    visitedRegions = listOf("성북구", "강북구", "종로구", "중구", "성동구")
                )
            )
        }
    }
}
