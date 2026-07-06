package com.example.passedpath.feature.bookmark.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.R
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmarkItem
import com.example.passedpath.feature.bookmark.presentation.component.DayRouteBookmarkListCard
import com.example.passedpath.feature.bookmark.presentation.state.DayRouteBookmarkListUiState
import com.example.passedpath.feature.bookmark.presentation.viewmodel.DayRouteBookmarkListViewModel
import com.example.passedpath.feature.bookmark.presentation.viewmodel.DayRouteBookmarkListViewModelFactory
import com.example.passedpath.ui.component.feedback.BaseEmptyContent
import com.example.passedpath.ui.component.feedback.NetworkFailureBanner
import com.example.passedpath.ui.component.loading.BaseLoadingLine
import com.example.passedpath.ui.component.loading.BaseSkeletonBlock
import com.example.passedpath.ui.component.loading.rememberBaseSkeletonBrush
import com.example.passedpath.ui.theme.Gray50
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun DayRouteBookmarkListRoute(
    onBackClick: () -> Unit,
    onBookmarkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DayRouteBookmarkListViewModel = viewModel(
        factory = DayRouteBookmarkListViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.fetchBookmarks()
    }

    DayRouteBookmarkListScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetryClick = { viewModel.fetchBookmarks(forceRefresh = true) },
        onLoadMore = viewModel::fetchNextBookmarks,
        onBookmarkClick = onBookmarkClick,
        modifier = modifier
    )
}

@Composable
internal fun DayRouteBookmarkListScreen(
    uiState: DayRouteBookmarkListUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onLoadMore: () -> Unit,
    onBookmarkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50)
            .statusBarsPadding()
    ) {
        DayRouteBookmarkListTopBar(onBackClick = onBackClick)

        when {
            uiState.isLoading && !uiState.hasLoaded -> {
                DayRouteBookmarkLoadingList(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            uiState.errorMessage != null && !uiState.hasLoaded -> {
                DayRouteBookmarkInitialError(
                    onRetryClick = onRetryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            uiState.bookmarks.isEmpty() -> {
                DayRouteBookmarkEmptyContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            else -> {
                DayRouteBookmarkLoadedList(
                    uiState = uiState,
                    onRetryClick = onRetryClick,
                    onLoadMore = onLoadMore,
                    onBookmarkClick = onBookmarkClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DayRouteBookmarkListTopBar(
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = stringResource(R.string.calendar_back),
                tint = Gray900,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = stringResource(R.string.calendar_favorite_list),
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleMedium,
            color = Gray900,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun DayRouteBookmarkLoadedList(
    uiState: DayRouteBookmarkListUiState,
    onRetryClick: () -> Unit,
    onLoadMore: () -> Unit,
    onBookmarkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisibleIndex >= layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(listState, uiState.hasNext, uiState.isLoadingMore, uiState.errorMessage) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if (
                    shouldLoad &&
                    uiState.hasNext &&
                    !uiState.isLoadingMore &&
                    uiState.errorMessage == null
                ) {
                    onLoadMore()
                }
            }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(
            start = BookmarkListHorizontalPadding,
            top = 16.dp,
            end = BookmarkListHorizontalPadding,
            bottom = 40.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(
            items = uiState.bookmarks,
            key = { index, bookmark -> "${bookmark.date}:$index" }
        ) { _, bookmark ->
            DayRouteBookmarkListCard(
                bookmark = bookmark,
                onClick = { onBookmarkClick(bookmark.date) }
            )
        }

        if (uiState.isLoadingMore) {
            item(key = "bookmark_list_loading_more") {
                BaseLoadingLine(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }

        if (uiState.errorMessage != null && uiState.hasLoaded && !uiState.isLoadingMore) {
            item(key = "bookmark_list_error") {
                NetworkFailureBanner(
                    retryText = stringResource(R.string.route_retry),
                    onRetryClick = onRetryClick
                )
            }
        }
    }
}

@Composable
private fun DayRouteBookmarkLoadingList(
    modifier: Modifier = Modifier
) {
    val skeletonBrush = rememberBaseSkeletonBrush()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = BookmarkListHorizontalPadding,
            top = 16.dp,
            end = BookmarkListHorizontalPadding,
            bottom = 40.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(5) { index ->
            item(key = "bookmark_list_skeleton_$index") {
                DayRouteBookmarkCardSkeleton(brush = skeletonBrush)
            }
        }
    }
}

@Composable
private fun DayRouteBookmarkCardSkeleton(
    brush: Brush,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = White,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 28.dp, vertical = 16.dp)
    ) {
        BaseSkeletonBlock(
            brush = brush,
            modifier = Modifier
                .width(96.dp)
                .height(13.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        BaseSkeletonBlock(
            brush = brush,
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(18.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        BaseSkeletonBlock(
            brush = brush,
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp),
            shape = RoundedCornerShape(999.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BaseSkeletonBlock(
                brush = brush,
                modifier = Modifier.size(16.dp),
                shape = RoundedCornerShape(999.dp)
            )
            BaseSkeletonBlock(
                brush = brush,
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .height(12.dp)
            )
        }
    }
}

@Composable
private fun DayRouteBookmarkInitialError(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(horizontal = BookmarkListHorizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        NetworkFailureBanner(
            retryText = stringResource(R.string.route_retry),
            onRetryClick = onRetryClick
        )
    }
}

@Composable
private fun DayRouteBookmarkEmptyContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(horizontal = BookmarkListHorizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        BaseEmptyContent(
            title = stringResource(R.string.day_route_bookmark_list_empty_title),
            description = stringResource(R.string.day_route_bookmark_list_empty_body)
        )
    }
}

private val BookmarkListHorizontalPadding = 16.dp

@Preview(
    name = "Day Route Bookmark List",
    showBackground = true,
    widthDp = 393,
    heightDp = 760
)
@Composable
private fun DayRouteBookmarkListScreenPreview() {
    PassedPathTheme {
        DayRouteBookmarkListScreen(
            uiState = DayRouteBookmarkListUiState(
                bookmarkCount = 5,
                bookmarks = listOf(
                    DayRouteBookmarkItem(
                        date = "2026-01-20",
                        title = "수업 듣고 지연이 만나러 혜화 간 날",
                        visitedRegions = listOf("하루 요약의 방문 동네 리스트")
                    ),
                    DayRouteBookmarkItem(
                        date = "2026-01-26",
                        title = "공강에 힐링데이!",
                        visitedRegions = listOf("하루 요약의 방문 동네 리스트")
                    ),
                    DayRouteBookmarkItem(
                        date = "2026-01-26",
                        title = null,
                        visitedRegions = listOf("하루 요약의 방문 동네 리스트")
                    ),
                    DayRouteBookmarkItem(
                        date = "2026-01-20",
                        title = "수업 듣고 지연이 만나러 혜화 간 날",
                        visitedRegions = emptyList()
                    )
                ),
                hasLoaded = true
            ),
            onBackClick = {},
            onRetryClick = {},
            onLoadMore = {},
            onBookmarkClick = {}
        )
    }
}
