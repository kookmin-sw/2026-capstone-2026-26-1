package com.example.passedpath.feature.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

// 실제 화면이 아니라, 앱 진입시 흐름을 분기하는 Gate
// 위치 권한을 OS 기준으로만 판별하는 유틸

object LocationPermissionGate {

    // 포그라운드 위치 권한 허용 여부 확인
    fun isForegroundGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 백그라운드 위치가 ALWAYS 상태인지 확인
    fun isBackgroundAlwaysGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 9 이하는 포그라운드 허용이면 항상 허용(ALWAYS)으로 간주
            isForegroundGranted(context)
        }
    }
}


