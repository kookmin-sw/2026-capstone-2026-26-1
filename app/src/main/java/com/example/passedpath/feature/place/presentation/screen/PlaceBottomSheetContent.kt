package com.example.passedpath.feature.place.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun PlaceBottomSheetContent(
    selectedDateKey: String,
    placeListUiState: PlaceListUiState,
    onAddPlaceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isBannerVisible by rememberSaveable { mutableStateOf(true) }
    val sortedPlaces = placeListUiState.places.sortedBy(VisitedPlace::orderIndex)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isBannerVisible) {
            PlaceGuideBanner(onClose = { isBannerVisible = false })
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.place_sheet_selected_date, selectedDateKey),
                style = MaterialTheme.typography.bodySmall,
                color = Gray400
            )
            Text(
                text = stringResource(R.string.place_sheet_visit_count, placeListUiState.placeCount),
                style = MaterialTheme.typography.bodyLarge,
                color = Gray700,
                fontWeight = FontWeight.SemiBold
            )
        }

        when {
            placeListUiState.isLoading -> LoadingPlaceSection()
            placeListUiState.errorMessage != null -> ErrorPlaceSection(placeListUiState.errorMessage)
            sortedPlaces.isEmpty() -> EmptyPlaceSection()
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(
                        items = sortedPlaces,
                        key = { _, place -> place.placeId }
                    ) { index, place ->
                        PlaceTimelineItem(
                            place = place,
                            isFirst = index == 0,
                            isLast = index == sortedPlaces.lastIndex
                        )
                    }
                }
            }
        }

        PlaceAddButton(onClick = onAddPlaceClick)
        Spacer(modifier = Modifier.height(4.dp))
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
    message: String
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
        }
    }
}

@Composable
private fun PlaceTimelineItem(
    place: VisitedPlace,
    isFirst: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
