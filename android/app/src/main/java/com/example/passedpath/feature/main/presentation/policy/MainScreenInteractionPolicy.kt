package com.example.passedpath.feature.main.presentation.policy

import com.example.passedpath.feature.main.presentation.screen.MainBottomSheetTab
import com.example.passedpath.feature.main.presentation.screen.MainBottomSheetValue
import com.example.passedpath.feature.main.presentation.screen.MainScreenLocalUiState

internal data class MainScreenInteractionResult(
    val state: MainScreenLocalUiState,
    val shouldRefreshPlaces: Boolean = false
)

internal fun reduceForDateChange(state: MainScreenLocalUiState): MainScreenInteractionResult {
    return MainScreenInteractionResult(
        state = state.copy(
            selectedPlaceId = null,
            requestedSheetValue = null
        )
    )
}

internal fun reduceForSheetValueChange(
    state: MainScreenLocalUiState,
    bottomSheetValue: MainBottomSheetValue
): MainScreenInteractionResult {
    return MainScreenInteractionResult(
        state = if (bottomSheetValue == MainBottomSheetValue.HIDDEN) {
            state.copy(
                bottomSheetValue = bottomSheetValue,
                selectedPlaceId = null,
                requestedSheetValue = null
            )
        } else {
            state.copy(bottomSheetValue = bottomSheetValue)
        }
    )
}

internal fun shouldShowCurrentLocationButton(
    bottomSheetValue: MainBottomSheetValue
): Boolean {
    return bottomSheetValue == MainBottomSheetValue.HIDDEN
}

internal fun reduceForPlaceMarkerClick(
    state: MainScreenLocalUiState,
    placeId: Long
): MainScreenInteractionResult {
    return MainScreenInteractionResult(
        state = state.copy(
            selectedPlaceId = placeId,
            selectedBottomSheetTab = MainBottomSheetTab.PLACE,
            requestedSheetValue = MainBottomSheetValue.MIDDLE
        ),
        shouldRefreshPlaces = state.selectedBottomSheetTab != MainBottomSheetTab.PLACE
    )
}

internal fun reduceForSheetHideRequest(
    state: MainScreenLocalUiState
): MainScreenInteractionResult {
    return MainScreenInteractionResult(
        state = state.copy(
            requestedSheetValue = MainBottomSheetValue.HIDDEN,
            selectedPlaceId = null
        )
    )
}

internal fun reduceForBottomSheetTabSelection(
    state: MainScreenLocalUiState,
    selectedTab: MainBottomSheetTab
): MainScreenInteractionResult {
    val decision = resolveBottomSheetTabSelection(
        currentSheetValue = state.bottomSheetValue,
        currentTab = state.selectedBottomSheetTab,
        selectedTab = selectedTab,
        selectedPlaceId = state.selectedPlaceId
    )

    return MainScreenInteractionResult(
        state = state.copy(
            selectedBottomSheetTab = selectedTab,
            requestedSheetValue = decision.requestedSheetValue,
            selectedPlaceId = decision.selectedPlaceId
        ),
        shouldRefreshPlaces = decision.shouldRefreshPlaces
    )
}

internal fun reduceForSelectedPlaceHandled(
    state: MainScreenLocalUiState
): MainScreenInteractionResult {
    return MainScreenInteractionResult(
        state = state.copy(selectedPlaceId = null)
    )
}

internal fun reduceForSheetCommandConsumed(
    state: MainScreenLocalUiState,
    consumedValue: MainBottomSheetValue
): MainScreenInteractionResult {
    return MainScreenInteractionResult(
        state = if (state.requestedSheetValue == consumedValue) {
            state.copy(requestedSheetValue = null)
        } else {
            state
        }
    )
}
