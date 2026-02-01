package com.example.passedpath

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, "5438e21dc2bb87eaf82c9ba604ba5cbb")
    }
}