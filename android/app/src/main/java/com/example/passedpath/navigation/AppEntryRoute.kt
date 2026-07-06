package com.example.passedpath.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.ui.theme.Green300
import com.example.passedpath.ui.theme.PassedPathTheme
import kotlinx.coroutines.delay

@Composable
fun AppEntryRoute(
    onResolved: (String) -> Unit,
    viewModel: AppEntryViewModel
) {
    val state by viewModel.state.collectAsState()
    val gradientProgress = remember { Animatable(0f) }
    val logoScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        gradientProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = AppEntryGradientExpansionDurationMillis,
                easing = FastOutSlowInEasing
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(AppEntryLogoBreathingDelayMillis)
        while (true) {
            logoScale.animateTo(
                targetValue = 1.04f,
                animationSpec = tween(
                    durationMillis = AppEntryLogoBreathingDurationMillis,
                    easing = FastOutSlowInEasing
                )
            )
            logoScale.animateTo(
                targetValue = 0.96f,
                animationSpec = tween(
                    durationMillis = AppEntryLogoBreathingDurationMillis,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    LaunchedEffect(state) {
        val readyState = state as? AppEntryState.Ready ?: return@LaunchedEffect
        onResolved(readyState.destination)
    }

    AppEntryScreen(
        logoScale = logoScale.value,
        gradientProgress = gradientProgress.value
    )
}

@Composable
private fun AppEntryScreen(
    logoScale: Float,
    gradientProgress: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxRadius = constraints.maxWidth
                .coerceAtLeast(constraints.maxHeight)
                .toFloat() * 1.25f
            val radius = maxRadius * (0.2f + gradientProgress * 0.8f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Green300.copy(alpha = 0.64f * gradientProgress),
                                Green300.copy(alpha = 0.18f * gradientProgress),
                                Color.Transparent
                            ),
                            center = Offset(0f, constraints.maxHeight.toFloat()),
                            radius = radius
                        )
                    )
            )
        }

        Image(
            painter = painterResource(id = R.drawable.ic_logo_background_empty),
            contentDescription = stringResource(R.string.login_logo_content_description),
            modifier = Modifier
                .align(Alignment.Center)
                .size(AppEntryLogoSize)
                .graphicsLayer {
                    scaleX = logoScale
                    scaleY = logoScale
                }
        )
    }
}

@Preview(showBackground = true, name = "App Entry")
@Composable
private fun AppEntryRoutePreview() {
    PassedPathTheme {
        AppEntryScreen(
            logoScale = 1f,
            gradientProgress = 1f
        )
    }
}

private val AppEntryLogoSize = 104.dp
private const val AppEntryGradientExpansionDurationMillis = 850
private const val AppEntryLogoBreathingDelayMillis = 220L
private const val AppEntryLogoBreathingDurationMillis = 650
