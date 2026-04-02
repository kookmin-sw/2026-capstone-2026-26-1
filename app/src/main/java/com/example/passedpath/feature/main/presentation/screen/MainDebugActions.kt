package com.example.passedpath.feature.main.presentation.screen

data class MainDebugActions(
    val refreshSystemState: () -> Unit,
    val reloadRoute: () -> Unit
)
