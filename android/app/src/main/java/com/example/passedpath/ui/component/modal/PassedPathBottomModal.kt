package com.example.passedpath.ui.component.modal

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PassedPathBottomModal(
    onDimClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBackPress: () -> Unit = onDimClick,
    floatingBottomContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onBackPress,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val dimInteractionSource = remember { MutableInteractionSource() }
        val modalVisibleState = remember {
            MutableTransitionState(false).apply {
                targetState = true
            }
        }

        fun clearDialogInputFocus() {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }

        BackHandler {
            clearDialogInputFocus()
            onBackPress()
        }

        Box(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = dimInteractionSource,
                        indication = null,
                        onClick = {
                            clearDialogInputFocus()
                            onDimClick()
                        }
                    )
            )
            AnimatedVisibility(
                visibleState = modalVisibleState,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(
                    animationSpec = tween(durationMillis = BottomModalEnterTransitionMillis),
                    initialOffsetY = { fullHeight -> fullHeight }
                ) + fadeIn(animationSpec = tween(durationMillis = BottomModalEnterTransitionMillis)),
                exit = slideOutVertically(
                    animationSpec = tween(durationMillis = BottomModalExitTransitionMillis),
                    targetOffsetY = { fullHeight -> fullHeight }
                ) + fadeOut(animationSpec = tween(durationMillis = BottomModalExitTransitionMillis))
            ) {
                content()
            }
            floatingBottomContent?.let { floatingContent ->
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    floatingContent()
                }
            }
        }
    }
}

private const val BottomModalEnterTransitionMillis = 260
private const val BottomModalExitTransitionMillis = 180
