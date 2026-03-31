package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.daynote.presentation.screen.DayNoteBottomSheetContent
import com.example.passedpath.feature.place.presentation.screen.PlaceBottomSheetContent
import kotlin.math.abs

@Composable
internal fun MainBottomSheet(
    selectedTab: MainBottomSheetTab,
    onTabSelected: (MainBottomSheetTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = androidx.compose.ui.graphics.Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 14.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 22.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            Spacer(modifier = Modifier.height(12.dp))
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                MainBottomSheetTab.entries.forEach { tab ->
                    Tab(
                        selected = tab == selectedTab,
                        onClick = { onTabSelected(tab) },
                        text = {
                            Text(
                                text = stringResource(tab.titleResId),
                                fontWeight = if (tab == selectedTab) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            when (selectedTab) {
                MainBottomSheetTab.PLACE -> PlaceBottomSheetContent(modifier = Modifier.padding(horizontal = 20.dp))
                MainBottomSheetTab.DAYNOTE -> DayNoteBottomSheetContent(modifier = Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}

internal fun settleSheetOffset(
    currentOffset: Float,
    currentValue: MainBottomSheetValue,
    anchors: List<Float>,
    velocity: Float
): Float {
    val velocityThreshold = 1800f
    if (velocity <= -velocityThreshold) {
        return when (currentValue) {
            MainBottomSheetValue.COLLAPSED -> anchors[1]
            MainBottomSheetValue.MIDDLE -> anchors[0]
            MainBottomSheetValue.EXPANDED -> anchors[0]
        }
    }
    if (velocity >= velocityThreshold) {
        return when (currentValue) {
            MainBottomSheetValue.EXPANDED -> anchors[1]
            MainBottomSheetValue.MIDDLE -> anchors[2]
            MainBottomSheetValue.COLLAPSED -> anchors[2]
        }
    }
    return anchors.minBy { abs(it - currentOffset) }
}

internal fun nearestSheetValue(offset: Float, anchors: List<Float>): MainBottomSheetValue {
    return when (anchors.minBy { abs(it - offset) }) {
        anchors[0] -> MainBottomSheetValue.EXPANDED
        anchors[1] -> MainBottomSheetValue.MIDDLE
        else -> MainBottomSheetValue.COLLAPSED
    }
}

internal enum class MainBottomSheetTab(val titleResId: Int) {
    PLACE(R.string.record_sheet_tab_place),
    DAYNOTE(R.string.record_sheet_tab_daynote)
}

internal enum class MainBottomSheetValue {
    COLLAPSED,
    MIDDLE,
    EXPANDED
}
