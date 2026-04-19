package com.example.passedpath.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun BaseCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    containerColor: Color = Color.White,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier.size(size),
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation
    ) {
        Box(
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BaseCircleButtonPreview() {
    PassedPathTheme {
        BaseCircleButton(onClick = {}) {}
    }
}
