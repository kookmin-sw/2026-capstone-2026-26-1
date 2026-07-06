package com.example.passedpath.feature.calendar.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.feature.calendar.presentation.model.CalendarDayStatus
import com.example.passedpath.feature.calendar.presentation.model.CalendarMonthCell
import com.example.passedpath.feature.calendar.presentation.model.buildCalendarMonthCells
import com.example.passedpath.feature.calendar.presentation.model.toCalendarDateKey
import com.example.passedpath.ui.component.button.BaseButton
import com.example.passedpath.ui.component.feedback.NetworkFailureBanner
import com.example.passedpath.ui.component.loading.BaseLoadingLine
import com.example.passedpath.ui.theme.CalendarSaturdayColor
import com.example.passedpath.ui.theme.CalendarSundayColor
import com.example.passedpath.ui.theme.DateBookmarkColor
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray50
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green100
import com.example.passedpath.ui.theme.Green300
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.Green700
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White
import java.text.DateFormatSymbols
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun CalendarFavoriteSettingsContent(
    anchorDate: LocalDate,
    visibleMonth: YearMonth,
    selectedDateKeys: Set<String>,
    today: LocalDate,
    dayStatuses: Map<LocalDate, CalendarDayStatus>,
    isLoading: Boolean,
    errorMessage: String?,
    isSubmitting: Boolean,
    hasChanges: Boolean,
    onBackClick: () -> Unit,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onMonthTitleClick: () -> Unit,
    onClearVisibleMonthSelectionClick: () -> Unit,
    onRestoreVisibleMonthSelectionClick: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    onRetryClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cells = remember(visibleMonth, dayStatuses) {
        buildCalendarMonthCells(
            visibleMonth = visibleMonth,
            dayStatuses = dayStatuses
        )
    }
    val ctaText = if (hasChanges) {
        stringResource(R.string.calendar_favorite_settings_submit)
    } else {
        stringResource(R.string.calendar_favorite_settings_empty_cta)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50)
            .statusBarsPadding()
    ) {
        CalendarFavoriteSettingsTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(White)
                .padding(top = 8.dp, bottom = 12.dp)
        ) {
            CalendarFavoriteSettingsCardHeader(
                anchorDate = anchorDate,
                hasSelectedDates = selectedDateKeys.isNotEmpty(),
                hasChanges = hasChanges,
                onPreviousMonthClick = onPreviousMonthClick,
                onNextMonthClick = onNextMonthClick,
                onMonthTitleClick = onMonthTitleClick,
                onClearVisibleMonthSelectionClick = onClearVisibleMonthSelectionClick,
                onRestoreVisibleMonthSelectionClick = onRestoreVisibleMonthSelectionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            if (isLoading && errorMessage == null) {
                Spacer(modifier = Modifier.height(18.dp))
                BaseLoadingLine(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(34.dp))
            }

            CalendarFavoriteSettingsWeekdayHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            CalendarFavoriteSettingsMonthGrid(
                cells = cells,
                today = today,
                selectedDateKeys = selectedDateKeys,
                onDateClick = onDateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            CalendarFavoriteSettingsLegend(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, end = 16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (errorMessage != null) {
            NetworkFailureBanner(
                retryText = stringResource(R.string.route_retry),
                onRetryClick = onRetryClick,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            )
        }

        BaseButton(
            text = ctaText,
            onClick = onSubmitClick,
            enabled = hasChanges && !isSubmitting,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            textFontSize = 15.sp,
            textFontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CalendarFavoriteSettingsTopBar(
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
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
                contentDescription = stringResource(R.string.calendar_back),
                tint = Gray900,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = stringResource(R.string.calendar_favorite_settings_title),
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Gray900,
            maxLines = 1
        )
    }
}

@Composable
private fun CalendarFavoriteSettingsCardHeader(
    anchorDate: LocalDate,
    hasSelectedDates: Boolean,
    hasChanges: Boolean,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onMonthTitleClick: () -> Unit,
    onClearVisibleMonthSelectionClick: () -> Unit,
    onRestoreVisibleMonthSelectionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.height(44.dp),
        contentAlignment = Alignment.Center
    ) {
        CalendarFavoriteSettingsMonthBar(
            anchorDate = anchorDate,
            onPreviousMonthClick = onPreviousMonthClick,
            onNextMonthClick = onNextMonthClick,
            onMonthTitleClick = onMonthTitleClick,
            modifier = Modifier.align(Alignment.Center)
        )

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(70.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.End)
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hasChanges) {
                    IconButton(
                        onClick = onRestoreVisibleMonthSelectionClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_undo),
                            contentDescription = stringResource(
                                R.string.calendar_favorite_settings_restore
                            ),
                            tint = Gray400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(30.dp)
                    .height(36.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (hasSelectedDates) {
                    Text(
                        text = stringResource(R.string.calendar_favorite_settings_clear_month),
                        modifier = Modifier
                            .heightIn(min = 36.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(onClick = onClearVisibleMonthSelectionClick)
                            .padding(start = 6.dp, end = 0.dp, top = 10.dp, bottom = 4.dp),
                        color = Gray400,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarFavoriteSettingsMonthBar(
    anchorDate: LocalDate,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onMonthTitleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CalendarFavoriteSettingsMonthArrowButton(
            iconResId = R.drawable.ic_arrow_left,
            contentDescription = stringResource(R.string.calendar_previous_month),
            onClick = onPreviousMonthClick
        )
        Row(
            modifier = Modifier
                .clickable(onClick = onMonthTitleClick)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = anchorDate.format(FavoriteSettingsMonthFormatter),
                color = Gray900,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_down),
                contentDescription = null,
                tint = Gray900,
                modifier = Modifier.size(width = 12.dp, height = 7.dp)
            )
        }
        CalendarFavoriteSettingsMonthArrowButton(
            iconResId = R.drawable.ic_arrow_right,
            contentDescription = stringResource(R.string.calendar_next_month),
            onClick = onNextMonthClick
        )
    }
}

@Composable
private fun CalendarFavoriteSettingsMonthArrowButton(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = Gray400,
            modifier = Modifier.size(width = 10.dp, height = 12.dp)
        )
    }
}

@Composable
private fun CalendarFavoriteSettingsWeekdayHeader(
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        favoriteSettingsWeekdayLabels().forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Gray400,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CalendarFavoriteSettingsMonthGrid(
    cells: List<CalendarMonthCell>,
    today: LocalDate,
    selectedDateKeys: Set<String>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        cells.chunked(DaysPerWeek).forEach { weekCells ->
            Row(modifier = Modifier.fillMaxWidth()) {
                weekCells.forEach { cell ->
                    CalendarFavoriteSettingsDayCell(
                        cell = cell,
                        isToday = cell.date == today,
                        isSelected = cell.date
                            ?.toCalendarDateKey()
                            ?.let { dateKey -> dateKey in selectedDateKeys } == true,
                        onDateClick = onDateClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarFavoriteSettingsDayCell(
    cell: CalendarMonthCell,
    isToday: Boolean,
    isSelected: Boolean,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val date = cell.date
    Box(
        modifier = modifier.height(44.dp),
        contentAlignment = Alignment.Center
    ) {
        if (date == null) return@Box

        val dateColor = when {
            isSelected -> White
            date.dayOfWeek == DayOfWeek.SUNDAY -> CalendarSundayColor
            date.dayOfWeek == DayOfWeek.SATURDAY -> CalendarSaturdayColor
            else -> Gray900
        }
        val backgroundColor = when {
            isSelected -> Green500
            isToday -> Green100
            else -> Color.Transparent
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable { onDateClick(date) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = dateColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (cell.status.isBookmarked) {
                CalendarFavoriteSettingsFavoriteIndicator(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 5.dp)
                )
            }
        }

        CalendarFavoriteSettingsDayIndicators(
            status = cell.status,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 1.dp)
        )
    }
}

@Composable
private fun CalendarFavoriteSettingsDayIndicators(
    status: CalendarDayStatus,
    modifier: Modifier = Modifier
) {
    val colors = buildList {
        if (status.hasManualData) add(Green700)
        if (status.hasLocationData) add(Green300)
    }
    if (colors.isEmpty()) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun CalendarFavoriteSettingsFavoriteIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(DateBookmarkColor)
    )
}

@Composable
private fun CalendarFavoriteSettingsLegend(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CalendarFavoriteSettingsLegendItem(
            color = Green700,
            text = stringResource(R.string.calendar_manual_data)
        )
        Spacer(modifier = Modifier.width(10.dp))
        CalendarFavoriteSettingsLegendItem(
            color = Green300,
            text = stringResource(R.string.calendar_location_data)
        )
        Spacer(modifier = Modifier.width(10.dp))
        CalendarFavoriteSettingsLegendItem(
            color = DateBookmarkColor,
            text = stringResource(R.string.calendar_favorite)
        )
    }
}

@Composable
private fun CalendarFavoriteSettingsLegendItem(
    color: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            color = Gray400,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

private fun favoriteSettingsWeekdayLabels(): List<String> {
    val weekdays = DateFormatSymbols(Locale.KOREAN).shortWeekdays
    return (1..DaysPerWeek).map { index -> weekdays[index] }
}

private val FavoriteSettingsMonthFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)

private const val DaysPerWeek = 7

@Preview(
    name = "Calendar Favorite Settings",
    showBackground = true,
    widthDp = 393,
    heightDp = 760
)
@Composable
private fun CalendarFavoriteSettingsContentPreview() {
    PassedPathTheme {
        CalendarFavoriteSettingsContent(
            anchorDate = LocalDate.of(2026, 1, 20),
            visibleMonth = YearMonth.of(2026, 1),
            selectedDateKeys = setOf("2026-01-07", "2026-01-08", "2026-01-19"),
            today = LocalDate.of(2026, 1, 22),
            dayStatuses = mapOf(
                LocalDate.of(2026, 1, 3) to CalendarDayStatus(isBookmarked = true),
                LocalDate.of(2026, 1, 13) to CalendarDayStatus(
                    hasManualData = true,
                    isBookmarked = true
                ),
                LocalDate.of(2026, 1, 20) to CalendarDayStatus(
                    hasManualData = true,
                    hasLocationData = true
                )
            ),
            isLoading = false,
            errorMessage = null,
            isSubmitting = false,
            hasChanges = true,
            onBackClick = {},
            onPreviousMonthClick = {},
            onNextMonthClick = {},
            onMonthTitleClick = {},
            onClearVisibleMonthSelectionClick = {},
            onRestoreVisibleMonthSelectionClick = {},
            onDateClick = {},
            onRetryClick = {},
            onSubmitClick = {}
        )
    }
}
