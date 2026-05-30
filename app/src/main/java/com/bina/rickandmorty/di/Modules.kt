package com.bina.rickandmorty.di

import com.bina.character_details.di.characterDetailsModule
import com.bina.chat.di.chatModule
import com.bina.rickandmorty.BuildConfig
import com.bina.home.data.datasource.CharacterDataSource
import com.bina.home.data.datasource.CharacterDataSourceImpl
import com.bina.home.data.remote.RickAndMortyApiService
import com.bina.home.data.repository.HomeRepositoryImpl
import com.bina.home.domain.repository.HomeRepository
import com.bina.home.domain.usecase.GetCharactersUseCase
import com.bina.home.presentation.mapper.CharacterUiMapper
import com.bina.home.presentation.viewmodel.HomeViewModel
import com.bina.network.NetworkClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule = module {
    single<Retrofit> { NetworkClient.retrofit }
    single<RickAndMortyApiService> { get<Retrofit>().create(RickAndMortyApiService::class.java) }
}

val homeModule = module {
    factory<CharacterDataSource> { CharacterDataSourceImpl(get()) }
    factory<HomeRepository> { HomeRepositoryImpl(get()) }
    factory { GetCharactersUseCase(get()) }
    factory { CharacterUiMapper() }
    viewModel { HomeViewModel(get(), get()) }
}

val keysModule = module {
    single(named("geminiApiKey")) { BuildConfig.GEMINI_API_KEY }
}

val appModules = listOf(networkModule, homeModule, characterDetailsModule, keysModule, chatModule)
