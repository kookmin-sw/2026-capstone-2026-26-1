package com.example.passedpath.ui.component.banner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.ui.theme.Green100
import com.example.passedpath.ui.theme.Green300

@Composable
fun RequestActionBottomBanner(
    message: String,
    actionText: String,
    onClickAction: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color? = Green100,
    shadowElevation: Dp = 0.dp
) {
    IconActionBottomBanner(
        message = message,
        actionText = actionText,
        onClickAction = onClickAction,
        modifier = modifier,
        iconResId = R.drawable.ic_alert,
        cornerRadius = 22.dp,
        iconTint = Green300,
        borderColor = borderColor,
        shadowElevation = shadowElevation
    )
}

@Preview(showBackground = true, name = "Request Action Bottom Banner")
@Composable
private fun RequestActionBottomBannerPreview() {
    com.example.passedpath.ui.theme.PassedPathTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F6))
                .padding(16.dp)
        ) {
            RequestActionBottomBanner(
                message = "Network request failed.",
                actionText = "Retry",
                onClickAction = {}
            )
        }
    }
}
