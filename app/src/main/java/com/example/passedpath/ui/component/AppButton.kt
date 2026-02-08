package com.example.passedpath.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.ui.theme.Gray200
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.PassedPathTheme
import androidx.compose.foundation.layout.Spacer // Need to add this import

@Composable
fun AppButton(
    text: String,                    // 버튼에 표시할 텍스트
    onClick: () -> Unit,            // 버튼 클릭시 동작
    modifier: Modifier = Modifier,  // 수정자
    enabled: Boolean = true,        // 비활성 여부
    variant: ButtonVariant = ButtonVariant.PRIMARY, // 버튼 종류
) {
    when (variant) {
        ButtonVariant.PRIMARY, ButtonVariant.SECONDARY -> {
            val backgroundColor = when (variant) {
                ButtonVariant.PRIMARY -> MaterialTheme.colorScheme.primary
                ButtonVariant.SECONDARY -> MaterialTheme.colorScheme.secondary
                ButtonVariant.TEXT_ONLY -> Color.Transparent // Should not be reached with this structure, but for exhaustiveness
            }
            val contentColor = when (variant) {
                ButtonVariant.PRIMARY -> MaterialTheme.colorScheme.onPrimary
                ButtonVariant.SECONDARY -> MaterialTheme.colorScheme.onSecondary
                ButtonVariant.TEXT_ONLY -> Color.Unspecified // Should not be reached with this structure, but for exhaustiveness
            }

            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier
                    .fillMaxWidth()
                    .height(56.dp), // 가로 꽉차게, 높이 56dp
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = backgroundColor,
                    contentColor = contentColor,
                    disabledContainerColor = Gray200,
                    disabledContentColor = Gray400
                )
            ) {
                Text(
                    text = text,
                    fontFamily = MaterialTheme.typography.labelLarge.fontFamily
                )
            }
        }
        ButtonVariant.TEXT_ONLY -> {
            TextButton(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier, // Use the passed modifier directly, don't force fillWidth/height
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Transparent, // Transparent background
                    contentColor = Gray400, // Text color for TEXT_ONLY
                    disabledContentColor = Gray200
                )
            ) {
                Text(
                    text = text,
                    fontFamily = MaterialTheme.typography.labelLarge.fontFamily
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppButtonPreview() {
    PassedPathTheme {
        Column { // Wrap previews in a Column for better display
            AppButton(
                text = "PRIMARY 버튼",
                onClick = {},
                variant = ButtonVariant.PRIMARY
            )
            Spacer(Modifier.height(8.dp))
            AppButton(
                text = "SECONDARY 버튼",
                onClick = {},
                variant = ButtonVariant.SECONDARY
            )
            Spacer(Modifier.height(8.dp))
            AppButton(
                text = "TEXT_ONLY 버튼",
                onClick = {},
                variant = ButtonVariant.TEXT_ONLY
            )
        }
    }
}
