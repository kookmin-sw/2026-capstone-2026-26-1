package com.example.passedpath.feature.locationtracking.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.passedpath.feature.locationtracking.data.local.dao.DayRouteDao
import com.example.passedpath.feature.locationtracking.data.local.dao.GpsPointDao
import com.example.passedpath.feature.locationtracking.data.local.entity.DayRouteEntity
import com.example.passedpath.feature.locationtracking.data.local.entity.GpsPointEntity

@Database(
    entities = [GpsPointEntity::class, DayRouteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PassedPathDatabase : RoomDatabase() {
    abstract fun gpsPointDao(): GpsPointDao
    abstract fun dayRouteDao(): DayRouteDao
}
