package com.example.passedpath.feature.locationtracking.domain.policy

import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object LocationPersistencePolicy {
    const val MIN_SAVE_DISTANCE_METERS = 35.0
    const val MAX_ACCEPTABLE_ACCURACY_METERS = 35f
    private const val EarthRadiusMeters = 6_371_000.0

    fun shouldPersistLocation(
        latestSavedLocation: TrackedLocation?,
        candidateLocation: TrackedLocation
    ): Boolean {
        if (
            candidateLocation.accuracyMeters != null &&
            candidateLocation.accuracyMeters > MAX_ACCEPTABLE_ACCURACY_METERS
        ) {
            return false
        }

        if (latestSavedLocation == null) {
            return true
        }

        val movedDistanceMeters = distanceBetweenMeters(latestSavedLocation, candidateLocation)
        return movedDistanceMeters >= requiredSaveDistanceMeters(candidateLocation)
    }

    fun requiredSaveDistanceMeters(candidateLocation: TrackedLocation): Double {
        val accuracyBasedDistance = (candidateLocation.accuracyMeters ?: 0f) * 1.5
        return maxOf(MIN_SAVE_DISTANCE_METERS, accuracyBasedDistance.toDouble())
    }

    private fun distanceBetweenMeters(
        start: TrackedLocation,
        end: TrackedLocation
    ): Double {
        val startLatitudeRadians = Math.toRadians(start.latitude)
        val endLatitudeRadians = Math.toRadians(end.latitude)
        val deltaLatitudeRadians = Math.toRadians(end.latitude - start.latitude)
        val deltaLongitudeRadians = Math.toRadians(end.longitude - start.longitude)

        val haversine =
            sin(deltaLatitudeRadians / 2).pow(2) +
                cos(startLatitudeRadians) * cos(endLatitudeRadians) *
                sin(deltaLongitudeRadians / 2).pow(2)
        val angularDistance = 2 * atan2(sqrt(haversine), sqrt(1 - haversine))
        return EarthRadiusMeters * angularDistance
    }
}
