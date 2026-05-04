package com.example.passedpath.ui.component.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import com.example.passedpath.ui.theme.Gray200
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun BaseButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: BaseButtonVariant = BaseButtonVariant.PRIMARY,
    border: BorderStroke? = null,
    containerColor: Color? = null,
    contentColor: Color? = null,
    @DrawableRes leadingIconResId: Int? = null,
    textFontSize: TextUnit = TextUnit.Unspecified,
    textFontWeight: FontWeight? = null,
) {
    when (variant) {
        BaseButtonVariant.PRIMARY, BaseButtonVariant.SECONDARY -> {
            val backgroundColor = when (variant) {
                BaseButtonVariant.PRIMARY -> MaterialTheme.colorScheme.primary
                BaseButtonVariant.SECONDARY -> MaterialTheme.colorScheme.secondary
                BaseButtonVariant.TEXT_ONLY -> Color.Transparent
            }.let { containerColor ?: it }
            val contentColor = when (variant) {
                BaseButtonVariant.PRIMARY -> MaterialTheme.colorScheme.onPrimary
                BaseButtonVariant.SECONDARY -> MaterialTheme.colorScheme.onSecondary
                BaseButtonVariant.TEXT_ONLY -> Color.Unspecified
            }.let { contentColor ?: it }

            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                border = border,
                colors = ButtonDefaults.buttonColors(
                    containerColor = backgroundColor,
                    contentColor = contentColor,
                    disabledContainerColor = Gray200,
                    disabledContentColor = Gray400
                )
            ) {
                BaseButtonContent(
                    text = text,
                    leadingIconResId = leadingIconResId,
                    textFontSize = textFontSize,
                    textFontWeight = textFontWeight
                )
            }
        }

        BaseButtonVariant.TEXT_ONLY -> {
            TextButton(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier,
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = contentColor ?: Gray400,
                    disabledContentColor = Gray200
                )
            ) {
                BaseButtonContent(
                    text = text,
                    leadingIconResId = leadingIconResId,
                    textFontSize = textFontSize,
                    textFontWeight = textFontWeight
                )
            }
        }
    }
}

@Composable
private fun BaseButtonContent(
    text: String,
    @DrawableRes leadingIconResId: Int?,
    textFontSize: TextUnit,
    textFontWeight: FontWeight?
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIconResId?.let { iconResId ->
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            fontFamily = MaterialTheme.typography.labelLarge.fontFamily,
            fontSize = textFontSize,
            fontWeight = textFontWeight
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BaseButtonPreview() {
    PassedPathTheme {
        Column {
            BaseButton(
                text = "PRIMARY Button",
                onClick = {},
                variant = BaseButtonVariant.PRIMARY
            )
            Spacer(Modifier.height(8.dp))
            BaseButton(
                text = "SECONDARY Button",
                onClick = {},
                variant = BaseButtonVariant.SECONDARY
            )
            Spacer(Modifier.height(8.dp))
            BaseButton(
                text = "TEXT_ONLY Button",
                onClick = {},
                variant = BaseButtonVariant.TEXT_ONLY
            )
        }
    }
}
