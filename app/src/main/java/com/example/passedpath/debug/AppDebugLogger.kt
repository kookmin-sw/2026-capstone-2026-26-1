package com.example.passedpath.debug

import android.util.Log
import com.example.passedpath.BuildConfig

object AppDebugLogger {

    fun debug(tag: String, message: String) {
        if (!BuildConfig.DEBUG) return

        try {
            Log.d(tag, message)
        } catch (_: RuntimeException) {
            println("[$tag] $message")
        }
    }
}

object DebugLogTag {
    const val MAIN_FLOW = "MainFlow"
    const val ROUTE_LOAD = "RouteLoad"
    const val TRACKING = "Tracking"
    const val PERMISSION = "Permission"
}
