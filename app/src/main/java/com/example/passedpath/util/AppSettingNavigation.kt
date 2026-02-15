package com.example.passedpath.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

// 설정 이동 유틸
object AppSettingsNavigator {

    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}