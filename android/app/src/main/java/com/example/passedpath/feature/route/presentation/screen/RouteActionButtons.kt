package com.example.passedpath.feature.route.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.ui.component.button.BasePillButton
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray700
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.Primary

@Composable
internal fun RoutePlaybackButton(onClick: () -> Unit) {
    RouteFloatingPillButton(
        text = stringResource(R.string.route_open_playback),
        onClick = onClick
    )
}

@Composable
internal fun TrackingToggleButton(
    isTracking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isTracking) Gray700 else Gray400

    BasePillButton(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        contentSpacing = 10.dp,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isTracking) Primary else Gray400)
        )

        Text(
            text = stringResource(
                if (isTracking) {
                    R.string.route_tracking_active
                } else {
                    R.string.route_tracking_inactive
                }
            ),
            color = contentColor,
            fontSize = 16.sp
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_swap),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
internal fun RouteFloatingPillButton(text: String, onClick: () -> Unit) {
    BasePillButton(onClick = onClick, shadowElevation = 6.dp) {
        Text(text = text, color = Gray700, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true, name = "Tracking Toggle Inactive")
@Composable
private fun TrackingToggleButtonInactivePreview() {
    PassedPathTheme {
        TrackingToggleButton(
            isTracking = false,
            onClick = {}
        )
    }
}
