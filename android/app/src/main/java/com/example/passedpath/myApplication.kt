package com.example.passedpath

import android.app.Application
import com.example.passedpath.app.AppContainer
import com.kakao.sdk.common.KakaoSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GlobalApplication : Application() {
    lateinit var appContainer: AppContainer
        private set
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        appContainer = AppContainer(this)
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        applicationScope.launch {
            appContainer.cleanupTrackingLocalDataUseCase()
        }
    }
}
