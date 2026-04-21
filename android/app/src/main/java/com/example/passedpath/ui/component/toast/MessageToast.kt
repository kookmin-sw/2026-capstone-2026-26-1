package com.example.passedpath.ui.component.toast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun MessageToast(
    message: String,
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageToastPreview() {
    PassedPathTheme {
        MessageToast(
            message = "저장했습니다.",
            triggerKey = "preview",
            modifier = Modifier.padding(16.dp)
        )
    }
}
