package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.main.presentation.state.DailyPathUiState
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MainScreen(
    uiState: MainUiState
) {
    val fallbackPosition = LatLng(37.5662952, 126.9779451)
    val initialCameraTarget = uiState.currentLocation?.toLatLng() ?: fallbackPosition
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCameraTarget, 15f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false)
        ) {
            uiState.currentLocation?.let { currentLocation ->
                Marker(
                    state = MarkerState(position = currentLocation.toLatLng()),
                    title = stringResource(R.string.main_map_marker_title)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = stringResource(R.string.main_title))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = permissionText(uiState.permissionState))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Today path points: ${uiState.todayPath.points.size}")
                    }
                }
            }
        }
    }
}

private fun permissionText(permissionState: LocationPermissionUiState): String {
    return when (permissionState) {
        LocationPermissionUiState.ALWAYS -> "Background location is enabled"
        LocationPermissionUiState.FOREGROUND_ONLY -> "Foreground location is enabled"
        LocationPermissionUiState.DENIED -> "Location permission is not granted"
    }
}

private fun MainCoordinateUiState.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(
        uiState = MainUiState(
            permissionState = LocationPermissionUiState.ALWAYS,
            currentLocation = MainCoordinateUiState(
                latitude = 37.5662952,
                longitude = 126.9779451
            ),
            todayPath = DailyPathUiState(
                dateKey = "2026-03-19",
                points = listOf(
                    MainCoordinateUiState(
                        latitude = 37.5662952,
                        longitude = 126.9779451
                    )
                )
            )
        )
    )
}
