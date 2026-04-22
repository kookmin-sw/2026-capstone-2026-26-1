package com.example.passedpath.app

import android.content.Context
import androidx.room.Room
import com.example.passedpath.data.datastore.AuthSessionStorage
import com.example.passedpath.data.network.RetrofitClient
import com.example.passedpath.debug.TrackingDiagnosticsLogger
import com.example.passedpath.feature.auth.data.manager.AuthTokenManager
import com.example.passedpath.feature.auth.data.remote.api.AuthApi
import com.example.passedpath.feature.auth.data.repository.AuthRepository
import com.example.passedpath.feature.bookmark.data.remote.api.DayRouteBookmarkApi
import com.example.passedpath.feature.bookmark.data.repository.DayRouteBookmarkRepositoryImpl
import com.example.passedpath.feature.bookmark.domain.repository.DayRouteBookmarkRepository
import com.example.passedpath.feature.bookmark.domain.usecase.ToggleDayRouteBookmarkUseCase
import com.example.passedpath.feature.daynote.data.remote.api.DayRouteMemoApi
import com.example.passedpath.feature.daynote.data.remote.api.DayRouteTitleApi
import com.example.passedpath.feature.daynote.data.repository.DayRouteMemoRepositoryImpl
import com.example.passedpath.feature.daynote.data.repository.DayRouteTitleRepositoryImpl
import com.example.passedpath.feature.daynote.domain.repository.DayRouteMemoRepository
import com.example.passedpath.feature.daynote.domain.repository.DayRouteTitleRepository
import com.example.passedpath.feature.daynote.domain.usecase.PatchDayRouteMemoUseCase
import com.example.passedpath.feature.daynote.domain.usecase.PatchDayRouteTitleUseCase
import com.example.passedpath.feature.locationtracking.data.local.PassedPathDatabase
import com.example.passedpath.feature.locationtracking.data.manager.AndroidNetworkConnectivityObserver
import com.example.passedpath.feature.locationtracking.data.manager.LocationTrackingServiceStateReader
import com.example.passedpath.feature.locationtracking.data.manager.LocationTrackingServiceStateWriter
import com.example.passedpath.feature.locationtracking.data.manager.NetworkConnectivityObserver
import com.example.passedpath.feature.locationtracking.data.manager.PersistentLocationTrackingServiceStateHolder
import com.example.passedpath.feature.locationtracking.data.manager.TrackingLocationProvider
import com.example.passedpath.feature.locationtracking.data.remote.api.DayRouteApi
import com.example.passedpath.feature.locationtracking.data.repository.RoomDayRouteRepository
import com.example.passedpath.feature.locationtracking.data.repository.RoomTrackingDebugLogRepository
import com.example.passedpath.feature.locationtracking.data.repository.RoomLocationTrackingRepository
import com.example.passedpath.feature.locationtracking.domain.policy.FixedTrackingDayBoundaryTimeProvider
import com.example.passedpath.feature.locationtracking.domain.policy.TrackingDateKeyResolver
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.LocationTrackingRepository
import com.example.passedpath.feature.locationtracking.domain.repository.TrackingDebugLogRepository
import com.example.passedpath.feature.locationtracking.domain.tracker.LocationTracker
import com.example.passedpath.feature.locationtracking.domain.usecase.CleanupTrackingLocalDataUseCase
import com.example.passedpath.feature.locationtracking.domain.usecase.HandleTrackedLocationUseCase
import com.example.passedpath.feature.locationtracking.domain.usecase.ObserveRecentTrackingEventsUseCase
import com.example.passedpath.feature.locationtracking.domain.usecase.StartLocationTrackingUseCase
import com.example.passedpath.feature.locationtracking.domain.usecase.StopLocationTrackingUseCase
import com.example.passedpath.feature.locationtracking.domain.usecase.UploadGpsPointsBatchUseCase
import com.example.passedpath.feature.main.data.manager.CurrentLocationProvider
import com.example.passedpath.feature.main.data.repository.TestRepository
import com.example.passedpath.feature.permission.data.manager.AndroidLocationPermissionStatusReader
import com.example.passedpath.feature.permission.data.manager.AndroidLocationServiceStatusReader
import com.example.passedpath.feature.permission.data.manager.LocationPermissionStatusReader
import com.example.passedpath.feature.permission.data.manager.LocationServiceStatusReader
import com.example.passedpath.feature.place.data.remote.api.PlaceApi
import com.example.passedpath.feature.place.data.repository.PlaceRepositoryImpl
import com.example.passedpath.feature.place.domain.repository.PlaceRepository
import com.example.passedpath.feature.place.domain.usecase.AddPlaceUseCase
import com.example.passedpath.feature.place.domain.usecase.DeletePlaceUseCase
import com.example.passedpath.feature.place.domain.usecase.GetVisitedPlacesUseCase
import com.example.passedpath.feature.place.domain.usecase.ReorderPlacesUseCase
import com.example.passedpath.feature.place.domain.usecase.UpdateBookmarkPlaceUseCase
import com.example.passedpath.feature.place.domain.usecase.UpdatePlaceUseCase
import com.example.passedpath.interceptor.AccessTokenAuthenticator
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

    val locationServiceStatusReader: LocationServiceStatusReader by lazy {
        AndroidLocationServiceStatusReader(appContext)
    }

    val currentLocationTracker: LocationTracker by lazy {
        CurrentLocationProvider(appContext)
    }

    val trackingLocationTracker: LocationTracker by lazy {
        TrackingLocationProvider(appContext)
    }

    val networkConnectivityObserver: NetworkConnectivityObserver by lazy {
        AndroidNetworkConnectivityObserver(appContext)
    }

    private val locationTrackingServiceStateHolder by lazy {
        PersistentLocationTrackingServiceStateHolder(appContext)
    }

    val locationTrackingServiceStateReader: LocationTrackingServiceStateReader by lazy {
        locationTrackingServiceStateHolder
    }

    val locationTrackingServiceStateWriter: LocationTrackingServiceStateWriter by lazy {
        locationTrackingServiceStateHolder
    }

    private val trackingDatabase: PassedPathDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            PassedPathDatabase::class.java,
            "passed-path.db"
        )
            .addMigrations(PassedPathDatabase.MIGRATION_1_2)
            .build()
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

    private val refreshRetrofit by lazy {
        RetrofitClient.provideRetrofit(
            sessionStorage = authSessionStorage,
            attachAuthorizationToRefreshRequest = true
        )
    }

    private val refreshAuthApi by lazy {
        refreshRetrofit.create(AuthApi::class.java)
    }

    private val authTokenManager by lazy {
        AuthTokenManager(
            authApi = refreshAuthApi,
            sessionStorage = authSessionStorage
        )
    }

    private val retrofit by lazy {
        RetrofitClient.provideRetrofit(
            sessionStorage = authSessionStorage,
            authenticator = AccessTokenAuthenticator(
                sessionStorage = authSessionStorage,
                tokenManager = authTokenManager
            )
        )
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

    private val dayRouteBookmarkApi by lazy {
        retrofit.create(DayRouteBookmarkApi::class.java)
    }

    private val dayRouteTitleApi by lazy {
        retrofit.create(DayRouteTitleApi::class.java)
    }

    private val dayRouteMemoApi by lazy {
        retrofit.create(DayRouteMemoApi::class.java)
    }

    private val placeApi by lazy {
        retrofit.create(PlaceApi::class.java)
    }

    val trackingDebugLogRepository: TrackingDebugLogRepository by lazy {
        RoomTrackingDebugLogRepository(
            trackingDebugLogDao = trackingDatabase.trackingDebugLogDao()
        )
    }

    val trackingDiagnosticsLogger: TrackingDiagnosticsLogger by lazy {
        TrackingDiagnosticsLogger(repository = trackingDebugLogRepository)
    }

    val observeRecentTrackingEventsUseCase: ObserveRecentTrackingEventsUseCase by lazy {
        ObserveRecentTrackingEventsUseCase(trackingDebugLogRepository = trackingDebugLogRepository)
    }

    val cleanupTrackingLocalDataUseCase: CleanupTrackingLocalDataUseCase by lazy {
        CleanupTrackingLocalDataUseCase(
            gpsPointDao = trackingDatabase.gpsPointDao(),
            dayRouteDao = trackingDatabase.dayRouteDao(),
            trackingDebugLogDao = trackingDatabase.trackingDebugLogDao(),
            diagnosticsLogger = trackingDiagnosticsLogger
        )
    }

    val locationTrackingRepository: LocationTrackingRepository by lazy {
        RoomLocationTrackingRepository(
            gpsPointDao = trackingDatabase.gpsPointDao(),
            dayRouteDao = trackingDatabase.dayRouteDao(),
            dateKeyResolver = trackingDateKeyResolver,
            diagnosticsLogger = trackingDiagnosticsLogger
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
        StartLocationTrackingUseCase(
            context = appContext,
            trackingServiceStateWriter = locationTrackingServiceStateWriter
        )
    }

    val stopLocationTrackingUseCase: StopLocationTrackingUseCase by lazy {
        StopLocationTrackingUseCase(
            context = appContext,
            trackingServiceStateWriter = locationTrackingServiceStateWriter
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

    val dayRouteBookmarkRepository: DayRouteBookmarkRepository by lazy {
        DayRouteBookmarkRepositoryImpl(dayRouteBookmarkApi)
    }

    val dayRouteTitleRepository: DayRouteTitleRepository by lazy {
        DayRouteTitleRepositoryImpl(dayRouteTitleApi)
    }

    val dayRouteMemoRepository: DayRouteMemoRepository by lazy {
        DayRouteMemoRepositoryImpl(dayRouteMemoApi)
    }

    val placeRepository: PlaceRepository by lazy {
        PlaceRepositoryImpl(placeApi)
    }

    val uploadGpsPointsBatchUseCase: UploadGpsPointsBatchUseCase by lazy {
        UploadGpsPointsBatchUseCase(
            dayRouteApi = dayRouteApi,
            locationTrackingRepository = locationTrackingRepository,
            dayRouteRepository = dayRouteRepository,
            diagnosticsLogger = trackingDiagnosticsLogger
        )
    }

    val handleTrackedLocationUseCase: HandleTrackedLocationUseCase by lazy {
        HandleTrackedLocationUseCase(
            locationTrackingRepository = locationTrackingRepository,
            dateKeyResolver = trackingDateKeyResolver
        )
    }

    val toggleDayRouteBookmarkUseCase: ToggleDayRouteBookmarkUseCase by lazy {
        ToggleDayRouteBookmarkUseCase(dayRouteBookmarkRepository = dayRouteBookmarkRepository)
    }

    val patchDayRouteTitleUseCase: PatchDayRouteTitleUseCase by lazy {
        PatchDayRouteTitleUseCase(dayRouteTitleRepository = dayRouteTitleRepository)
    }

    val patchDayRouteMemoUseCase: PatchDayRouteMemoUseCase by lazy {
        PatchDayRouteMemoUseCase(dayRouteMemoRepository = dayRouteMemoRepository)
    }

    val addPlaceUseCase: AddPlaceUseCase by lazy {
        AddPlaceUseCase(placeRepository = placeRepository)
    }

    val getVisitedPlacesUseCase: GetVisitedPlacesUseCase by lazy {
        GetVisitedPlacesUseCase(placeRepository = placeRepository)
    }

    val deletePlaceUseCase: DeletePlaceUseCase by lazy {
        DeletePlaceUseCase(placeRepository = placeRepository)
    }

    val updatePlaceUseCase: UpdatePlaceUseCase by lazy {
        UpdatePlaceUseCase(placeRepository = placeRepository)
    }

    val updateBookmarkPlaceUseCase: UpdateBookmarkPlaceUseCase by lazy {
        UpdateBookmarkPlaceUseCase(placeRepository = placeRepository)
    }

    val reorderPlacesUseCase: ReorderPlacesUseCase by lazy {
        ReorderPlacesUseCase(placeRepository = placeRepository)
    }
}



