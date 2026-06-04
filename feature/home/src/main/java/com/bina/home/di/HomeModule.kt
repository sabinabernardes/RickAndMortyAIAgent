package com.bina.home.di

import com.bina.home.data.datasource.CharacterDataSource
import com.bina.home.data.datasource.CharacterDataSourceImpl
import com.bina.home.data.remote.RickAndMortyApiService
import com.bina.home.data.repository.HomeRepositoryImpl
import com.bina.home.domain.repository.HomeRepository
import com.bina.home.domain.usecase.GetCharactersUseCase
import com.bina.home.presentation.mapper.CharacterUiMapper
import com.bina.home.presentation.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val homeModule = module {
    single<RickAndMortyApiService> { get<Retrofit>().create(RickAndMortyApiService::class.java) }
    factory<CharacterDataSource> { CharacterDataSourceImpl(get()) }
    factory<HomeRepository> { HomeRepositoryImpl(get()) }
    factory { GetCharactersUseCase(get()) }
    factory { CharacterUiMapper() }
    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
}
