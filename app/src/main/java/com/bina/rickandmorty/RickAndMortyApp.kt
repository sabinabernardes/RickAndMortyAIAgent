package com.bina.rickandmorty

import android.app.Application
import com.bina.rickandmorty.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class RickAndMortyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@RickAndMortyApp)
            modules(appModules)
        }
    }
}
