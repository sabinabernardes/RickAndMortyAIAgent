package com.bina.rickandmorty.di

import com.bina.analytics.di.analyticsModule
import com.bina.character_details.di.characterDetailsModule
import com.bina.chat.di.chatModule
import com.bina.home.di.homeModule
import com.bina.logging.di.loggingModule
import com.bina.network.NetworkClient
import com.bina.rickandmorty.BuildConfig
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule = module {
    single<Retrofit> { NetworkClient.retrofit }
}

val keysModule = module {
    single(named("geminiApiKey")) { BuildConfig.GEMINI_API_KEY }
}

val appModules = listOf(
    loggingModule,
    analyticsModule,
    networkModule,
    homeModule,
    characterDetailsModule,
    keysModule,
    chatModule,
)
