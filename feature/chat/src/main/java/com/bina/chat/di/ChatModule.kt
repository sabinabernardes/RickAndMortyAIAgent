package com.bina.chat.di

import com.bina.chat.data.datasource.ChatDataSource
import com.bina.chat.data.datasource.ChatDataSourceImpl
import com.bina.chat.data.repository.ChatRepositoryImpl
import com.bina.chat.domain.repository.ChatRepository
import com.bina.chat.domain.usecase.CheckModelAvailabilityUseCase
import com.bina.chat.domain.usecase.SendMessageUseCase
import com.bina.chat.presentation.mapper.ChatMessageUiMapper
import com.bina.chat.presentation.viewmodel.ChatViewModel
import com.google.ai.client.generativeai.GenerativeModel
import org.koin.core.qualifier.named
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
    single<GenerativeModel> {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = get(named("geminiApiKey"))
        )
    }
    factory<ChatDataSource> { ChatDataSourceImpl(get()) }
    factory<ChatRepository> { ChatRepositoryImpl(get()) }
    factory { CheckModelAvailabilityUseCase(get()) }
    factory { SendMessageUseCase(get()) }
    factory { ChatMessageUiMapper() }
    viewModel { ChatViewModel(get(), get(), get(), get()) }
}