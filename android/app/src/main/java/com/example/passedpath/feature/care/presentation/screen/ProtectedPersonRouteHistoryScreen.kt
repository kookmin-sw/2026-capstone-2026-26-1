package com.example.passedpath.feature.care.presentation.screen

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.R
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.care.presentation.component.ProtectedPersonRouteDateCard
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonRouteDateUiState
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonRouteHistoryUiState
import com.example.passedpath.feature.care.presentation.viewmodel.ProtectedPersonRouteHistoryViewModel
import com.example.passedpath.feature.care.presentation.viewmodel.ProtectedPersonRouteHistoryViewModelFactory
import com.example.passedpath.ui.component.feedback.BaseEmptyContent
import com.example.passedpath.ui.component.feedback.NetworkFailureBanner
import com.example.passedpath.ui.component.loading.BaseLoadingLine
import com.example.passedpath.ui.component.loading.BaseSkeletonBlock
import com.example.passedpath.ui.component.loading.rememberBaseSkeletonBrush
import com.example.passedpath.ui.theme.Gray50
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green700
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ProtectedPersonRouteHistoryRoute(
    dependentUserId: Long,
    dependentNickname: String,
    onBackClick: () -> Unit,
    onRouteDateClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProtectedPersonRouteHistoryViewModel = viewModel(
        factory = ProtectedPersonRouteHistoryViewModelFactory(
            appContainer = LocalContext.current.appContainer,
            dependentUserId = dependentUserId,
            dependentNickname = dependentNickname
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.fetchRoutes()
    }

    ProtectedPersonRouteHistoryScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetryClick = { viewModel.fetchRoutes(forceRefresh = true) },
        onLoadMore = viewModel::fetchNextRoutes,
        onRouteDateClick = onRouteDateClick,
        modifier = modifier
    )
}

@Composable
internal fun ProtectedPersonRouteHistoryScreen(
    uiState: ProtectedPersonRouteHistoryUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onLoadMore: () -> Unit,
    onRouteDateClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
            .statusBarsPadding()
    ) {
        ProtectedPersonRouteHistoryTopBar(
            dependentNickname = uiState.dependentNickname,
            onBackClick = onBackClick
        )

        when {
            uiState.isLoading && !uiState.hasLoaded -> {
                ProtectedPersonRouteHistoryLoadingList(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            uiState.errorMessage != null && !uiState.hasLoaded -> {
                ProtectedPersonRouteHistoryInitialError(
                    onRetryClick = onRetryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            uiState.dayRoutes.isEmpty() -> {
                ProtectedPersonRouteHistoryEmptyContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            else -> {
                ProtectedPersonRouteHistoryLoadedList(
                    uiState = uiState,
                    onLoadMore = onLoadMore,
                    onRouteDateClick = onRouteDateClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProtectedPersonRouteHistoryTopBar(
    dependentNickname: String,
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
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Green700)) {
                    append(dependentNickname)
                }
                append(stringResource(R.string.care_route_history_title_suffix))
            },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 44.dp),
            style = MaterialTheme.typography.titleMedium,
            color = Gray900,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProtectedPersonRouteHistoryLoadedList(
    uiState: ProtectedPersonRouteHistoryUiState,
    onLoadMore: () -> Unit,
    onRouteDateClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
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
            start = RouteHistoryHorizontalPadding,
            top = 24.dp,
            end = RouteHistoryHorizontalPadding,
            bottom = 40.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = uiState.dayRoutes,
            key = ProtectedPersonRouteDateUiState::dateKey
        ) { routeDate ->
            ProtectedPersonRouteDateCard(
                dateText = routeDate.dateText,
                outingTimeText = routeDate.outingTimeText,
                enterHomeTimeText = routeDate.enterHomeTimeText,
                outingCountText = routeDate.outingCountText,
                onClick = { onRouteDateClick(routeDate.dateKey) }
            )
        }

        if (uiState.isLoadingMore) {
            item(key = "protected_person_route_history_loading_more") {
                BaseLoadingLine(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }

        if (uiState.errorMessage != null && uiState.hasLoaded && !uiState.isLoadingMore) {
            item(key = "protected_person_route_history_error") {
                NetworkFailureBanner(
                    retryText = stringResource(R.string.route_retry),
                    onRetryClick = onLoadMore
                )
            }
        }
    }
}

@Composable
private fun ProtectedPersonRouteHistoryLoadingList(
    modifier: Modifier = Modifier
) {
    val skeletonBrush = rememberBaseSkeletonBrush()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = RouteHistoryHorizontalPadding,
            top = 24.dp,
            end = RouteHistoryHorizontalPadding,
            bottom = 40.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) { index ->
            item(key = "protected_person_route_history_skeleton_$index") {
                ProtectedPersonRouteHistoryCardSkeleton(brush = skeletonBrush)
            }
        }
    }
}

@Composable
private fun ProtectedPersonRouteHistoryCardSkeleton(
    brush: Brush,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 98.dp)
            .background(
                color = Gray50,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BaseSkeletonBlock(
                brush = brush,
                modifier = Modifier
                    .width(72.dp)
                    .height(18.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            BaseSkeletonBlock(
                brush = brush,
                modifier = Modifier.size(width = 7.dp, height = 12.dp),
                shape = RoundedCornerShape(999.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                Column(modifier = Modifier.weight(1f)) {
                    BaseSkeletonBlock(
                        brush = brush,
                        modifier = Modifier
                            .width(42.dp)
                            .height(12.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BaseSkeletonBlock(
                        brush = brush,
                        modifier = Modifier
                            .width(48.dp)
                            .height(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtectedPersonRouteHistoryInitialError(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(horizontal = RouteHistoryHorizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        NetworkFailureBanner(
            retryText = stringResource(R.string.route_retry),
            onRetryClick = onRetryClick
        )
    }
}

@Composable
private fun ProtectedPersonRouteHistoryEmptyContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(horizontal = RouteHistoryHorizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        BaseEmptyContent(
            title = stringResource(R.string.care_route_history_empty_title),
            description = stringResource(R.string.care_route_history_empty_body)
        )
    }
}

private val RouteHistoryHorizontalPadding = 24.dp

@Preview(
    name = "Protected Person Route History",
    showBackground = true,
    widthDp = 393,
    heightDp = 760
)
@Composable
private fun ProtectedPersonRouteHistoryScreenPreview() {
    PassedPathTheme {
        ProtectedPersonRouteHistoryScreen(
            uiState = ProtectedPersonRouteHistoryUiState(
                dependentNickname = "\uB538\uD61C\uC6D0",
                dayRoutes = listOf(
                    ProtectedPersonRouteDateUiState(
                        dateKey = "2026-04-03",
                        dateText = "4\uC6D4 3\uC77C",
                        outingTimeText = "09:12",
                        enterHomeTimeText = "23:40",
                        outingCountText = "3\uD68C"
                    ),
                    ProtectedPersonRouteDateUiState(
                        dateKey = "2026-03-20",
                        dateText = "3\uC6D4 20\uC77C",
                        outingTimeText = "09:12",
                        enterHomeTimeText = "23:40",
                        outingCountText = "1\uD68C"
                    ),
                    ProtectedPersonRouteDateUiState(
                        dateKey = "2026-03-16",
                        dateText = "3\uC6D4 16\uC77C",
                        outingTimeText = "10:05",
                        enterHomeTimeText = "21:30",
                        outingCountText = "2\uD68C"
                    )
                ),
                hasLoaded = true
            ),
            onBackClick = {},
            onRetryClick = {},
            onLoadMore = {},
            onRouteDateClick = {}
        )
    }
}
