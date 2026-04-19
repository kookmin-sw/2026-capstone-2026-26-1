package com.example.passedpath.feature.place.presentation.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.place.domain.model.VisitedPlace
import com.example.passedpath.feature.place.presentation.state.PlaceListUiState
import com.example.passedpath.ui.component.PlaceCard
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray700
import com.example.passedpath.ui.theme.Green50
import com.example.passedpath.ui.theme.Green500
import kotlinx.coroutines.delay

@Composable
fun PlaceBottomSheetContent(
    selectedDateKey: String,
    placeListUiState: PlaceListUiState,
    selectedPlaceId: Long?,
    onSelectedPlaceHandled: () -> Unit,
    onRetryClick: () -> Unit,
    onAddPlaceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isBannerVisible by rememberSaveable { mutableStateOf(true) }
    var animatedPlaceId by remember { mutableStateOf<Long?>(null) }
    val sortedPlaces = placeListUiState.places.sortedBy(VisitedPlace::orderIndex)
    val listState = rememberLazyListState()
    val placeSectionStartIndex = (if (isBannerVisible) 1 else 0) + 1

    LaunchedEffect(selectedPlaceId, sortedPlaces) {
        val placeId = selectedPlaceId ?: return@LaunchedEffect
        val selectedIndex = sortedPlaces.indexOfFirst { it.placeId == placeId }
        if (selectedIndex < 0) {
            onSelectedPlaceHandled()
            return@LaunchedEffect
        }

        listState.animateScrollToItem(placeSectionStartIndex + selectedIndex)
        animatedPlaceId = placeId
        delay(320)
        animatedPlaceId = null
        onSelectedPlaceHandled()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                clip = true
                shape = RectangleShape
            },
        state = listState,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isBannerVisible) {
            item(key = "banner") {
                PlaceGuideBanner(onClose = { isBannerVisible = false })
            }
        }

        item(key = "summary") {
            PlaceSummarySection(
                selectedDateKey = selectedDateKey,
                placeCount = placeListUiState.placeCount
            )
        }

        if (placeListUiState.isStale && placeListUiState.errorMessage != null) {
            item(key = "stale_notice") {
                StalePlaceSection(
                    message = placeListUiState.errorMessage,
                    onRetryClick = onRetryClick
                )
            }
        }

        when {
            placeListUiState.isLoading && !placeListUiState.hasRetainedContent -> {
                item(key = "loading") { LoadingPlaceSection() }
            }
            placeListUiState.errorMessage != null && !placeListUiState.isStale -> {
                item(key = "error") {
                    ErrorPlaceSection(
                        message = placeListUiState.errorMessage,
                        onRetryClick = onRetryClick
                    )
                }
            }
            sortedPlaces.isEmpty() -> {
                item(key = "empty") { EmptyPlaceSection() }
            }
            else -> {
                itemsIndexed(
                    items = sortedPlaces,
                    key = { _, place -> place.placeId }
                ) { index, place ->
                    PlaceTimelineItem(
                        place = place,
                        shouldAnimate = place.placeId == animatedPlaceId,
                        isFirst = index == 0,
                        isLast = index == sortedPlaces.lastIndex
                    )
                }
            }
        }

        item(key = "footer") {
            PlaceAddButton(onClick = onAddPlaceClick)
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun PlaceSummarySection(
    selectedDateKey: String,
    placeCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.place_sheet_selected_date, selectedDateKey),
            style = MaterialTheme.typography.bodySmall,
            color = Gray400
        )
        Text(
            text = stringResource(R.string.place_sheet_visit_count, placeCount),
            style = MaterialTheme.typography.bodyLarge,
            color = Gray700,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LoadingPlaceSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Gray100, shape = RoundedCornerShape(22.dp))
            .padding(horizontal = 18.dp, vertical = 24.dp)
    ) {
        Text(
            text = "방문 장소를 불러오는 중입니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray400
        )
    }
}

@Composable
private fun ErrorPlaceSection(
    message: String,
    onRetryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Gray100, shape = RoundedCornerShape(22.dp))
            .padding(horizontal = 18.dp, vertical = 24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "방문 장소를 불러오지 못했습니다.",
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
            .background(color = Gray100, shape = RoundedCornerShape(22.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp)
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
private fun EmptyPlaceSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Gray100, shape = RoundedCornerShape(22.dp))
            .padding(horizontal = 18.dp, vertical = 24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.place_sheet_empty_title),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.place_sheet_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = Gray400
            )
        }
    }
}

@Composable
private fun PlaceGuideBanner(
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Green50, shape = RoundedCornerShape(18.dp))
            .padding(start = 16.dp, top = 14.dp, end = 12.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.place_sheet_banner_title),
                style = MaterialTheme.typography.bodyMedium,
                color = Green500,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.place_sheet_banner_body),
                style = MaterialTheme.typography.bodySmall,
                color = Gray400
            )
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.place_sheet_banner_close),
                tint = Gray400,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun PlaceTimelineItem(
    place: VisitedPlace,
    shouldAnimate: Boolean,
    isFirst: Boolean,
    isLast: Boolean
) {
    val horizontalOffset = remember(place.placeId) {
        Animatable(0f)
    }

    LaunchedEffect(shouldAnimate) {
        if (!shouldAnimate) {
            horizontalOffset.snapTo(0f)
            return@LaunchedEffect
        }

        horizontalOffset.snapTo(0f)
        horizontalOffset.animateTo(10f, animationSpec = tween(durationMillis = 70))
        horizontalOffset.animateTo(-8f, animationSpec = tween(durationMillis = 90))
        horizontalOffset.animateTo(6f, animationSpec = tween(durationMillis = 80))
        horizontalOffset.animateTo(0f, animationSpec = tween(durationMillis = 70))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = horizontalOffset.value.toCardOffset()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        TimelineDecoration(
            orderIndex = place.orderIndex,
            isFirst = isFirst,
            isLast = isLast
        )
        PlaceCard(
            title = place.placeName.ifBlank {
                stringResource(R.string.route_place_fallback_title, place.orderIndex)
            },
            subtitle = place.roadAddress.ifBlank {
                stringResource(
                    R.string.place_card_coordinate,
                    place.latitude,
                    place.longitude
                )
            },
            tertiaryText = place.roadAddress.takeIf { it.isNotBlank() }?.let {
                stringResource(
                    R.string.place_card_coordinate,
                    place.latitude,
                    place.longitude
                )
            },
            modifier = Modifier.weight(1f)
        )
    }
}

private fun Float.toCardOffset(): Dp = this.dp

@Composable
private fun TimelineDecoration(
    orderIndex: Int,
    isFirst: Boolean,
    isLast: Boolean
) {
    Box(
        modifier = Modifier
            .padding(top = 10.dp)
            .size(width = 26.dp, height = 96.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerX = size.width / 2f
            val pointCenterY = 14.dp.toPx()
            val pointRadius = 5.dp.toPx()
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 7f), 0f)
            val dashColor = Green500.copy(alpha = 0.35f)
            val strokeWidth = 2.dp.toPx()
            val gap = 7.dp.toPx()

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
                color = Green500,
                radius = pointRadius,
                center = Offset(centerX, pointCenterY)
            )
        }
        Text(
            text = orderIndex.toString(),
            modifier = Modifier.padding(top = 28.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Green500,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PlaceAddButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Green50, shape = RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 17.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "+",
                color = Green500,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.place_sheet_add),
                color = Green500,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
