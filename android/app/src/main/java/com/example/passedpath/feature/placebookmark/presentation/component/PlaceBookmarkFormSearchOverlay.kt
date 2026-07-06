package com.example.passedpath.feature.placebookmark.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.passedpath.R
import com.example.passedpath.feature.place.domain.model.PlaceSearchResult
import com.example.passedpath.feature.place.presentation.screen.PlaceSearchSelectionScreen

@Composable
internal fun PlaceBookmarkFormSearchOverlay(
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
                animationSpec = tween(durationMillis = PlaceBookmarkFormSearchEnterTransitionMillis),
                initialOffsetX = { fullWidth -> fullWidth }
            ) + fadeIn(animationSpec = tween(durationMillis = PlaceBookmarkFormSearchEnterTransitionMillis)),
            exit = slideOutHorizontally(
                animationSpec = tween(durationMillis = PlaceBookmarkFormSearchExitTransitionMillis),
                targetOffsetX = { fullWidth -> fullWidth }
            ) + fadeOut(animationSpec = tween(durationMillis = PlaceBookmarkFormSearchExitTransitionMillis))
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

private const val PlaceBookmarkFormSearchEnterTransitionMillis = 250
private const val PlaceBookmarkFormSearchExitTransitionMillis = 230
