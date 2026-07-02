package com.agon.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class with Hilt
 */
@HiltAndroidApp
class DominoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide components here
    }
}
