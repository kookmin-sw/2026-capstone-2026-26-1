package com.example.passedpath.ui.component.bottomsheet

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun BaseAnchoredBottomSheetScaffold(
    modifier: Modifier = Modifier,
    initialSheetValue: BaseBottomSheetValue = BaseBottomSheetValue.HIDDEN,
    requestedSheetValue: BaseBottomSheetValue? = null,
    hiddenVisibleHeight: Dp = BaseBottomSheetDefaults.hiddenVisibleHeight,
    middleVisibleHeight: Dp = BaseBottomSheetDefaults.middleVisibleHeight,
    expandedTopInset: Dp = BaseBottomSheetDefaults.expandedTopInset,
    floatingPadding: Dp = BaseBottomSheetDefaults.floatingPadding,
    onSheetValueChanged: (BaseBottomSheetValue) -> Unit = {},
    onSheetCommandConsumed: (BaseBottomSheetValue) -> Unit = {},
    content: @Composable () -> Unit,
    floatingOverlay: @Composable (Dp) -> Unit = {},
    sheet: @Composable (Modifier) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val containerHeightPx = constraints.maxHeight.toFloat()
        val hiddenVisibleHeightPx = with(density) { hiddenVisibleHeight.toPx() }
        val middleVisibleHeightPx = with(density) { middleVisibleHeight.toPx() }
        val expandedTopInsetPx = with(density) { expandedTopInset.toPx() }
        val hiddenOffset = (containerHeightPx - hiddenVisibleHeightPx).coerceAtLeast(0f)
        val middleOffset = (containerHeightPx - middleVisibleHeightPx)
            .coerceIn(expandedTopInsetPx, hiddenOffset)
        val expandedOffset = expandedTopInsetPx.coerceAtMost(middleOffset)
        val expandedVisibleHeightDp = with(density) { (containerHeightPx - expandedOffset).toDp() }
        val sheetAnchors = remember(expandedOffset, middleOffset, hiddenOffset) {
            BaseBottomSheetAnchors(
                expanded = expandedOffset,
                middle = middleOffset,
                hidden = hiddenOffset
            )
        }
        val motionState = rememberBaseBottomSheetMotionState(
            initialOffset = sheetAnchors.offsetFor(initialSheetValue)
        )
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
        val nestedScrollConnection = rememberBaseBottomSheetNestedScrollConnection(
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
        val floatingBottomPadding = visibleSheetHeightDp + floatingPadding
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
            content()
            floatingOverlay(floatingBottomPadding)
            sheet(sheetModifier)
        }
    }
}

private data class BaseBottomSheetAnchors(
    val expanded: Float,
    val middle: Float,
    val hidden: Float
) {
    fun offsetFor(value: BaseBottomSheetValue): Float {
        return when (value) {
            BaseBottomSheetValue.EXPANDED -> expanded
            BaseBottomSheetValue.MIDDLE -> middle
            BaseBottomSheetValue.HIDDEN -> hidden
        }
    }
}

@Stable
private class BaseBottomSheetMotionState(
    initialOffset: Float
) {
    var sheetOffset by mutableFloatStateOf(initialOffset)
        private set

    fun syncToNearestAnchor(anchors: BaseBottomSheetAnchors) {
        sheetOffset = anchors.offsetFor(currentValue(anchors))
    }

    fun consumeDragDelta(
        delta: Float,
        anchors: BaseBottomSheetAnchors
    ): Float {
        val nextOffset = (sheetOffset + delta).coerceIn(anchors.expanded, anchors.hidden)
        val consumed = nextOffset - sheetOffset
        if (consumed != 0f) {
            sheetOffset = nextOffset
        }
        return consumed
    }

    fun currentValue(anchors: BaseBottomSheetAnchors): BaseBottomSheetValue {
        return nearestSheetValue(sheetOffset, anchors)
    }

    suspend fun animateTo(
        targetValue: BaseBottomSheetValue,
        anchors: BaseBottomSheetAnchors
    ) {
        animateToOffset(anchors.offsetFor(targetValue))
    }

    suspend fun settle(
        anchors: BaseBottomSheetAnchors,
        velocity: Float
    ) {
        val targetOffset = settleSheetOffset(
            currentOffset = sheetOffset,
            currentValue = currentValue(anchors),
            anchors = anchors,
            velocity = velocity
        )
        animateToOffset(targetOffset)
    }

    private suspend fun animateToOffset(targetOffset: Float) {
        animate(
            initialValue = sheetOffset,
            targetValue = targetOffset,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) { value, _ ->
            sheetOffset = value
        }
    }
}

@Composable
private fun rememberBaseBottomSheetMotionState(
    initialOffset: Float
): BaseBottomSheetMotionState {
    return remember {
        BaseBottomSheetMotionState(initialOffset = initialOffset)
    }
}

@Composable
private fun rememberBaseBottomSheetNestedScrollConnection(
    motionState: BaseBottomSheetMotionState,
    anchors: BaseBottomSheetAnchors
): NestedScrollConnection {
    return remember(motionState, anchors) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                if (available.y >= 0f) return Offset.Zero
                return Offset(
                    x = 0f,
                    y = motionState.consumeDragDelta(
                        delta = available.y,
                        anchors = anchors
                    )
                )
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                if (available.y <= 0f) return Offset.Zero
                return Offset(
                    x = 0f,
                    y = motionState.consumeDragDelta(
                        delta = available.y,
                        anchors = anchors
                    )
                )
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (available.y >= 0f || motionState.sheetOffset <= anchors.expanded) {
                    return Velocity.Zero
                }
                motionState.settle(
                    anchors = anchors,
                    velocity = available.y
                )
                return available
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (available.y <= 0f || motionState.sheetOffset >= anchors.hidden) {
                    return Velocity.Zero
                }
                motionState.settle(
                    anchors = anchors,
                    velocity = available.y
                )
                return available
            }
        }
    }
}

private fun settleSheetOffset(
    currentOffset: Float,
    currentValue: BaseBottomSheetValue,
    anchors: BaseBottomSheetAnchors,
    velocity: Float
): Float {
    val velocityThreshold = 1800f
    if (velocity <= -velocityThreshold) {
        return when (currentValue) {
            BaseBottomSheetValue.HIDDEN -> anchors.middle
            BaseBottomSheetValue.MIDDLE -> anchors.expanded
            BaseBottomSheetValue.EXPANDED -> anchors.expanded
        }
    }
    if (velocity >= velocityThreshold) {
        return when (currentValue) {
            BaseBottomSheetValue.EXPANDED -> anchors.middle
            BaseBottomSheetValue.MIDDLE -> anchors.hidden
            BaseBottomSheetValue.HIDDEN -> anchors.hidden
        }
    }
    return listOf(anchors.expanded, anchors.middle, anchors.hidden)
        .minBy { abs(it - currentOffset) }
}

private fun nearestSheetValue(
    offset: Float,
    anchors: BaseBottomSheetAnchors
): BaseBottomSheetValue {
    return when (
        listOf(anchors.expanded, anchors.middle, anchors.hidden)
            .minBy { abs(it - offset) }
    ) {
        anchors.expanded -> BaseBottomSheetValue.EXPANDED
        anchors.middle -> BaseBottomSheetValue.MIDDLE
        else -> BaseBottomSheetValue.HIDDEN
    }
}
