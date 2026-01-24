package com.windhands.attendix

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for Attendix
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 */
@HiltAndroidApp
class AttendixApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: AttendixApp
            private set
    }
}
