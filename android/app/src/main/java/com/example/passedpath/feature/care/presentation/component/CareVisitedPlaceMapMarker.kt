package com.example.passedpath.feature.care.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.ui.theme.Black
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White

@Composable
fun CareVisitedPlaceMapMarker(
    orderIndex: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(42.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .offset(y = 2.dp)
                .clip(CircleShape)
                .background(Black.copy(alpha = 0.32f))
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(White, CircleShape)
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White, CircleShape)
                    .drawBehind {
                        drawCircle(
                            color = Green500.copy(alpha = 0.1f),
                            radius = size.minDimension / 2f
                        )
                        drawCircle(
                            color = Green500,
                            radius = size.minDimension / 2f - 1.dp.toPx(),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = orderIndex.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Green500,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CareVisitedPlaceMapMarkerPreview() {
    PassedPathTheme {
        CareVisitedPlaceMapMarker(orderIndex = 2)
    }
}
