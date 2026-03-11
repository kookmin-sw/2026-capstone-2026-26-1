package com.example.passedpath.feature.permission.presentation.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.passedpath.R
import com.example.passedpath.feature.permission.data.manager.LocationPermissionGate
import com.example.passedpath.ui.PermissionSettingDialog
import com.example.passedpath.ui.component.AppButton
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.util.AppSettingsNavigator

@Composable
fun LocationPermissionIntroScreen(
    onPermissionResolved: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showPermissionDialog by remember { mutableStateOf(false) }
    var waitingForSettings by remember { mutableStateOf(false) }

    val foregroundPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (LocationPermissionGate.isBackgroundAlwaysGranted(context)) {
                onPermissionResolved()
            } else {
                showPermissionDialog = true
            }
        }

    DisposableEffect(lifecycleOwner, waitingForSettings) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && waitingForSettings) {
                waitingForSettings = false
                onPermissionResolved()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
            Text(text = "Location permission is needed")
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "To keep tracking your path in background, allow location access.")
        }

        Image(
            painter = painterResource(id = R.drawable.location_perrmission_intro_image),
            contentDescription = "Onboarding illustration for location permission",
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 48.dp)
        )

        AppButton(
            text = "Continue",
            onClick = {
                when {
                    LocationPermissionGate.isBackgroundAlwaysGranted(context) -> {
                        onPermissionResolved()
                    }

                    !LocationPermissionGate.isForegroundGranted(context) -> {
                        foregroundPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }

                    else -> {
                        showPermissionDialog = true
                    }
                }
            },
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }

    if (showPermissionDialog) {
        PermissionSettingDialog(
            onConfirm = {
                showPermissionDialog = false
                waitingForSettings = true
                AppSettingsNavigator.openAppSettings(context)
            },
            onDismiss = {
                showPermissionDialog = false
                onPermissionResolved()
            }
        )
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
private fun LocationPermissionIntroScreenPreview() {
    PassedPathTheme {
        LocationPermissionIntroScreen(onPermissionResolved = {})
    }
}
