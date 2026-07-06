package com.example.passedpath.app

import android.content.Context
import androidx.room.Room
import com.example.passedpath.BuildConfig
import com.example.passedpath.data.datastore.AuthSessionStorage
import com.example.passedpath.data.network.RetrofitClient
import com.example.passedpath.debug.TrackingDiagnosticsLogger
import com.example.passedpath.feature.auth.data.manager.AuthTokenManager
import com.example.passedpath.feature.auth.data.remote.api.AuthApi
import com.example.passedpath.feature.auth.data.repository.AuthRepository
import com.example.passedpath.feature.bookmark.data.remote.api.DayRouteBookmarkApi
import com.example.passedpath.feature.bookmark.data.repository.DayRouteBookmarkRepositoryImpl
import com.example.passedpath.feature.bookmark.domain.repository.DayRouteBookmarkRepository
import com.example.passedpath.feature.bookmark.domain.usecase.GetDayRouteBookmarksUseCase
import com.example.passedpath.feature.bookmark.domain.usecase.ToggleDayRouteBookmarkUseCase
import com.example.passedpath.feature.bookmark.domain.usecase.ToggleDayRouteBookmarksUseCase
import com.example.passedpath.feature.calendar.data.remote.api.CalendarMonthlyRouteApi
import com.example.passedpath.feature.calendar.data.repository.CalendarMonthlyRouteRepositoryImpl
import com.example.passedpath.feature.calendar.domain.repository.CalendarMonthlyRouteRepository
import com.example.passedpath.feature.calendar.domain.usecase.GetCalendarMonthlyRouteUseCase
import com.example.passedpath.feature.daynote.data.remote.api.DayRouteMemoApi
import com.example.passedpath.feature.daynote.data.remote.api.DayRouteTitleApi
import com.example.passedpath.feature.daynote.data.repository.DayRouteMemoRepositoryImpl
import com.example.passedpath.feature.daynote.data.repository.DayRouteTitleRepositoryImpl
import com.example.passedpath.feature.daynote.domain.repository.DayRouteMemoRepository
import com.example.passedpath.feature.daynote.domain.repository.DayRouteTitleRepository
import com.example.passedpath.feature.daynote.domain.usecase.PatchDayRouteMemoUseCase
import com.example.passedpath.feature.daynote.domain.usecase.PatchDayRouteTitleUseCase
import com.example.passedpath.feature.care.data.remote.datasource.OkHttpCareDependentLocationStreamRemoteDataSource
import com.example.passedpath.feature.care.data.remote.api.CareDependentApi
import com.example.passedpath.feature.care.data.remote.api.CareDependentDayRouteApi
import com.example.passedpath.feature.care.data.remote.api.CareDependentDayRoutePlacesApi
import com.example.passedpath.feature.care.data.remote.api.CareDependentDayRouteSummaryApi
import com.example.passedpath.feature.care.data.remote.api.CareRelationshipInviteApi
import com.example.passedpath.feature.care.data.remote.api.ProtectedPersonStatisticsApi
import com.example.passedpath.feature.care.data.repository.CareDependentLocationStreamRepositoryImpl
import com.example.passedpath.feature.care.data.repository.CareDependentRepositoryImpl
import com.example.passedpath.feature.care.data.repository.CareGuideRepositoryImpl
import com.example.passedpath.feature.care.data.repository.CareRelationshipInviteRepositoryImpl
import com.example.passedpath.feature.care.data.repository.ProtectedPersonDayRouteRepositoryImpl
import com.example.passedpath.feature.care.data.repository.ProtectedPersonDaySummaryRepositoryImpl
import com.example.passedpath.feature.care.data.repository.ProtectedPersonStatisticsRepositoryImpl
import com.example.passedpath.feature.care.data.repository.ProtectedPersonVisitedPlaceRepositoryImpl
import com.example.passedpath.feature.care.domain.repository.CareDependentLocationStreamRepository
import com.example.passedpath.feature.care.domain.repository.CareDependentRepository
import com.example.passedpath.feature.care.domain.repository.CareGuideRepository
import com.example.passedpath.feature.care.domain.repository.CareRelationshipInviteRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDaySummaryRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonStatisticsRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonVisitedPlaceRepository
import com.example.passedpath.feature.care.domain.usecase.AcceptCareRelationshipInviteUseCase
import com.example.passedpath.feature.care.domain.usecase.CreateCareRelationshipInviteLinkUseCase
import com.example.passedpath.feature.care.domain.usecase.GetCareDependentsUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonDayRouteListUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonDayRouteUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonDaySummaryUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonStatisticMetricUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonVisitStatisticsUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonVisitedPlacesUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonWeeklyStatisticsUseCase
import com.example.passedpath.feature.care.domain.usecase.ObserveCareDependentLocationStreamUseCase
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
import com.example.passedpath.feature.place.data.remote.api.PlaceSearchApi
import com.example.passedpath.feature.place.data.repository.PlaceGuideRepositoryImpl
import com.example.passedpath.feature.place.data.repository.PlaceRepositoryImpl
import com.example.passedpath.feature.place.data.repository.PlaceSearchRepositoryImpl
import com.example.passedpath.feature.place.domain.repository.PlaceGuideRepository
import com.example.passedpath.feature.place.domain.repository.PlaceRepository
import com.example.passedpath.feature.place.domain.repository.PlaceSearchRepository
import com.example.passedpath.feature.place.domain.usecase.AddPlaceUseCase
import com.example.passedpath.feature.place.domain.usecase.CreatePlaceFromSearchResultUseCase
import com.example.passedpath.feature.place.domain.usecase.DeletePlaceUseCase
import com.example.passedpath.feature.place.domain.usecase.GetVisitedPlacesUseCase
import com.example.passedpath.feature.place.domain.usecase.ReorderPlacesUseCase
import com.example.passedpath.feature.place.domain.usecase.SearchPlacesUseCase
import com.example.passedpath.feature.place.domain.usecase.UpdateBookmarkPlaceUseCase
import com.example.passedpath.feature.place.domain.usecase.UpdatePlaceUseCase
import com.example.passedpath.feature.placebookmark.data.remote.api.PlaceBookmarkApi
import com.example.passedpath.feature.placebookmark.data.repository.PlaceBookmarkRepositoryImpl
import com.example.passedpath.feature.placebookmark.domain.repository.PlaceBookmarkRepository
import com.example.passedpath.feature.placebookmark.domain.usecase.CreatePlaceBookmarkUseCase
import com.example.passedpath.feature.placebookmark.domain.usecase.DeletePlaceBookmarkUseCase
import com.example.passedpath.feature.placebookmark.domain.usecase.GetPlaceBookmarksUseCase
import com.example.passedpath.feature.placebookmark.domain.usecase.UpdatePlaceBookmarkUseCase
import com.example.passedpath.feature.summary.data.remote.api.DayRouteSummaryApi
import com.example.passedpath.feature.summary.data.remote.api.StatisticMetricApi
import com.example.passedpath.feature.summary.data.remote.api.VisitStatisticsApi
import com.example.passedpath.feature.summary.data.remote.api.WeeklyStatisticsApi
import com.example.passedpath.feature.summary.data.repository.DayRouteSummaryRepositoryImpl
import com.example.passedpath.feature.summary.data.repository.StatisticMetricRepositoryImpl
import com.example.passedpath.feature.summary.data.repository.VisitStatisticsRepositoryImpl
import com.example.passedpath.feature.summary.data.repository.WeeklyStatisticsRepositoryImpl
import com.example.passedpath.feature.summary.domain.repository.DayRouteSummaryRepository
import com.example.passedpath.feature.summary.domain.repository.StatisticMetricRepository
import com.example.passedpath.feature.summary.domain.repository.VisitStatisticsRepository
import com.example.passedpath.feature.summary.domain.repository.WeeklyStatisticsRepository
import com.example.passedpath.feature.summary.domain.usecase.GetDayRouteSummaryUseCase
import com.example.passedpath.feature.summary.domain.usecase.GetEnterHomeTimeStatisticsUseCase
import com.example.passedpath.feature.summary.domain.usecase.GetOutingTimeStatisticsUseCase
import com.example.passedpath.feature.summary.domain.usecase.GetTotalOutingCountStatisticsUseCase
import com.example.passedpath.feature.summary.domain.usecase.GetTotalOutingSecondsStatisticsUseCase
import com.example.passedpath.feature.summary.domain.usecase.GetVisitStatisticsUseCase
import com.example.passedpath.feature.summary.domain.usecase.GetWeeklyStatisticsUseCase
import com.example.passedpath.interceptor.AccessTokenAuthenticator
import java.util.concurrent.TimeUnit
import java.time.LocalTime
import okhttp3.sse.EventSources

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
            .addMigrations(PassedPathDatabase.MIGRATION_2_3)
            .addMigrations(PassedPathDatabase.MIGRATION_3_4)
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
            tokenStore = authSessionStorage
        )
    }

    private val retrofit by lazy {
        RetrofitClient.provideRetrofit(
            sessionStorage = authSessionStorage,
            authenticator = AccessTokenAuthenticator(
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

    private val dayRouteSummaryApi by lazy {
        retrofit.create(DayRouteSummaryApi::class.java)
    }

    private val weeklyStatisticsApi by lazy {
        retrofit.create(WeeklyStatisticsApi::class.java)
    }

    private val visitStatisticsApi by lazy {
        retrofit.create(VisitStatisticsApi::class.java)
    }

    private val statisticMetricApi by lazy {
        retrofit.create(StatisticMetricApi::class.java)
    }

    private val calendarMonthlyRouteApi by lazy {
        retrofit.create(CalendarMonthlyRouteApi::class.java)
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

    private val placeSearchApi by lazy {
        retrofit.create(PlaceSearchApi::class.java)
    }

    private val placeBookmarkApi by lazy {
        retrofit.create(PlaceBookmarkApi::class.java)
    }

    private val careDependentApi by lazy {
        retrofit.create(CareDependentApi::class.java)
    }

    private val careDependentDayRouteApi by lazy {
        retrofit.create(CareDependentDayRouteApi::class.java)
    }

    private val careDependentDayRouteSummaryApi by lazy {
        retrofit.create(CareDependentDayRouteSummaryApi::class.java)
    }

    private val careDependentDayRoutePlacesApi by lazy {
        retrofit.create(CareDependentDayRoutePlacesApi::class.java)
    }

    private val careRelationshipInviteApi by lazy {
        retrofit.create(CareRelationshipInviteApi::class.java)
    }

    private val protectedPersonStatisticsApi by lazy {
        retrofit.create(ProtectedPersonStatisticsApi::class.java)
    }

    private val careDependentLocationStreamOkHttpClient by lazy {
        RetrofitClient.provideOkHttpClient(
            sessionStorage = authSessionStorage,
            authenticator = AccessTokenAuthenticator(
                tokenManager = authTokenManager
            )
        )
            .newBuilder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
    }

    private val careDependentLocationStreamRemoteDataSource by lazy {
        OkHttpCareDependentLocationStreamRemoteDataSource(
            eventSourceFactory = EventSources.createFactory(careDependentLocationStreamOkHttpClient),
            baseUrl = BuildConfig.BASE_URL
        )
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

    val dayRouteSummaryRepository: DayRouteSummaryRepository by lazy {
        DayRouteSummaryRepositoryImpl(dayRouteSummaryApi = dayRouteSummaryApi)
    }

    val weeklyStatisticsRepository: WeeklyStatisticsRepository by lazy {
        WeeklyStatisticsRepositoryImpl(weeklyStatisticsApi = weeklyStatisticsApi)
    }

    val visitStatisticsRepository: VisitStatisticsRepository by lazy {
        VisitStatisticsRepositoryImpl(visitStatisticsApi = visitStatisticsApi)
    }

    val statisticMetricRepository: StatisticMetricRepository by lazy {
        StatisticMetricRepositoryImpl(statisticMetricApi = statisticMetricApi)
    }

    val calendarMonthlyRouteRepository: CalendarMonthlyRouteRepository by lazy {
        CalendarMonthlyRouteRepositoryImpl(
            calendarMonthlyRouteApi = calendarMonthlyRouteApi
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

    val placeSearchRepository: PlaceSearchRepository by lazy {
        PlaceSearchRepositoryImpl(placeSearchApi)
    }

    val placeGuideRepository: PlaceGuideRepository by lazy {
        PlaceGuideRepositoryImpl(appContext)
    }

    val placeBookmarkRepository: PlaceBookmarkRepository by lazy {
        PlaceBookmarkRepositoryImpl(placeBookmarkApi)
    }

    val careGuideRepository: CareGuideRepository by lazy {
        CareGuideRepositoryImpl(appContext)
    }

    val careDependentRepository: CareDependentRepository by lazy {
        CareDependentRepositoryImpl(careDependentApi = careDependentApi)
    }

    val protectedPersonDayRouteRepository: ProtectedPersonDayRouteRepository by lazy {
        ProtectedPersonDayRouteRepositoryImpl(
            careDependentDayRouteApi = careDependentDayRouteApi
        )
    }

    val protectedPersonDaySummaryRepository: ProtectedPersonDaySummaryRepository by lazy {
        ProtectedPersonDaySummaryRepositoryImpl(
            careDependentDayRouteSummaryApi = careDependentDayRouteSummaryApi
        )
    }

    val protectedPersonVisitedPlaceRepository: ProtectedPersonVisitedPlaceRepository by lazy {
        ProtectedPersonVisitedPlaceRepositoryImpl(
            careDependentDayRoutePlacesApi = careDependentDayRoutePlacesApi
        )
    }

    val protectedPersonStatisticsRepository: ProtectedPersonStatisticsRepository by lazy {
        ProtectedPersonStatisticsRepositoryImpl(
            protectedPersonStatisticsApi = protectedPersonStatisticsApi
        )
    }

    val careDependentLocationStreamRepository: CareDependentLocationStreamRepository by lazy {
        CareDependentLocationStreamRepositoryImpl(
            remoteDataSource = careDependentLocationStreamRemoteDataSource
        )
    }

    val careRelationshipInviteRepository: CareRelationshipInviteRepository by lazy {
        CareRelationshipInviteRepositoryImpl(
            careRelationshipInviteApi = careRelationshipInviteApi
        )
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

    val getDayRouteBookmarksUseCase: GetDayRouteBookmarksUseCase by lazy {
        GetDayRouteBookmarksUseCase(dayRouteBookmarkRepository = dayRouteBookmarkRepository)
    }

    val toggleDayRouteBookmarksUseCase: ToggleDayRouteBookmarksUseCase by lazy {
        ToggleDayRouteBookmarksUseCase(dayRouteBookmarkRepository = dayRouteBookmarkRepository)
    }

    val getDayRouteSummaryUseCase: GetDayRouteSummaryUseCase by lazy {
        GetDayRouteSummaryUseCase(dayRouteSummaryRepository = dayRouteSummaryRepository)
    }

    val getWeeklyStatisticsUseCase: GetWeeklyStatisticsUseCase by lazy {
        GetWeeklyStatisticsUseCase(weeklyStatisticsRepository = weeklyStatisticsRepository)
    }

    val getVisitStatisticsUseCase: GetVisitStatisticsUseCase by lazy {
        GetVisitStatisticsUseCase(visitStatisticsRepository = visitStatisticsRepository)
    }

    val getOutingTimeStatisticsUseCase: GetOutingTimeStatisticsUseCase by lazy {
        GetOutingTimeStatisticsUseCase(
            statisticMetricRepository = statisticMetricRepository
        )
    }

    val getEnterHomeTimeStatisticsUseCase: GetEnterHomeTimeStatisticsUseCase by lazy {
        GetEnterHomeTimeStatisticsUseCase(
            statisticMetricRepository = statisticMetricRepository
        )
    }

    val getTotalOutingSecondsStatisticsUseCase: GetTotalOutingSecondsStatisticsUseCase by lazy {
        GetTotalOutingSecondsStatisticsUseCase(
            statisticMetricRepository = statisticMetricRepository
        )
    }

    val getTotalOutingCountStatisticsUseCase: GetTotalOutingCountStatisticsUseCase by lazy {
        GetTotalOutingCountStatisticsUseCase(
            statisticMetricRepository = statisticMetricRepository
        )
    }

    val getCalendarMonthlyRouteUseCase: GetCalendarMonthlyRouteUseCase by lazy {
        GetCalendarMonthlyRouteUseCase(
            calendarMonthlyRouteRepository = calendarMonthlyRouteRepository
        )
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

    val createPlaceFromSearchResultUseCase: CreatePlaceFromSearchResultUseCase by lazy {
        CreatePlaceFromSearchResultUseCase(addPlaceUseCase = addPlaceUseCase)
    }

    val getVisitedPlacesUseCase: GetVisitedPlacesUseCase by lazy {
        GetVisitedPlacesUseCase(placeRepository = placeRepository)
    }

    val searchPlacesUseCase: SearchPlacesUseCase by lazy {
        SearchPlacesUseCase(repository = placeSearchRepository)
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

    val updatePlaceBookmarkUseCase: UpdatePlaceBookmarkUseCase by lazy {
        UpdatePlaceBookmarkUseCase(placeBookmarkRepository = placeBookmarkRepository)
    }

    val deletePlaceBookmarkUseCase: DeletePlaceBookmarkUseCase by lazy {
        DeletePlaceBookmarkUseCase(placeBookmarkRepository = placeBookmarkRepository)
    }

    val getPlaceBookmarksUseCase: GetPlaceBookmarksUseCase by lazy {
        GetPlaceBookmarksUseCase(placeBookmarkRepository = placeBookmarkRepository)
    }

    val createPlaceBookmarkUseCase: CreatePlaceBookmarkUseCase by lazy {
        CreatePlaceBookmarkUseCase(placeBookmarkRepository = placeBookmarkRepository)
    }

    val getProtectedPersonDayRouteUseCase: GetProtectedPersonDayRouteUseCase by lazy {
        GetProtectedPersonDayRouteUseCase(
            repository = protectedPersonDayRouteRepository
        )
    }

    val getProtectedPersonDayRouteListUseCase: GetProtectedPersonDayRouteListUseCase by lazy {
        GetProtectedPersonDayRouteListUseCase(
            repository = protectedPersonDayRouteRepository
        )
    }

    val getCareDependentsUseCase: GetCareDependentsUseCase by lazy {
        GetCareDependentsUseCase(repository = careDependentRepository)
    }

    val getProtectedPersonDaySummaryUseCase: GetProtectedPersonDaySummaryUseCase by lazy {
        GetProtectedPersonDaySummaryUseCase(
            repository = protectedPersonDaySummaryRepository
        )
    }

    val getProtectedPersonVisitedPlacesUseCase: GetProtectedPersonVisitedPlacesUseCase by lazy {
        GetProtectedPersonVisitedPlacesUseCase(
            repository = protectedPersonVisitedPlaceRepository
        )
    }

    val getProtectedPersonWeeklyStatisticsUseCase: GetProtectedPersonWeeklyStatisticsUseCase by lazy {
        GetProtectedPersonWeeklyStatisticsUseCase(
            repository = protectedPersonStatisticsRepository
        )
    }

    val getProtectedPersonVisitStatisticsUseCase: GetProtectedPersonVisitStatisticsUseCase by lazy {
        GetProtectedPersonVisitStatisticsUseCase(
            repository = protectedPersonStatisticsRepository
        )
    }

    val getProtectedPersonStatisticMetricUseCase: GetProtectedPersonStatisticMetricUseCase by lazy {
        GetProtectedPersonStatisticMetricUseCase(
            repository = protectedPersonStatisticsRepository
        )
    }

    val observeCareDependentLocationStreamUseCase: ObserveCareDependentLocationStreamUseCase by lazy {
        ObserveCareDependentLocationStreamUseCase(
            repository = careDependentLocationStreamRepository
        )
    }

    val createCareRelationshipInviteLinkUseCase: CreateCareRelationshipInviteLinkUseCase by lazy {
        CreateCareRelationshipInviteLinkUseCase(
            repository = careRelationshipInviteRepository
        )
    }

    val acceptCareRelationshipInviteUseCase: AcceptCareRelationshipInviteUseCase by lazy {
        AcceptCareRelationshipInviteUseCase(
            repository = careRelationshipInviteRepository
        )
    }

    val reorderPlacesUseCase: ReorderPlacesUseCase by lazy {
        ReorderPlacesUseCase(placeRepository = placeRepository)
    }
}



