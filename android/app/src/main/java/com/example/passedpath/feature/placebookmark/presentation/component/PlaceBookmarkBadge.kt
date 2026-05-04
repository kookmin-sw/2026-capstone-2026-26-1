package com.example.passedpath.feature.placebookmark.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.ui.theme.BookmarkCompanyColor
import com.example.passedpath.ui.theme.BookmarkHomeColor
import com.example.passedpath.ui.theme.BookmarkOtherColor
import com.example.passedpath.ui.theme.BookmarkSchoolColor
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White

@Composable
fun PlaceBookmarkBadge(
    type: BookmarkPlaceType,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    iconSize: Dp = 32.dp,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(type.badgeColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = type.badgeIconResId),
            contentDescription = contentDescription,
            tint = White,
            modifier = Modifier.size(iconSize)
        )
    }
}

private val BookmarkPlaceType.badgeColor: Color
    get() = when (this) {
        BookmarkPlaceType.HOME -> BookmarkHomeColor
        BookmarkPlaceType.COMPANY -> BookmarkCompanyColor
        BookmarkPlaceType.SCHOOL -> BookmarkSchoolColor
        BookmarkPlaceType.ETC -> BookmarkOtherColor
    }

@get:DrawableRes
private val BookmarkPlaceType.badgeIconResId: Int
    get() = when (this) {
        BookmarkPlaceType.HOME -> R.drawable.ic_bookmark_home
        BookmarkPlaceType.COMPANY -> R.drawable.ic_bookmark_company
        BookmarkPlaceType.SCHOOL -> R.drawable.ic_bookmark_school
        BookmarkPlaceType.ETC -> R.drawable.ic_bookmark_other
    }

@Preview(showBackground = true, name = "Place Bookmark Badge")
@Composable
private fun PlaceBookmarkBadgePreview() {
    PassedPathTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PlaceBookmarkBadge(type = BookmarkPlaceType.HOME)
            PlaceBookmarkBadge(type = BookmarkPlaceType.COMPANY)
            PlaceBookmarkBadge(type = BookmarkPlaceType.SCHOOL)
            PlaceBookmarkBadge(type = BookmarkPlaceType.ETC)
        }
    }
}
