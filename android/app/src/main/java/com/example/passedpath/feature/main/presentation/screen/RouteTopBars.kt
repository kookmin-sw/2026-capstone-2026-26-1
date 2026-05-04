package com.example.passedpath.feature.main.presentation.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.passedpath.R
import com.example.passedpath.feature.main.presentation.component.MainMorePopupMenu
import com.example.passedpath.feature.route.presentation.screen.formatDistanceKm
import com.example.passedpath.feature.route.presentation.state.SelectedDayRouteUiState
import com.example.passedpath.ui.theme.Gray300
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.PassedPathTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val TopBarDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd. EEE", Locale.KOREAN)

private val DateNavigationBarHeight = 40.dp
private val DaySummaryBarHeight = 36.dp
internal val RouteTopBarsHeight = DateNavigationBarHeight + DaySummaryBarHeight
private val SecondaryTextColor = Color(0xFF4B5563)
private val RouteTopBarsPreviewBackground = Color(0xFFEFF3F8)

@Composable
internal fun RouteTopBars(
    route: SelectedDayRouteUiState,
    isBookmarkUpdating: Boolean,
    onDateSelected: (String) -> Unit,
    onBookmarkClick: () -> Unit,
    isMoreMenuVisible: Boolean = false,
    onMoreClick: () -> Unit = {},
    onMoreDismissRequest: () -> Unit = {},
    onMorePlaceBookmarkClick: () -> Unit = {},
    onMoreDeleteRecordClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            DateNavigationBar(
                route = route,
                isBookmarkUpdating = isBookmarkUpdating,
                onDateSelected = onDateSelected,
                onBookmarkClick = onBookmarkClick,
                isMoreMenuVisible = isMoreMenuVisible,
                onMoreClick = onMoreClick,
                onMoreDismissRequest = onMoreDismissRequest,
                onMorePlaceBookmarkClick = onMorePlaceBookmarkClick,
                onMoreDeleteRecordClick = onMoreDeleteRecordClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DateNavigationBarHeight)
            )
            DaySummaryBar(
                totalDistanceKm = route.totalDistanceKm,
                pathPointCount = route.pathPointCount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DaySummaryBarHeight)
            )
        }
    }
}

@Composable
internal fun DateNavigationBar(
    route: SelectedDayRouteUiState,
    isBookmarkUpdating: Boolean,
    onDateSelected: (String) -> Unit,
    onBookmarkClick: () -> Unit,
    isMoreMenuVisible: Boolean,
    onMoreClick: () -> Unit,
    onMoreDismissRequest: () -> Unit,
    onMorePlaceBookmarkClick: () -> Unit,
    onMoreDeleteRecordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDate = parseDateOrToday(route.dateKey)
    val moreMenuOffset = with(LocalDensity.current) {
        IntOffset(
            x = 0,
            y = (DateNavigationBarHeight + 8.dp).roundToPx()
        )
    }

    Surface(
        modifier = modifier,
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NavigationArrowButton(
                    iconResId = R.drawable.ic_arrow_left,
                    onClick = { onDateSelected(shiftDate(route.dateKey, -1)) }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    NavigationArrowButton(
                        iconResId = R.drawable.ic_arrow_right,
                        onClick = { onDateSelected(shiftDate(route.dateKey, 1)) }
                    )
                    MoreMenuButton(onClick = onMoreClick)
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(start = 48.dp, end = 76.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
            ) {
                BookmarkToggleButton(
                    isBookmarked = route.isBookmarked,
                    isEnabled = !isBookmarkUpdating,
                    onClick = onBookmarkClick
                )
                Row(
                    modifier = Modifier.clickable {
                        showDatePicker(context, route.dateKey, onDateSelected)
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = selectedDate.format(TopBarDateFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id =
                                R.drawable.ic_arrow_down),
                            contentDescription = null,
                            tint = Gray900,
                            modifier = Modifier.size(width = 12.dp,
                                height = 7.dp)
                        )
                    }
                }
            }
            if (isMoreMenuVisible) {
                Popup(
                    alignment = Alignment.TopEnd,
                    offset = moreMenuOffset,
                    onDismissRequest = onMoreDismissRequest,
                    properties = PopupProperties(focusable = true)
                ) {
                    MainMorePopupMenu(
                        onPlaceBookmarkClick = onMorePlaceBookmarkClick,
                        onDeleteRecordClick = onMoreDeleteRecordClick
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationArrowButton(
    iconResId: Int,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(24.dp)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = Gray400,
            modifier = Modifier.size(width = 7.dp, height = 12.dp)
        )
    }
}


@Composable
private fun MoreMenuButton(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_more_vert),
            contentDescription = stringResource(R.string.main_more),
            tint = Gray900,
            modifier = Modifier.size(width = 4.dp, height = 18.dp)
        )
    }
}

@Composable
private fun BookmarkToggleButton(
    isBookmarked: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            painter = painterResource(
                id = if (isBookmarked) {
                    R.drawable.ic_bookmark_star_filled
                } else {
                    R.drawable.ic_bookmark_star_outline
                }
            ),
            contentDescription = stringResource(R.string.main_toggle_bookmark),
            tint = Color.Unspecified,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
internal fun DaySummaryBar(
    totalDistanceKm: Double,
    pathPointCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = Gray300
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryItem(
                    text = stringResource(
                        R.string.main_total_distance_value,
                        totalDistanceKm.formatDistanceKm()
                    )
                )
                Text(
                    text = "|",
                    color = Gray300,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                SummaryItem(
                    text = stringResource(R.string.main_path_points_value, pathPointCount)
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = SecondaryTextColor,
        maxLines = 1
    )
}

private fun showDatePicker(
    context: android.content.Context,
    initialDateKey: String,
    onDateSelected: (String) -> Unit
) {
    val initialDate = runCatching { LocalDate.parse(initialDateKey, DateFormatter) }
        .getOrDefault(LocalDate.now())

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth).format(DateFormatter))
        },
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth
    ).show()
}

private fun parseDateOrToday(dateKey: String): LocalDate {
    return runCatching { LocalDate.parse(dateKey, DateFormatter) }.getOrDefault(LocalDate.now())
}

private fun shiftDate(dateKey: String, days: Long): String {
    return parseDateOrToday(dateKey).plusDays(days).format(DateFormatter)
}

@Preview(showBackground = true, name = "Route Top Bars")
@Composable
private fun RouteTopBarsPreview() {
    PassedPathTheme {
        RouteTopBars(
            route = previewRouteTopBarsState(),
            isBookmarkUpdating = false,
            onDateSelected = {},
            onBookmarkClick = {},
            modifier = Modifier.background(RouteTopBarsPreviewBackground)
        )
    }
}

private fun previewRouteTopBarsState(): SelectedDayRouteUiState {
    return SelectedDayRouteUiState(
        dateKey = "2026-01-20",
        isBookmarked = true,
        totalDistanceKm = 3.4,
        pathPointCount = 1274
    )
}
