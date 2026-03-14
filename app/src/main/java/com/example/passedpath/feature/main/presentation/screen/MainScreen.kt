package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState

@Composable
fun MainScreen(
    permissionState: LocationPermissionUiState,
    testResult: String?,
    onTestClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Main")
        Spacer(modifier = Modifier.height(16.dp))

        if (permissionState == LocationPermissionUiState.FULL) {
            Text(text = "Background location is enabled")
        } else {
            Text(text = "Background location is limited")
        }

        Button(onClick = onTestClick) {
            Text("Test API")
        }

        testResult?.let { result ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = result)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onLogoutClick) {
            Text(text = "Logout")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(
        permissionState = LocationPermissionUiState.FULL,
        testResult = "OK",
        onTestClick = {},
        onLogoutClick = {}
    )
}
