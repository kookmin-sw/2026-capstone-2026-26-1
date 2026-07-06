package com.example.passedpath.feature.placebookmark.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.R
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.place.domain.model.PlaceSearchResult
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmarkSummary
import com.example.passedpath.feature.placebookmark.presentation.component.PlaceBookmarkDeleteConfirmDialog
import com.example.passedpath.feature.placebookmark.presentation.component.PlaceBookmarkFormOverlay
import com.example.passedpath.feature.placebookmark.presentation.component.PlaceBookmarkFormSearchOverlay
import com.example.passedpath.feature.placebookmark.presentation.component.PlaceBookmarkListContent
import com.example.passedpath.feature.placebookmark.presentation.state.PlaceBookmarkUiState
import com.example.passedpath.feature.placebookmark.presentation.viewmodel.PlaceBookmarkViewModel
import com.example.passedpath.feature.placebookmark.presentation.viewmodel.PlaceBookmarkViewModelFactory
import com.example.passedpath.ui.component.toast.ToastOverlayHost
import com.example.passedpath.ui.component.toast.ToastOverlayItem
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White

data class PlaceBookmarkSearchResultEvent(
    val id: Int,
    val place: PlaceSearchResult
)

private enum class PlaceBookmarkFormMode {
    ADD,
    EDIT
}

@Composable
fun PlaceBookmarkRoute(
    onBackClick: () -> Unit,
    onNavigateToPlaceBookmarkSearch: () -> Unit,
    onPlaceBookmarkChanged: (Long) -> Unit = {},
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

    LaunchedEffect(viewModel) {
        viewModel.placeBookmarkChanged.collect { bookmarkPlaceId ->
            onPlaceBookmarkChanged(bookmarkPlaceId)
        }
    }

    PlaceBookmarkScreen(
        uiState = uiState,
        searchResultEvent = searchResultEvent,
        onSearchResultEventConsumed = onSearchResultEventConsumed,
        onBackClick = onBackClick,
        onAddPlaceBookmarkClick = onNavigateToPlaceBookmarkSearch,
        onCreatePlaceBookmark = viewModel::createPlaceBookmark,
        onUpdatePlaceBookmark = viewModel::updatePlaceBookmark,
        onDeletePlaceBookmark = viewModel::deletePlaceBookmark,
        onFeedbackDismissed = viewModel::consumeFeedback
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
internal fun PlaceBookmarkScreen(
    uiState: PlaceBookmarkUiState,
    searchResultEvent: PlaceBookmarkSearchResultEvent?,
    onSearchResultEventConsumed: (Int) -> Unit,
    onBackClick: () -> Unit,
    onAddPlaceBookmarkClick: () -> Unit,
    onCreatePlaceBookmark: (BookmarkPlaceType, String, String, Double, Double) -> Unit,
    onUpdatePlaceBookmark: (Long, BookmarkPlaceType, String, String, Double, Double) -> Unit,
    onDeletePlaceBookmark: (Long) -> Unit,
    onFeedbackDismissed: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var openedMenuBookmarkId by rememberSaveable { mutableStateOf<Long?>(null) }
    var formMode by rememberSaveable { mutableStateOf<PlaceBookmarkFormMode?>(null) }
    var editingBookmarkId by rememberSaveable { mutableStateOf<Long?>(null) }
    var originalPlaceName by rememberSaveable { mutableStateOf("") }
    var originalRoadAddress by rememberSaveable { mutableStateOf("") }
    var originalLatitude by rememberSaveable { mutableStateOf(0.0) }
    var originalLongitude by rememberSaveable { mutableStateOf(0.0) }
    var originalType by rememberSaveable { mutableStateOf(BookmarkPlaceType.ETC) }
    var formPlaceName by rememberSaveable { mutableStateOf("") }
    var formRoadAddress by rememberSaveable { mutableStateOf("") }
    var formLatitude by rememberSaveable { mutableStateOf(0.0) }
    var formLongitude by rememberSaveable { mutableStateOf(0.0) }
    var selectedType by rememberSaveable { mutableStateOf(BookmarkPlaceType.ETC) }
    var isNameFocused by rememberSaveable { mutableStateOf(false) }
    var submittedFeedbackEventId by rememberSaveable { mutableStateOf<Long?>(null) }
    var isFormSearchVisible by rememberSaveable { mutableStateOf(false) }
    var shouldRenderFormSearch by rememberSaveable { mutableStateOf(false) }
    var formSearchSessionId by rememberSaveable { mutableStateOf(0) }
    var pendingAddType by rememberSaveable { mutableStateOf(BookmarkPlaceType.ETC) }
    var pendingDeleteBookmark by remember { mutableStateOf<PlaceBookmarkSummary?>(null) }
    val feedbackMessage = uiState.errorMessage ?: uiState.successMessage

    fun clearInputFocus() {
        isNameFocused = false
    }

    fun dismissFormSheet() {
        formMode = null
        editingBookmarkId = null
        originalPlaceName = ""
        originalRoadAddress = ""
        originalLatitude = 0.0
        originalLongitude = 0.0
        originalType = BookmarkPlaceType.ETC
        formPlaceName = ""
        formRoadAddress = ""
        formLatitude = 0.0
        formLongitude = 0.0
        selectedType = BookmarkPlaceType.ETC
        pendingAddType = BookmarkPlaceType.ETC
        submittedFeedbackEventId = null
        isFormSearchVisible = false
        shouldRenderFormSearch = false
        clearInputFocus()
    }

    fun openAddSheet(place: PlaceSearchResult) {
        val initialType = pendingAddType
        formMode = PlaceBookmarkFormMode.ADD
        editingBookmarkId = null
        originalPlaceName = ""
        originalRoadAddress = ""
        originalLatitude = 0.0
        originalLongitude = 0.0
        originalType = BookmarkPlaceType.ETC
        formPlaceName = place.name
        formRoadAddress = place.displayAddress
        formLatitude = place.latitude
        formLongitude = place.longitude
        selectedType = initialType
        pendingAddType = BookmarkPlaceType.ETC
        isFormSearchVisible = false
        shouldRenderFormSearch = false
        clearInputFocus()
    }

    fun openEditSheet(placeBookmark: PlaceBookmarkSummary) {
        formMode = PlaceBookmarkFormMode.EDIT
        editingBookmarkId = placeBookmark.bookmarkPlaceId
        originalPlaceName = placeBookmark.placeName
        originalRoadAddress = placeBookmark.roadAddress
        originalLatitude = placeBookmark.latitude
        originalLongitude = placeBookmark.longitude
        originalType = placeBookmark.type
        formPlaceName = placeBookmark.placeName
        formRoadAddress = placeBookmark.roadAddress
        formLatitude = placeBookmark.latitude
        formLongitude = placeBookmark.longitude
        selectedType = placeBookmark.type
        isFormSearchVisible = false
        shouldRenderFormSearch = false
        clearInputFocus()
    }

    fun showFormSearch() {
        formSearchSessionId += 1
        shouldRenderFormSearch = true
        isFormSearchVisible = true
        clearInputFocus()
    }

    fun hideFormSearch() {
        isFormSearchVisible = false
    }

    fun removeFormSearch() {
        shouldRenderFormSearch = false
    }

    fun applyFormSearchResult(place: PlaceSearchResult) {
        formPlaceName = place.name
        formRoadAddress = place.displayAddress
        formLatitude = place.latitude
        formLongitude = place.longitude
        hideFormSearch()
    }

    fun hasFormChanges(): Boolean {
        return formPlaceName.trim() != originalPlaceName.trim() ||
            formRoadAddress.trim() != originalRoadAddress.trim() ||
            formLatitude != originalLatitude ||
            formLongitude != originalLongitude ||
            selectedType != originalType
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
            uiState.successMessage != null -> dismissFormSheet()
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
                onRegisterHomeClick = {
                    pendingAddType = BookmarkPlaceType.HOME
                    onAddPlaceBookmarkClick()
                },
                onAddPlaceBookmarkClick = {
                    pendingAddType = BookmarkPlaceType.ETC
                    onAddPlaceBookmarkClick()
                },
                onEditPlaceBookmarkClick = ::openEditSheet,
                onDeletePlaceBookmarkClick = { pendingDeleteBookmark = it },
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

        if (formMode != null) {
            val isEditForm = formMode == PlaceBookmarkFormMode.EDIT
            PlaceBookmarkFormOverlay(
                title = stringResource(
                    if (isEditForm) {
                        R.string.place_bookmark_edit_title
                    } else {
                        R.string.place_bookmark_add_title
                    }
                ),
                submitText = stringResource(
                    if (isEditForm) {
                        R.string.place_bookmark_edit_submit
                    } else {
                        R.string.place_bookmark_add_submit
                    }
                ),
                placeNameMarker = originalPlaceName.takeIf {
                    isEditForm && it.isNotBlank()
                },
                placeName = formPlaceName,
                roadAddress = formRoadAddress,
                selectedType = selectedType,
                isSubmitting = uiState.isSubmitting,
                isSubmitEnabled = formPlaceName.trim().isNotBlank() &&
                    formRoadAddress.trim().isNotBlank() &&
                    !uiState.isSubmitting &&
                    (!isEditForm || hasFormChanges()),
                isNameFocused = isNameFocused,
                onPlaceNameChange = { formPlaceName = it },
                onNameFocusChanged = { isNameFocused = it },
                onTypeSelected = { selectedType = it },
                onClearInputFocus = ::clearInputFocus,
                onAddressClick = ::showFormSearch,
                onDismiss = ::dismissFormSheet,
                onSubmit = {
                    submittedFeedbackEventId = uiState.feedbackEventId
                    if (isEditForm) {
                        editingBookmarkId?.let { bookmarkPlaceId ->
                            onUpdatePlaceBookmark(
                                bookmarkPlaceId,
                                selectedType,
                                formPlaceName,
                                formRoadAddress,
                                formLatitude,
                                formLongitude
                            )
                        }
                    } else {
                        onCreatePlaceBookmark(
                            selectedType,
                            formPlaceName,
                            formRoadAddress,
                            formLatitude,
                            formLongitude
                        )
                    }
                }
            )
        }

        if (formMode != null && shouldRenderFormSearch) {
            PlaceBookmarkFormSearchOverlay(
                visible = isFormSearchVisible,
                viewModelKey = "place-bookmark-form-search-$formSearchSessionId",
                onBackClick = ::hideFormSearch,
                onPlaceSelected = ::applyFormSearchResult,
                onDismissed = ::removeFormSearch
            )
        }

        pendingDeleteBookmark?.let { placeBookmark ->
            PlaceBookmarkDeleteConfirmDialog(
                placeName = placeBookmark.placeName,
                onDismiss = { pendingDeleteBookmark = null },
                onConfirm = {
                    pendingDeleteBookmark = null
                    onDeletePlaceBookmark(placeBookmark.bookmarkPlaceId)
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "집 등록됨")
@Composable
private fun PlaceBookmarkScreenWithHomePreview() {
    PassedPathTheme {
        PlaceBookmarkScreenPreviewContent(
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

@Preview(showBackground = true, name = "집 미등록")
@Composable
private fun PlaceBookmarkScreenWithoutHomePreview() {
    PassedPathTheme {
        PlaceBookmarkScreenPreviewContent(
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

@Composable
private fun PlaceBookmarkScreenPreviewContent(
    bookmarkPlaces: List<PlaceBookmarkSummary>
) {
    PlaceBookmarkScreen(
        uiState = PlaceBookmarkUiState(
            placeCount = bookmarkPlaces.size,
            hasLoaded = true,
            bookmarkPlaces = bookmarkPlaces
        ),
        searchResultEvent = null,
        onSearchResultEventConsumed = {},
        onBackClick = {},
        onAddPlaceBookmarkClick = {},
        onCreatePlaceBookmark = { _, _, _, _, _ -> },
        onUpdatePlaceBookmark = { _, _, _, _, _, _ -> },
        onDeletePlaceBookmark = {},
        onFeedbackDismissed = {}
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
