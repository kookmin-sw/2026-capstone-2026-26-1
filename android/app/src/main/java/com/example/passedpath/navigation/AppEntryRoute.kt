package com.example.passedpath.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.ui.theme.Green300

@Composable
fun AppEntryRoute(
    onResolved: (String) -> Unit,
    viewModel: AppEntryViewModel
) {
    val state by viewModel.state.collectAsState()
    val logoTransition = rememberInfiniteTransition(label = "intro-logo")
    val logoScale by logoTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1100,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "intro-logo-scale"
    )

    LaunchedEffect(state) {
        val readyState = state as? AppEntryState.Ready ?: return@LaunchedEffect
        onResolved(readyState.destination)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Green300,
                                Color(0x00E2F8F6)
                            ),
                            center = Offset(0f, constraints.maxHeight.toFloat()),
                            radius = 1000f
                        )
                    )
            )
        }

        Image(
            painter = painterResource(id = R.drawable.ic_logo_background_empty),
            contentDescription = stringResource(R.string.login_logo_content_description),
            modifier = Modifier
                .align(Alignment.Center)
                .size(104.dp)
                .graphicsLayer {
                    scaleX = logoScale
                    scaleY = logoScale
                }
        )
    }
}
