package com.example.passedpath.ui.component.menu

import androidx.annotation.DrawableRes

data class MenuActionItem(
    val text: String,
    @DrawableRes val iconResId: Int,
    val onClick: () -> Unit,
)
