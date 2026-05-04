package com.example.passedpath.feature.placebookmark.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.R
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.place.domain.model.PlaceSearchResult
import com.example.passedpath.feature.place.presentation.screen.PlaceSearchSelectionScreen
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmarkSummary
import com.example.passedpath.feature.placebookmark.presentation.component.PlaceBookmarkBadge
import com.example.passedpath.feature.placebookmark.presentation.component.PlaceBookmarkCard
import com.example.passedpath.feature.placebookmark.presentation.state.PlaceBookmarkUiState
import com.example.passedpath.feature.placebookmark.presentation.viewmodel.PlaceBookmarkViewModel
import com.example.passedpath.feature.placebookmark.presentation.viewmodel.PlaceBookmarkViewModelFactory
import com.example.passedpath.ui.component.button.BaseButton
import com.example.passedpath.ui.component.input.BaseInputButton
import com.example.passedpath.ui.component.input.BaseInputField
import com.example.passedpath.ui.component.loading.BaseLoadingIndicator
import com.example.passedpath.ui.component.menu.MenuActionItem
import com.example.passedpath.ui.component.modal.PassedPathBottomModal
import com.example.passedpath.ui.component.toast.ToastOverlayHost
import com.example.passedpath.ui.component.toast.ToastOverlayItem
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray200
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green50
import com.example.passedpath.ui.theme.Green100
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White
import kotlinx.coroutines.flow.distinctUntilChanged

data class PlaceBookmarkSearchResultEvent(
    val id: Int,
    val place: PlaceSearchResult
)

@Composable
fun PlaceBookmarkRoute(
    onBackClick: () -> Unit,
    onNavigateToPlaceBookmarkSearch: () -> Unit,
    searchResultEvent: PlaceBookmarkSearchResultEvent? = null,
    onSearchResultEventConsumed: (Int) -> Unit = {},
    viewModel: PlaceBookmarkViewModel = viewModel(
        factory = PlaceBookmarkViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.fetchPlaceBookmarks()
    }

    PlaceBookmarkScreen(
        uiState = uiState,
        searchResultEvent = searchResultEvent,
        onSearchResultEventConsumed = onSearchResultEventConsumed,
        onBackClick = onBackClick,
        onAddPlaceBookmarkClick = onNavigateToPlaceBookmarkSearch,
        onRetryClick = viewModel::fetchPlaceBookmarks,
        onCreatePlaceBookmark = viewModel::createPlaceBookmark,
        onFeedbackDismissed = viewModel::consumeFeedback
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun PlaceBookmarkScreen(
    uiState: PlaceBookmarkUiState,
    searchResultEvent: PlaceBookmarkSearchResultEvent?,
    onSearchResultEventConsumed: (Int) -> Unit,
    onBackClick: () -> Unit,
    onAddPlaceBookmarkClick: () -> Unit,
    onRetryClick: () -> Unit,
    onCreatePlaceBookmark: (BookmarkPlaceType, String, String, Double, Double) -> Unit,
    onFeedbackDismissed: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var openedMenuBookmarkId by rememberSaveable { mutableStateOf<Long?>(null) }
    var isAddSheetVisible by rememberSaveable { mutableStateOf(false) }
    var addPlaceName by rememberSaveable { mutableStateOf("") }
    var addRoadAddress by rememberSaveable { mutableStateOf("") }
    var addLatitude by rememberSaveable { mutableStateOf(0.0) }
    var addLongitude by rememberSaveable { mutableStateOf(0.0) }
    var selectedType by rememberSaveable { mutableStateOf(BookmarkPlaceType.ETC) }
    var isNameFocused by rememberSaveable { mutableStateOf(false) }
    var submittedFeedbackEventId by rememberSaveable { mutableStateOf<Long?>(null) }
    var isAddSearchVisible by rememberSaveable { mutableStateOf(false) }
    var shouldRenderAddSearch by rememberSaveable { mutableStateOf(false) }
    var addSearchSessionId by rememberSaveable { mutableStateOf(0) }
    val feedbackMessage = uiState.errorMessage ?: uiState.successMessage

    fun clearInputFocus() {
        isNameFocused = false
    }

    fun dismissAddSheet() {
        isAddSheetVisible = false
        addPlaceName = ""
        addRoadAddress = ""
        addLatitude = 0.0
        addLongitude = 0.0
        selectedType = BookmarkPlaceType.ETC
        submittedFeedbackEventId = null
        isAddSearchVisible = false
        shouldRenderAddSearch = false
        clearInputFocus()
    }

    fun openAddSheet(place: PlaceSearchResult) {
        addPlaceName = place.name
        addRoadAddress = place.displayAddress
        addLatitude = place.latitude
        addLongitude = place.longitude
        selectedType = BookmarkPlaceType.ETC
        isAddSheetVisible = true
        isAddSearchVisible = false
        shouldRenderAddSearch = false
        clearInputFocus()
    }

    fun showAddSearch() {
        addSearchSessionId += 1
        shouldRenderAddSearch = true
        isAddSearchVisible = true
        clearInputFocus()
    }

    fun hideAddSearch() {
        isAddSearchVisible = false
    }

    fun removeAddSearch() {
        shouldRenderAddSearch = false
    }

    fun applyAddSearchResult(place: PlaceSearchResult) {
        addPlaceName = place.name
        addRoadAddress = place.displayAddress
        addLatitude = place.latitude
        addLongitude = place.longitude
        hideAddSearch()
    }

    LaunchedEffect(searchResultEvent?.id) {
        val event = searchResultEvent ?: return@LaunchedEffect
        openAddSheet(event.place)
        onSearchResultEventConsumed(event.id)
    }

    LaunchedEffect(
        submittedFeedbackEventId,
        uiState.isSubmitting,
        uiState.feedbackEventId,
        uiState.successMessage,
        uiState.errorMessage
    ) {
        val startEventId = submittedFeedbackEventId ?: return@LaunchedEffect
        if (uiState.isSubmitting) return@LaunchedEffect
        if (uiState.feedbackEventId == startEventId) return@LaunchedEffect

        when {
            uiState.successMessage != null -> dismissAddSheet()
            uiState.errorMessage != null -> submittedFeedbackEventId = null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.place_bookmark_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = null,
                                tint = Gray900
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
            PlaceBookmarkListContent(
                uiState = uiState,
                openedMenuBookmarkId = openedMenuBookmarkId,
                onMenuOpenedChange = { openedMenuBookmarkId = it },
                onAddPlaceBookmarkClick = onAddPlaceBookmarkClick,
                onRetryClick = onRetryClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }

        ToastOverlayHost(
            toasts = buildList {
                feedbackMessage?.let { message ->
                    add(
                        ToastOverlayItem(
                            message = message,
                            triggerKey = "place-bookmark:${uiState.feedbackEventId}:$message",
                            onDismissed = { onFeedbackDismissed(uiState.feedbackEventId) }
                        )
                    )
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (isAddSheetVisible) {
            PlaceBookmarkAddOverlay(
                placeName = addPlaceName,
                roadAddress = addRoadAddress,
                selectedType = selectedType,
                isSubmitting = uiState.isSubmitting,
                isNameFocused = isNameFocused,
                onPlaceNameChange = { addPlaceName = it },
                onNameFocusChanged = { isNameFocused = it },
                onTypeSelected = { selectedType = it },
                onClearInputFocus = ::clearInputFocus,
                onAddressClick = ::showAddSearch,
                onDismiss = ::dismissAddSheet,
                onSubmit = {
                    submittedFeedbackEventId = uiState.feedbackEventId
                    onCreatePlaceBookmark(
                        selectedType,
                        addPlaceName,
                        addRoadAddress,
                        addLatitude,
                        addLongitude
                    )
                }
            )
        }

        if (isAddSheetVisible && shouldRenderAddSearch) {
            PlaceBookmarkAddSearchOverlay(
                visible = isAddSearchVisible,
                viewModelKey = "place-bookmark-add-search-$addSearchSessionId",
                onBackClick = ::hideAddSearch,
                onPlaceSelected = ::applyAddSearchResult,
                onDismissed = ::removeAddSearch
            )
        }
    }
}

@Composable
private fun PlaceBookmarkListContent(
    uiState: PlaceBookmarkUiState,
    openedMenuBookmarkId: Long?,
    onMenuOpenedChange: (Long?) -> Unit,
    onAddPlaceBookmarkClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
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
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                BaseLoadingIndicator()
            }
        }

        uiState.errorMessage != null && !uiState.hasLoaded -> {
            PlaceBookmarkErrorContent(
                message = uiState.errorMessage,
                onRetryClick = onRetryClick,
                modifier = modifier
            )
        }

        else -> {
            Column(
                modifier = modifier
            ) {
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
                    item(key = "header") {
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
                        Spacer(modifier = Modifier.height(24.dp))
                        PlaceBookmarkCountText(placeCount = uiState.placeCount)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (uiState.bookmarkPlaces.isEmpty()) {
                        item(key = "empty") {
                            PlaceBookmarkEmptyContent(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 120.dp)
                            )
                        }
                    } else {
                        items(
                            items = uiState.bookmarkPlaces,
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
                                onDismissMenu = { onMenuOpenedChange(null) }
                            )
                        }
                    }
                }
            }
        }
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
private fun PlaceBookmarkCountText(
    placeCount: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = buildAnnotatedString {
            append(stringResource(R.string.place_bookmark_count_prefix))
            withStyle(
                SpanStyle(
                    color = Green500,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(stringResource(R.string.place_bookmark_count_suffix, placeCount))
            }
        },
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 14.sp,
        color = Gray400,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun PlaceBookmarkListItem(
    placeBookmark: PlaceBookmarkSummary,
    isMenuVisible: Boolean,
    onMoreClick: () -> Unit,
    onDismissMenu: () -> Unit,
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
                onClick = onDismissMenu
            ),
            MenuActionItem(
                text = deleteText,
                iconResId = R.drawable.ic_trash,
                onClick = onDismissMenu
            )
        ),
        modifier = modifier
    )
}

@Composable
private fun PlaceBookmarkErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.place_bookmark_error_title),
            style = MaterialTheme.typography.titleMedium,
            color = Gray900,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500
        )
        Spacer(modifier = Modifier.height(20.dp))
        BaseButton(
            text = stringResource(R.string.route_retry),
            onClick = onRetryClick
        )
    }
}

@Composable
private fun PlaceBookmarkEmptyContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_information),
            contentDescription = null,
            tint = Gray400,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.place_bookmark_empty_title),
            style = MaterialTheme.typography.bodyLarge,
            color = Gray900,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.place_bookmark_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = Gray400
        )
    }
}

@Composable
private fun PlaceBookmarkAddOverlay(
    placeName: String,
    roadAddress: String,
    selectedType: BookmarkPlaceType,
    isSubmitting: Boolean,
    isNameFocused: Boolean,
    onPlaceNameChange: (String) -> Unit,
    onNameFocusChanged: (Boolean) -> Unit,
    onTypeSelected: (BookmarkPlaceType) -> Unit,
    onClearInputFocus: () -> Unit,
    onAddressClick: () -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    PassedPathBottomModal(
        onDimClick = onClearInputFocus,
        modifier = modifier,
        onBackPress = {
            if (isNameFocused) {
                onClearInputFocus()
            } else {
                onClearInputFocus()
                onDismiss()
            }
        }
    ) {
        PlaceBookmarkAddBottomSheet(
            placeName = placeName,
            roadAddress = roadAddress,
            selectedType = selectedType,
            isSubmitting = isSubmitting,
            onPlaceNameChange = onPlaceNameChange,
            onNameFocusChanged = onNameFocusChanged,
            onTypeSelected = onTypeSelected,
            onClearInputFocus = onClearInputFocus,
            onAddressClick = onAddressClick,
            onDismiss = onDismiss,
            onSubmit = onSubmit
        )
    }
}

@Composable
private fun PlaceBookmarkAddSearchOverlay(
    visible: Boolean,
    viewModelKey: String,
    onBackClick: () -> Unit,
    onPlaceSelected: (PlaceSearchResult) -> Unit,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleState = remember {
        MutableTransitionState(false).apply {
            targetState = visible
        }
    }

    LaunchedEffect(visible) {
        visibleState.targetState = visible
    }

    LaunchedEffect(visible, visibleState.currentState, visibleState.isIdle) {
        if (!visible && visibleState.isIdle && !visibleState.currentState) {
            onDismissed()
        }
    }

    Dialog(
        onDismissRequest = onBackClick,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            modifier = modifier.fillMaxSize(),
            enter = slideInHorizontally(
                animationSpec = tween(durationMillis = PlaceBookmarkAddSearchEnterTransitionMillis),
                initialOffsetX = { fullWidth -> fullWidth }
            ) + fadeIn(animationSpec = tween(durationMillis = PlaceBookmarkAddSearchEnterTransitionMillis)),
            exit = slideOutHorizontally(
                animationSpec = tween(durationMillis = PlaceBookmarkAddSearchExitTransitionMillis),
                targetOffsetX = { fullWidth -> fullWidth }
            ) + fadeOut(animationSpec = tween(durationMillis = PlaceBookmarkAddSearchExitTransitionMillis))
        ) {
            PlaceSearchSelectionScreen(
                dateKey = "",
                title = stringResource(R.string.place_search_title),
                confirmButtonText = stringResource(R.string.place_search_edit_confirm),
                onBackClick = onBackClick,
                onPlaceSelected = onPlaceSelected,
                modifier = Modifier.fillMaxSize(),
                viewModelKey = viewModelKey
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PlaceBookmarkAddBottomSheet(
    placeName: String,
    roadAddress: String,
    selectedType: BookmarkPlaceType,
    isSubmitting: Boolean,
    onPlaceNameChange: (String) -> Unit,
    onNameFocusChanged: (Boolean) -> Unit,
    onTypeSelected: (BookmarkPlaceType) -> Unit,
    onClearInputFocus: () -> Unit,
    onAddressClick: () -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canSubmit = placeName.trim().isNotBlank() &&
        roadAddress.trim().isNotBlank() &&
        !isSubmitting
    val sheetInteractionSource = remember { MutableInteractionSource() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun clearSheetInputFocus() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        onClearInputFocus()
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = White,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .clickable(
                    interactionSource = sheetInteractionSource,
                    indication = null,
                    onClick = ::clearSheetInputFocus
                )
                .padding(start = 20.dp, top = 26.dp, end = 20.dp, bottom = 40.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.place_bookmark_add_title),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleMedium,
                    color = Gray900,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                IconButton(
                    onClick = {
                        clearSheetInputFocus()
                        onDismiss()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            PlaceBookmarkFieldLabel(text = stringResource(R.string.place_bookmark_name_label))
            Spacer(modifier = Modifier.height(10.dp))
            BaseInputField(
                value = placeName,
                onValueChange = onPlaceNameChange,
                placeholder = stringResource(R.string.place_bookmark_name_label),
                singleLine = true,
                imeAction = ImeAction.Done,
                onFocusChanged = onNameFocusChanged,
                onImeAction = ::clearSheetInputFocus
            )
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = stringResource(R.string.place_bookmark_name_helper),
                style = MaterialTheme.typography.bodySmall,
                color = Green500,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(22.dp))
            PlaceBookmarkFieldLabel(text = stringResource(R.string.place_bookmark_address_label))
            Spacer(modifier = Modifier.height(10.dp))
            BaseInputButton(
                text = roadAddress,
                onClick = {
                    clearSheetInputFocus()
                    onAddressClick()
                },
                placeholder = stringResource(R.string.place_bookmark_address_label),
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = null,
                        tint = Green500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(22.dp))
            PlaceBookmarkFieldLabel(text = stringResource(R.string.place_bookmark_type_label))
            Spacer(modifier = Modifier.height(12.dp))
            PlaceBookmarkTypeSelector(
                selectedType = selectedType,
                onTypeSelected = { type ->
                    clearSheetInputFocus()
                    onTypeSelected(type)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
            BaseButton(
                text = stringResource(R.string.place_bookmark_add_submit),
                onClick = {
                    clearSheetInputFocus()
                    onSubmit()
                },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            )
        }
    }
}

@Composable
private fun PlaceBookmarkFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Gray400,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )
}

@Composable
private fun PlaceBookmarkTypeSelector(
    selectedType: BookmarkPlaceType,
    onTypeSelected: (BookmarkPlaceType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BookmarkPlaceType.entries.forEach { type ->
            PlaceBookmarkTypeOption(
                type = type,
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlaceBookmarkTypeOption(
    type: BookmarkPlaceType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Green50 else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) Green100 else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (selected) Green500 else Gray200,
                    shape = CircleShape
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            PlaceBookmarkBadge(
                type = type,
                size = 36.dp,
                iconSize = 18.dp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = type.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) Green500 else Gray400,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

private val BookmarkPlaceType.displayName: String
    get() = when (this) {
        BookmarkPlaceType.HOME -> "집"
        BookmarkPlaceType.COMPANY -> "회사"
        BookmarkPlaceType.SCHOOL -> "학교"
        BookmarkPlaceType.ETC -> "기타"
}

private const val PlaceBookmarkAddSearchEnterTransitionMillis = 250
private const val PlaceBookmarkAddSearchExitTransitionMillis = 230

@Preview(showBackground = true)
@Composable
private fun PlaceBookmarkScreenPreview() {
    PassedPathTheme {
        PlaceBookmarkScreen(
            uiState = PlaceBookmarkUiState(
                placeCount = 3,
                hasLoaded = true,
                bookmarkPlaces = listOf(
                    previewPlaceBookmark(1L, BookmarkPlaceType.SCHOOL),
                    previewPlaceBookmark(2L, BookmarkPlaceType.COMPANY),
                    previewPlaceBookmark(3L, BookmarkPlaceType.HOME)
                )
            ),
            searchResultEvent = null,
            onSearchResultEventConsumed = {},
            onBackClick = {},
            onAddPlaceBookmarkClick = {},
            onRetryClick = {},
            onCreatePlaceBookmark = { _, _, _, _, _ -> },
            onFeedbackDismissed = {}
        )
    }
}

private fun previewPlaceBookmark(
    id: Long,
    type: BookmarkPlaceType
): PlaceBookmarkSummary {
    return PlaceBookmarkSummary(
        bookmarkPlaceId = id,
        type = type,
        placeName = "국민대학교 복지관",
        roadAddress = "서울 성북구 정릉로 77",
        latitude = 37.6113,
        longitude = 126.9958
    )
}
