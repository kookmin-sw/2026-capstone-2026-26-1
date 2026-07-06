package com.example.passedpath.feature.calendar.presentation.screen

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.R
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.calendar.presentation.component.CalendarFavoriteSettingsContent
import com.example.passedpath.feature.calendar.presentation.component.CalendarMoreActionSheet
import com.example.passedpath.feature.calendar.presentation.model.CalendarDayStatus
import com.example.passedpath.feature.calendar.presentation.model.CalendarMonthCell
import com.example.passedpath.feature.calendar.presentation.model.buildCalendarMonthCells
import com.example.passedpath.feature.calendar.presentation.model.toCalendarDateKey
import com.example.passedpath.feature.calendar.presentation.model.toggleCalendarSelectedDateKey
import com.example.passedpath.feature.calendar.presentation.viewmodel.CalendarViewModel
import com.example.passedpath.feature.calendar.presentation.viewmodel.CalendarViewModelFactory
import com.example.passedpath.ui.component.button.BaseButton
import com.example.passedpath.ui.component.dialog.BaseConfirmDialog
import com.example.passedpath.ui.component.feedback.NetworkFailureBanner
import com.example.passedpath.ui.component.loading.BaseLoadingLine
import com.example.passedpath.ui.component.modal.PassedPathBottomModal
import com.example.passedpath.ui.component.toast.ToastOverlayHost
import com.example.passedpath.ui.component.toast.ToastOverlayItem
import com.example.passedpath.ui.theme.Black
import com.example.passedpath.ui.theme.CalendarSaturdayColor
import com.example.passedpath.ui.theme.CalendarSundayColor
import com.example.passedpath.ui.theme.DateBookmarkColor
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green100
import com.example.passedpath.ui.theme.Green300
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.Green700
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min

@Composable
fun CalendarRoute(
    initialDateKey: String,
    onBackClick: () -> Unit,
    onDateConfirmed: (String) -> Unit,
    onFavoriteListClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val context = LocalContext.current
    val initialDate = remember(initialDateKey) { parseDateOrToday(initialDateKey) }
    var anchorDateKey by rememberSaveable(initialDateKey) {
        mutableStateOf(initialDate.toCalendarDateKey())
    }
    var selectedDateKey by rememberSaveable(initialDateKey) {
        mutableStateOf<String?>(null)
    }
    var screenMode by rememberSaveable {
        mutableStateOf(CalendarScreenMode.Normal)
    }
    var selectedMonthFavoriteDateKeys by rememberSaveable {
        mutableStateOf(emptyList<String>())
    }
    var originalMonthFavoriteDateKeys by rememberSaveable {
        mutableStateOf(emptyList<String>())
    }
    var editingFavoriteMonthKey by rememberSaveable {
        mutableStateOf<String?>(null)
    }
    var pendingFavoriteTargetDateKey by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingFavoriteCloseAfterDiscard by rememberSaveable { mutableStateOf(false) }
    var isMoreActionSheetVisible by rememberSaveable { mutableStateOf(false) }
    val anchorDate = remember(anchorDateKey) { parseDateOrToday(anchorDateKey) }
    val selectedDate = remember(selectedDateKey) {
        selectedDateKey?.let(::parseDateOrToday)
    }
    val selectedMonthFavoriteDateKeySet = remember(selectedMonthFavoriteDateKeys) {
        selectedMonthFavoriteDateKeys.toSet()
    }
    val originalMonthFavoriteDateKeySet = remember(originalMonthFavoriteDateKeys) {
        originalMonthFavoriteDateKeys.toSet()
    }
    val visibleMonth = remember(anchorDate) { YearMonth.from(anchorDate) }
    val changedMonthFavoriteDateKeys = remember(
        selectedMonthFavoriteDateKeySet,
        originalMonthFavoriteDateKeySet
    ) {
        (
            (selectedMonthFavoriteDateKeySet - originalMonthFavoriteDateKeySet) +
                (originalMonthFavoriteDateKeySet - selectedMonthFavoriteDateKeySet)
            ).sorted()
    }
    val hasFavoriteSettingsChanges = changedMonthFavoriteDateKeys.isNotEmpty()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bookmarkRegistrationUiState = uiState.bookmarkRegistrationUiState

    fun resetFavoriteMonthEditState() {
        selectedMonthFavoriteDateKeys = emptyList()
        originalMonthFavoriteDateKeys = emptyList()
        editingFavoriteMonthKey = null
    }

    fun closeFavoriteSettingsMode() {
        screenMode = CalendarScreenMode.Normal
        resetFavoriteMonthEditState()
    }

    fun requestFavoriteTargetDate(targetDate: LocalDate) {
        val targetDateKey = targetDate.toCalendarDateKey()
        if (hasFavoriteSettingsChanges) {
            pendingFavoriteTargetDateKey = targetDateKey
            pendingFavoriteCloseAfterDiscard = false
        } else {
            resetFavoriteMonthEditState()
            anchorDateKey = targetDateKey
        }
    }

    fun requestCloseFavoriteSettingsMode() {
        if (hasFavoriteSettingsChanges) {
            pendingFavoriteTargetDateKey = null
            pendingFavoriteCloseAfterDiscard = true
        } else {
            closeFavoriteSettingsMode()
        }
    }

    fun dismissFavoriteDiscardDialog() {
        pendingFavoriteTargetDateKey = null
        pendingFavoriteCloseAfterDiscard = false
    }

    fun confirmFavoriteDiscardDialog() {
        val targetDateKey = pendingFavoriteTargetDateKey
        val shouldClose = pendingFavoriteCloseAfterDiscard
        dismissFavoriteDiscardDialog()
        if (shouldClose) {
            closeFavoriteSettingsMode()
        } else if (targetDateKey != null) {
            resetFavoriteMonthEditState()
            anchorDateKey = targetDateKey
        }
    }

    LaunchedEffect(visibleMonth) {
        viewModel.loadMonth(visibleMonth = visibleMonth)
    }

    LaunchedEffect(
        screenMode,
        uiState.loadedMonth,
        uiState.dayStatuses,
        visibleMonth,
        editingFavoriteMonthKey
    ) {
        if (
            screenMode == CalendarScreenMode.FavoriteSettings &&
            uiState.loadedMonth == visibleMonth &&
            editingFavoriteMonthKey != visibleMonth.toString()
        ) {
            val loadedFavoriteDateKeys = uiState.dayStatuses
                .filter { (date, status) ->
                    YearMonth.from(date) == visibleMonth && status.isBookmarked
                }
                .keys
                .map(LocalDate::toCalendarDateKey)
                .sorted()

            originalMonthFavoriteDateKeys = loadedFavoriteDateKeys
            selectedMonthFavoriteDateKeys = loadedFavoriteDateKeys
            editingFavoriteMonthKey = visibleMonth.toString()
        }
    }

    LaunchedEffect(
        bookmarkRegistrationUiState.feedbackEventId,
        bookmarkRegistrationUiState.isSuccess
    ) {
        if (
            bookmarkRegistrationUiState.feedbackEventId > 0L &&
            bookmarkRegistrationUiState.isSuccess
        ) {
            originalMonthFavoriteDateKeys = selectedMonthFavoriteDateKeys.distinct().sorted()
        }
    }

    BackHandler(enabled = screenMode == CalendarScreenMode.FavoriteSettings) {
        requestCloseFavoriteSettingsMode()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (screenMode) {
            CalendarScreenMode.Normal -> {
                CalendarScreen(
                    anchorDate = anchorDate,
                    visibleMonth = visibleMonth,
                    selectedDate = selectedDate,
                    today = LocalDate.now(),
                    dayStatuses = uiState.dayStatuses,
                    isLoading = uiState.isLoading && uiState.loadingMonth == visibleMonth,
                    errorMessage = uiState.errorMessage,
                    onBackClick = onBackClick,
                    onDatePickedFromSystem = { pickedDate ->
                        anchorDateKey = pickedDate.toCalendarDateKey()
                        selectedDateKey = null
                    },
                    onPreviousMonthClick = {
                        anchorDateKey = anchorDate.shiftMonth(-1).toCalendarDateKey()
                        selectedDateKey = null
                    },
                    onNextMonthClick = {
                        anchorDateKey = anchorDate.shiftMonth(1).toCalendarDateKey()
                        selectedDateKey = null
                    },
                    onDateClick = { date ->
                        anchorDateKey = date.toCalendarDateKey()
                        selectedDateKey = toggleCalendarSelectedDateKey(
                            currentSelectedDateKey = selectedDateKey,
                            clickedDate = date
                        )
                    },
                    onFavoriteListClick = onFavoriteListClick,
                    onMoreClick = {
                        isMoreActionSheetVisible = true
                        onMoreClick()
                    },
                    onRetryClick = {
                        viewModel.loadMonth(
                            visibleMonth = visibleMonth,
                            forceRefresh = true
                        )
                    },
                    onConfirmClick = {
                        selectedDateKey?.let(onDateConfirmed)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            CalendarScreenMode.FavoriteSettings -> {
                CalendarFavoriteSettingsContent(
                    anchorDate = anchorDate,
                    visibleMonth = visibleMonth,
                    selectedDateKeys = selectedMonthFavoriteDateKeySet,
                    today = LocalDate.now(),
                    dayStatuses = uiState.dayStatuses,
                    isLoading = uiState.isLoading && uiState.loadingMonth == visibleMonth,
                    errorMessage = uiState.errorMessage,
                    isSubmitting = bookmarkRegistrationUiState.isSubmitting,
                    hasChanges = hasFavoriteSettingsChanges,
                    onBackClick = { requestCloseFavoriteSettingsMode() },
                    onPreviousMonthClick = {
                        requestFavoriteTargetDate(anchorDate.shiftMonth(-1))
                    },
                    onNextMonthClick = {
                        requestFavoriteTargetDate(anchorDate.shiftMonth(1))
                    },
                    onMonthTitleClick = {
                        showCalendarDatePicker(
                            context = context,
                            initialDate = anchorDate,
                            onDatePicked = { pickedDate ->
                                requestFavoriteTargetDate(pickedDate)
                            }
                        )
                    },
                    onClearVisibleMonthSelectionClick = {
                        selectedMonthFavoriteDateKeys = emptyList()
                    },
                    onRestoreVisibleMonthSelectionClick = {
                        selectedMonthFavoriteDateKeys =
                            originalMonthFavoriteDateKeys.distinct().sorted()
                    },
                    onDateClick = { date ->
                        val dateKey = date.toCalendarDateKey()
                        selectedMonthFavoriteDateKeys =
                            if (dateKey in selectedMonthFavoriteDateKeySet) {
                                selectedMonthFavoriteDateKeys.filterNot { it == dateKey }
                            } else {
                                (selectedMonthFavoriteDateKeys + dateKey).distinct().sorted()
                            }
                    },
                    onRetryClick = {
                        viewModel.loadMonth(
                            visibleMonth = visibleMonth,
                            forceRefresh = true
                        )
                    },
                    onSubmitClick = {
                        viewModel.toggleChangedDateBookmarks(
                            changedDateKeys = changedMonthFavoriteDateKeys,
                            selectedDateKeys = selectedMonthFavoriteDateKeySet
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        bookmarkRegistrationUiState.feedbackMessage?.let { message ->
            ToastOverlayHost(
                toasts = listOf(
                    ToastOverlayItem(
                        message = message,
                        triggerKey = "calendar-bookmark:" +
                            "${bookmarkRegistrationUiState.feedbackEventId}:$message",
                        onDismissed = {
                            viewModel.consumeBookmarkRegistrationFeedback(
                                bookmarkRegistrationUiState.feedbackEventId
                            )
                        }
                    )
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 84.dp)
            )
        }
    }

    if (isMoreActionSheetVisible) {
        PassedPathBottomModal(
            onDimClick = { isMoreActionSheetVisible = false },
            modifier = Modifier.background(Black.copy(alpha = 0.22f)),
            onBackPress = { isMoreActionSheetVisible = false }
        ) {
            CalendarMoreActionSheet(
                onFavoriteSettingsClick = {
                    isMoreActionSheetVisible = false
                    selectedDateKey = null
                    resetFavoriteMonthEditState()
                    screenMode = CalendarScreenMode.FavoriteSettings
                },
                onDeleteRecordsClick = { isMoreActionSheetVisible = false }
            )
        }
    }

    if (pendingFavoriteCloseAfterDiscard || pendingFavoriteTargetDateKey != null) {
        BaseConfirmDialog(
            title = stringResource(R.string.calendar_favorite_settings_discard_title),
            message = stringResource(R.string.calendar_favorite_settings_discard_message),
            dismissText = stringResource(R.string.calendar_favorite_settings_discard_cancel),
            confirmText = stringResource(R.string.calendar_favorite_settings_discard_confirm),
            onDismiss = { dismissFavoriteDiscardDialog() },
            onConfirm = { confirmFavoriteDiscardDialog() }
        )
    }
}

@Composable
private fun CalendarScreen(
    anchorDate: LocalDate,
    visibleMonth: YearMonth,
    selectedDate: LocalDate?,
    today: LocalDate,
    dayStatuses: Map<LocalDate, CalendarDayStatus>,
    isLoading: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onDatePickedFromSystem: (LocalDate) -> Unit,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    onFavoriteListClick: () -> Unit,
    onMoreClick: () -> Unit,
    onRetryClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cells = remember(visibleMonth, dayStatuses) {
        buildCalendarMonthCells(
            visibleMonth = visibleMonth,
            dayStatuses = dayStatuses
        )
    }
    val selectedDateKey = selectedDate?.toCalendarDateKey()
    val ctaText = selectedDateKey?.let { dateKey ->
        stringResource(R.string.calendar_record_view_cta, dateKey)
    } ?: stringResource(R.string.calendar_no_date_selected_cta)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
            .statusBarsPadding()
    ) {
        CalendarTopBar(
            anchorDate = anchorDate,
            onBackClick = onBackClick,
            onDateClick = {
                showCalendarDatePicker(
                    context = context,
                    initialDate = anchorDate,
                    onDatePicked = onDatePickedFromSystem
                )
            },
            onPreviousMonthClick = onPreviousMonthClick,
            onNextMonthClick = onNextMonthClick,
            onFavoriteListClick = onFavoriteListClick,
            onMoreClick = onMoreClick
        )

        if (isLoading && errorMessage == null) {
            Spacer(modifier = Modifier.height(20.dp))
            BaseLoadingLine(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(9.dp))
        } else {
            Spacer(modifier = Modifier.height(33.dp))
        }

        WeekdayHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        CalendarMonthGrid(
            cells = cells,
            today = today,
            selectedDate = selectedDate,
            onDateClick = onDateClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        CalendarLegend(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, end = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        if (errorMessage != null) {
            CalendarErrorNotice(
                onRetryClick = onRetryClick,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            )
        }

        BaseButton(
            text = ctaText,
            onClick = onConfirmClick,
            enabled = selectedDate != null,
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
private fun CalendarErrorNotice(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NetworkFailureBanner(
        retryText = stringResource(R.string.route_retry),
        onRetryClick = onRetryClick,
        modifier = modifier
    )
}

@Composable
private fun CalendarTopBar(
    anchorDate: LocalDate,
    onBackClick: () -> Unit,
    onDateClick: () -> Unit,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onFavoriteListClick: () -> Unit,
    onMoreClick: () -> Unit
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

        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MonthArrowButton(
                iconResId = R.drawable.ic_arrow_left,
                contentDescription = stringResource(R.string.calendar_previous_month),
                onClick = onPreviousMonthClick
            )
            CalendarDateTitle(
                anchorDate = anchorDate,
                onClick = onDateClick
            )
            MonthArrowButton(
                iconResId = R.drawable.ic_arrow_right,
                contentDescription = stringResource(R.string.calendar_next_month),
                onClick = onNextMonthClick
            )
        }

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onFavoriteListClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star_list),
                    contentDescription = stringResource(R.string.calendar_favorite_list),
                    tint = Gray400,
                    modifier = Modifier.size(width = 27.dp, height = 17.dp)
                )
            }
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_vert),
                    contentDescription = stringResource(R.string.main_more),
                    tint = Gray400,
                    modifier = Modifier.size(width = 4.dp, height = 18.dp)
                )
            }
        }
    }
}

@Composable
private fun CalendarDateTitle(
    anchorDate: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
    ) {
        Text(
            text = anchorDate.format(CalendarHeaderFormatter),
            style = MaterialTheme.typography.titleMedium,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Gray900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_down),
            contentDescription = null,
            tint = Gray900,
            modifier = Modifier.size(width = 12.dp, height = 7.dp)
        )
    }
}

@Composable
private fun MonthArrowButton(
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
private fun WeekdayHeader(
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        weekdayLabels().forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Gray400,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    cells: List<CalendarMonthCell>,
    today: LocalDate,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        cells.chunked(7).forEach { weekCells ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekCells.forEach { cell ->
                    CalendarDayCell(
                        cell = cell,
                        isToday = cell.date == today,
                        isSelected = cell.date == selectedDate,
                        onDateClick = onDateClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
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
            date.dayOfWeek == java.time.DayOfWeek.SUNDAY -> SundayColor
            date.dayOfWeek == java.time.DayOfWeek.SATURDAY -> SaturdayColor
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
                CalendarFavoriteIndicator(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 5.dp)
                )
            }
        }

        CalendarDayIndicators(
            status = cell.status,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 1.dp)
        )
    }
}

@Composable
private fun CalendarDayIndicators(
    status: CalendarDayStatus,
    modifier: Modifier = Modifier
) {
    val colors = buildList {
        if (status.hasManualData) add(ManualDataColor)
        if (status.hasLocationData) add(LocationDataColor)
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
private fun CalendarFavoriteIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(FavoriteColor)
    )
}

@Composable
private fun CalendarLegend(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CalendarLegendItem(
            color = ManualDataColor,
            text = stringResource(R.string.calendar_manual_data)
        )
        Spacer(modifier = Modifier.width(12.dp))
        CalendarLegendItem(
            color = LocationDataColor,
            text = stringResource(R.string.calendar_location_data)
        )
        Spacer(modifier = Modifier.width(12.dp))
        CalendarLegendItem(
            color = FavoriteColor,
            text = stringResource(R.string.calendar_favorite)
        )
    }
}

@Composable
private fun CalendarLegendItem(
    color: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            color = Gray400,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun weekdayLabels(): List<String> {
    val weekdays = java.text.DateFormatSymbols(Locale.KOREAN).shortWeekdays
    return (1..7).map { index -> weekdays[index] }
}

private fun showCalendarDatePicker(
    context: android.content.Context,
    initialDate: LocalDate,
    onDatePicked: (LocalDate) -> Unit
) {
    val zoneId = ZoneId.systemDefault()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDatePicked(LocalDate.of(year, month + 1, dayOfMonth))
        },
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth
    ).apply {
        datePicker.minDate = LocalDate.of(MinSelectableYear, 1, 1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
        datePicker.maxDate = LocalDate.of(MaxSelectableYear, 12, 31)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }.show()
}

private fun parseDateOrToday(dateKey: String): LocalDate {
    return runCatching { LocalDate.parse(dateKey) }
        .getOrDefault(LocalDate.now())
        .coerceToSelectableDateRange()
}

private fun LocalDate.shiftMonth(monthDelta: Long): LocalDate {
    val nextMonth = YearMonth.from(this).plusMonths(monthDelta)
    val nextDayOfMonth = min(dayOfMonth, nextMonth.lengthOfMonth())
    return nextMonth.atDay(nextDayOfMonth).coerceToSelectableDateRange()
}

private fun LocalDate.coerceToSelectableDateRange(): LocalDate {
    val minDate = LocalDate.of(MinSelectableYear, 1, 1)
    val maxDate = LocalDate.of(MaxSelectableYear, 12, 31)
    return when {
        isBefore(minDate) -> minDate
        isAfter(maxDate) -> maxDate
        else -> this
    }
}

private val CalendarHeaderFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd. EEE", Locale.KOREAN)

private val SundayColor = CalendarSundayColor
private val SaturdayColor = CalendarSaturdayColor
private val ManualDataColor = Green700
private val LocationDataColor = Green300
private val FavoriteColor = DateBookmarkColor

private const val MinSelectableYear = 2000
private const val MaxSelectableYear = 3000

private enum class CalendarScreenMode {
    Normal,
    FavoriteSettings
}

@Preview(
    name = "Calendar Content",
    group = "Calendar",
    showBackground = true,
    widthDp = 393,
    heightDp = 760
)
@Composable
private fun CalendarContentPreview() {
    PassedPathTheme {
        CalendarScreen(
            anchorDate = CalendarPreviewAnchorDate,
            visibleMonth = CalendarPreviewMonth,
            selectedDate = null,
            today = CalendarPreviewToday,
            dayStatuses = CalendarPreviewStatuses,
            isLoading = false,
            errorMessage = null,
            onBackClick = {},
            onDatePickedFromSystem = {},
            onPreviousMonthClick = {},
            onNextMonthClick = {},
            onDateClick = {},
            onFavoriteListClick = {},
            onMoreClick = {},
            onRetryClick = {},
            onConfirmClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

private val CalendarPreviewMonth = YearMonth.of(2026, 1)
private val CalendarPreviewAnchorDate = LocalDate.of(2026, 1, 20)
private val CalendarPreviewToday = LocalDate.of(2026, 1, 22)
private val CalendarPreviewStatuses = mapOf(
    LocalDate.of(2026, 1, 3) to CalendarDayStatus(isBookmarked = true),
    LocalDate.of(2026, 1, 6) to CalendarDayStatus(hasManualData = true),
    LocalDate.of(2026, 1, 13) to CalendarDayStatus(
        hasManualData = true,
        hasLocationData = true,
        isBookmarked = true
    ),
    LocalDate.of(2026, 1, 14) to CalendarDayStatus(isBookmarked = true),
    LocalDate.of(2026, 1, 20) to CalendarDayStatus(
        hasManualData = true,
        hasLocationData = true
    )
)
