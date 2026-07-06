package com.example.passedpath.ui.component.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.ui.component.banner.RequestActionBottomBanner
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun MapOverlayNetworkFailureBanner(
    retryText: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.network_request_failed_banner)
) {
    RequestActionBottomBanner(
        message = message,
        actionText = retryText,
        onClickAction = onRetryClick,
        modifier = modifier,
        borderColor = null,
        shadowElevation = 6.dp
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF4B5563)
@Composable
private fun MapOverlayNetworkFailureBannerPreview() {
    PassedPathTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF4B5563))
                .padding(16.dp)
        ) {
            MapOverlayNetworkFailureBanner(
                retryText = "Retry",
                onRetryClick = {}
            )
        }
    }
}
