package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private data class SheetAnchors(
    val expanded: Float,
    val middle: Float,
    val hidden: Float
)

@Composable
internal fun MainBottomSheetScaffold(
    modifier: Modifier = Modifier,
    requestedSheetValue: MainBottomSheetValue? = null,
    onSheetValueChanged: (MainBottomSheetValue) -> Unit = {},
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
            SheetAnchors(
                expanded = expandedOffset,
                middle = middleOffset,
                hidden = hiddenOffset
            )
        }
        var sheetOffset by remember { mutableFloatStateOf(hiddenOffset) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(expandedOffset, middleOffset, hiddenOffset) {
            sheetOffset = when (nearestSheetValue(sheetOffset, sheetAnchors)) {
                MainBottomSheetValue.EXPANDED -> sheetAnchors.expanded
                MainBottomSheetValue.MIDDLE -> sheetAnchors.middle
                MainBottomSheetValue.HIDDEN -> sheetAnchors.hidden
            }
        }

        LaunchedEffect(requestedSheetValue, sheetAnchors) {
            val targetValue = requestedSheetValue ?: return@LaunchedEffect
            val targetOffset = when (targetValue) {
                MainBottomSheetValue.EXPANDED -> sheetAnchors.expanded
                MainBottomSheetValue.MIDDLE -> sheetAnchors.middle
                MainBottomSheetValue.HIDDEN -> sheetAnchors.hidden
            }
            animateSheetOffsetTo(
                currentOffset = sheetOffset,
                targetOffset = targetOffset,
                onOffsetChanged = { sheetOffset = it }
            )
        }

        val draggableState = rememberDraggableState { delta ->
            sheetOffset = (sheetOffset + delta)
                .coerceIn(sheetAnchors.expanded, sheetAnchors.hidden)
        }
        val nestedScrollConnection = remember(sheetAnchors, coroutineScope) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (source != NestedScrollSource.UserInput) return Offset.Zero
                    if (available.y >= 0f) return Offset.Zero
                    val consumedY = consumeSheetDragDelta(
                        delta = available.y,
                        currentOffset = sheetOffset,
                        minOffset = sheetAnchors.expanded,
                        maxOffset = sheetAnchors.hidden,
                        onOffsetChanged = { sheetOffset = it }
                    )
                    return Offset(x = 0f, y = consumedY)
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    if (source != NestedScrollSource.UserInput) return Offset.Zero
                    if (available.y <= 0f) return Offset.Zero
                    val consumedY = consumeSheetDragDelta(
                        delta = available.y,
                        currentOffset = sheetOffset,
                        minOffset = sheetAnchors.expanded,
                        maxOffset = sheetAnchors.hidden,
                        onOffsetChanged = { sheetOffset = it }
                    )
                    return Offset(x = 0f, y = consumedY)
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    if (available.y >= 0f || sheetOffset <= sheetAnchors.expanded) {
                        return Velocity.Zero
                    }
                    val targetOffset = settleSheetOffset(
                        currentOffset = sheetOffset,
                        currentValue = nearestSheetValue(sheetOffset, sheetAnchors),
                        anchors = sheetAnchors,
                        velocity = available.y
                    )
                    animateSheetOffsetTo(
                        currentOffset = sheetOffset,
                        targetOffset = targetOffset,
                        onOffsetChanged = { sheetOffset = it }
                    )
                    return available
                }

                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    if (available.y <= 0f || sheetOffset >= sheetAnchors.hidden) {
                        return Velocity.Zero
                    }
                    val targetOffset = settleSheetOffset(
                        currentOffset = sheetOffset,
                        currentValue = nearestSheetValue(sheetOffset, sheetAnchors),
                        anchors = sheetAnchors,
                        velocity = available.y
                    )
                    animateSheetOffsetTo(
                        currentOffset = sheetOffset,
                        targetOffset = targetOffset,
                        onOffsetChanged = { sheetOffset = it }
                    )
                    return available
                }
            }
        }

        val boundedSheetOffset = sheetOffset.coerceIn(sheetAnchors.expanded, sheetAnchors.hidden)
        val visibleSheetHeightDp = with(density) {
            (containerHeightPx - boundedSheetOffset)
                .coerceAtLeast(0f)
                .toDp()
        }
        val floatingBottomPadding = visibleSheetHeightDp + BottomSheetFloatingPadding
        val currentSheetValue = nearestSheetValue(boundedSheetOffset, sheetAnchors)

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
                        val targetOffset = settleSheetOffset(
                            currentOffset = sheetOffset,
                            currentValue = nearestSheetValue(sheetOffset, sheetAnchors),
                            anchors = sheetAnchors,
                            velocity = velocity
                        )
                        animateSheetOffsetTo(
                            currentOffset = sheetOffset,
                            targetOffset = targetOffset,
                            onOffsetChanged = { sheetOffset = it }
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

private suspend fun animateSheetOffsetTo(
    currentOffset: Float,
    targetOffset: Float,
    onOffsetChanged: (Float) -> Unit
) {
    animate(
        initialValue = currentOffset,
        targetValue = targetOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    ) { value, _ ->
        onOffsetChanged(value)
    }
}

private fun consumeSheetDragDelta(
    delta: Float,
    currentOffset: Float,
    minOffset: Float,
    maxOffset: Float,
    onOffsetChanged: (Float) -> Unit
): Float {
    val nextOffset = (currentOffset + delta).coerceIn(minOffset, maxOffset)
    val consumed = nextOffset - currentOffset
    if (consumed != 0f) {
        onOffsetChanged(nextOffset)
    }
    return consumed
}

private fun settleSheetOffset(
    currentOffset: Float,
    currentValue: MainBottomSheetValue,
    anchors: SheetAnchors,
    velocity: Float
): Float {
    val velocityThreshold = 1800f
    if (velocity <= -velocityThreshold) {
        return when (currentValue) {
            MainBottomSheetValue.HIDDEN -> anchors.middle
            MainBottomSheetValue.MIDDLE -> anchors.expanded
            MainBottomSheetValue.EXPANDED -> anchors.expanded
        }
    }
    if (velocity >= velocityThreshold) {
        return when (currentValue) {
            MainBottomSheetValue.EXPANDED -> anchors.middle
            MainBottomSheetValue.MIDDLE -> anchors.hidden
            MainBottomSheetValue.HIDDEN -> anchors.hidden
        }
    }
    return listOf(anchors.expanded, anchors.middle, anchors.hidden)
        .minBy { abs(it - currentOffset) }
}

private fun nearestSheetValue(
    offset: Float,
    anchors: SheetAnchors
): MainBottomSheetValue {
    return when (
        listOf(anchors.expanded, anchors.middle, anchors.hidden)
            .minBy { abs(it - offset) }
    ) {
        anchors.expanded -> MainBottomSheetValue.EXPANDED
        anchors.middle -> MainBottomSheetValue.MIDDLE
        else -> MainBottomSheetValue.HIDDEN
    }
}
