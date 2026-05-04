package com.example.passedpath.feature.placebookmark.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.passedpath.R
import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmarkSummary
import com.example.passedpath.ui.component.menu.ActionPopupMenu
import com.example.passedpath.ui.component.menu.MenuActionItem
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray50
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White

@Composable
fun PlaceBookmarkCard(
    placeBookmark: PlaceBookmarkSummary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    showMoreButton: Boolean = true,
    isMenuVisible: Boolean = false,
    onDismissMenu: (() -> Unit)? = null,
    menuItems: List<MenuActionItem> = emptyList()
) {
    val cardShape = RoundedCornerShape(20.dp)
    val density = LocalDensity.current
    val menuOffset = with(density) {
        IntOffset(
            x = -8.dp.roundToPx(),
            y = 60.dp.roundToPx()
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = onClick != null) {
                    onClick?.invoke()
                },
            shape = cardShape,
            color = Gray50,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            PlaceBookmarkCardContent(
                placeBookmark = placeBookmark,
                showMoreButton = showMoreButton,
                onMoreClick = onMoreClick,
                isMenuVisible = isMenuVisible
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
private fun PlaceBookmarkCardContent(
    placeBookmark: PlaceBookmarkSummary,
    showMoreButton: Boolean,
    onMoreClick: (() -> Unit)?,
    isMenuVisible: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                PaddingValues(
                    start = 20.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 20.dp
                )
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaceBookmarkBadge(
            type = placeBookmark.type,
            size = 48.dp,
            iconSize = 24.dp
        )

        PlaceBookmarkCardTextSection(
            name = placeBookmark.placeName,
            address = placeBookmark.roadAddress,
            modifier = Modifier.weight(1f)
        )

        if (showMoreButton) {
            PlaceBookmarkCardMoreButton(
                onClick = onMoreClick,
                isMenuVisible = isMenuVisible
            )
        }
    }
}

@Composable
private fun PlaceBookmarkCardTextSection(
    name: String,
    address: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = Gray900,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = address,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray400,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlaceBookmarkCardMoreButton(
    onClick: (() -> Unit)?,
    isMenuVisible: Boolean
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .offset(y = 6.dp)
            .clip(CircleShape)
            .background(if (isMenuVisible) Gray100 else Color.Transparent)
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
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

@Preview(showBackground = true, name = "Place Bookmark Card List")
@Composable
private fun PlaceBookmarkCardListPreview() {
    PassedPathTheme {
        Column(
            modifier = Modifier
                .background(White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            previewPlaceBookmarks().forEach { placeBookmark ->
                PlaceBookmarkCard(
                    placeBookmark = placeBookmark,
                    onMoreClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Place Bookmark Card Menu")
@Composable
private fun PlaceBookmarkCardMenuPreview() {
    PassedPathTheme {
        Box(
            modifier = Modifier
                .background(White)
                .padding(16.dp)
        ) {
            PlaceBookmarkCard(
                placeBookmark = previewPlaceBookmarks().first(),
                onMoreClick = {},
                isMenuVisible = true,
                menuItems = listOf(
                    MenuActionItem(
                        text = "Edit",
                        iconResId = R.drawable.ic_check,
                        onClick = {}
                    ),
                    MenuActionItem(
                        text = "Delete",
                        iconResId = R.drawable.ic_trash,
                        onClick = {}
                    )
                )
            )
        }
    }
}

private fun previewPlaceBookmarks(): List<PlaceBookmarkSummary> {
    return listOf(
        PlaceBookmarkSummary(
            bookmarkPlaceId = 1L,
            type = BookmarkPlaceType.SCHOOL,
            placeName = "Kookmin Welfare Center",
            roadAddress = "77 Jeongneung-ro, Seongbuk-gu",
            latitude = 37.6113,
            longitude = 126.9958
        ),
        PlaceBookmarkSummary(
            bookmarkPlaceId = 2L,
            type = BookmarkPlaceType.COMPANY,
            placeName = "Kookmin Welfare Center",
            roadAddress = "77 Jeongneung-ro, Seongbuk-gu",
            latitude = 37.6113,
            longitude = 126.9958
        ),
        PlaceBookmarkSummary(
            bookmarkPlaceId = 3L,
            type = BookmarkPlaceType.HOME,
            placeName = "Kookmin Welfare Center",
            roadAddress = "77 Jeongneung-ro, Seongbuk-gu",
            latitude = 37.6113,
            longitude = 126.9958
        ),
        PlaceBookmarkSummary(
            bookmarkPlaceId = 4L,
            type = BookmarkPlaceType.ETC,
            placeName = "Dongdaemun Prime City 2",
            roadAddress = "77 Jeongneung-ro, Seongbuk-gu",
            latitude = 37.6113,
            longitude = 126.9958
        )
    )
}
