package com.sample.carservicesample

import android.app.Application
import com.sample.carservicesample.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@CarApplication)
            modules(appModule)
        }
    }
}
