package com.example.passedpath.app

import android.content.Context
import androidx.room.Room
import com.example.passedpath.data.datastore.AuthSessionStorage
import com.example.passedpath.data.network.RetrofitClient
import com.example.passedpath.feature.auth.data.manager.AuthTokenManager
import com.example.passedpath.feature.auth.data.remote.api.AuthApi
import com.example.passedpath.feature.auth.data.repository.AuthRepository
import com.example.passedpath.feature.locationtracking.data.local.PassedPathDatabase
import com.example.passedpath.feature.locationtracking.data.remote.api.DayRouteApi
import com.example.passedpath.feature.locationtracking.data.repository.RoomDayRouteRepository
import com.example.passedpath.feature.locationtracking.data.repository.RoomLocationTrackingRepository
import com.example.passedpath.feature.locationtracking.domain.policy.FixedTrackingDayBoundaryTimeProvider
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.LocationTrackingRepository
import com.example.passedpath.feature.locationtracking.domain.policy.TrackingDateKeyResolver
import com.example.passedpath.feature.locationtracking.domain.tracker.LocationTracker
import com.example.passedpath.feature.locationtracking.domain.usecase.StartLocationTrackingUseCase
import com.example.passedpath.feature.locationtracking.domain.usecase.StopLocationTrackingUseCase
import com.example.passedpath.feature.locationtracking.domain.usecase.UploadGpsPointsBatchUseCase
import com.example.passedpath.feature.main.data.manager.CurrentLocationProvider
import com.example.passedpath.feature.main.data.repository.TestRepository
import com.example.passedpath.feature.permission.data.manager.LocationPermissionChecker
import java.time.LocalTime

// 앱의 의존성 생성 책임을 한 곳에 모은 클래스
// 수동 DI(Dependency Injection) Container
//      DI 구조에서 Composition Root에 해당 (의존 객체의 생성 담당)
class AppContainer(
    context: Context
) {
    // Activity나 Service보다 오래 살아야 하는 객체들은 applicationContext를 사용한다.
    private val appContext = context.applicationContext

    // 인증 세션/토큰 저장소
    val authSessionStorage: AuthSessionStorage by lazy {
        AuthSessionStorage(appContext)
    }

    // 현재 앱의 위치 권한 수준 확인 객체
    val permissionChecker: LocationPermissionChecker by lazy {
        LocationPermissionChecker(appContext)
    }

    // 위치를 "어떻게 가져오는지"에 대한 구현체다.
    val locationTracker: LocationTracker by lazy {
        // 바깥에서는 CurrentLocationProvider가 아니라 LocationTracker 인터페이스로만 본다.
        CurrentLocationProvider(appContext)
    }

    // 위치 추적 로컬 저장용 Room DB를 한 번만 생성해서 재사용한다.
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

    // raw GPS 포인트 저장, 업로드 대기 조회, 일자별 경로 Flow 제공을 담당한다.
    val locationTrackingRepository: LocationTrackingRepository by lazy {
        RoomLocationTrackingRepository(
            gpsPointDao = trackingDatabase.gpsPointDao(),
            dayRouteDao = trackingDatabase.dayRouteDao(),
            dateKeyResolver = trackingDateKeyResolver
        )
    }

    // 하루 단위 경로 요약 정보를 다루는 Repository다.
    // 현재는 로컬 조회만 구현되어 있고, 원격 새로고침은 이후 이슈에서 확장될 예정이다.
    val dayRouteRepository: DayRouteRepository by lazy {
        RoomDayRouteRepository(
            dayRouteDao = trackingDatabase.dayRouteDao(),
            gpsPointDao = trackingDatabase.gpsPointDao()
        )
    }

    // UI는 Service를 직접 다루지 않고, 시작 UseCase를 통해 추적 시작 요청만 보낸다.
    val startLocationTrackingUseCase: StartLocationTrackingUseCase by lazy {
        StartLocationTrackingUseCase(appContext)
    }

    // UI는 종료 UseCase를 통해 추적 중단 요청만 보낸다.
    val stopLocationTrackingUseCase: StopLocationTrackingUseCase by lazy {
        StopLocationTrackingUseCase(appContext)
    }

    // 아래부터는 네트워크 계층 배선이다.
    private val retrofit by lazy {
        RetrofitClient.provideRetrofit(authSessionStorage)
    }

    // 인증 관련 API 인터페이스 구현체 생성
    private val authApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    // 테스트용 API 인터페이스 구현체 생성
    private val testApi by lazy {
        retrofit.create(com.example.passedpath.data.network.api.TestApi::class.java)
    }

    private val dayRouteApi by lazy {
        retrofit.create(DayRouteApi::class.java)
    }

    // 액세스 토큰 재발급 같은 인증 토큰 관리 책임을 분리한다.
    private val authTokenManager by lazy {
        AuthTokenManager(
            authApi = authApi,
            sessionStorage = authSessionStorage
        )
    }

    // 로그인/로그아웃/토큰 흐름 등 인증 기능에서 사용할 Repository다.
    val authRepository: AuthRepository by lazy {
        AuthRepository(
            authApi = authApi,
            tokenManager = authTokenManager,
            sessionStorage = authSessionStorage
        )
    }

    // 메인 화면의 테스트 API 호출에 사용하는 Repository다.
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
