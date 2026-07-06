package com.example.passedpath.feature.locationtracking.domain.policy

import kotlin.math.cos
import kotlin.math.hypot

object MapPolylineSimplificationPolicy {
    const val MAX_MAP_POLYLINE_POINTS = 800

    fun simplifyIndexes(
        size: Int,
        latitudeAt: (Int) -> Double,
        longitudeAt: (Int) -> Double
    ): List<Int> {
        if (size <= MAX_MAP_POLYLINE_POINTS) return List(size) { it }
        if (size <= 2) return List(size) { it }

        var lowToleranceMeters = 0.0
        var highToleranceMeters = INITIAL_POLYLINE_SIMPLIFICATION_TOLERANCE_METERS
        var bestIndexes = emptyList<Int>()

        while (highToleranceMeters <= MAX_POLYLINE_SIMPLIFICATION_TOLERANCE_METERS) {
            val indexes = ramerDouglasPeuckerIndexes(
                size = size,
                latitudeAt = latitudeAt,
                longitudeAt = longitudeAt,
                toleranceMeters = highToleranceMeters
            )
            if (indexes.size <= MAX_MAP_POLYLINE_POINTS) {
                bestIndexes = indexes
                break
            }
            lowToleranceMeters = highToleranceMeters
            highToleranceMeters *= 2
        }

        repeat(POLYLINE_SIMPLIFICATION_SEARCH_ITERATIONS) {
            if (bestIndexes.isEmpty()) return@repeat
            val midToleranceMeters = (lowToleranceMeters + highToleranceMeters) / 2
            val indexes = ramerDouglasPeuckerIndexes(
                size = size,
                latitudeAt = latitudeAt,
                longitudeAt = longitudeAt,
                toleranceMeters = midToleranceMeters
            )
            if (indexes.size <= MAX_MAP_POLYLINE_POINTS) {
                bestIndexes = indexes
                highToleranceMeters = midToleranceMeters
            } else {
                lowToleranceMeters = midToleranceMeters
            }
        }

        return if (bestIndexes.isEmpty()) {
            evenlyCapIndexes(List(size) { it }, MAX_MAP_POLYLINE_POINTS)
        } else {
            evenlyCapIndexes(bestIndexes, MAX_MAP_POLYLINE_POINTS)
        }
    }

    private fun ramerDouglasPeuckerIndexes(
        size: Int,
        latitudeAt: (Int) -> Double,
        longitudeAt: (Int) -> Double,
        toleranceMeters: Double
    ): List<Int> {
        val keep = BooleanArray(size)
        keep[0] = true
        keep[size - 1] = true

        val ranges = ArrayDeque<Pair<Int, Int>>()
        ranges.add(0 to size - 1)

        while (ranges.isNotEmpty()) {
            val (startIndex, endIndex) = ranges.removeLast()
            if (endIndex <= startIndex + 1) continue

            var farthestIndex = -1
            var farthestDistanceMeters = 0.0
            for (index in startIndex + 1 until endIndex) {
                val distanceMeters = perpendicularDistanceMeters(
                    latitude = latitudeAt(index),
                    longitude = longitudeAt(index),
                    startLatitude = latitudeAt(startIndex),
                    startLongitude = longitudeAt(startIndex),
                    endLatitude = latitudeAt(endIndex),
                    endLongitude = longitudeAt(endIndex)
                )
                if (distanceMeters > farthestDistanceMeters) {
                    farthestDistanceMeters = distanceMeters
                    farthestIndex = index
                }
            }

            if (farthestDistanceMeters > toleranceMeters && farthestIndex != -1) {
                keep[farthestIndex] = true
                ranges.add(startIndex to farthestIndex)
                ranges.add(farthestIndex to endIndex)
            }
        }

        return buildList {
            keep.forEachIndexed { index, shouldKeep ->
                if (shouldKeep) add(index)
            }
        }
    }

    private fun perpendicularDistanceMeters(
        latitude: Double,
        longitude: Double,
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): Double {
        val meanLatitudeRadians = Math.toRadians((latitude + startLatitude + endLatitude) / 3.0)
        val pointX = Math.toRadians(longitude - startLongitude) *
            cos(meanLatitudeRadians) *
            EARTH_RADIUS_METERS
        val pointY = Math.toRadians(latitude - startLatitude) * EARTH_RADIUS_METERS
        val endX = Math.toRadians(endLongitude - startLongitude) *
            cos(meanLatitudeRadians) *
            EARTH_RADIUS_METERS
        val endY = Math.toRadians(endLatitude - startLatitude) * EARTH_RADIUS_METERS
        val segmentLengthSquared = endX * endX + endY * endY

        if (segmentLengthSquared == 0.0) return hypot(pointX, pointY)

        val projectionRatio = ((pointX * endX + pointY * endY) / segmentLengthSquared)
            .coerceIn(0.0, 1.0)
        val projectedX = projectionRatio * endX
        val projectedY = projectionRatio * endY
        return hypot(pointX - projectedX, pointY - projectedY)
    }

    private fun evenlyCapIndexes(indexes: List<Int>, maxSize: Int): List<Int> {
        if (indexes.size <= maxSize) return indexes
        if (maxSize <= 2) return listOf(indexes.first(), indexes.last()).distinct()

        val lastResultIndex = maxSize - 1
        val lastSourceIndex = indexes.lastIndex
        return List(maxSize) { resultIndex ->
            indexes[(resultIndex * lastSourceIndex) / lastResultIndex]
        }.distinct()
    }

    private const val INITIAL_POLYLINE_SIMPLIFICATION_TOLERANCE_METERS = 2.0
    private const val MAX_POLYLINE_SIMPLIFICATION_TOLERANCE_METERS = 10_000.0
    private const val POLYLINE_SIMPLIFICATION_SEARCH_ITERATIONS = 12
    private const val EARTH_RADIUS_METERS = 6_371_000.0
}
