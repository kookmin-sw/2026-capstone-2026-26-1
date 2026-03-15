package com.example.passedpath.feature.mypage.presentation.screen

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
import com.example.passedpath.ui.state.AsyncUiState

@Composable
fun MyPageScreen(
    testResult: AsyncUiState<String>,
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
        Text(text = stringResource(R.string.mypage_title))
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onTestClick) {
            Text(text = stringResource(R.string.main_test_api))
        }

        when (testResult) {
            AsyncUiState.Idle -> Unit
            AsyncUiState.Loading -> {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = stringResource(R.string.main_test_loading))
            }
            is AsyncUiState.Success -> {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = testResult.data)
            }
            is AsyncUiState.Error -> {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = stringResource(testResult.messageResId))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onLogoutClick) {
            Text(text = stringResource(R.string.main_logout))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyPageScreenPreview() {
    MyPageScreen(
        testResult = AsyncUiState.Success("test"),
        onTestClick = {},
        onLogoutClick = {}
    )
}
