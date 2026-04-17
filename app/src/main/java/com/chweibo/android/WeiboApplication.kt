package com.chweibo.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WeiboApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
