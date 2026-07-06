package com.example.passedpath.app

import android.content.Context
import com.example.passedpath.debug.AppDebugLogger
import com.example.passedpath.debug.DebugLogTag
import com.google.android.gms.maps.MapsInitializer

enum class GoogleMapsRendererStatus {
    UNKNOWN,
    LATEST,
    LEGACY
}

object GoogleMapsRendererStateHolder {
    @Volatile
    var status: GoogleMapsRendererStatus = GoogleMapsRendererStatus.UNKNOWN
        private set

    fun markInitialized(renderer: MapsInitializer.Renderer?) {
        status = when (renderer) {
            MapsInitializer.Renderer.LATEST -> GoogleMapsRendererStatus.LATEST
            MapsInitializer.Renderer.LEGACY -> GoogleMapsRendererStatus.LEGACY
            null -> GoogleMapsRendererStatus.UNKNOWN
        }
    }

    fun markInitializationFailed() {
        status = GoogleMapsRendererStatus.UNKNOWN
    }
}

object GoogleMapsRendererInitializer {
    fun initialize(context: Context) {
        try {
            MapsInitializer.initialize(
                context.applicationContext,
                MapsInitializer.Renderer.LATEST
            ) { renderer ->
                GoogleMapsRendererStateHolder.markInitialized(renderer)
                AppDebugLogger.debug(
                    DebugLogTag.MAP_RENDERER,
                    "Google Maps renderer initialized: requested=LATEST, actual=${GoogleMapsRendererStateHolder.status}"
                )
            }
        } catch (exception: Exception) {
            GoogleMapsRendererStateHolder.markInitializationFailed()
            AppDebugLogger.debug(
                DebugLogTag.MAP_RENDERER,
                "Google Maps renderer initialization failed: ${exception.javaClass.simpleName}"
            )
        }
    }
}
