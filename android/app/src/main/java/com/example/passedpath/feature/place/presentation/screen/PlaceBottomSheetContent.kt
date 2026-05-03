package com.example.passedpath.feature.place.presentation.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.place.domain.model.PlaceSourceType
import com.example.passedpath.feature.place.domain.model.VisitedPlace
import com.example.passedpath.feature.place.presentation.component.PlaceCard
import com.example.passedpath.feature.place.presentation.state.PlaceListUiState
import com.example.passedpath.ui.component.button.BaseButton
import com.example.passedpath.ui.component.button.BaseButtonVariant
import com.example.passedpath.ui.component.menu.MenuActionItem
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray700
import com.example.passedpath.ui.theme.Green100
import com.example.passedpath.ui.theme.Green300
import com.example.passedpath.ui.theme.Green50
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.Green600
import com.example.passedpath.ui.theme.PassedPathTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PlaceBottomSheetContent(
    selectedDateKey: String,
    placeListUiState: PlaceListUiState,
    selectedPlaceId: Long?,
    onSelectedPlaceHandled: () -> Unit,
    onRetryClick: () -> Unit,
    onAddPlaceClick: () -> Unit,
    onReorderPlaces: (List<Long>) -> Unit,
    onCloseReorderGuideBanner: () -> Unit,
    modifier: Modifier = Modifier,
    isReorderSubmitting: Boolean = false,
    onEditPlaceClick: (Long) -> Unit = {},
    onPlaceClick: (Long) -> Unit = {},
    onDeletePlaceRequested: (Long) -> Unit = {},
    onScrollStateChanged: (Boolean) -> Unit = {}
) {
    val isBannerVisible = placeListUiState.isReorderGuideBannerVisible
    var animatedPlaceId by remember { mutableStateOf<Long?>(null) }
    val sortedPlaces = placeListUiState.places.sortedBy(VisitedPlace::orderIndex)
    var reorderedPlaces by remember { mutableStateOf(sortedPlaces) }
    val currentReorderedPlaces by rememberUpdatedState(reorderedPlaces)
    var draggedPlaceId by remember { mutableStateOf<Long?>(null) }
    var openedMenuPlaceId by remember { mutableStateOf<Long?>(null) }
    var wasReorderSubmitting by remember { mutableStateOf(false) }
    val canReorder = sortedPlaces.size > 1 &&
        !isReorderSubmitting &&
        !placeListUiState.isLoading &&
        !(placeListUiState.errorMessage != null && !placeListUiState.isStale)
    val listState = rememberLazyListState()
    val isContentScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 0
        }
    }
    val footerTopSpacing = if (reorderedPlaces.isEmpty()) 0.dp else PlaceTimelineItemSpacing
    val placeSectionStartIndex = (if (isBannerVisible) 1 else 0) + 1

    fun closeOpenedMenu() {
        openedMenuPlaceId = null
    }

    LaunchedEffect(isContentScrolled) {
        onScrollStateChanged(isContentScrolled)
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            closeOpenedMenu()
        }
    }

    LaunchedEffect(sortedPlaces) {
        if (draggedPlaceId == null) {
            reorderedPlaces = sortedPlaces
        }
        if (openedMenuPlaceId != null && sortedPlaces.none { it.placeId == openedMenuPlaceId }) {
            closeOpenedMenu()
        }
    }

    LaunchedEffect(selectedDateKey) {
        closeOpenedMenu()
    }

    LaunchedEffect(isReorderSubmitting, placeListUiState.errorMessage, sortedPlaces) {
        if (wasReorderSubmitting && !isReorderSubmitting && placeListUiState.errorMessage != null) {
            reorderedPlaces = sortedPlaces
            draggedPlaceId = null
        }
        wasReorderSubmitting = isReorderSubmitting
    }

    LaunchedEffect(selectedPlaceId, reorderedPlaces) {
        val placeId = selectedPlaceId ?: return@LaunchedEffect
        val selectedIndex = reorderedPlaces.indexOfFirst { it.placeId == placeId }
        if (selectedIndex < 0) {
            return@LaunchedEffect
        }
        closeOpenedMenu()
        listState.animateScrollToItem(placeSectionStartIndex + selectedIndex)
        animatedPlaceId = placeId
        delay(1_700)
        animatedPlaceId = null
        onSelectedPlaceHandled()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .placeReorderGesture(
                enabled = canReorder,
                placesProvider = { currentReorderedPlaces },
                listState = listState,
                onDragStartPlace = { placeId ->
                    draggedPlaceId = placeId
                    closeOpenedMenu()
                },
                onMovePlace = { fromIndex, toIndex ->
                    reorderedPlaces = reorderedPlaces.moved(fromIndex, toIndex)
                },
                onDragFinished = {
                    val nextPlaceIds = reorderedPlaces.map(VisitedPlace::placeId)
                    if (nextPlaceIds != sortedPlaces.map(VisitedPlace::placeId)) {
                        onReorderPlaces(nextPlaceIds)
                    }
                    draggedPlaceId = null
                },
                onDragCancelled = {
                    reorderedPlaces = sortedPlaces
                    draggedPlaceId = null
                }
            )
            .graphicsLayer {
                clip = true
                shape = RectangleShape
            },
        state = listState,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        if (isBannerVisible) {
            item(key = "banner") {
                Box(modifier = Modifier.padding(bottom = PlaceTimelineItemSpacing)) {
                    PlaceGuideBanner(onClose = onCloseReorderGuideBanner)
                }
            }
        }

        item(key = "summary") {
            Box(modifier = Modifier.padding(bottom = PlaceTimelineItemSpacing)) {
                PlaceSummarySection(placeCount = placeListUiState.placeCount)
            }
        }

        if (placeListUiState.isStale && placeListUiState.errorMessage != null) {
            item(key = "stale_notice") {
                Box(modifier = Modifier.padding(bottom = PlaceTimelineItemSpacing)) {
                    StalePlaceSection(
                        message = placeListUiState.errorMessage,
                        onRetryClick = onRetryClick
                    )
                }
            }
        }

        when {
            placeListUiState.isLoading && !placeListUiState.hasRetainedContent -> {
                item(key = "loading") {
                    Box(modifier = Modifier.padding(bottom = PlaceTimelineItemSpacing)) {
                        PlaceLoadingSkeletonList()
                    }
                }
            }

            placeListUiState.errorMessage != null && !placeListUiState.isStale -> {
                item(key = "error") {
                    Box(modifier = Modifier.padding(bottom = PlaceTimelineItemSpacing)) {
                        ErrorPlaceNotice(
                            message = placeListUiState.errorMessage,
                            onRetryClick = onRetryClick
                        )
                    }
                }
            }

            reorderedPlaces.isEmpty() -> Unit

            else -> {
                itemsIndexed(
                    items = reorderedPlaces,
                    key = { _, place -> place.placeId }
                ) { index, place ->
                    PlaceTimelineItem(
                        place = place,
                        shouldAnimate = place.placeId == animatedPlaceId,
                        isFirst = index == 0,
                        isLast = index == reorderedPlaces.lastIndex,
                        displayOrderIndex = index + 1,
                        isMenuVisible = openedMenuPlaceId == place.placeId,
                        onMoreClick = {
                            openedMenuPlaceId = if (openedMenuPlaceId == place.placeId) {
                                null
                            } else {
                                place.placeId
                            }
                        },
                        onDismissMenu = ::closeOpenedMenu,
                        onEditPlaceClick = {
                            closeOpenedMenu()
                            onEditPlaceClick(place.placeId)
                        },
                        onPlaceClick = {
                            closeOpenedMenu()
                            onPlaceClick(place.placeId)
                        },
                        onDeletePlaceClick = {
                            closeOpenedMenu()
                            onDeletePlaceRequested(place.placeId)
                        }
                    )
                }
            }
        }

        item(key = "footer") {
            Box(modifier = Modifier.padding(top = footerTopSpacing)) {
                PlaceAddButton(onClick = onAddPlaceClick)
            }
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PlaceSummarySection(placeCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.place_sheet_visit_count_prefix))
                withStyle(SpanStyle(color = Green300)) {
                    append(stringResource(R.string.place_sheet_visit_count_suffix, placeCount))
                }
            },
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = Gray500
        )
    }
}

@Composable
private fun ErrorPlaceNotice(
    message: String,
    onRetryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Gray100, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.place_sheet_error_title),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray400
            )
            TextButton(onClick = onRetryClick) {
                Text(text = stringResource(R.string.route_retry), color = Green500)
            }
        }
    }
}

@Composable
private fun StalePlaceSection(
    message: String,
    onRetryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Green50, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.place_sheet_stale_title),
                style = MaterialTheme.typography.bodyMedium,
                color = Gray700,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Gray400
            )
            TextButton(onClick = onRetryClick) {
                Text(text = stringResource(R.string.route_retry), color = Green500)
            }
        }
    }
}

@Composable
private fun PlaceLoadingSkeletonList() {
    val shimmerBrush = rememberPlaceSkeletonBrush()

    Column(verticalArrangement = Arrangement.spacedBy(PlaceTimelineItemSpacing)) {
        repeat(2) { index ->
            PlaceTimelineSkeletonItem(
                shimmerBrush = shimmerBrush,
                isFirst = index == 0,
                isLast = index == 1
            )
        }
    }
}

@Composable
private fun PlaceTimelineSkeletonItem(
    shimmerBrush: Brush,
    isFirst: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        SkeletonTimelineDecoration(
            shimmerBrush = shimmerBrush,
            isFirst = isFirst,
            isLast = isLast
        )
        SkeletonPlaceCard(
            shimmerBrush = shimmerBrush,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SkeletonPlaceCard(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(PlaceTimelineCardHeight)
            .background(
                brush = shimmerBrush,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(start = 16.dp, top = 15.dp, end = 16.dp, bottom = 15.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SkeletonBlock(
            shimmerBrush = shimmerBrush,
            modifier = Modifier
                .fillMaxWidth(0.46f)
                .height(14.dp)
        )
        SkeletonBlock(
            shimmerBrush = shimmerBrush,
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(12.dp)
        )
        SkeletonBlock(
            shimmerBrush = shimmerBrush,
            modifier = Modifier
                .width(96.dp)
                .height(10.dp)
        )
    }
}

@Composable
private fun SkeletonTimelineDecoration(
    shimmerBrush: Brush,
    isFirst: Boolean,
    isLast: Boolean
) {
    val decorationHeight = PlaceTimelineCardHeight + if (isLast) 0.dp else PlaceTimelineItemSpacing

    Box(
        modifier = Modifier
            .size(width = 22.dp, height = decorationHeight),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerX = size.width / 2f
            val pointCenterY = PlaceTimelinePointCenterY.toPx()
            val pointRadius = 8.dp.toPx()
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 14f), 0f)
            val strokeWidth = 1.dp.toPx()
            val gap = 4.dp.toPx()

            if (!isFirst) {
                drawLine(
                    color = Gray100,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, pointCenterY - pointRadius - gap),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                    pathEffect = dashEffect
                )
            }
            if (!isLast) {
                drawLine(
                    color = Gray100,
                    start = Offset(centerX, pointCenterY + pointRadius + gap),
                    end = Offset(centerX, size.height),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                    pathEffect = dashEffect
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(top = PlaceTimelinePointCenterY - 8.dp)
                .size(16.dp)
                .background(brush = shimmerBrush, shape = RoundedCornerShape(8.dp))
        )
    }
}

@Composable
private fun SkeletonBlock(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(
            brush = shimmerBrush,
            shape = RoundedCornerShape(8.dp)
        )
    )
}

@Composable
private fun rememberPlaceSkeletonBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "place_skeleton")
    val shimmerOffset by transition.animateFloat(
        initialValue = -320f,
        targetValue = 720f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_250, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "place_skeleton_offset"
    )

    return Brush.linearGradient(
        colors = listOf(
            Gray100,
            Color.White.copy(alpha = 0.88f),
            Gray100
        ),
        start = Offset(shimmerOffset, shimmerOffset),
        end = Offset(shimmerOffset + 220f, shimmerOffset + 220f)
    )
}

@Composable
private fun PlaceGuideBanner(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Green50, shape = RoundedCornerShape(14.dp))
            .padding(start = 14.dp, top = 10.dp, end = 12.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_information),
            contentDescription = null,
            tint = Green500,
            modifier = Modifier.size(17.dp)
        )
        Text(
            text = stringResource(R.string.place_sheet_banner_title),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = Gray700
        )
        Text(
            text = stringResource(R.string.place_sheet_banner_close),
            modifier = Modifier
                .clickable(onClick = onClose)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Green500,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PlaceTimelineItem(
    place: VisitedPlace,
    shouldAnimate: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    displayOrderIndex: Int,
    isMenuVisible: Boolean,
    onMoreClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onEditPlaceClick: () -> Unit,
    onPlaceClick: () -> Unit,
    onDeletePlaceClick: () -> Unit
) {
    val highlightProgress = remember(place.placeId) { Animatable(0f) }
    val bottomSpacing = if (isLast) 0.dp else PlaceTimelineItemSpacing
    val editMenuText = stringResource(R.string.place_menu_edit)
    val deleteMenuText = stringResource(R.string.place_menu_delete)

    LaunchedEffect(shouldAnimate) {
        if (!shouldAnimate) {
            highlightProgress.snapTo(0f)
            return@LaunchedEffect
        }

        highlightProgress.snapTo(0f)
        highlightProgress.animateTo(1f, animationSpec = tween(durationMillis = 350))
        delay(500)
        highlightProgress.animateTo(0f, animationSpec = tween(durationMillis = 850))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            TimelineDecoration(
                orderIndex = displayOrderIndex,
                isFirst = isFirst,
                isLast = isLast
            )
            PlaceCard(
                name = place.placeName.ifBlank {
                    stringResource(R.string.route_place_fallback_title, displayOrderIndex)
                },
                address = place.roadAddress.ifBlank {
                    stringResource(
                        R.string.place_card_coordinate,
                        place.latitude,
                        place.longitude
                    )
                },
                startTimeText = place.startTime.toPlaceCardTimeText(),
                endTimeText = place.endTime.toPlaceCardTimeText(),
                isFavoritePlace = place.bookmarkType != null,
                onClick = onPlaceClick,
                onMoreClick = onMoreClick,
                onDismissMenu = onDismissMenu,
                isMenuVisible = isMenuVisible,
                menuItems = listOf(
                    MenuActionItem(
                        text = editMenuText,
                        iconResId = R.drawable.ic_check,
                        onClick = onEditPlaceClick
                    ),
                    MenuActionItem(
                        text = deleteMenuText,
                        iconResId = R.drawable.ic_trash,
                        onClick = onDeletePlaceClick
                    )
                ),
                highlightProgress = highlightProgress.value,
                modifier = Modifier.weight(1f),
                isCompact = true
            )
        }

        if (bottomSpacing > 0.dp) {
            Spacer(modifier = Modifier.height(bottomSpacing))
        }
    }
}

private fun Modifier.placeReorderGesture(
    enabled: Boolean,
    placesProvider: () -> List<VisitedPlace>,
    listState: LazyListState,
    onDragStartPlace: (Long) -> Unit,
    onMovePlace: (fromIndex: Int, toIndex: Int) -> Unit,
    onDragFinished: () -> Unit,
    onDragCancelled: () -> Unit
): Modifier {
    if (!enabled) return this

    return pointerInput(enabled, listState) {
        var draggedPlaceId: Long? = null

        coroutineScope {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    val places = placesProvider()
                    val placeId = listState.placeIdAtOffset(offset.y, places)
                    draggedPlaceId = placeId
                    placeId?.let(onDragStartPlace)
                },
                onDrag = { change, _ ->
                    val placeId = draggedPlaceId ?: return@detectDragGesturesAfterLongPress
                    change.consume()
                    val places = placesProvider()
                    val fromIndex = places.indexOfFirst { it.placeId == placeId }
                    val toIndex = listState.placeIndexAtOffset(change.position.y, places)
                    if (fromIndex >= 0 && toIndex != null && fromIndex != toIndex) {
                        onMovePlace(fromIndex, toIndex)
                    }
                    val scrollDelta = listState.edgeScrollDelta(change.position.y)
                    if (scrollDelta != 0f) {
                        launch {
                            listState.scrollBy(scrollDelta)
                        }
                    }
                },
                onDragEnd = {
                    if (draggedPlaceId != null) {
                        onDragFinished()
                    }
                    draggedPlaceId = null
                },
                onDragCancel = {
                    if (draggedPlaceId != null) {
                        onDragCancelled()
                    }
                    draggedPlaceId = null
                }
            )
        }
    }
}

private fun LazyListState.placeIdAtOffset(offsetY: Float, places: List<VisitedPlace>): Long? {
    val itemKey = layoutInfo.visibleItemsInfo
        .firstOrNull { item ->
            item.key is Long &&
                offsetY.toInt() in item.offset..(item.offset + item.size)
        }
        ?.key as? Long

    return itemKey?.takeIf { key -> places.any { it.placeId == key } }
}

private fun LazyListState.placeIndexAtOffset(offsetY: Float, places: List<VisitedPlace>): Int? {
    val placeId = placeIdAtOffset(offsetY, places) ?: return null
    return places.indexOfFirst { it.placeId == placeId }.takeIf { it >= 0 }
}

private fun LazyListState.edgeScrollDelta(offsetY: Float): Float {
    val viewportStart = layoutInfo.viewportStartOffset.toFloat()
    val viewportEnd = layoutInfo.viewportEndOffset.toFloat()
    val edgeSize = ReorderAutoScrollEdgeSizePx

    return when {
        offsetY < viewportStart + edgeSize -> -ReorderAutoScrollStepPx
        offsetY > viewportEnd - edgeSize -> ReorderAutoScrollStepPx
        else -> 0f
    }
}

private fun List<VisitedPlace>.moved(fromIndex: Int, toIndex: Int): List<VisitedPlace> {
    if (fromIndex !in indices || toIndex !in indices || fromIndex == toIndex) return this

    return toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
    }
}

private const val ReorderAutoScrollEdgeSizePx = 96f
private const val ReorderAutoScrollStepPx = 28f
private val PlaceTimelineCardHeight = 78.dp
private val PlaceTimelineItemSpacing = 16.dp
private val PlaceTimelinePointCenterY = 34.dp

private fun String?.toPlaceCardTimeText(): String? {
    val timestamp = this ?: return null
    return runCatching {
        OffsetDateTime.parse(timestamp)
            .format(PlaceCardTimeFormatter)
    }.getOrNull()
}

@Composable
private fun TimelineDecoration(
    orderIndex: Int,
    isFirst: Boolean,
    isLast: Boolean
) {
    Box(
        modifier = Modifier
            .width(22.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerX = size.width / 2f
            val pointCenterY = PlaceTimelinePointCenterY.toPx()
            val pointRadius = 8.dp.toPx()
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 14f), 0f)
            val dashColor = Green500.copy(alpha = 0.24f)
            val strokeWidth = 1.dp.toPx()
            val gap = 4.dp.toPx()

            if (!isFirst) {
                drawLine(
                    color = dashColor,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, pointCenterY - pointRadius - gap),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                    pathEffect = dashEffect
                )
            }
            if (!isLast) {
                drawLine(
                    color = dashColor,
                    start = Offset(centerX, pointCenterY + pointRadius + gap),
                    end = Offset(centerX, size.height),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                    pathEffect = dashEffect
                )
            }
            drawCircle(
                color = Green50,
                radius = pointRadius,
                center = Offset(centerX, pointCenterY)
            )
        }
        Box(
            modifier = Modifier
                .padding(top = PlaceTimelinePointCenterY - 11.dp)
                .size(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = orderIndex.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Green600,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PlaceAddButton(onClick: () -> Unit) {
    BaseButton(
        text = "+ ${stringResource(R.string.place_sheet_add)}",
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        variant = BaseButtonVariant.SECONDARY,
        border = BorderStroke(1.dp, Green100)
    )
}

@Preview(showBackground = true, heightDp = 620, name = "Place Sheet Content")
@Composable
private fun PlaceBottomSheetContentPreview() {
    PassedPathTheme {
        PlaceBottomSheetContent(
            selectedDateKey = "2026-04-23",
            placeListUiState = PlaceListUiState(
                dateKey = "2026-04-23",
                places = previewVisitedPlaces(),
                placeCount = previewVisitedPlaces().size,
                hasLoaded = true
            ),
            selectedPlaceId = null,
            onSelectedPlaceHandled = {},
            onRetryClick = {},
            onAddPlaceClick = {},
            onReorderPlaces = {},
            onCloseReorderGuideBanner = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        )
    }
}

@Preview(showBackground = true, heightDp = 620, name = "Place Sheet Empty")
@Composable
private fun PlaceBottomSheetContentEmptyPreview() {
    PassedPathTheme {
        PlaceBottomSheetContent(
            selectedDateKey = "2026-04-23",
            placeListUiState = PlaceListUiState(
                dateKey = "2026-04-23",
                hasLoaded = true
            ),
            selectedPlaceId = null,
            onSelectedPlaceHandled = {},
            onRetryClick = {},
            onAddPlaceClick = {},
            onReorderPlaces = {},
            onCloseReorderGuideBanner = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        )
    }
}

private fun previewVisitedPlaces(): List<VisitedPlace> {
    return listOf(
        VisitedPlace(
            placeId = 1L,
            placeName = "Campus Hall",
            source = PlaceSourceType.MANUAL,
            roadAddress = "서울 성북구 정릉로 77",
            latitude = 37.6109,
            longitude = 126.9970,
            orderIndex = 1
        ),
        VisitedPlace(
            placeId = 2L,
            placeName = "Memoir Bakery",
            source = PlaceSourceType.AUTO,
            bookmarkType = BookmarkPlaceType.HOME,
            roadAddress = "서울 종로구 대명길 34 2층",
            latitude = 37.5839,
            longitude = 127.0008,
            orderIndex = 2,
            startTime = "2026-04-23T09:00:00+09:00",
            endTime = "2026-04-23T10:10:00+09:00"
        ),
        VisitedPlace(
            placeId = 3L,
            placeName = "Dongdaemun Prime City",
            source = PlaceSourceType.MANUAL,
            roadAddress = "서울 동대문구 왕산로 18",
            latitude = 37.5764,
            longitude = 127.0253,
            orderIndex = 3
        )
    )
}

private val PlaceCardTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA)
