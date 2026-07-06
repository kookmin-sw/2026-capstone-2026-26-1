package com.example.passedpath.feature.care.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.passedpath.R
import com.example.passedpath.feature.care.presentation.state.CareInviteAcceptUiState
import com.example.passedpath.ui.component.button.BaseButton
import com.example.passedpath.ui.component.button.BaseButtonVariant
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray300
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White

@Composable
fun CareInviteAcceptDialog(
    uiState: CareInviteAcceptUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!uiState.isVisible) return

    Dialog(
        onDismissRequest = {
            if (!uiState.isSubmitting) {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = White,
                tonalElevation = 0.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 34.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.care_invite_accept_title),
                        color = Gray900,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 34.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.care_invite_accept_message),
                        color = Gray400,
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    if (uiState.errorMessage != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Gray100
                        ) {
                            Text(
                                text = stringResource(R.string.care_invite_accept_error),
                                color = Gray500,
                                fontSize = 13.sp,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(
                                    horizontal = 14.dp,
                                    vertical = 12.dp
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        BaseButton(
                            text = stringResource(R.string.care_invite_accept_cancel),
                            onClick = onDismiss,
                            enabled = !uiState.isSubmitting,
                            modifier = Modifier.weight(1f),
                            variant = BaseButtonVariant.SECONDARY,
                            border = BorderStroke(1.dp, Gray300),
                            containerColor = White,
                            contentColor = Gray400
                        )
                        BaseButton(
                            text = if (uiState.isSubmitting) {
                                stringResource(R.string.care_invite_accept_submitting)
                            } else {
                                stringResource(R.string.care_invite_accept_confirm)
                            },
                            onClick = onConfirm,
                            enabled = !uiState.isSubmitting,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC, name = "Care Invite Accept")
@Composable
private fun CareInviteAcceptDialogPreview() {
    PassedPathTheme {
        CareInviteAcceptDialog(
            uiState = CareInviteAcceptUiState(
                isVisible = true,
                inviteCode = "T5rfCFFy9j"
            ),
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC, name = "Care Invite Accept Error")
@Composable
private fun CareInviteAcceptDialogErrorPreview() {
    PassedPathTheme {
        CareInviteAcceptDialog(
            uiState = CareInviteAcceptUiState(
                isVisible = true,
                inviteCode = "T5rfCFFy9j",
                errorMessage = "Failed"
            ),
            onDismiss = {},
            onConfirm = {}
        )
    }
}
