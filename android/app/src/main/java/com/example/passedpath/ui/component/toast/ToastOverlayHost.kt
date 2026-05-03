package com.example.passedpath.ui.component.toast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class ToastOverlayItem(
    val message: String,
    val triggerKey: String,
    val onDismissed: (() -> Unit)? = null
)

@Composable
fun ToastOverlayHost(
    toasts: List<ToastOverlayItem>,
    modifier: Modifier = Modifier
) {
    if (toasts.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        toasts.forEach { toast ->
            key(toast.triggerKey) {
                MessageToast(
                    message = toast.message,
                    triggerKey = toast.triggerKey,
                    onDismissed = { toast.onDismissed?.invoke() }
                )
            }
        }
    }
}
