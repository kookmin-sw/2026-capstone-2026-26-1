package com.example.passedpath

import android.app.Application
import com.example.passedpath.app.AppContainer
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        appContainer = AppContainer(this)
        KakaoSdk.init(this, "5438e21dc2bb87eaf82c9ba604ba5cbb")
    }
}
