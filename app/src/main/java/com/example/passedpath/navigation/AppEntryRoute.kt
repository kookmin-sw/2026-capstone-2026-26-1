package com.example.passedpath.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun AppEntryRoute(
    onResolved: (String) -> Unit,
    viewModel: AppEntryViewModel
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        val readyState = state as? AppEntryState.Ready ?: return@LaunchedEffect
        onResolved(readyState.destination)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    )
}
