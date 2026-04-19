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
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.ui.component.BasePillButton
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray700
import com.example.passedpath.ui.theme.Primary

@Composable
internal fun RoutePlaybackButton(onClick: () -> Unit) {
    FloatingPillButton(
        text = stringResource(R.string.route_open_playback),
        onClick = onClick
    )
}

@Composable
internal fun TrackingToggleButton(
    isTracking: Boolean,
    onClick: () -> Unit
) {
    BasePillButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        contentSpacing = 8.dp,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isTracking) Primary else Gray500)
        )

        Text(
            text = stringResource(
                if (isTracking) {
                    R.string.route_tracking_active
                } else {
                    R.string.route_tracking_inactive
                }
            ),
            color = Gray700
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_swap),
            contentDescription = null,
            tint = Gray500,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
internal fun FloatingPillButton(text: String, onClick: () -> Unit) {
    BasePillButton(onClick = onClick, shadowElevation = 6.dp) {
        Text(text = text, color = Gray700, fontWeight = FontWeight.Medium)
    }
}
