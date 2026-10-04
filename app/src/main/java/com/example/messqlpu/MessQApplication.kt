package com.example.messqlpu

import android.app.Application
import com.example.messqlpu.di.AppContainer
import com.example.messqlpu.di.DefaultAppContainer

class MessQApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = DefaultAppContainer()
        com.example.messqlpu.data.firebase.FirebaseManager.initFcm()
    }

    companion object {
        lateinit var instance: MessQApplication
            private set
    }
}
