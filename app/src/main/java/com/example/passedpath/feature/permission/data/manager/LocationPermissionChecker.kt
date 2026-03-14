package com.example.passedpath.feature.permission.data.manager

import android.content.Context

class LocationPermissionChecker(
    private val context: Context
) {
    fun isForegroundGranted(): Boolean {
        return LocationPermissionGate.isForegroundGranted(context)
    }

    fun isBackgroundAlwaysGranted(): Boolean {
        return LocationPermissionGate.isBackgroundAlwaysGranted(context)
    }
}
