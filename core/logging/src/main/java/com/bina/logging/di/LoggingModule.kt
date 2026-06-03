package com.bina.logging.di

import com.bina.logging.AppLogger
import com.bina.logging.impl.LogcatLogger
import org.koin.dsl.module

val loggingModule = module {
    single<AppLogger> { LogcatLogger() }
}
