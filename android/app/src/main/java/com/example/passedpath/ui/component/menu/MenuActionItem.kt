package com.example.passedpath.ui.component.menu

import androidx.compose.ui.graphics.vector.ImageVector

data class MenuActionItem(
    val text: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)
