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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val BottomSheetCollapsedVisibleHeight = 92.dp
private val BottomSheetMiddleVisibleHeight = 332.dp
private val BottomSheetExpandedTopInset = 92.dp

private data class SheetAnchors(
    val expanded: Float,
    val middle: Float,
    val collapsed: Float
)

@Composable
internal fun MainBottomSheetScaffold(
    modifier: Modifier = Modifier,
    content: @Composable (Dp) -> Unit,
    sheet: @Composable (Modifier) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val containerHeightPx = constraints.maxHeight.toFloat()
        val collapsedVisibleHeightPx = with(density) { BottomSheetCollapsedVisibleHeight.toPx() }
        val middleVisibleHeightPx = with(density) { BottomSheetMiddleVisibleHeight.toPx() }
        val expandedTopInsetPx = with(density) { BottomSheetExpandedTopInset.toPx() }
        val collapsedOffset = (containerHeightPx - collapsedVisibleHeightPx).coerceAtLeast(0f)
        val middleOffset = (containerHeightPx - middleVisibleHeightPx)
            .coerceIn(expandedTopInsetPx, collapsedOffset)
        val expandedOffset = expandedTopInsetPx.coerceAtMost(middleOffset)
        val sheetAnchors = remember(expandedOffset, middleOffset, collapsedOffset) {
            SheetAnchors(
                expanded = expandedOffset,
                middle = middleOffset,
                collapsed = collapsedOffset
            )
        }
        var sheetOffset by remember { mutableFloatStateOf(collapsedOffset) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(expandedOffset, middleOffset, collapsedOffset) {
            sheetOffset = when (nearestSheetValue(sheetOffset, sheetAnchors)) {
                MainBottomSheetValue.EXPANDED -> sheetAnchors.expanded
                MainBottomSheetValue.MIDDLE -> sheetAnchors.middle
                MainBottomSheetValue.COLLAPSED -> sheetAnchors.collapsed
            }
        }

        val draggableState = rememberDraggableState { delta ->
            sheetOffset = (sheetOffset + delta)
                .coerceIn(sheetAnchors.expanded, sheetAnchors.collapsed)
        }

        val visibleSheetHeightDp = with(density) { (containerHeightPx - sheetOffset).toDp() }
        val floatingBottomPadding = visibleSheetHeightDp + 16.dp
        val sheetModifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .height(visibleSheetHeightDp)
            .offset { IntOffset(0, sheetOffset.roundToInt()) }
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
            )

        Box(modifier = Modifier.fillMaxSize()) {
            content(floatingBottomPadding)
            sheet(sheetModifier)
        }
    }
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
            MainBottomSheetValue.COLLAPSED -> anchors.middle
            MainBottomSheetValue.MIDDLE -> anchors.expanded
            MainBottomSheetValue.EXPANDED -> anchors.expanded
        }
    }
    if (velocity >= velocityThreshold) {
        return when (currentValue) {
            MainBottomSheetValue.EXPANDED -> anchors.middle
            MainBottomSheetValue.MIDDLE -> anchors.collapsed
            MainBottomSheetValue.COLLAPSED -> anchors.collapsed
        }
    }
    return listOf(anchors.expanded, anchors.middle, anchors.collapsed)
        .minBy { abs(it - currentOffset) }
}

private fun nearestSheetValue(
    offset: Float,
    anchors: SheetAnchors
): MainBottomSheetValue {
    return when (
        listOf(anchors.expanded, anchors.middle, anchors.collapsed)
            .minBy { abs(it - offset) }
    ) {
        anchors.expanded -> MainBottomSheetValue.EXPANDED
        anchors.middle -> MainBottomSheetValue.MIDDLE
        else -> MainBottomSheetValue.COLLAPSED
    }
}
