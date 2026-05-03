package com.example.passedpath.ui.component.modal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

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
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        val dimInteractionSource = remember { MutableInteractionSource() }

        Box(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = dimInteractionSource,
                        indication = null,
                        onClick = onDimClick
                    )
            )
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
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
