package com.example.passedpath.app

import android.content.Context
import com.example.passedpath.GlobalApplication

val Context.appContainer: AppContainer
    get() = (applicationContext as GlobalApplication).appContainer
