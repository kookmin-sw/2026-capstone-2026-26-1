package com.example.passedpath.ui.component.toast

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun RouteEmptyToast(
    triggerKey: String,
    modifier: Modifier = Modifier,
    durationMillis: Long = 2500L
) {
    var isVisible by remember(triggerKey) { mutableStateOf(true) }

    LaunchedEffect(triggerKey, durationMillis) {
        isVisible = true
        kotlinx.coroutines.delay(durationMillis)
        isVisible = false
    }

    if (!isVisible) return

    BaseToast(modifier = modifier) {
        Text(
            text = stringResource(R.string.route_empty_past_toast),
            color = Color.White
        )
    }
}

@Preview(showBackground = true, name = "Route Empty Toast")
@Composable
private fun RouteEmptyToastPreview() {
    PassedPathTheme {
        RouteEmptyToast(
            triggerKey = "2026-04-02",
            modifier = Modifier.padding(16.dp)
        )
    }
}
