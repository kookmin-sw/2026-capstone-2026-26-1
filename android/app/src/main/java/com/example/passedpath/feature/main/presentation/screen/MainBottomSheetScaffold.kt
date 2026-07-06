package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.example.passedpath.ui.component.bottomsheet.BaseAnchoredBottomSheetScaffold
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetDefaults

@Composable
internal fun MainBottomSheetScaffold(
    modifier: Modifier = Modifier,
    initialSheetValue: MainBottomSheetValue = MainBottomSheetValue.HIDDEN,
    requestedSheetValue: MainBottomSheetValue? = null,
    onSheetValueChanged: (MainBottomSheetValue) -> Unit = {},
    onSheetCommandConsumed: (MainBottomSheetValue) -> Unit = {},
    content: @Composable () -> Unit,
    floatingOverlay: @Composable (Dp) -> Unit = {},
    sheet: @Composable (Modifier) -> Unit
) {
    BaseAnchoredBottomSheetScaffold(
        modifier = modifier,
        initialSheetValue = initialSheetValue,
        requestedSheetValue = requestedSheetValue,
        hiddenVisibleHeight = BaseBottomSheetDefaults.hiddenVisibleHeight,
        middleVisibleHeight = BaseBottomSheetDefaults.middleVisibleHeight,
        expandedTopInset = BaseBottomSheetDefaults.expandedTopInset,
        floatingPadding = BaseBottomSheetDefaults.floatingPadding,
        onSheetValueChanged = onSheetValueChanged,
        onSheetCommandConsumed = onSheetCommandConsumed,
        content = content,
        floatingOverlay = floatingOverlay,
        sheet = sheet
    )
}
