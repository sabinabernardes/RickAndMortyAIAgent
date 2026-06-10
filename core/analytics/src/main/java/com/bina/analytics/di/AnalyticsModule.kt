package com.bina.analytics.di

import com.bina.analytics.AnalyticsTracker
import com.bina.analytics.AnalyticsUseCaseObserver
import com.bina.analytics.PerformanceTracker
import com.bina.analytics.impl.LogcatAnalyticsTracker
import com.bina.analytics.impl.LogcatPerformanceTracker
import com.bina.domain.UseCaseObserver
import org.koin.dsl.module

val analyticsModule = module {
    single<AnalyticsTracker> { LogcatAnalyticsTracker(get()) }
    single<PerformanceTracker> { LogcatPerformanceTracker(get()) }
    single<UseCaseObserver> { AnalyticsUseCaseObserver(get(), get()) }
}
