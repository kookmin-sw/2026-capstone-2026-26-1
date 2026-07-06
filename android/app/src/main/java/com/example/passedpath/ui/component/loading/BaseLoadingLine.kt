package com.example.passedpath.ui.component.loading

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.passedpath.ui.theme.Green100
import com.example.passedpath.ui.theme.Green300
import com.example.passedpath.ui.theme.Green400
import com.example.passedpath.ui.theme.Green500

@Composable
fun BaseLoadingLine(
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Green100.copy(alpha = 0.18f))
    ) {
        val segmentWidth = (maxWidth * 0.26f).coerceIn(56.dp, 108.dp)
        val transition = rememberInfiniteTransition(label = "base_loading_line")
        val segmentOffset by transition.animateFloat(
            initialValue = -segmentWidth.value,
            targetValue = maxWidth.value,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1_700, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "base_loading_line_offset"
        )
        val segmentBrush = remember {
            Brush.horizontalGradient(
                colors = listOf(
                    Green100.copy(alpha = 0f),
                    Green300.copy(alpha = 0.55f),
                    Green400.copy(alpha = 0.99f),
                    Green300.copy(alpha = 0.55f),
                    Green100.copy(alpha = 0f)
                )
            )
        }

        Box(
            modifier = Modifier
                .offset(x = segmentOffset.dp)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(999.dp))
                .background(segmentBrush)
        )
    }
}
