package com.example.passedpath.feature.care.presentation.screen

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.R
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.care.presentation.viewmodel.CareViewModel
import com.example.passedpath.feature.care.presentation.viewmodel.CareViewModelFactory

@Composable
fun CareRoute(
    refreshEventId: Int = 0,
    modifier: Modifier = Modifier,
    onNavigateToProtectedPersonRouteHistory: (Long, String) -> Unit = { _, _ -> },
    onNavigateToProtectedPersonWeeklySummary: (Long, String) -> Unit = { _, _ -> },
    viewModel: CareViewModel = viewModel(
        factory = CareViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.care_invite_share_title)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(viewModel) {
        viewModel.startLocationStream()
        onDispose {
            viewModel.stopLocationStream()
        }
    }

    LaunchedEffect(refreshEventId) {
        if (refreshEventId > 0) {
            viewModel.refreshDependents()
        }
    }

    CareScreen(
        uiState = uiState,
        onDependentSelected = viewModel::selectDependent,
        onRetryClick = viewModel::refreshDependents,
        onInviteClick = viewModel::openInviteModal,
        onSheetValueChanged = viewModel::onSheetValueChanged,
        onSheetCommandConsumed = viewModel::onSheetCommandConsumed,
        onTabSelected = viewModel::selectBottomSheetTab,
        onPlaceMarkerClick = viewModel::onPlaceMarkerClick,
        onPlaceCardClick = viewModel::onPlaceCardClick,
        onSelectedPlaceHandled = viewModel::onSelectedPlaceHandled,
        onFocusedPlaceHandled = viewModel::onFocusedPlaceHandled,
        onMapClick = viewModel::onMapClick,
        onPlaceRetryClick = viewModel::retryProtectedPersonPlaces,
        onPlaceGuideBannerClose = viewModel::dismissProtectedPersonPlaceGuideBanner,
        onSummaryRetryClick = viewModel::retryProtectedPersonSummary,
        onRouteRetryClick = viewModel::retryProtectedPersonRoute,
        onProtectedPersonWeeklySummaryClick = {
            uiState.selectedDependent?.let { dependent ->
                onNavigateToProtectedPersonWeeklySummary(
                    dependent.dependentUserId,
                    dependent.nickname
                )
            }
        },
        onProtectedPersonPastRouteClick = {
            uiState.selectedDependent?.let { dependent ->
                onNavigateToProtectedPersonRouteHistory(
                    dependent.dependentUserId,
                    dependent.nickname
                )
            }
        },
        onLocationStreamRetryClick = viewModel::retryLocationStream,
        onLocationStreamErrorDismiss = viewModel::dismissLocationStreamError,
        onInviteDismiss = viewModel::dismissInviteModal,
        onInviteRetryClick = viewModel::retryCreateInviteLink,
        onInviteLinkCopyClick = { inviteLink ->
            copyInviteLink(
                context = context,
                inviteLink = inviteLink
            )
            viewModel.onInviteLinkCopied()
        },
        onInviteLinkShareClick = { inviteLink ->
            shareInviteLink(
                context = context,
                chooserTitle = shareTitle,
                inviteLink = inviteLink
            )
        },
        modifier = modifier
    )
}

private fun copyInviteLink(
    context: Context,
    inviteLink: String
) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE)
        as? android.content.ClipboardManager ?: return
    clipboardManager.setPrimaryClip(
        ClipData.newPlainText("care relationship invite link", inviteLink)
    )
}

private fun shareInviteLink(
    context: Context,
    chooserTitle: String,
    inviteLink: String
) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, inviteLink)
    }
    runCatching {
        context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
    }
}
