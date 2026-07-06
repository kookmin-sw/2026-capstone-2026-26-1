package com.example.passedpath.feature.placebookmark.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmarkSummary
import com.example.passedpath.feature.placebookmark.presentation.state.PlaceBookmarkUiState
import com.example.passedpath.ui.component.button.BaseButton
import com.example.passedpath.ui.component.feedback.BaseEmptyContent
import com.example.passedpath.ui.component.loading.BaseSkeletonBlock
import com.example.passedpath.ui.component.loading.rememberBaseSkeletonBrush
import com.example.passedpath.ui.component.menu.MenuActionItem
import com.example.passedpath.ui.theme.Gray200
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray50
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green50
import com.example.passedpath.ui.theme.Green100
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
internal fun PlaceBookmarkListContent(
    uiState: PlaceBookmarkUiState,
    openedMenuBookmarkId: Long?,
    onMenuOpenedChange: (Long?) -> Unit,
    onRegisterHomeClick: () -> Unit,
    onAddPlaceBookmarkClick: () -> Unit,
    onEditPlaceBookmarkClick: (PlaceBookmarkSummary) -> Unit,
    onDeletePlaceBookmarkClick: (PlaceBookmarkSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val homeBookmark = remember(uiState.bookmarkPlaces) {
        uiState.bookmarkPlaces.firstOrNull { it.type == BookmarkPlaceType.HOME }
    }
    val generalBookmarks = remember(uiState.bookmarkPlaces) {
        uiState.bookmarkPlaces.filterNot { it.type == BookmarkPlaceType.HOME }
    }
    val isListScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 0
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrollInProgress ->
                if (isScrollInProgress) {
                    onMenuOpenedChange(null)
                }
            }
    }

    when {
        uiState.isLoading && !uiState.hasLoaded -> {
            Column(modifier = modifier) {
                PlaceBookmarkListDivider(visible = false)
                PlaceBookmarkLoadingContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }

        else -> {
            Column(modifier = modifier) {
                PlaceBookmarkListDivider(visible = isListScrolled)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 40.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "home-section") {
                        PlaceBookmarkHomeSection(
                            homeBookmark = homeBookmark,
                            isMenuVisible = homeBookmark?.bookmarkPlaceId == openedMenuBookmarkId,
                            onRegisterHomeClick = onRegisterHomeClick,
                            onMoreClick = {
                                homeBookmark?.bookmarkPlaceId?.let { bookmarkId ->
                                    onMenuOpenedChange(
                                        if (openedMenuBookmarkId == bookmarkId) null else bookmarkId
                                    )
                                }
                            },
                            onDismissMenu = { onMenuOpenedChange(null) },
                            onEditClick = {
                                homeBookmark?.let {
                                    onMenuOpenedChange(null)
                                    onEditPlaceBookmarkClick(it)
                                }
                            },
                            onDeleteClick = {
                                homeBookmark?.let {
                                    onMenuOpenedChange(null)
                                    onDeletePlaceBookmarkClick(it)
                                }
                            }
                        )
                    }

                    item(key = "home-general-separator") {
                        PlaceBookmarkSectionSeparator()
                    }

                    item(key = "general-header") {
                        PlaceBookmarkGeneralHeader(
                            placeCount = generalBookmarks.size,
                            onAddPlaceBookmarkClick = onAddPlaceBookmarkClick
                        )
                    }

                    if (generalBookmarks.isEmpty()) {
                        item(key = "general-empty") {
                            PlaceBookmarkEmptyContent(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 56.dp)
                            )
                        }
                    } else {
                        items(
                            items = generalBookmarks,
                            key = PlaceBookmarkSummary::bookmarkPlaceId
                        ) { placeBookmark ->
                            PlaceBookmarkListItem(
                                placeBookmark = placeBookmark,
                                isMenuVisible = openedMenuBookmarkId == placeBookmark.bookmarkPlaceId,
                                onMoreClick = {
                                    onMenuOpenedChange(
                                        if (openedMenuBookmarkId == placeBookmark.bookmarkPlaceId) {
                                            null
                                        } else {
                                            placeBookmark.bookmarkPlaceId
                                        }
                                    )
                                },
                                onDismissMenu = { onMenuOpenedChange(null) },
                                onEditClick = {
                                    onMenuOpenedChange(null)
                                    onEditPlaceBookmarkClick(placeBookmark)
                                },
                                onDeleteClick = {
                                    onMenuOpenedChange(null)
                                    onDeletePlaceBookmarkClick(placeBookmark)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceBookmarkHomeSection(
    homeBookmark: PlaceBookmarkSummary?,
    isMenuVisible: Boolean,
    onRegisterHomeClick: () -> Unit,
    onMoreClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PlaceBookmarkSectionHeader(
            title = "집 위치",
            description = "외출·귀가 요약 기준 장소"
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (homeBookmark == null) {
            HomeRegistrationCard(onClick = onRegisterHomeClick)
        } else {
            PlaceBookmarkListItem(
                placeBookmark = homeBookmark,
                isMenuVisible = isMenuVisible,
                onMoreClick = onMoreClick,
                onDismissMenu = onDismissMenu,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick
            )
        }
    }
}

@Composable
private fun HomeRegistrationCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Green50,
        border = BorderStroke(width = 1.dp, color = Green100),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(
                start = 18.dp,
                top = 18.dp,
                end = 18.dp,
                bottom = 16.dp
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlaceBookmarkBadge(
                    type = BookmarkPlaceType.HOME,
                    size = 44.dp,
                    iconSize = 22.dp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "집 위치가 아직 없어요",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp,
                        color = Gray900,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "외출·귀가 요약을 확인하려면 집을 등록해 주세요",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 13.sp,
                        color = Gray500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BaseButton(
                text = "집 위치 등록하기",
                onClick = onClick,
                border = BorderStroke(width = 1.dp, color = Green100),
                containerColor = White,
                contentColor = Green500,
                leadingIconResId = R.drawable.ic_plus,
                textFontSize = 15.sp,
                textFontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PlaceBookmarkGeneralHeader(
    placeCount: Int,
    onAddPlaceBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PlaceBookmarkSectionHeader(
            title = "즐겨찾는 장소",
            description = buildAnnotatedString {
                append("자주 가는 장소 · ")
                withStyle(
                    SpanStyle(
                        color = Green500,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("${placeCount}곳")
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        BaseButton(
            text = stringResource(R.string.place_bookmark_add_button),
            onClick = onAddPlaceBookmarkClick,
            border = BorderStroke(width = 1.dp, color = Green100),
            containerColor = Green50,
            contentColor = Green500,
            leadingIconResId = R.drawable.ic_plus,
            textFontSize = 16.sp,
            textFontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PlaceBookmarkSectionSeparator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Gray200)
        )
    }
}

@Composable
private fun PlaceBookmarkSectionHeader(
    title: String,
    description: AnnotatedString,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 15.sp,
            color = Gray900,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 12.sp,
            color = Gray400,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PlaceBookmarkSectionHeader(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    PlaceBookmarkSectionHeader(
        title = title,
        description = buildAnnotatedString { append(description) },
        modifier = modifier
    )
}

@Composable
private fun PlaceBookmarkListItem(
    placeBookmark: PlaceBookmarkSummary,
    isMenuVisible: Boolean,
    onMoreClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val editText = stringResource(R.string.place_menu_edit)
    val deleteText = stringResource(R.string.place_menu_delete)

    PlaceBookmarkCard(
        placeBookmark = placeBookmark,
        onMoreClick = onMoreClick,
        isMenuVisible = isMenuVisible,
        onDismissMenu = onDismissMenu,
        menuItems = listOf(
            MenuActionItem(
                text = editText,
                iconResId = R.drawable.ic_check,
                onClick = onEditClick
            ),
            MenuActionItem(
                text = deleteText,
                iconResId = R.drawable.ic_delete,
                onClick = onDeleteClick
            )
        ),
        modifier = modifier
    )
}

@Composable
private fun PlaceBookmarkLoadingContent(
    modifier: Modifier = Modifier
) {
    val skeletonBrush = rememberBaseSkeletonBrush()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 40.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "bookmark_home_loading") {
            PlaceBookmarkHomeSkeleton(shimmerBrush = skeletonBrush)
        }

        item(key = "bookmark_add_loading") {
            BaseSkeletonBlock(
                brush = skeletonBrush,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp)
            )
        }

        repeat(3) { index ->
            item(key = "bookmark_card_loading_$index") {
                PlaceBookmarkCardSkeleton(shimmerBrush = skeletonBrush)
            }
        }
    }
}

@Composable
private fun PlaceBookmarkHomeSkeleton(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        BaseSkeletonBlock(
            brush = shimmerBrush,
            modifier = Modifier
                .fillMaxWidth(0.22f)
                .height(16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        PlaceBookmarkCardSkeleton(shimmerBrush = shimmerBrush)
    }
}

@Composable
private fun PlaceBookmarkCardSkeleton(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Gray50,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(
                PaddingValues(
                    start = 20.dp,
                    top = 14.dp,
                    end = 12.dp,
                    bottom = 14.dp
                )
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BaseSkeletonBlock(
            brush = shimmerBrush,
            modifier = Modifier.size(48.dp),
            shape = CircleShape
        )

        Column(modifier = Modifier.weight(1f)) {
            BaseSkeletonBlock(
                brush = shimmerBrush,
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .height(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
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
private fun PlaceBookmarkListDivider(visible: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(if (visible) Gray200 else Color.Transparent)
    )
}

@Composable
private fun PlaceBookmarkEmptyContent(
    modifier: Modifier = Modifier
) {
    BaseEmptyContent(
        title = stringResource(R.string.place_bookmark_empty_title),
        description = stringResource(R.string.place_bookmark_empty_body),
        modifier = modifier
    )
}

@Preview(showBackground = true, name = "Place Bookmark List With Home")
@Composable
private fun PlaceBookmarkListContentWithHomePreview() {
    PassedPathTheme {
        PlaceBookmarkListContentPreview(
            bookmarkPlaces = listOf(
                previewPlaceBookmark(
                    id = 1L,
                    type = BookmarkPlaceType.HOME,
                    placeName = "우리집",
                    roadAddress = "서울 성북구 정릉로 77"
                ),
                previewPlaceBookmark(
                    id = 2L,
                    type = BookmarkPlaceType.SCHOOL,
                    placeName = "국민대학교 복지관",
                    roadAddress = "서울 성북구 정릉로 77"
                ),
                previewPlaceBookmark(
                    id = 3L,
                    type = BookmarkPlaceType.COMPANY,
                    placeName = "패스드패스 사무실",
                    roadAddress = "서울 종로구 대명길 34 2층"
                ),
                previewPlaceBookmark(
                    id = 4L,
                    type = BookmarkPlaceType.ETC,
                    placeName = "동대문프라임시티2차",
                    roadAddress = "서울 동대문구 왕산로 18"
                )
            )
        )
    }
}

@Preview(showBackground = true, name = "Place Bookmark List Without Home")
@Composable
private fun PlaceBookmarkListContentWithoutHomePreview() {
    PassedPathTheme {
        PlaceBookmarkListContentPreview(
            bookmarkPlaces = listOf(
                previewPlaceBookmark(
                    id = 1L,
                    type = BookmarkPlaceType.SCHOOL,
                    placeName = "국민대학교 복지관",
                    roadAddress = "서울 성북구 정릉로 77"
                ),
                previewPlaceBookmark(
                    id = 2L,
                    type = BookmarkPlaceType.COMPANY,
                    placeName = "성수 공유오피스",
                    roadAddress = "서울 성동구 연무장길 12"
                )
            )
        )
    }
}

@Preview(showBackground = true, name = "Place Bookmark List Empty")
@Composable
private fun PlaceBookmarkListContentEmptyPreview() {
    PassedPathTheme {
        PlaceBookmarkListContentPreview(bookmarkPlaces = emptyList())
    }
}

@Composable
private fun PlaceBookmarkListContentPreview(
    bookmarkPlaces: List<PlaceBookmarkSummary>
) {
    PlaceBookmarkListContent(
        uiState = PlaceBookmarkUiState(
            placeCount = bookmarkPlaces.size,
            hasLoaded = true,
            bookmarkPlaces = bookmarkPlaces
        ),
        openedMenuBookmarkId = null,
        onMenuOpenedChange = {},
        onRegisterHomeClick = {},
        onAddPlaceBookmarkClick = {},
        onEditPlaceBookmarkClick = {},
        onDeletePlaceBookmarkClick = {},
    )
}

private fun previewPlaceBookmark(
    id: Long,
    type: BookmarkPlaceType,
    placeName: String,
    roadAddress: String
): PlaceBookmarkSummary {
    return PlaceBookmarkSummary(
        bookmarkPlaceId = id,
        type = type,
        placeName = placeName,
        roadAddress = roadAddress,
        latitude = 37.6113,
        longitude = 126.9958
    )
}
