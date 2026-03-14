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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.viewmodel.MainViewModel
import com.example.passedpath.feature.main.presentation.viewmodel.MainViewModelFactory

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val permissionState by viewModel.permissionUiState.collectAsState()
    val testResult by viewModel.testResult.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkPermission()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "ë©”ì¸ ?”ë©´")
        Spacer(modifier = Modifier.height(16.dp))

        if (permissionState == LocationPermissionUiState.FULL) {
            Text(text = "?“ ?„ì¹˜ ê¸°ëŠ¥ ?œì„±?”ë¨")
        } else {
            Text(text = "?“ ?œí•œ?íƒœ")
        }

        Button(
            onClick = {
                viewModel.testApi()
            }
        ) {
            Text("TEST API ?¸ì¶œ")
        }

        testResult?.let { result ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = result)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onLogout()
            }
        ) {
            Text(text = "ë¡œê·¸?„ì›ƒ")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(
        onLogout = {}
    )
}
