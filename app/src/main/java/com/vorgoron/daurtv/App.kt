package com.vorgoron.daurtv

import android.app.Application
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import timber.log.Timber
import java.util.Calendar

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

    }
}

fun isNewYear(): Boolean {
    val calendar = Calendar.getInstance()
    val month: Int = calendar.get(Calendar.MONTH)
    val day: Int = calendar.get(Calendar.DATE)
    return (month == 11 && day > 23) || (month == 0 && day < 15)
}