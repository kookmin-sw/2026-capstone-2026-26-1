package com.example.passedpath.feature.place.presentation.screen

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.place.domain.model.PlaceSearchResult
import com.example.passedpath.feature.place.presentation.state.AddPlaceUiState
import com.example.passedpath.feature.place.presentation.viewmodel.AddPlaceViewModel
import com.example.passedpath.feature.place.presentation.viewmodel.AddPlaceViewModelFactory
import com.example.passedpath.ui.component.button.BaseButton
import com.example.passedpath.feature.place.presentation.component.PlaceSearchCard
import com.example.passedpath.feature.place.presentation.component.PlaceSearchTextField
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun AddPlaceScreen(
    dateKey: String,
    onBackClick: () -> Unit,
    onPlaceCreated: () -> Unit,
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
        viewModel.placeCreated.collect {
            onPlaceCreated()
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
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "\uC7A5\uC18C \uCD94\uAC00\uD558\uAE30",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "\uB4A4\uB85C\uAC00\uAE30"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Gray100
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Gray100
            ) {
                BaseButton(
                    text = "\uC774 \uC7A5\uC18C \uCD94\uAC00\uD558\uAE30",
                    onClick = onAddPlaceClick,
                    enabled = uiState.canConfirmPlace,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
            }
        },
        containerColor = Gray100
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
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
    }
}

@Composable
private fun SearchResultList(
    uiState: AddPlaceUiState,
    onPlaceSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "\uAC80\uC0C9 \uACB0\uACFC",
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
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(
                items = uiState.places,
                key = PlaceSearchResult::stableKey
            ) { place ->
                PlaceSearchCard(
                    title = place.name,
                    address = place.displayAddress,
                    isSelected = place.stableKey == uiState.selectedPlaceId,
                    category = place.category,
                    onClick = { onPlaceSelected(place.stableKey) }
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
            text = errorMessage ?: "\uAC80\uC0C9 \uACB0\uACFC\uAC00 \uC5C6\uC5B4\uC694",
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
            modifier = Modifier.background(Gray100)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPlaceScreenSelectedPreview() {
    PassedPathTheme {
        val places = listOf(
            PlaceSearchResult(
                id = "1",
                name = "Kookmin University Main Hall",
                category = "University",
                roadAddress = "77 Jeongneung-ro, Seongbuk-gu, Seoul",
                address = "Jeongneung-dong, Seongbuk-gu, Seoul",
                latitude = 37.6100,
                longitude = 126.9970
            ),
            PlaceSearchResult(
                id = "2",
                name = "Kookmin University Library",
                category = "Library",
                roadAddress = "77 Jeongneung-ro, Seongbuk-gu, Seoul",
                address = "Jeongneung-dong, Seongbuk-gu, Seoul",
                latitude = 37.6110,
                longitude = 126.9960
            )
        )
        AddPlaceScreenContent(
            uiState = AddPlaceUiState(
                query = "Kookmin",
                places = places,
                selectedPlaceId = places.first().stableKey
            ),
            onBackClick = {},
            onQueryChanged = {},
            onPlaceSelected = {},
            onAddPlaceClick = {}
        )
    }
}
