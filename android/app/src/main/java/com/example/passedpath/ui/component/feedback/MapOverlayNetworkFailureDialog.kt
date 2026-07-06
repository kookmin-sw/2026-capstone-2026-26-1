package com.example.passedpath.ui.component.feedback

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.passedpath.R
import com.example.passedpath.ui.component.dialog.BaseConfirmDialog
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun MapOverlayNetworkFailureDialog(
    retryText: String,
    onRetryClick: () -> Unit,
    onDismiss: () -> Unit,
    message: String = stringResource(R.string.network_failure_dialog_message)
) {
    BaseConfirmDialog(
        title = stringResource(R.string.network_failure_dialog_title),
        message = message,
        dismissText = stringResource(R.string.network_failure_dialog_close),
        confirmText = retryText,
        onDismiss = onDismiss,
        onConfirm = onRetryClick
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun MapOverlayNetworkFailureDialogPreview() {
    PassedPathTheme {
        MapOverlayNetworkFailureDialog(
            retryText = "Retry",
            onRetryClick = {},
            onDismiss = {}
        )
    }
}
