package com.example.passedpath.feature.care.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.feature.care.presentation.state.CareInviteUiState
import com.example.passedpath.ui.component.button.BaseButton
import com.example.passedpath.ui.component.button.BaseButtonVariant
import com.example.passedpath.ui.component.loading.BaseLoadingIndicator
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray200
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray700
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White

@Composable
fun CareInviteBottomSheet(
    uiState: CareInviteUiState,
    onCopyClick: (String) -> Unit,
    onShareClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                .padding(start = 20.dp, top = 26.dp, end = 20.dp, bottom = 40.dp)
        ) {
            CareInviteHeader(onDismiss = onDismiss)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.care_invite_sheet_description),
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )
            Spacer(modifier = Modifier.height(24.dp))

            when {
                uiState.isLoading -> CareInviteLoadingContent()
                uiState.errorMessage != null -> CareInviteErrorContent(onRetryClick = onRetryClick)
                !uiState.inviteLink.isNullOrBlank() -> CareInviteLinkContent(
                    inviteLink = uiState.inviteLink,
                    copyFeedbackEventId = uiState.copyFeedbackEventId,
                    onCopyClick = onCopyClick,
                    onShareClick = onShareClick
                )
            }
        }
    }
}

@Composable
private fun CareInviteHeader(onDismiss: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.care_invite_sheet_title),
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleMedium,
            color = Gray900,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(34.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = stringResource(R.string.care_invite_sheet_close),
                tint = Gray400,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun CareInviteLoadingContent() {
    Column {
        CareInviteFieldLabel(text = stringResource(R.string.care_invite_sheet_link_label))
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(Gray100, RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BaseLoadingIndicator(
                modifier = Modifier.size(20.dp),
                color = Green500,
                strokeWidth = 2.5.dp
            )
            Text(
                text = stringResource(R.string.care_invite_sheet_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = Gray700,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CareInviteErrorContent(onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        CareInviteFieldLabel(text = stringResource(R.string.care_invite_sheet_link_label))
        Spacer(modifier = Modifier.height(10.dp))
        CareInviteMessageBox(message = stringResource(R.string.care_invite_sheet_error))
        Spacer(modifier = Modifier.height(24.dp))
        BaseButton(
            text = stringResource(R.string.care_invite_sheet_retry),
            onClick = onRetryClick,
            variant = BaseButtonVariant.PRIMARY
        )
    }
}

@Composable
private fun CareInviteLinkContent(
    inviteLink: String,
    copyFeedbackEventId: Long,
    onCopyClick: (String) -> Unit,
    onShareClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CareInviteFieldLabel(text = stringResource(R.string.care_invite_sheet_link_label))
        Spacer(modifier = Modifier.height(10.dp))
        CareInviteLinkBox(inviteLink = inviteLink)
        if (copyFeedbackEventId > 0L) {
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = stringResource(R.string.care_invite_sheet_copied),
                style = MaterialTheme.typography.bodySmall,
                color = Green500,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BaseButton(
                text = stringResource(R.string.care_invite_sheet_copy),
                onClick = { onCopyClick(inviteLink) },
                modifier = Modifier.weight(1f),
                variant = BaseButtonVariant.SECONDARY,
                border = BorderStroke(1.dp, Gray200)
            )
            BaseButton(
                text = stringResource(R.string.care_invite_sheet_share),
                onClick = { onShareClick(inviteLink) },
                modifier = Modifier.weight(1f),
                variant = BaseButtonVariant.PRIMARY
            )
        }
    }
}

@Composable
private fun CareInviteFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Gray400,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )
}

@Composable
private fun CareInviteLinkBox(inviteLink: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 46.dp)
            .background(Gray100, RoundedCornerShape(10.dp))
            .border(BorderStroke(1.5.dp, Gray200), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Text(
            text = inviteLink,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray900,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CareInviteMessageBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 46.dp)
            .background(Gray100, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray700,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true, name = "Care Invite - Loading")
@Composable
private fun CareInviteBottomSheetLoadingPreview() {
    PassedPathTheme {
        CareInviteBottomSheet(
            uiState = CareInviteUiState(isVisible = true, isLoading = true),
            onCopyClick = {},
            onShareClick = {},
            onRetryClick = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Care Invite - Success")
@Composable
private fun CareInviteBottomSheetSuccessPreview() {
    PassedPathTheme {
        CareInviteBottomSheet(
            uiState = CareInviteUiState(
                isVisible = true,
                inviteLink = "https://passedpath.site/care-relationship/invite?inviteCode=T5rfCFFy9j",
                copyFeedbackEventId = 1L
            ),
            onCopyClick = {},
            onShareClick = {},
            onRetryClick = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Care Invite - Error")
@Composable
private fun CareInviteBottomSheetErrorPreview() {
    PassedPathTheme {
        CareInviteBottomSheet(
            uiState = CareInviteUiState(
                isVisible = true,
                errorMessage = "Failed"
            ),
            onCopyClick = {},
            onShareClick = {},
            onRetryClick = {},
            onDismiss = {}
        )
    }
}
