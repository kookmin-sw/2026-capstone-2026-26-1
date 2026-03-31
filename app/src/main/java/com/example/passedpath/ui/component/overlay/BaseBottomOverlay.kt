package com.example.passedpath.ui.component.overlay

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
fun BaseBottomOverlay(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = containerColor,
            tonalElevation = 0.dp,
            shadowElevation = 10.dp
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
            ) {
                content()
            }
        }
    }
}
