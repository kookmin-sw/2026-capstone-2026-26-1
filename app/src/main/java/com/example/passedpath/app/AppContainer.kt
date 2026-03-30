package com.example.passedpath.app

import android.content.Context
import androidx.room.Room
import com.example.passedpath.data.datastore.AuthSessionStorage
import com.example.passedpath.data.network.RetrofitClient
import com.example.passedpath.feature.auth.data.manager.AuthTokenManager
import com.example.passedpath.feature.auth.data.remote.api.AuthApi
import com.example.passedpath.feature.auth.data.repository.AuthRepository
import com.example.passedpath.feature.locationtracking.data.local.PassedPathDatabase
import com.example.passedpath.feature.locationtracking.data.manager.TrackingLocationProvider
import com.example.passedpath.feature.locationtracking.data.remote.api.DayRouteApi
import com.example.passedpath.feature.locationtracking.data.repository.RoomDayRouteRepository
import com.example.passedpath.feature.locationtracking.data.repository.RoomLocationTrackingRepository
import com.example.passedpath.feature.locationtracking.domain.policy.FixedTrackingDayBoundaryTimeProvider
import com.example.passedpath.feature.locationtracking.domain.policy.TrackingDateKeyResolver
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.LocationTrackingRepository
import com.example.passedpath.feature.locationtracking.domain.tracker.LocationTracker
import com.example.passedpath.feature.locationtracking.domain.usecase.StartLocationTrackingUseCase
import com.example.passedpath.feature.locationtracking.domain.usecase.StopLocationTrackingUseCase
import com.example.passedpath.feature.locationtracking.domain.usecase.UploadGpsPointsBatchUseCase
import com.example.passedpath.feature.main.data.manager.CurrentLocationProvider
import com.example.passedpath.feature.main.data.repository.TestRepository
import com.example.passedpath.feature.permission.data.manager.AndroidLocationPermissionStatusReader
import com.example.passedpath.feature.permission.data.manager.LocationPermissionStatusReader
import java.time.LocalTime

class AppContainer(
    context: Context
) {
    private val appContext = context.applicationContext

    val authSessionStorage: AuthSessionStorage by lazy {
        AuthSessionStorage(appContext)
    }

    val locationPermissionStatusReader: LocationPermissionStatusReader by lazy {
        AndroidLocationPermissionStatusReader(appContext)
    }

    val currentLocationTracker: LocationTracker by lazy {
        CurrentLocationProvider(appContext)
    }

    val trackingLocationTracker: LocationTracker by lazy {
        TrackingLocationProvider(appContext)
    }

    private val trackingDatabase: PassedPathDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            PassedPathDatabase::class.java,
            "passed-path.db"
        ).build()
    }

    val trackingDayBoundaryTimeProvider by lazy {
        FixedTrackingDayBoundaryTimeProvider(
            boundaryLocalTime = LocalTime.MIDNIGHT
        )
    }

    val trackingDateKeyResolver by lazy {
        TrackingDateKeyResolver(
            boundaryTimeProvider = trackingDayBoundaryTimeProvider
        )
    }

    private val retrofit by lazy {
        RetrofitClient.provideRetrofit(authSessionStorage)
    }

    private val authApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    private val testApi by lazy {
        retrofit.create(com.example.passedpath.data.network.api.TestApi::class.java)
    }

    private val dayRouteApi by lazy {
        retrofit.create(DayRouteApi::class.java)
    }

    val locationTrackingRepository: LocationTrackingRepository by lazy {
        RoomLocationTrackingRepository(
            gpsPointDao = trackingDatabase.gpsPointDao(),
            dayRouteDao = trackingDatabase.dayRouteDao(),
            dateKeyResolver = trackingDateKeyResolver
        )
    }

    val dayRouteRepository: DayRouteRepository by lazy {
        RoomDayRouteRepository(
            dayRouteDao = trackingDatabase.dayRouteDao(),
            gpsPointDao = trackingDatabase.gpsPointDao(),
            dayRouteApi = dayRouteApi
        )
    }

    val startLocationTrackingUseCase: StartLocationTrackingUseCase by lazy {
        StartLocationTrackingUseCase(appContext)
    }

    val stopLocationTrackingUseCase: StopLocationTrackingUseCase by lazy {
        StopLocationTrackingUseCase(appContext)
    }

    private val authTokenManager by lazy {
        AuthTokenManager(
            authApi = authApi,
            sessionStorage = authSessionStorage
        )
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(
            authApi = authApi,
            tokenManager = authTokenManager,
            sessionStorage = authSessionStorage
        )
    }

    val testRepository: TestRepository by lazy {
        TestRepository(testApi)
    }

    val uploadGpsPointsBatchUseCase: UploadGpsPointsBatchUseCase by lazy {
        UploadGpsPointsBatchUseCase(
            dayRouteApi = dayRouteApi,
            locationTrackingRepository = locationTrackingRepository,
            dayRouteRepository = dayRouteRepository
        )
    }
}
