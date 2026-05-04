package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

internal data class BottomSheetAnchors(
    val expanded: Float,
    val middle: Float,
    val hidden: Float
) {
    fun offsetFor(value: MainBottomSheetValue): Float {
        return when (value) {
            MainBottomSheetValue.EXPANDED -> expanded
            MainBottomSheetValue.MIDDLE -> middle
            MainBottomSheetValue.HIDDEN -> hidden
        }
    }
}

@Stable
internal class MainBottomSheetMotionState(
    initialOffset: Float
) {
    var sheetOffset by mutableFloatStateOf(initialOffset)
        private set

    fun syncToNearestAnchor(anchors: BottomSheetAnchors) {
        sheetOffset = anchors.offsetFor(currentValue(anchors))
    }

    fun consumeDragDelta(
        delta: Float,
        anchors: BottomSheetAnchors
    ): Float {
        val nextOffset = (sheetOffset + delta).coerceIn(anchors.expanded, anchors.hidden)
        val consumed = nextOffset - sheetOffset
        if (consumed != 0f) {
            sheetOffset = nextOffset
        }
        return consumed
    }

    fun currentValue(anchors: BottomSheetAnchors): MainBottomSheetValue {
        return nearestSheetValue(sheetOffset, anchors)
    }

    suspend fun animateTo(
        targetValue: MainBottomSheetValue,
        anchors: BottomSheetAnchors
    ) {
        animateToOffset(anchors.offsetFor(targetValue))
    }

    suspend fun settle(
        anchors: BottomSheetAnchors,
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
internal fun rememberMainBottomSheetMotionState(
    initialOffset: Float
): MainBottomSheetMotionState {
    return remember {
        MainBottomSheetMotionState(initialOffset = initialOffset)
    }
}

@Composable
internal fun rememberMainBottomSheetNestedScrollConnection(
    motionState: MainBottomSheetMotionState,
    anchors: BottomSheetAnchors
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
    currentValue: MainBottomSheetValue,
    anchors: BottomSheetAnchors,
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
    anchors: BottomSheetAnchors
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
