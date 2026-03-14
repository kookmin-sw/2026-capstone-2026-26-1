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
import androidx.compose.ui.res.stringResource
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

            Text(
                text = stringResource(R.string.permission_dialog_title),
                fontSize = 24.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.permission_dialog_subtitle),
                fontSize = 16.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(36.dp))

            Image(
                painter = painterResource(id = R.drawable.location_perrmission_intro_image),
                contentDescription = stringResource(R.string.permission_dialog_image_content_description),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(Color(0xFFF0F2F5), RoundedCornerShape(16.dp))
                    .padding(vertical = 24.dp)
            )
            Spacer(modifier = Modifier.height(36.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                InstructionStep(
                    step = 1,
                    text = stringResource(R.string.permission_dialog_step_1)
                )
                Spacer(modifier = Modifier.height(16.dp))
                InstructionStep(
                    step = 2,
                    text = stringResource(R.string.permission_dialog_step_2)
                )
                Spacer(modifier = Modifier.height(16.dp))
                InstructionStep(
                    step = 3,
                    text = stringResource(R.string.permission_dialog_step_3_prefix),
                    highlightText = stringResource(R.string.permission_dialog_step_3_highlight),
                    suffixText = stringResource(R.string.permission_dialog_step_3_suffix)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            AppButton(
                text = stringResource(R.string.permission_dialog_open_settings),
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            AppButton(
                text = stringResource(R.string.permission_dialog_not_now),
                onClick = onDismiss,
                modifier = Modifier,
                variant = ButtonVariant.TEXT_ONLY
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InstructionStep(
    step: Int,
    text: String,
    highlightText: String? = null,
    suffixText: String? = null
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = Color(0xFF1ABC9C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            ) {
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
