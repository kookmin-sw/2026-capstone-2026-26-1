package com.example.passedpath.ui.component.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.passedpath.ui.component.button.BaseButton
import com.example.passedpath.ui.component.button.BaseButtonVariant
import com.example.passedpath.ui.theme.Gray300
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green50
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White

@Composable
fun BaseConfirmDialog(
    title: String,
    message: String,
    dismissText: String,
    confirmText: String,
    topContent: (@Composable () -> Unit)? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = White,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                topContent?.invoke()
                Text(
                    text = title,
                    color = Gray900,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = message,
                    color = Gray400,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 34.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BaseButton(
                        text = dismissText,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        variant = BaseButtonVariant.SECONDARY,
                        border = BorderStroke(1.dp, Gray300),
                        containerColor = White,
                        contentColor = Gray400
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
            message = "변경사항을 저장하지 않으면 사라집니다",
            dismissText = "취소",
            confirmText = "저장",
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun BaseConfirmDialogWithTopContentPreview() {
    PassedPathTheme {
        BaseConfirmDialog(
            title = "이 장소를 삭제할까요?",
            message = "선택한 날짜에서 이 장소만 삭제돼요",
            dismissText = "취소",
            confirmText = "삭제",
            topContent = {
                ConfirmDialogPlaceNamePill(placeName = "국민대학교 복지관")
            },
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Composable
private fun ConfirmDialogPlaceNamePill(placeName: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Green50)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = placeName,
            color = Green500,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
