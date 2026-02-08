package com.example.passedpath.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.passedpath.R
import com.example.passedpath.ui.component.AppButton
import com.example.passedpath.ui.component.ButtonVariant
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.Pretendard

@Composable
fun PermissionSettingDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Title
            Text(
                text = "이렇게 설정해 주세요",
                fontSize = 24.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "앱이 종료되어도 지나온 길을 기록할 수 있어요",
                fontSize = 16.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(36.dp))

            // Image
            Image(
                painter = painterResource(id = R.drawable.setting_image),
                contentDescription = "Location permission setting guide",
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(Color(0xFFF0F2F5), RoundedCornerShape(16.dp))
                    .padding(vertical = 24.dp)
            )
            Spacer(modifier = Modifier.height(36.dp))

            // Instructions
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                InstructionStep(
                    step = 1,
                    text = "아래에서 ‘설정 열기’를 눌러 주세요"
                )
                Spacer(modifier = Modifier.height(16.dp))
                InstructionStep(
                    step = 2,
                    text = "어플리케이션 > 앱 권한 > 위치에서"
                )
                Spacer(modifier = Modifier.height(16.dp))
                InstructionStep(
                    step = 3,
                    text = "위치 접근을 ",
                    highlightText = "‘항상 허용’",
                    suffixText = "으로 바꿔 주세요"
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // "설정 열기" button
            AppButton(
                text = "설정 열기",
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp)) // Spacer between buttons

            // "나중에 할게요" button
            AppButton(
                text = "나중에 할게요",
                onClick = onDismiss,
                modifier = Modifier,
                variant = ButtonVariant.TEXT_ONLY
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InstructionStep(step: Int, text: String, highlightText: String? = null, suffixText: String? = null) {
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(
                color = Color(0xFF1ABC9C),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )) {
                append("$step ")
            }
            append(text)
            if (highlightText != null) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(highlightText)
                }
            }
            if (suffixText != null) {
                append(suffixText)
            }
        },
        fontSize = 16.sp,
        fontFamily = Pretendard,
        color = Color.Black
    )
}

/* ---------- Preview ---------- */

@Preview(showBackground = true, name = "Permission Setting Dialog")
@Composable
private fun PermissionSettingDialogPreview() {
    PassedPathTheme {
        PermissionSettingDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}