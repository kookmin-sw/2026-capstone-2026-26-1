package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

internal data class MainScreenLocalUiState(
    val selectedBottomSheetTab: MainBottomSheetTab = MainBottomSheetTab.PLACE,
    val bottomSheetValue: MainBottomSheetValue = MainBottomSheetValue.HIDDEN,
    val requestedSheetValue: MainBottomSheetValue? = null,
    val selectedPlaceId: Long? = null
)

internal val MainScreenLocalUiStateSaver: Saver<MainScreenLocalUiState, Any> = listSaver(
    save = { state ->
        listOf(
            state.selectedBottomSheetTab.name,
            state.bottomSheetValue.name,
            state.requestedSheetValue?.name,
            state.selectedPlaceId
        )
    },
    restore = { values ->
        MainScreenLocalUiState(
            selectedBottomSheetTab = MainBottomSheetTab.valueOf(values[0] as String),
            bottomSheetValue = MainBottomSheetValue.valueOf(values[1] as String),
            requestedSheetValue = (values[2] as String?)?.let(MainBottomSheetValue::valueOf),
            selectedPlaceId = values[3] as Long?
        )
    }
)
