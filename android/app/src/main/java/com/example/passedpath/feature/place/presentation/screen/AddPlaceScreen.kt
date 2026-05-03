package com.example.passedpath.feature.place.presentation.screen

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.R
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.place.domain.model.PlaceSearchResult
import com.example.passedpath.feature.place.presentation.component.PlaceSearchResultCard
import com.example.passedpath.feature.place.presentation.component.PlaceSearchTextField
import com.example.passedpath.feature.place.presentation.state.AddPlaceUiState
import com.example.passedpath.feature.place.presentation.viewmodel.AddPlaceViewModel
import com.example.passedpath.feature.place.presentation.viewmodel.AddPlaceViewModelFactory
import com.example.passedpath.ui.component.button.BaseButton
import com.example.passedpath.ui.theme.Black
import com.example.passedpath.ui.theme.Gray200
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White

@Composable
fun AddPlaceScreen(
    dateKey: String,
    onBackClick: () -> Unit,
    onPlaceCreated: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddPlaceViewModel = viewModel(
        factory = AddPlaceViewModelFactory(
            appContainer = androidx.compose.ui.platform.LocalContext.current.appContainer,
            dateKey = dateKey
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.placeCreated.collect { placeId ->
            onPlaceCreated(placeId)
        }
    }

    AddPlaceScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onQueryChanged = viewModel::onQueryChanged,
        onPlaceSelected = viewModel::onPlaceSelected,
        onAddPlaceClick = viewModel::onAddPlaceClicked,
        onLoadNextPage = viewModel::onLoadNextPage,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPlaceScreenContent(
    uiState: AddPlaceUiState,
    onBackClick: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onPlaceSelected: (String) -> Unit,
    onAddPlaceClick: () -> Unit,
    onLoadNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShowConfirmButton = uiState.shouldShowResults
    val focusManager = LocalFocusManager.current
    val clearFocus = { focusManager.clearFocus(force = true) }
    var searchFieldBounds by remember { mutableStateOf<Rect?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .clearFocusOutsideBoundsOnTap(
                excludedBounds = searchFieldBounds,
                onTap = clearFocus
            )
            .imePadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "장소 추가하기",
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "뒤로가기",
                            tint = Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = White
                )
            )
        },
        containerColor = White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlaceSearchTextField(
                    value = uiState.query,
                    onValueChange = onQueryChanged,
                    onImeDone = clearFocus,
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        searchFieldBounds = coordinates.boundsInRoot()
                    }
                )

                when {
                    uiState.query.isBlank() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }

                    uiState.isAwaitingFirstSearch -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }

                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.shouldShowResults -> {
                        SearchResultList(
                            uiState = uiState,
                            onPlaceSelected = onPlaceSelected,
                            onLoadNextPage = onLoadNextPage,
                            onClearFocus = clearFocus,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    else -> {
                        SearchEmptyResult(
                            errorMessage = uiState.errorMessage,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (shouldShowConfirmButton) {
                BaseButton(
                    text = "이 장소 추가하기",
                    onClick = {
                        clearFocus()
                        onAddPlaceClick()
                    },
                    enabled = uiState.canConfirmPlace,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchResultList(
    uiState: AddPlaceUiState,
    onPlaceSelected: (String) -> Unit,
    onLoadNextPage: () -> Unit,
    onClearFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val shouldLoadNextPage by remember(
        uiState.places,
        uiState.isLoading,
        uiState.isLoadingNextPage,
        uiState.isEnd
    ) {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            uiState.places.isNotEmpty() &&
                !uiState.isLoading &&
                !uiState.isLoadingNextPage &&
                !uiState.isEnd &&
                lastVisibleIndex >= uiState.places.lastIndex - 2
        }
    }
    val isResultScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 0
        }
    }

    LaunchedEffect(shouldLoadNextPage) {
        if (shouldLoadNextPage) {
            onLoadNextPage()
        }
    }

    Column(
        modifier = modifier
    ) {


        Spacer(modifier = Modifier.height(4.dp))
        SearchResultDivider(visible = isResultScrolled)

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(
                items = uiState.places,
                key = PlaceSearchResult::stableKey
            ) { place ->
                PlaceSearchResultCard(
                    title = place.name,
                    address = place.displayAddress,
                    isSelected = place.stableKey == uiState.selectedPlaceId,
                    category = place.category,
                    onClick = {
                        onClearFocus()
                        onPlaceSelected(place.stableKey)
                    }
                )
            }

            if (uiState.isLoadingNextPage) {
                item(key = "next_page_loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultDivider(visible: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .drawBehind {
                val horizontalOverflow = 16.dp.toPx()
                drawRect(
                    color = if (visible) Gray200 else Color.Transparent,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x = -horizontalOverflow,
                        y = 0f
                    ),
                    size = Size(
                        width = size.width + horizontalOverflow * 2,
                        height = size.height
                    )
                )
            }
    )
}

@Composable
private fun SearchEmptyResult(
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = errorMessage ?: "검색 결과가 없어요",
            style = MaterialTheme.typography.bodyMedium,
            color = errorMessage?.let { MaterialTheme.colorScheme.error } ?: Gray500,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPlaceScreenInitialPreview() {
    PassedPathTheme {
        AddPlaceScreenContent(
            uiState = AddPlaceUiState(),
            onBackClick = {},
            onQueryChanged = {},
            onPlaceSelected = {},
            onAddPlaceClick = {},
            onLoadNextPage = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPlaceScreenResultsPreview() {
    PassedPathTheme {
        val places = previewPlaceSearchResults()
        AddPlaceScreenContent(
            uiState = AddPlaceUiState(
                query = "국민대학교",
                places = places
            ),
            onBackClick = {},
            onQueryChanged = {},
            onPlaceSelected = {},
            onAddPlaceClick = {},
            onLoadNextPage = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPlaceScreenConfirmEnabledPreview() {
    PassedPathTheme {
        val places = previewPlaceSearchResults()
        AddPlaceScreenContent(
            uiState = AddPlaceUiState(
                query = "국민대학교",
                places = places,
                selectedPlaceId = places[2].stableKey
            ),
            onBackClick = {},
            onQueryChanged = {},
            onPlaceSelected = {},
            onAddPlaceClick = {},
            onLoadNextPage = {}
        )
    }
}

private fun Modifier.clearFocusOutsideBoundsOnTap(
    excludedBounds: Rect?,
    onTap: () -> Unit
): Modifier = pointerInput(excludedBounds, onTap) {
    awaitEachGesture {
        val down = awaitFirstDown(
            requireUnconsumed = false,
            pass = PointerEventPass.Final
        )
        val up = waitForUpOrCancellation(pass = PointerEventPass.Final)
        if (up != null && excludedBounds?.contains(down.position) != true) {
            onTap()
        }
    }
}

private fun previewPlaceSearchResults(): List<PlaceSearchResult> {
    return listOf(
        PlaceSearchResult(
            id = "1",
            name = "국민대학교 본부관",
            category = "",
            roadAddress = "서울특별시 성북구 정릉로 77",
            address = "서울특별시 성북구 정릉동",
            latitude = 37.6100,
            longitude = 126.9970
        ),
        PlaceSearchResult(
            id = "2",
            name = "국민대학교 정문",
            category = "",
            roadAddress = "서울특별시 성북구 정릉동 861-1",
            address = "서울특별시 성북구 정릉동",
            latitude = 37.6110,
            longitude = 126.9960
        ),
        PlaceSearchResult(
            id = "3",
            name = "국민대학교 경영관",
            category = "",
            roadAddress = "서울특별시 성북구 정릉로 77",
            address = "서울특별시 성북구 정릉동",
            latitude = 37.6120,
            longitude = 126.9950
        ),
        PlaceSearchResult(
            id = "4",
            name = "국민대학교 본부관",
            category = "",
            roadAddress = "서울특별시 성북구 정릉로 77",
            address = "서울특별시 성북구 정릉동",
            latitude = 37.6130,
            longitude = 126.9940
        ),
        PlaceSearchResult(
            id = "5",
            name = "국민대학교 정문",
            category = "",
            roadAddress = "서울특별시 성북구 정릉동 861-1",
            address = "서울특별시 성북구 정릉동",
            latitude = 37.6140,
            longitude = 126.9930
        ),
        PlaceSearchResult(
            id = "6",
            name = "국민대학교 경영관",
            category = "",
            roadAddress = "서울특별시 성북구 정릉로 77",
            address = "서울특별시 성북구 정릉동",
            latitude = 37.6150,
            longitude = 126.9920
        ),
        PlaceSearchResult(
            id = "7",
            name = "국민대학교 과학관",
            category = "",
            roadAddress = "서울특별시 성북구 정릉로 77",
            address = "서울특별시 성북구 정릉동",
            latitude = 37.6160,
            longitude = 126.9910
        ),
        PlaceSearchResult(
            id = "8",
            name = "국민대학교 북악관",
            category = "",
            roadAddress = "서울특별시 성북구 정릉로 77",
            address = "서울특별시 성북구 정릉동",
            latitude = 37.6170,
            longitude = 126.9900
        )
    )
}
