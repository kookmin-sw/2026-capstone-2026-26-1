package com.example.passedpath.feature.main.data.manager

import android.annotation.SuppressLint
import android.content.Context
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CurrentLocationProvider(
    context: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): MainCoordinateUiState? = suspendCoroutine { continuation ->
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient
            .getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )
            .addOnSuccessListener { location ->
                continuation.resume(
                    location?.let {
                        MainCoordinateUiState(
                            latitude = it.latitude,
                            longitude = it.longitude
                        )
                    }
                )
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }
}
