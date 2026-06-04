package com.bina.auth.di

import com.bina.auth.data.repository.AuthRepositoryImpl
import com.bina.auth.domain.repository.AuthRepository
import com.bina.auth.domain.usecase.LoginUseCase
import com.bina.auth.presentation.viewmodel.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    factory<AuthRepository> { AuthRepositoryImpl(get()) }
    factory { LoginUseCase(get()) }
    viewModel { LoginViewModel(get(), get(), get()) }
}
