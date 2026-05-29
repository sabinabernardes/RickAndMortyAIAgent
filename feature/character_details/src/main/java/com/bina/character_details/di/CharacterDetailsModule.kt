package com.bina.character_details.di

import com.bina.character_details.data.datasource.CharacterDetailsDataSource
import com.bina.character_details.data.datasource.CharacterDetailsDataSourceImpl
import com.bina.character_details.data.remote.CharacterDetailsApiService
import com.bina.character_details.data.repository.CharacterDetailsRepositoryImpl
import com.bina.character_details.domain.repository.CharacterDetailsRepository
import com.bina.character_details.domain.usecase.GetCharacterDetailsUseCase
import com.bina.character_details.presentation.mapper.CharacterDetailsUiMapper
import com.bina.character_details.presentation.viewmodel.CharacterDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val characterDetailsModule = module {
    single<CharacterDetailsApiService> { get<Retrofit>().create(CharacterDetailsApiService::class.java) }
    factory<CharacterDetailsDataSource> { CharacterDetailsDataSourceImpl(get()) }
    factory<CharacterDetailsRepository> { CharacterDetailsRepositoryImpl(get()) }
    factory { GetCharacterDetailsUseCase(get()) }
    factory { CharacterDetailsUiMapper() }
    viewModel { CharacterDetailsViewModel(get(), get()) }
}
