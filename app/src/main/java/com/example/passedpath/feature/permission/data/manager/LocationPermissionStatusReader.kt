package com.example.passedpath.feature.permission.data.manager

import android.content.Context

interface LocationPermissionStatusReader {
    fun isForegroundGranted(): Boolean
    fun isBackgroundAlwaysGranted(): Boolean
}

class AndroidLocationPermissionStatusReader(
    private val context: Context
) : LocationPermissionStatusReader {
    override fun isForegroundGranted(): Boolean {
        return LocationPermissionGate.isForegroundGranted(context)
    }

    override fun isBackgroundAlwaysGranted(): Boolean {
        return LocationPermissionGate.isBackgroundAlwaysGranted(context)
    }
}
