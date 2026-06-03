package com.bina.analytics.di

import com.bina.analytics.AnalyticsTracker
import com.bina.analytics.PerformanceTracker
import com.bina.analytics.impl.LogcatAnalyticsTracker
import com.bina.analytics.impl.LogcatPerformanceTracker
import org.koin.dsl.module

val analyticsModule = module {
    single<AnalyticsTracker> { LogcatAnalyticsTracker(get()) }
    single<PerformanceTracker> { LogcatPerformanceTracker(get()) }
}
