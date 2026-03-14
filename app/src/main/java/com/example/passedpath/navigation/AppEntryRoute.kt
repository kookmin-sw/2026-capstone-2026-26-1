package com.example.passedpath.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.app.appContainer

@Composable
fun AppEntryRoute(
    onResolved: (String) -> Unit,
    viewModel: AppEntryViewModel = viewModel(
        factory = AppEntryViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        val readyState = state as? AppEntryState.Ready ?: return@LaunchedEffect
        onResolved(readyState.destination)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
