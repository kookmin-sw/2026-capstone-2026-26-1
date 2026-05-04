package com.example.passedpath.feature.main.presentation.policy

import com.example.passedpath.feature.main.presentation.screen.MainBottomSheetTab
import com.example.passedpath.feature.main.presentation.screen.MainBottomSheetValue

internal data class BottomSheetTabSelectionDecision(
    val requestedSheetValue: MainBottomSheetValue?,
    val selectedPlaceId: Long?,
    val shouldRefreshPlaces: Boolean
)

internal fun resolveBottomSheetTabSelection(
    currentSheetValue: MainBottomSheetValue,
    currentTab: MainBottomSheetTab,
    selectedTab: MainBottomSheetTab,
    selectedPlaceId: Long?
): BottomSheetTabSelectionDecision {
    val requestedSheetValue = when (currentSheetValue) {
        MainBottomSheetValue.HIDDEN -> MainBottomSheetValue.MIDDLE
        MainBottomSheetValue.MIDDLE,
        MainBottomSheetValue.EXPANDED -> null
    }

    return BottomSheetTabSelectionDecision(
        requestedSheetValue = requestedSheetValue,
        selectedPlaceId = if (selectedTab == MainBottomSheetTab.DAYNOTE) null else selectedPlaceId,
        shouldRefreshPlaces = selectedTab == MainBottomSheetTab.PLACE
    )
}
