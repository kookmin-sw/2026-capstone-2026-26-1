package com.example.passedpath.feature.permission.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.ui.PermissionSettingDialog
import com.example.passedpath.ui.component.AppButton
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun LocationPermissionIntroScreen(
    showSettingsDialog: Boolean,
    onContinueClick: () -> Unit,
    onDialogConfirm: () -> Unit,
    onDialogDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(R.string.permission_intro_title))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = stringResource(R.string.permission_intro_description))
        }

        Image(
            painter = painterResource(id = R.drawable.location_perrmission_intro_image),
            contentDescription = stringResource(R.string.permission_intro_image_content_description),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 48.dp)
        )

        AppButton(
            text = stringResource(R.string.permission_intro_continue),
            onClick = onContinueClick,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }

    if (showSettingsDialog) {
        PermissionSettingDialog(
            onConfirm = onDialogConfirm,
            onDismiss = onDialogDismiss
        )
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
private fun LocationPermissionIntroScreenPreview() {
    PassedPathTheme {
        LocationPermissionIntroScreen(
            showSettingsDialog = true,
            onContinueClick = {},
            onDialogConfirm = {},
            onDialogDismiss = {}
        )
    }
}
