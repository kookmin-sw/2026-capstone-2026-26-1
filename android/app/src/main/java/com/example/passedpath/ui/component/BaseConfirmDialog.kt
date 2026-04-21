package com.example.passedpath.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray700
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun BaseConfirmDialog(
    title: String,
    message: String,
    dismissText: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    color = Gray700,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = message,
                    color = Gray400
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BaseButton(
                        text = dismissText,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        variant = BaseButtonVariant.TEXT_ONLY
                    )
                    BaseButton(
                        text = confirmText,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun BaseConfirmDialogPreview() {
    PassedPathTheme {
        BaseConfirmDialog(
            title = "변경 사항을 저장할까요?",
            message = "저장하지 않으면 작성 중인 내용이 사라집니다.",
            dismissText = "취소",
            confirmText = "저장",
            onDismiss = {},
            onConfirm = {}
        )
    }
}
