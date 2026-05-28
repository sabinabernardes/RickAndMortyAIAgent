package com.bina.home.di

import com.bina.home.domain.repository.HomeRepository
import com.bina.home.data.repository.HomeRepositoryImpl
import com.bina.home.data.datasource.CharacterDataSource
import com.bina.home.data.datasource.CharacterDataSourceImpl
import com.bina.home.data.pagingSouce.CharacterPagingSource
import com.bina.home.domain.usecase.GetCharactersUseCase
import com.bina.home.data.remote.RickAndMortyApiService
import com.bina.home.presentation.mapper.CharacterUiMapper
import com.bina.home.presentation.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val homeModule = module {
    single<RickAndMortyApiService> {
        Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/api/") // ajuste se necessário
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RickAndMortyApiService::class.java)
    }
    single<CharacterDataSource> { CharacterDataSourceImpl(get<RickAndMortyApiService>()) }
    single<HomeRepository> { HomeRepositoryImpl(get()) }
    factory { GetCharactersUseCase(get()) }
    factory { (query: String) -> CharacterPagingSource(get(), query) }
    factory { CharacterUiMapper() }
    viewModel { HomeViewModel(get(), get()) }
}