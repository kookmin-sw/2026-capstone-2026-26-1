package com.example.passedpath.feature.permission.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.ui.PermissionSettingDialog
import com.example.passedpath.ui.component.button.BaseButton
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun LocationPermissionIntroScreen(
    showSettingsDialog: Boolean,
    onContinueClick: () -> Unit,
    onDialogConfirm: () -> Unit,
    onDialogDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        PermissionIntroHeader(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp, start = 24.dp, end = 24.dp)
        )

        PermissionIntroIllustration(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 118.dp)
        )

        PermissionIntroBottomAction(
            onContinueClick = onContinueClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 24.dp, end = 24.dp, bottom = 34.dp)
        )
    }

    if (showSettingsDialog) {
        PermissionSettingDialog(
            onConfirm = onDialogConfirm,
            onDismiss = onDialogDismiss
        )
    }
}

@Composable
private fun PermissionIntroHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.permission_intro_title),
            color = Gray900,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            )
        )

        Text(
            text = stringResource(R.string.permission_intro_description),
            color = Gray500,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp,
                color = Gray400
            ),
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}

@Composable
private fun PermissionIntroIllustration(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.location_permission_intro_image),
        contentDescription = stringResource(R.string.permission_intro_image_content_description),
        contentScale = ContentScale.Fit,
        modifier = modifier.size(360.dp)
    )
}

@Composable
private fun PermissionIntroBottomAction(
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseButton(
        text = stringResource(R.string.permission_intro_continue),
        onClick = onContinueClick,
        modifier = modifier
    )
}

@Preview(showBackground = true, name = "Light")
@Composable
private fun LocationPermissionIntroScreenPreview() {
    PassedPathTheme {
        LocationPermissionIntroScreen(
            showSettingsDialog = false,
            onContinueClick = {},
            onDialogConfirm = {},
            onDialogDismiss = {}
        )
    }
}
