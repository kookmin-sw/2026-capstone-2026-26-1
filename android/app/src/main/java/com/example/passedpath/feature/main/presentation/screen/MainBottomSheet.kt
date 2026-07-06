package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.daynote.presentation.screen.DayNoteBottomSheetContent
import com.example.passedpath.feature.daynote.presentation.state.DayNoteUiState
import com.example.passedpath.feature.place.presentation.screen.PlaceBottomSheetContent
import com.example.passedpath.feature.place.presentation.state.PlaceUiState
import com.example.passedpath.feature.summary.presentation.screen.DaySummaryBottomSheetContent
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailMetric
import com.example.passedpath.feature.summary.presentation.state.DaySummaryUiState
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetContainer
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetTabItem
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetTabRow
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
internal fun MainBottomSheet(
    selectedDateKey: String,
    placeUiState: PlaceUiState,
    dayNoteUiState: DayNoteUiState,
    daySummaryUiState: DaySummaryUiState,
    selectedPlaceId: Long?,
    onSelectedPlaceHandled: () -> Unit,
    onDayNoteTitleChanged: (String) -> Unit,
    onDayNoteMemoChanged: (String) -> Unit,
    onDayNoteSaveClick: () -> Unit,
    onDaySummaryLoadRequest: (String) -> Unit,
    onDaySummaryRetryClick: () -> Unit,
    onDaySummaryMetricClick: (SummaryDetailMetric) -> Unit = {},
    selectedTab: MainBottomSheetTab,
    onTabSelected: (MainBottomSheetTab) -> Unit,
    onPlaceRetryClick: () -> Unit,
    onAddPlaceClick: () -> Unit,
    onReorderPlaces: (List<Long>) -> Unit,
    onCloseReorderGuideBanner: () -> Unit,
    onEditPlaceClick: (Long) -> Unit,
    onPlaceClick: (Long) -> Unit,
    onDeletePlaceRequested: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isContentScrolled by remember(selectedTab) { mutableStateOf(false) }
    val tabItems = remember {
        MainBottomSheetTab.entries.map(MainBottomSheetTab::toBaseTabItem)
    }

    LaunchedEffect(selectedTab) {
        isContentScrolled = false
    }

    BaseBottomSheetContainer(
        modifier = modifier,
        contentModifier = Modifier.clearFocusOnBackgroundTap {
            focusManager.clearFocus(force = true)
        },
        isContentScrolled = isContentScrolled,
        tabRow = {
            BaseBottomSheetTabRow(
                items = tabItems,
                selectedKey = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) {
        when (selectedTab) {
            MainBottomSheetTab.PLACE -> PlaceBottomSheetContent(
                selectedDateKey = selectedDateKey,
                placeListUiState = placeUiState.placeList,
                selectedPlaceId = selectedPlaceId,
                onSelectedPlaceHandled = onSelectedPlaceHandled,
                onRetryClick = onPlaceRetryClick,
                onAddPlaceClick = onAddPlaceClick,
                onReorderPlaces = onReorderPlaces,
                onCloseReorderGuideBanner = onCloseReorderGuideBanner,
                onEditPlaceClick = onEditPlaceClick,
                onPlaceClick = onPlaceClick,
                onDeletePlaceRequested = onDeletePlaceRequested,
                onScrollStateChanged = { isContentScrolled = it },
                isReorderSubmitting = placeUiState.isSubmitting,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            )
            MainBottomSheetTab.DAYNOTE -> DayNoteBottomSheetContent(
                uiState = dayNoteUiState,
                onTitleChanged = onDayNoteTitleChanged,
                onMemoChanged = onDayNoteMemoChanged,
                onSaveClick = onDayNoteSaveClick,
                onScrollStateChanged = { isContentScrolled = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            )
            MainBottomSheetTab.SUMMARY -> DaySummaryBottomSheetContent(
                selectedDateKey = selectedDateKey,
                uiState = daySummaryUiState,
                onLoadSummary = onDaySummaryLoadRequest,
                onRetryClick = onDaySummaryRetryClick,
                onScrollStateChanged = { isContentScrolled = it },
                onMetricClick = onDaySummaryMetricClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            )
        }
    }
}

private fun Modifier.clearFocusOnBackgroundTap(
    onTap: () -> Unit
): Modifier = pointerInput(onTap) {
    awaitEachGesture {
        val down = awaitFirstDown(
            requireUnconsumed = false,
            pass = PointerEventPass.Final
        )
        if (down.isConsumed) return@awaitEachGesture

        val up = waitForUpOrCancellation(pass = PointerEventPass.Final)
        if (up != null && !up.isConsumed) {
            onTap()
        }
    }
}

internal enum class MainBottomSheetTab {
    PLACE,
    DAYNOTE,
    SUMMARY
}

private fun MainBottomSheetTab.titleResId(): Int {
    return when (this) {
        MainBottomSheetTab.PLACE -> R.string.record_sheet_tab_place
        MainBottomSheetTab.DAYNOTE -> R.string.record_sheet_tab_daynote
        MainBottomSheetTab.SUMMARY -> R.string.record_sheet_tab_summary
    }
}

private fun MainBottomSheetTab.iconResId(): Int {
    return when (this) {
        MainBottomSheetTab.PLACE -> R.drawable.ic_bottom_sheet_place
        MainBottomSheetTab.DAYNOTE -> R.drawable.ic_bottom_sheet_memo
        MainBottomSheetTab.SUMMARY -> R.drawable.ic_day_summary
    }
}

private fun MainBottomSheetTab.toBaseTabItem(): BaseBottomSheetTabItem<MainBottomSheetTab> {
    return BaseBottomSheetTabItem(
        key = this,
        titleResId = titleResId(),
        iconResId = iconResId()
    )
}

@Preview(showBackground = true, heightDp = 720, name = "Main Bottom Sheet")
@Composable
private fun MainBottomSheetPreview() {
    PassedPathTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6))
        ) {
            MainBottomSheet(
                selectedDateKey = "2026-04-20",
                placeUiState = PlaceUiState(),
                dayNoteUiState = DayNoteUiState(dateKey = "2026-04-20"),
                daySummaryUiState = DaySummaryUiState(),
                selectedPlaceId = null,
                onSelectedPlaceHandled = {},
                onDayNoteTitleChanged = {},
                onDayNoteMemoChanged = {},
                onDayNoteSaveClick = {},
                onDaySummaryLoadRequest = {},
                onDaySummaryRetryClick = {},
                onDaySummaryMetricClick = {},
                selectedTab = MainBottomSheetTab.PLACE,
                onTabSelected = {},
                onPlaceRetryClick = {},
                onAddPlaceClick = {},
                onReorderPlaces = {},
                onCloseReorderGuideBanner = {},
                onEditPlaceClick = {},
                onPlaceClick = {},
                onDeletePlaceRequested = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
