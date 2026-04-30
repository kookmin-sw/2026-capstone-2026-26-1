package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.feature.daynote.presentation.screen.DayNoteBottomSheetContent
import com.example.passedpath.feature.daynote.presentation.state.DayNoteUiState
import com.example.passedpath.feature.place.presentation.screen.PlaceBottomSheetContent
import com.example.passedpath.feature.place.presentation.state.PlaceUiState
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray200
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray700
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green50
import com.example.passedpath.ui.theme.Green100
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
internal fun MainBottomSheet(
    selectedDateKey: String,
    placeUiState: PlaceUiState,
    dayNoteUiState: DayNoteUiState,
    selectedPlaceId: Long?,
    onSelectedPlaceHandled: () -> Unit,
    onDayNoteTitleChanged: (String) -> Unit,
    onDayNoteMemoChanged: (String) -> Unit,
    onDayNoteSaveClick: () -> Unit,
    selectedTab: MainBottomSheetTab,
    onTabSelected: (MainBottomSheetTab) -> Unit,
    onPlaceRetryClick: () -> Unit,
    onAddPlaceClick: () -> Unit,
    onReorderPlaces: (List<Long>) -> Unit,
    onCloseReorderGuideBanner: () -> Unit,
    onEditPlaceClick: (Long) -> Unit,
    onDeletePlaceRequested: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isContentScrolled by remember(selectedTab) { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        isContentScrolled = false
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 14.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp)
        ) {
            BottomSheetHandle(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(10.dp))
            BottomSheetTabRow(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
            Spacer(modifier = Modifier.height(12.dp))
            BottomSheetContentDivider(visible = isContentScrolled)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds()
                    .clearFocusOnBackgroundTap {
                        focusManager.clearFocus(force = true)
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
                }
            }
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

@Composable
private fun BottomSheetHandle(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 36.dp, height = 3.dp)
            .clip(CircleShape)
            .background(Gray200)
    )
}

@Composable
private fun BottomSheetTabRow(
    selectedTab: MainBottomSheetTab,
    onTabSelected: (MainBottomSheetTab) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Gray100),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MainBottomSheetTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onTabSelected(tab) },
                shape = RoundedCornerShape(18.dp),
                color = if (selected) Color.White else Color.Transparent,
                shadowElevation = if (selected) 5.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = tab.iconResId()),
                        contentDescription = null,
                        tint = if (selected) Green500 else Gray400,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(tab.titleResId()),
                        color = if (selected) Gray900 else Gray400,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 17.sp,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomSheetContentDivider(visible: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(if (visible) Gray200 else Color.Transparent)
    )
}

internal enum class MainBottomSheetTab {
    PLACE,
    DAYNOTE
}

private fun MainBottomSheetTab.titleResId(): Int {
    return when (this) {
        MainBottomSheetTab.PLACE -> R.string.record_sheet_tab_place
        MainBottomSheetTab.DAYNOTE -> R.string.record_sheet_tab_daynote
    }
}

private fun MainBottomSheetTab.iconResId(): Int {
    return when (this) {
        MainBottomSheetTab.PLACE -> R.drawable.ic_bottom_sheet_place
        MainBottomSheetTab.DAYNOTE -> R.drawable.ic_bottom_sheet_memo
    }
}

internal enum class MainBottomSheetValue {
    HIDDEN,
    MIDDLE,
    EXPANDED
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
                selectedPlaceId = null,
                onSelectedPlaceHandled = {},
                onDayNoteTitleChanged = {},
                onDayNoteMemoChanged = {},
                onDayNoteSaveClick = {},
                selectedTab = MainBottomSheetTab.PLACE,
                onTabSelected = {},
                onPlaceRetryClick = {},
                onAddPlaceClick = {},
                onReorderPlaces = {},
                onCloseReorderGuideBanner = {},
                onEditPlaceClick = {},
                onDeletePlaceRequested = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
