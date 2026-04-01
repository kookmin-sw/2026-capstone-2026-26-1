package com.example.passedpath.ui.component.snackbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BaseSnackbar(
    modifier: Modifier = Modifier,
    containerColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(999.dp), // feat: pill 형태
            color = containerColor,
            tonalElevation = 0.dp,
            shadowElevation = 6.dp
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                content()
            }
        }
    }
}