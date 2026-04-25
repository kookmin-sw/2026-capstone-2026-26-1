package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
internal fun MainBottomSheetScaffold(
    modifier: Modifier = Modifier,
    requestedSheetValue: MainBottomSheetValue? = null,
    onSheetValueChanged: (MainBottomSheetValue) -> Unit = {},
    onSheetCommandConsumed: (MainBottomSheetValue) -> Unit = {},
    content: @Composable (Dp) -> Unit,
    sheet: @Composable (Modifier) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val containerHeightPx = constraints.maxHeight.toFloat()
        val hiddenVisibleHeightPx = with(density) { BottomSheetHiddenVisibleHeight.toPx() }
        val middleVisibleHeightPx = with(density) { BottomSheetMiddleVisibleHeight.toPx() }
        val expandedTopInsetPx = with(density) { BottomSheetExpandedTopInset.toPx() }
        val hiddenOffset = (containerHeightPx - hiddenVisibleHeightPx).coerceAtLeast(0f)
        val middleOffset = (containerHeightPx - middleVisibleHeightPx)
            .coerceIn(expandedTopInsetPx, hiddenOffset)
        val expandedOffset = expandedTopInsetPx.coerceAtMost(middleOffset)
        val expandedVisibleHeightDp = with(density) { (containerHeightPx - expandedOffset).toDp() }
        val sheetAnchors = remember(expandedOffset, middleOffset, hiddenOffset) {
            BottomSheetAnchors(
                expanded = expandedOffset,
                middle = middleOffset,
                hidden = hiddenOffset
            )
        }
        val motionState = rememberMainBottomSheetMotionState(initialOffset = hiddenOffset)
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(expandedOffset, middleOffset, hiddenOffset) {
            motionState.syncToNearestAnchor(sheetAnchors)
        }

        LaunchedEffect(requestedSheetValue, sheetAnchors) {
            val targetValue = requestedSheetValue ?: return@LaunchedEffect
            motionState.animateTo(targetValue, sheetAnchors)
            onSheetCommandConsumed(targetValue)
        }

        val draggableState = rememberDraggableState { delta ->
            motionState.consumeDragDelta(
                delta = delta,
                anchors = sheetAnchors
            )
        }
        val nestedScrollConnection = rememberMainBottomSheetNestedScrollConnection(
            motionState = motionState,
            anchors = sheetAnchors
        )
        val boundedSheetOffset = motionState.sheetOffset
            .coerceIn(sheetAnchors.expanded, sheetAnchors.hidden)
        val visibleSheetHeightDp = with(density) {
            (containerHeightPx - boundedSheetOffset)
                .coerceAtLeast(0f)
                .toDp()
        }
        val floatingBottomPadding = visibleSheetHeightDp + BottomSheetFloatingPadding
        val currentSheetValue = motionState.currentValue(sheetAnchors)

        LaunchedEffect(currentSheetValue) {
            onSheetValueChanged(currentSheetValue)
        }

        val sheetModifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .height(expandedVisibleHeightDp)
            .offset { IntOffset(0, boundedSheetOffset.roundToInt()) }
            .nestedScroll(nestedScrollConnection)
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = { velocity ->
                    coroutineScope.launch {
                        motionState.settle(
                            anchors = sheetAnchors,
                            velocity = velocity
                        )
                    }
                }
            )

        Box(modifier = Modifier.fillMaxSize()) {
            content(floatingBottomPadding)
            sheet(sheetModifier)
        }
    }
}
