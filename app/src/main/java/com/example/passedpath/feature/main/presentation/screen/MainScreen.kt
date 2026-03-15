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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState

@Composable
fun MainScreen(
    permissionState: LocationPermissionUiState,
    onMyPageClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.main_title))
        Spacer(modifier = Modifier.height(16.dp))

        if (permissionState == LocationPermissionUiState.FULL) {
            Text(text = stringResource(R.string.main_permission_full))
        } else {
            Text(text = stringResource(R.string.main_permission_limited))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onMyPageClick) {
            Text(text = stringResource(R.string.main_go_to_mypage))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(
        permissionState = LocationPermissionUiState.FULL,
        onMyPageClick = {}
    )
}
