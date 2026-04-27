package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.ui.state.CoordinateUiState
import com.google.maps.android.compose.MarkerComposable

private val CurrentLocationGlowBase = Color(0xFF006B5F)

@Composable
internal fun CurrentLocationMapMarker(currentLocation: CoordinateUiState) {
    MarkerComposable(
        state = com.google.maps.android.compose.MarkerState(position = currentLocation.toLatLng()),
        title = stringResource(R.string.main_map_marker_title),
        anchor = Offset(0.5f, 0.58f),
        zIndex = 10f
    ) {
        Box(
            modifier = Modifier.size(104.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(94.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CurrentLocationGlowBase.copy(alpha = 0.80f),
                                CurrentLocationGlowBase.copy(alpha = 0.50f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Image(
                painter = painterResource(id = R.drawable.pp_location_marker),
                contentDescription = stringResource(R.string.main_map_marker_title),
                modifier = Modifier.size(70.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
