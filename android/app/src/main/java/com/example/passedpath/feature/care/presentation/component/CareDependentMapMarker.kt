package com.example.passedpath.feature.care.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.ui.theme.Black
import com.example.passedpath.ui.theme.Gray200
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White

@Composable
fun CareDependentMapMarker(
    nickname: String,
    profileImageUrl: String?,
    palette: CareDependentAvatarPalette,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(width = 58.dp, height = 66.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(
            modifier = Modifier
                .size(width = 20.dp, height = 14.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-5).dp)
        ) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(path = path, color = White)
            drawPath(
                path = path,
                color = if (selected) Green500 else Gray200,
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Box(
            modifier = Modifier
                .size(50.dp)
                .offset(y = 4.dp)
                .clip(CircleShape)
                .background(Black.copy(alpha = 0.24f))
        )
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(White)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) Green500 else Gray200,
                    shape = CircleShape
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            CareDependentAvatar(
                nickname = nickname,
                profileImageUrl = profileImageUrl,
                palette = palette,
                contentDescription = stringResource(
                    R.string.care_dependent_marker_content_description,
                    nickname
                ),
                modifier = Modifier.size(44.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CareDependentMapMarkerPreview() {
    PassedPathTheme {
        CareDependentMapMarker(
            nickname = "Jiyeon",
            profileImageUrl = null,
            palette = careDependentAvatarPalette(0),
            selected = true
        )
    }
}
