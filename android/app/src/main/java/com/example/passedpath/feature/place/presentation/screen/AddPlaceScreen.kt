package com.example.passedpath.feature.place.presentation.screen

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
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
    modifier: Modifier = Modifier
) {
    val shouldShowConfirmButton = uiState.shouldShowResults
    val focusManager = LocalFocusManager.current
    val clearFocus = { focusManager.clearFocus(force = true) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
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
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                PlaceSearchTextField(
                    value = uiState.query,
                    onValueChange = onQueryChanged
                )

                when {
                    uiState.query.isBlank() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clearFocusOnTap(clearFocus)
                        )
                    }

                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clearFocusOnTap(clearFocus),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.shouldShowResults -> {
                        SearchResultList(
                            uiState = uiState,
                            onPlaceSelected = onPlaceSelected,
                            onClearFocus = clearFocus,
                            modifier = Modifier
                                .weight(1f)
                                .clearFocusOnTap(clearFocus)
                        )
                    }

                    else -> {
                        SearchEmptyResult(
                            errorMessage = uiState.errorMessage,
                            modifier = Modifier
                                .weight(1f)
                                .clearFocusOnTap(clearFocus)
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
    onClearFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "검색 결과",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Gray900
        )

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
        }
    }
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
            onAddPlaceClick = {}
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
            onAddPlaceClick = {}
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
            onAddPlaceClick = {}
        )
    }
}

private fun Modifier.clearFocusOnTap(
    onTap: () -> Unit
): Modifier = pointerInput(onTap) {
    awaitEachGesture {
        awaitFirstDown(
            requireUnconsumed = false,
            pass = PointerEventPass.Final
        )
        val up = waitForUpOrCancellation(pass = PointerEventPass.Final)
        if (up != null) {
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
