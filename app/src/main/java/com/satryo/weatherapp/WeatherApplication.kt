package com.satryo.weatherapp

import android.app.Application
import com.satryo.weatherapp.data.container.AppContainer
import com.satryo.weatherapp.data.container.DefaultAppContainer

/**
 * Application class yang memegang [AppContainer] selama proses aplikasi hidup.
 * Didaftarkan di AndroidManifest melalui android:name=".WeatherApplication".
 */
class WeatherApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
