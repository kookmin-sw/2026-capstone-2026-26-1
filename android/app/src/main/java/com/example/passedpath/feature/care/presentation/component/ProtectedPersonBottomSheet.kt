package com.example.passedpath.feature.care.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.feature.care.presentation.model.ProtectedPersonBottomSheetTab
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonPlaceListUiState
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonPlaceUiState
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonSummaryContentUiState
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonSummaryUiState
import com.example.passedpath.feature.place.presentation.component.PlaceCard
import com.example.passedpath.feature.summary.presentation.component.DaySummaryMetricCard
import com.example.passedpath.feature.summary.presentation.component.DaySummaryMetricCardSkeleton
import com.example.passedpath.feature.summary.presentation.component.DaySummaryVisitedDongCard
import com.example.passedpath.feature.summary.presentation.component.DaySummaryVisitedDongCardSkeleton
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetContainer
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetTabItem
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetTabRow
import com.example.passedpath.ui.component.banner.InfoActionBottomBanner
import com.example.passedpath.ui.component.feedback.NetworkFailureBanner
import com.example.passedpath.ui.component.loading.BaseSkeletonBlock
import com.example.passedpath.ui.component.loading.rememberBaseSkeletonBrush
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray50
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray700
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Green300
import com.example.passedpath.ui.theme.Green50
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.Green600
import com.example.passedpath.ui.theme.PassedPathTheme
import kotlinx.coroutines.delay

@Composable
fun ProtectedPersonBottomSheet(
    selectedTab: ProtectedPersonBottomSheetTab,
    onTabSelected: (ProtectedPersonBottomSheetTab) -> Unit,
    placeListUiState: ProtectedPersonPlaceListUiState,
    summaryUiState: ProtectedPersonSummaryUiState,
    modifier: Modifier = Modifier,
    selectedPlaceId: Long? = null,
    onSelectedPlaceHandled: () -> Unit = {},
    onPlaceClick: (Long) -> Unit = {},
    onPlaceGuideBannerClose: () -> Unit = {},
    onPlaceRetryClick: () -> Unit = {},
    onSummaryRetryClick: () -> Unit = {}
) {
    var isContentScrolled by remember(selectedTab) { mutableStateOf(false) }
    val tabItems = remember {
        ProtectedPersonBottomSheetTab.entries.map(ProtectedPersonBottomSheetTab::toBaseTabItem)
    }

    LaunchedEffect(selectedTab) {
        isContentScrolled = false
    }

    BaseBottomSheetContainer(
        modifier = modifier,
        isContentScrolled = isContentScrolled,
        tabRow = {
            BaseBottomSheetTabRow(
                items = tabItems,
                selectedKey = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) {
        when (selectedTab) {
            ProtectedPersonBottomSheetTab.PLACE -> ProtectedPersonPlaceListContent(
                uiState = placeListUiState,
                selectedPlaceId = selectedPlaceId,
                onSelectedPlaceHandled = onSelectedPlaceHandled,
                onRetryClick = onPlaceRetryClick,
                onPlaceClick = onPlaceClick,
                onPlaceGuideBannerClose = onPlaceGuideBannerClose,
                onScrollStateChanged = { isContentScrolled = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            )

            ProtectedPersonBottomSheetTab.SUMMARY -> ProtectedPersonDaySummaryContent(
                uiState = summaryUiState,
                onRetryClick = onSummaryRetryClick,
                onScrollStateChanged = { isContentScrolled = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
fun ProtectedPersonPlaceListContent(
    uiState: ProtectedPersonPlaceListUiState,
    selectedPlaceId: Long?,
    onSelectedPlaceHandled: () -> Unit,
    onRetryClick: () -> Unit,
    onPlaceClick: (Long) -> Unit,
    onPlaceGuideBannerClose: () -> Unit,
    onScrollStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var animatedPlaceId by remember { mutableStateOf<Long?>(null) }
    val listState = rememberLazyListState()
    val isContentScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 0
        }
    }
    val places = uiState.places.sortedBy(ProtectedPersonPlaceUiState::orderIndex)
    val placeSectionStartIndex = (if (uiState.isPlaceGuideBannerVisible) 1 else 0) + 1

    LaunchedEffect(isContentScrolled) {
        onScrollStateChanged(isContentScrolled)
    }

    LaunchedEffect(selectedPlaceId, places) {
        val placeId = selectedPlaceId ?: return@LaunchedEffect
        val selectedIndex = places.indexOfFirst { place -> place.placeId == placeId }
        if (selectedIndex < 0) return@LaunchedEffect

        listState.animateScrollToItem(placeSectionStartIndex + selectedIndex)
        animatedPlaceId = placeId
        delay(1_700)
        animatedPlaceId = null
        onSelectedPlaceHandled()
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        if (uiState.isPlaceGuideBannerVisible) {
            item(key = "protected_person_place_guide_banner") {
                Box(modifier = Modifier.padding(bottom = PlaceTimelineItemSpacing)) {
                    ProtectedPersonPlaceGuideBanner(onClose = onPlaceGuideBannerClose)
                }
            }
        }

        item {
            Box(modifier = Modifier.padding(bottom = PlaceTimelineItemSpacing)) {
                ProtectedPersonPlaceSummarySection(placeCount = uiState.placeCount)
            }
        }

        when {
            uiState.errorMessage != null -> {
                item {
                    Box(modifier = Modifier.padding(bottom = PlaceTimelineItemSpacing)) {
                        ProtectedPersonPlaceErrorNotice(onRetryClick = onRetryClick)
                    }
                }
            }

            uiState.isLoading || !uiState.hasLoaded -> {
                item(key = "protected_person_place_skeleton") {
                    Box(modifier = Modifier.padding(bottom = PlaceTimelineItemSpacing)) {
                        ProtectedPersonPlaceSkeletonList()
                    }
                }
            }

            places.isEmpty() -> {
                item {
                    Box(modifier = Modifier.padding(bottom = PlaceTimelineItemSpacing)) {
                        ProtectedPersonEmptyPlaceNotice()
                    }
                }
            }

            else -> {
                itemsIndexed(
                    items = places,
                    key = { _, place -> place.placeId }
                ) { index, place ->
                    ProtectedPersonPlaceTimelineItem(
                        place = place,
                        shouldAnimate = place.placeId == animatedPlaceId,
                        isFirst = index == 0,
                        isLast = index == places.lastIndex,
                        displayOrderIndex = index + 1,
                        onPlaceClick = { onPlaceClick(place.placeId) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ProtectedPersonDaySummaryContent(
    uiState: ProtectedPersonSummaryUiState,
    onRetryClick: () -> Unit,
    onScrollStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val isContentScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 0
        }
    }

    LaunchedEffect(isContentScrolled) {
        onScrollStateChanged(isContentScrolled)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(2.dp))
        }

        when {
            uiState.errorMessage != null -> {
                item {
                    NetworkFailureBanner(
                        retryText = stringResource(R.string.route_retry),
                        onRetryClick = onRetryClick
                    )
                }
            }

            uiState.isLoading || !uiState.hasLoaded -> {
                item(key = "protected_person_summary_skeleton") {
                    ProtectedPersonSummarySkeletonList()
                }
            }

            else -> {
                item {
                    DaySummaryMetricCard(
                        label = stringResource(R.string.day_summary_outing_time),
                        value = uiState.summary.outingTimeText
                    )
                }
                item {
                    DaySummaryMetricCard(
                        label = stringResource(R.string.day_summary_enter_home_time),
                        value = uiState.summary.enterHomeTimeText
                    )
                }
                item {
                    DaySummaryMetricCard(
                        label = stringResource(R.string.day_summary_total_outing_duration),
                        value = uiState.summary.totalOutingDurationText
                    )
                }
                item {
                    DaySummaryMetricCard(
                        label = stringResource(R.string.day_summary_total_outing_count),
                        value = uiState.summary.totalOutingCountText
                    )
                }
                item {
                    DaySummaryVisitedDongCard(
                        label = stringResource(R.string.day_summary_visited_dong_title),
                        visitedDongNames = uiState.summary.visitedDongNames
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ProtectedPersonPlaceSummarySection(placeCount: Int) {
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

@Composable
private fun ProtectedPersonPlaceGuideBanner(onClose: () -> Unit) {
    InfoActionBottomBanner(
        message = stringResource(R.string.protected_person_place_banner),
        actionText = stringResource(R.string.protected_person_place_banner_close),
        onClickAction = onClose
    )
}

@Composable
private fun ProtectedPersonPlaceErrorNotice(onRetryClick: () -> Unit) {
    NetworkFailureBanner(
        retryText = stringResource(R.string.route_retry),
        onRetryClick = onRetryClick
    )
}

@Composable
private fun ProtectedPersonEmptyPlaceNotice() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray50, RoundedCornerShape(20.dp))
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.protected_person_place_empty_title),
            style = MaterialTheme.typography.bodyLarge,
            color = Gray700,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.protected_person_place_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = Gray400,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProtectedPersonPlaceTimelineItem(
    place: ProtectedPersonPlaceUiState,
    shouldAnimate: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    displayOrderIndex: Int,
    onPlaceClick: () -> Unit
) {
    val highlightProgress = remember(place.placeId) { Animatable(0f) }
    val bottomSpacing = if (isLast) 0.dp else PlaceTimelineItemSpacing

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
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = bottomSpacing)
        ) {
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
                startTimeText = place.startTimeText,
                endTimeText = place.endTimeText,
                isFavoritePlace = place.isFavoritePlace,
                showMoreButton = false,
                onClick = onPlaceClick,
                highlightProgress = highlightProgress.value,
                modifier = Modifier.fillMaxWidth(),
                isCompact = true
            )
        }
    }
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
private fun ProtectedPersonPlaceSkeletonList() {
    val skeletonBrush = rememberBaseSkeletonBrush()

    Column(verticalArrangement = Arrangement.spacedBy(PlaceTimelineItemSpacing)) {
        repeat(2) { index ->
            ProtectedPersonPlaceTimelineSkeletonItem(
                shimmerBrush = skeletonBrush,
                isFirst = index == 0,
                isLast = index == 1
            )
        }
    }
}

@Composable
private fun ProtectedPersonPlaceTimelineSkeletonItem(
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
    Row(
        modifier = modifier
            .height(PlaceTimelineCardHeight)
            .background(Gray50, RoundedCornerShape(20.dp))
            .padding(start = 20.dp, top = 14.dp, end = 16.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BaseSkeletonBlock(
                brush = shimmerBrush,
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .height(16.dp)
            )
            BaseSkeletonBlock(
                brush = shimmerBrush,
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .height(13.dp)
            )
        }
        BaseSkeletonBlock(
            brush = shimmerBrush,
            modifier = Modifier.size(36.dp),
            shape = CircleShape
        )
    }
}

@Composable
private fun SkeletonTimelineDecoration(
    shimmerBrush: Brush,
    isFirst: Boolean,
    isLast: Boolean
) {
    val decorationHeight = PlaceTimelineCardHeight + if (isLast) {
        0.dp
    } else {
        PlaceTimelineItemSpacing
    }

    Box(
        modifier = Modifier.size(width = 22.dp, height = decorationHeight),
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
        ) {
            BaseSkeletonBlock(
                brush = shimmerBrush,
                modifier = Modifier.matchParentSize(),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
private fun ProtectedPersonSummarySkeletonList() {
    val skeletonBrush = rememberBaseSkeletonBrush()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(4) {
            DaySummaryMetricCardSkeleton(shimmerBrush = skeletonBrush)
        }
        DaySummaryVisitedDongCardSkeleton(shimmerBrush = skeletonBrush)
    }
}

private fun ProtectedPersonBottomSheetTab.titleResId(): Int {
    return when (this) {
        ProtectedPersonBottomSheetTab.PLACE -> R.string.record_sheet_tab_place
        ProtectedPersonBottomSheetTab.SUMMARY -> R.string.record_sheet_tab_summary
    }
}

private fun ProtectedPersonBottomSheetTab.iconResId(): Int {
    return when (this) {
        ProtectedPersonBottomSheetTab.PLACE -> R.drawable.ic_bottom_sheet_place
        ProtectedPersonBottomSheetTab.SUMMARY -> R.drawable.ic_day_summary
    }
}

private fun ProtectedPersonBottomSheetTab.toBaseTabItem():
    BaseBottomSheetTabItem<ProtectedPersonBottomSheetTab> {
    return BaseBottomSheetTabItem(
        key = this,
        titleResId = titleResId(),
        iconResId = iconResId()
    )
}

private val PlaceTimelineCardHeight = 78.dp
private val PlaceTimelineItemSpacing = 16.dp
private val PlaceTimelinePointCenterY = 34.dp

@Preview(showBackground = true, heightDp = 720, name = "Protected Person - Places")
@Composable
private fun ProtectedPersonBottomSheetPlacePreview() {
    ProtectedPersonBottomSheetPreviewScaffold(
        selectedTab = ProtectedPersonBottomSheetTab.PLACE,
        placeListUiState = SampleProtectedPersonPlaceListUiState
    )
}

@Preview(showBackground = true, heightDp = 720, name = "Protected Person - Summary")
@Composable
private fun ProtectedPersonBottomSheetSummaryPreview() {
    ProtectedPersonBottomSheetPreviewScaffold(
        selectedTab = ProtectedPersonBottomSheetTab.SUMMARY,
        summaryUiState = SampleProtectedPersonSummaryUiState
    )
}

@Preview(showBackground = true, heightDp = 720, name = "Protected Person - Loading")
@Composable
private fun ProtectedPersonBottomSheetLoadingPreview() {
    ProtectedPersonBottomSheetPreviewScaffold(
        selectedTab = ProtectedPersonBottomSheetTab.PLACE,
        placeListUiState = ProtectedPersonPlaceListUiState(
            placeCount = 0,
            isLoading = true,
            isPlaceGuideBannerVisible = true
        )
    )
}

@Preview(showBackground = true, heightDp = 720, name = "Protected Person - Empty")
@Composable
private fun ProtectedPersonBottomSheetEmptyPreview() {
    ProtectedPersonBottomSheetPreviewScaffold(
        selectedTab = ProtectedPersonBottomSheetTab.PLACE,
        placeListUiState = ProtectedPersonPlaceListUiState(
            placeCount = 0,
            hasLoaded = true,
            isPlaceGuideBannerVisible = true
        )
    )
}

@Composable
private fun ProtectedPersonBottomSheetPreviewScaffold(
    selectedTab: ProtectedPersonBottomSheetTab,
    placeListUiState: ProtectedPersonPlaceListUiState = SampleProtectedPersonPlaceListUiState,
    summaryUiState: ProtectedPersonSummaryUiState = SampleProtectedPersonSummaryUiState
) {
    PassedPathTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6))
        ) {
            ProtectedPersonBottomSheet(
                selectedTab = selectedTab,
                onTabSelected = {},
                placeListUiState = placeListUiState,
                summaryUiState = summaryUiState,
                onPlaceGuideBannerClose = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private val SampleProtectedPersonPlaceListUiState = ProtectedPersonPlaceListUiState(
    places = listOf(
        ProtectedPersonPlaceUiState(
            placeId = 1L,
            placeName = "Maison Akai Hyehwa",
            roadAddress = "34 Daemyeong-gil, Jongno-gu, Seoul",
            latitude = 37.5839,
            longitude = 127.0008,
            orderIndex = 1,
            startTimeText = "10:12 AM",
            endTimeText = "11:08 AM"
        ),
        ProtectedPersonPlaceUiState(
            placeId = 2L,
            placeName = "National University Welfare Center",
            roadAddress = "77 Jeongneung-ro, Seongbuk-gu, Seoul",
            latitude = 37.6109,
            longitude = 126.9970,
            orderIndex = 2,
            startTimeText = "12:30 PM",
            endTimeText = "1:15 PM",
            isFavoritePlace = true
        ),
        ProtectedPersonPlaceUiState(
            placeId = 3L,
            placeName = "Cheonggye Plaza",
            roadAddress = "Taepyeong-ro 1-ga, Jung-gu, Seoul",
            latitude = 37.5704,
            longitude = 126.9789,
            orderIndex = 3,
            startTimeText = "4:20 PM",
            endTimeText = "5:05 PM"
        )
    ),
    placeCount = 3,
    hasLoaded = true,
    isPlaceGuideBannerVisible = true
)

private val SampleProtectedPersonSummaryUiState = ProtectedPersonSummaryUiState(
    hasLoaded = true,
    summary = ProtectedPersonSummaryContentUiState(
        outingTimeText = "09:12",
        enterHomeTimeText = "21:03",
        totalOutingDurationText = "11h 51m",
        totalOutingCountText = "3",
        visitedDongNames = listOf("Hyehwa-dong", "Jeongneung-dong", "Seongbuk-dong")
    )
)
