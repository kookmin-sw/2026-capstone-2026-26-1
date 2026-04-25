package com.example.passedpath.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.passedpath.ui.theme.Gray900

object FloatingButtonDefaults {
    val verticalSpacing = 12.dp
    val horizontalSpacing = 10.dp
    val containerColor = Color.White
    val iconTint = Gray900
}

@Composable
fun FloatingButtonColumn(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.End,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(FloatingButtonDefaults.verticalSpacing),
        content = content
    )
}

@Composable
fun FloatingButtonRow(
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(FloatingButtonDefaults.horizontalSpacing),
        verticalAlignment = verticalAlignment,
        content = content
    )
}

@Composable
fun FloatingCircleIconButton(
    onClick: () -> Unit,
    @DrawableRes iconResId: Int,
    @StringRes contentDescriptionResId: Int,
    modifier: Modifier = Modifier
) {
    BaseCircleButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = FloatingButtonDefaults.containerColor
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = stringResource(contentDescriptionResId),
            tint = FloatingButtonDefaults.iconTint
        )
    }
}
