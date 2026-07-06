package com.example.passedpath.feature.care.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.passedpath.ui.theme.CareAvatarBlue50
import com.example.passedpath.ui.theme.CareAvatarBlue500
import com.example.passedpath.ui.theme.CareAvatarPink50
import com.example.passedpath.ui.theme.CareAvatarPink500
import com.example.passedpath.ui.theme.CareAvatarPurple50
import com.example.passedpath.ui.theme.CareAvatarPurple500

data class CareDependentAvatarPalette(
    val backgroundColor: Color,
    val contentColor: Color
)

@Composable
fun CareDependentAvatar(
    nickname: String,
    profileImageUrl: String?,
    palette: CareDependentAvatarPalette,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(palette.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = nickname.initialText(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            color = palette.contentColor
        )
        if (!profileImageUrl.isNullOrBlank()) {
            AsyncImage(
                model = profileImageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
    }
}

fun careDependentAvatarPalette(index: Int): CareDependentAvatarPalette {
    return CareDependentAvatarPalettes[index.floorMod(CareDependentAvatarPalettes.size)]
}

private val CareDependentAvatarPalettes = listOf(
    CareDependentAvatarPalette(
        backgroundColor = CareAvatarPink50,
        contentColor = CareAvatarPink500
    ),
    CareDependentAvatarPalette(
        backgroundColor = CareAvatarPurple50,
        contentColor = CareAvatarPurple500
    ),
    CareDependentAvatarPalette(
        backgroundColor = CareAvatarBlue50,
        contentColor = CareAvatarBlue500
    )
)

private fun String.initialText(): String {
    return trim()
        .firstOrNull()
        ?.uppercaseChar()
        ?.toString()
        ?: "?"
}

private fun Int.floorMod(other: Int): Int {
    return ((this % other) + other) % other
}
