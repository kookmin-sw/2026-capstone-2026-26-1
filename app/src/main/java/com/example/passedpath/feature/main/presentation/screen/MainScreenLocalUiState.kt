package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

internal data class MainScreenLocalUiState(
    val selectedBottomSheetTab: MainBottomSheetTab = MainBottomSheetTab.PLACE,
    val isPlaceCreateSheetVisible: Boolean = false,
    val bottomSheetValue: MainBottomSheetValue = MainBottomSheetValue.COLLAPSED,
    val requestedSheetValue: MainBottomSheetValue? = null,
    val selectedPlaceId: Long? = null
)

internal val MainScreenLocalUiStateSaver: Saver<MainScreenLocalUiState, Any> = listSaver(
    save = { state ->
        listOf(
            state.selectedBottomSheetTab.name,
            state.isPlaceCreateSheetVisible,
            state.bottomSheetValue.name,
            state.requestedSheetValue?.name,
            state.selectedPlaceId
        )
    },
    restore = { values ->
        MainScreenLocalUiState(
            selectedBottomSheetTab = MainBottomSheetTab.valueOf(values[0] as String),
            isPlaceCreateSheetVisible = values[1] as Boolean,
            bottomSheetValue = MainBottomSheetValue.valueOf(values[2] as String),
            requestedSheetValue = (values[3] as String?)?.let(MainBottomSheetValue::valueOf),
            selectedPlaceId = values[4] as Long?
        )
    }
)
