package com.example.passedpath.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.passedpath.ui.component.BaseButton
import com.example.passedpath.ui.component.BaseButtonVariant
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.Pretendard

@Composable
fun PermissionSettingDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        PermissionSettingDialogContent(
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun PermissionSettingDialogContent(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = Color.White,
            tonalElevation = 2.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    fontSize = 14.sp,
                    fontFamily = Pretendard,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(20.dp))

                Image(
                    painter = painterResource(id = R.drawable.img_go_setting),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    InstructionStep(
                        step = 1,
                        text = stringResource(R.string.permission_dialog_step_1)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InstructionStep(
                        step = 2,
                        text = stringResource(R.string.permission_dialog_step_2)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InstructionStep(
                        step = 3,
                        text = stringResource(R.string.permission_dialog_step_3_prefix)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                BaseButton(
                    text = stringResource(R.string.permission_dialog_open_settings),
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                BaseButton(
                    text = stringResource(R.string.permission_dialog_not_now),
                    onClick = onDismiss,
                    variant = BaseButtonVariant.TEXT_ONLY
                )
            }
        }
    }
}

@Composable
private fun InstructionStep(
    step: Int,
    text: String,
    highlightText: String? = null,
    suffixText: String? = null
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("$step ")
            }
            append(text)
            if (highlightText != null) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                    append(highlightText)
                }
            }
            if (suffixText != null) {
                append(suffixText)
            }
        },
        fontSize = 15.sp,
        fontFamily = Pretendard,
        color = Color.Black
    )
}

@Preview(showBackground = true, name = "Permission Setting Dialog")
@Composable
private fun PermissionSettingDialogPreview() {
    PassedPathTheme {
        PermissionSettingDialogContent(
            onConfirm = {},
            onDismiss = {}
        )
    }
}

