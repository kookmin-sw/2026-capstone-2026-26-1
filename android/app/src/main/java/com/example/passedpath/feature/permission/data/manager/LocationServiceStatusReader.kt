package com.example.passedpath.feature.permission.data.manager

import android.content.Context
import android.location.LocationManager
import android.os.Build

interface LocationServiceStatusReader {
    fun isLocationServiceEnabled(): Boolean
}

class AndroidLocationServiceStatusReader(
    private val context: Context
) : LocationServiceStatusReader {
    override fun isLocationServiceEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }
}
