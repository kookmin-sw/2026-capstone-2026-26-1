package com.example.passedpath.feature.main.presentation.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.route.presentation.screen.formatDistanceKm
import com.example.passedpath.feature.route.presentation.state.SelectedDayRouteUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val TopBarDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd. EEE", Locale.KOREAN)

private val DateNavigationBarHeight = 56.dp
private val DaySummaryBarHeight = 42.dp
internal val RouteTopBarsHeight = DateNavigationBarHeight + DaySummaryBarHeight
private val TopBarBackground = Color(0xFFFDFDFD)
private val SummaryBarBackground = Color(0xFFF6F7F9)
private val BorderColor = Color(0xFFE5E7EB)
private val PrimaryTextColor = Color(0xFF111827)
private val SecondaryTextColor = Color(0xFF4B5563)
private val MutedIconColor = Color(0xFF9CA3AF)
private val ActiveBookmarkColor = Color(0xFFF4B400)

@Composable
internal fun RouteTopBars(
    route: SelectedDayRouteUiState,
    onDateSelected: (String) -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            DateNavigationBar(
                route = route,
                onDateSelected = onDateSelected,
                onBookmarkClick = onBookmarkClick,
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
    onDateSelected: (String) -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDate = parseDateOrToday(route.dateKey)

    Surface(
        modifier = modifier,
        color = TopBarBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NavigationArrowButton(
                    arrow = "<",
                    onClick = { onDateSelected(shiftDate(route.dateKey, -1)) }
                )
                NavigationArrowButton(
                    arrow = ">",
                    onClick = { onDateSelected(shiftDate(route.dateKey, 1)) }
                )
            }
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BookmarkToggleButton(
                    isBookmarked = route.isBookmarked,
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
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "v",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryTextColor
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationArrowButton(
    arrow: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Text(
            text = arrow,
            style = MaterialTheme.typography.titleMedium,
            color = MutedIconColor
        )
    }
}

@Composable
private fun BookmarkToggleButton(
    isBookmarked: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp)
    ) {
        Text(
            text = if (isBookmarked) "*" else "o",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isBookmarked) ActiveBookmarkColor else MutedIconColor
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
        color = SummaryBarBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = BorderColor
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
                    color = BorderColor,
                    modifier = Modifier.padding(horizontal = 12.dp)
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
