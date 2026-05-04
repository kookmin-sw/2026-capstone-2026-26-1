package com.example.passedpath.feature.place.presentation.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.passedpath.R
import com.example.passedpath.ui.component.menu.ActionPopupMenu
import com.example.passedpath.ui.component.menu.MenuActionItem
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray50
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green300
import com.example.passedpath.ui.theme.Green50
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White

@Composable
fun PlaceCard(
    name: String,
    address: String,
    modifier: Modifier = Modifier,
    startTimeText: String? = null,
    endTimeText: String? = null,
    isFavoritePlace: Boolean = false,
    isSelected: Boolean = false,
    showMoreButton: Boolean = true,
    onClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    onDismissMenu: (() -> Unit)? = null,
    isMenuVisible: Boolean = false,
    menuItems: List<MenuActionItem> = emptyList(),
    isCompact: Boolean = false,
    highlightProgress: Float = 0f,
    isDragging: Boolean = false
) {
    val cardShape = RoundedCornerShape(if (isCompact) 18.dp else 24.dp)
    val coercedHighlightProgress = highlightProgress.coerceIn(0f, 1f)
    val dragProgress by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "placeCardDragProgress"
    )
    val dragElevation by animateDpAsState(
        targetValue = if (isDragging) 10.dp else 0.dp,
        animationSpec = tween(durationMillis = 120),
        label = "placeCardDragElevation"
    )
    val visualProgress = maxOf(coercedHighlightProgress, dragProgress * 0.85f)
    val borderColor = when {
        dragProgress > 0f -> Green500.copy(alpha = 0.42f * dragProgress)
        coercedHighlightProgress > 0f -> Green300.copy(alpha = 0.45f * coercedHighlightProgress)
        isSelected -> Green500.copy(alpha = 0.28f)
        else -> Color.Transparent
    }
    val backgroundColor = lerp(Gray50, Green50, visualProgress)
    val density = LocalDensity.current
    val menuOffset = with(density) {
        IntOffset(
            x = -8.dp.roundToPx(),
            y = (if (isCompact) 58.dp else 60.dp).roundToPx()
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = 1f + (0.02f * dragProgress)
                    scaleY = 1f + (0.02f * dragProgress)
                }
                .clickable(enabled = onClick != null) {
                    onClick?.invoke()
                }
                .border(
                    width = if (isSelected || coercedHighlightProgress > 0f || dragProgress > 0f) {
                        1.dp
                    } else {
                        0.dp
                    },
                    color = borderColor,
                    shape = cardShape
                ),
            shape = cardShape,
            color = backgroundColor,
            tonalElevation = 0.dp,
            shadowElevation = dragElevation
        ) {
            PlaceCardContent(
                name = name,
                address = address,
                startTimeText = startTimeText,
                endTimeText = endTimeText,
                isFavoritePlace = isFavoritePlace,
                showMoreButton = showMoreButton,
                onMoreClick = onMoreClick,
                isMenuVisible = isMenuVisible,
                isCompact = isCompact
            )
        }

        if (isMenuVisible && menuItems.isNotEmpty()) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = menuOffset,
                onDismissRequest = { onDismissMenu?.invoke() },
                properties = PopupProperties(focusable = true)
            ) {
                ActionPopupMenu(items = menuItems)
            }
        }
    }
}

@Composable
private fun PlaceCardContent(
    name: String,
    address: String,
    startTimeText: String?,
    endTimeText: String?,
    isFavoritePlace: Boolean,
    showMoreButton: Boolean,
    onMoreClick: (() -> Unit)?,
    isMenuVisible: Boolean,
    isCompact: Boolean
) {
    val contentPadding = if (isCompact) {
        PaddingValues(start = 16.dp, top = 14.dp, end = 10.dp, bottom = 14.dp)
    } else {
        PaddingValues(start = 20.dp, top = 18.dp, end = 14.dp, bottom = 18.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 10.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaceCardTextSection(
            name = name,
            address = address,
            startTimeText = startTimeText,
            endTimeText = endTimeText,
            modifier = Modifier.weight(1f)
        )

        PlaceCardActionSection(
            isFavoritePlace = isFavoritePlace,
            showMoreButton = showMoreButton,
            onMoreClick = onMoreClick,
            isMenuVisible = isMenuVisible,
            isCompact = isCompact
        )
    }
}

@Composable
private fun PlaceCardTextSection(
    name: String,
    address: String,
    startTimeText: String?,
    endTimeText: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = Gray900,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = address,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray400,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        val hasTimeRange = !startTimeText.isNullOrBlank() && !endTimeText.isNullOrBlank()
        if (hasTimeRange) {
            Spacer(modifier = Modifier.height(10.dp))
            PlaceCardDurationRow(
                startTimeText = startTimeText.orEmpty(),
                endTimeText = endTimeText.orEmpty()
            )
        }
    }
}

@Composable
private fun PlaceCardDurationRow(
    startTimeText: String,
    endTimeText: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.AccessTime,
            contentDescription = null,
            tint = Gray500,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = "$startTimeText ~ $endTimeText",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlaceCardActionSection(
    isFavoritePlace: Boolean,
    showMoreButton: Boolean,
    onMoreClick: (() -> Unit)?,
    isMenuVisible: Boolean,
    isCompact: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val actionSize = if (isCompact) 32.dp else 36.dp

        if (isFavoritePlace) {
            Box(
                modifier = Modifier
                    .size(if (isCompact) 30.dp else 34.dp)
                    .clip(CircleShape)
                    .background(Green50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star_checked),
                    contentDescription = null,
                    tint = Green500,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (showMoreButton) {
            Box(
                modifier = Modifier
                    .size(actionSize)
                    .clip(CircleShape)
                    .background(if (isMenuVisible) Gray100 else Color.Transparent)
                    .clickable(enabled = onMoreClick != null) {
                        onMoreClick?.invoke()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Place Card States")
@Composable
private fun PlaceCardPreview() {
    PassedPathTheme {
        Column(
            modifier = Modifier
                .background(White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PlaceCard(
                name = "국민대학교 복지관",
                address = "서울 성북구 정릉로 77",
                onMoreClick = {}
            )
            PlaceCard(
                name = "동대문프라임시티2차",
                address = "서울 동대문구 왕산로 18",
                startTimeText = "오후 6:00",
                endTimeText = "오후 7:10",
                onMoreClick = {}
            )
            PlaceCard(
                name = "성수 카페거리",
                address = "서울 성동구 연무장길",
                startTimeText = "오후 7:20",
                endTimeText = "오후 8:05",
                isFavoritePlace = true,
                onMoreClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Place Card Menu")
@Composable
private fun PlaceCardMenuPreview() {
    PassedPathTheme {
        Box(
            modifier = Modifier
                .background(White)
                .padding(16.dp)
        ) {
            PlaceCard(
                name = "동대문프라임시티2차",
                address = "서울 동대문구 왕산로 18",
                startTimeText = "오후 6:00",
                endTimeText = "오후 7:10",
                isFavoritePlace = true,
                onMoreClick = {},
                isMenuVisible = true,
                menuItems = listOf(
                    MenuActionItem(
                        text = "수정하기",
                        iconResId = R.drawable.ic_check,
                        onClick = {}
                    ),
                    MenuActionItem(
                        text = "삭제하기",
                        iconResId = R.drawable.ic_trash,
                        onClick = {}
                    )
                )
            )
        }
    }
}
