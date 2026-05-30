package com.bina.chat.di

import com.bina.chat.chat.data.datasource.ChatDataSource
import com.bina.chat.chat.data.datasource.ChatDataSourceImpl
import com.bina.chat.chat.data.repository.ChatRepositoryImpl
import com.bina.chat.chat.domain.repository.ChatRepository
import com.bina.chat.chat.domain.usecase.CheckModelAvailabilityUseCase
import com.bina.chat.chat.domain.usecase.SendMessageUseCase
import com.bina.chat.chat.presentation.mapper.ChatMessageUiMapper
import com.bina.chat.chat.presentation.viewmodel.ChatViewModel
import com.bina.chat.search.data.datasource.CharacterSearchDataSource
import com.bina.chat.search.data.datasource.CharacterSearchDataSourceImpl
import com.bina.chat.search.data.remote.CharacterSearchApiService
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.defineFunction
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

val chatModule = module {
    single<GenerativeModel> {
        val showCharacterFn = defineFunction(
            name = "show_character",
            description = "Abre a tela de detalhes de um personagem de Rick and Morty pelo nome",
            parameters = listOf(Schema.str("name", "Nome do personagem")),
            requiredParameters = listOf("name")
        )
        val searchCharactersFn = defineFunction(
            name = "search_characters",
            description = "Abre a tela inicial com uma busca aplicada por nome de personagem",
            parameters = listOf(Schema.str("query", "Nome ou fragmento para buscar")),
            requiredParameters = listOf("query")
        )
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = get(named("geminiApiKey")),
            tools = listOf(Tool(functionDeclarations = listOf(showCharacterFn, searchCharactersFn)))
        )
    }
    single<CharacterSearchApiService> { get<Retrofit>().create(CharacterSearchApiService::class.java) }
    factory<CharacterSearchDataSource> { CharacterSearchDataSourceImpl(get()) }
    factory<ChatDataSource> { ChatDataSourceImpl(get()) }
    factory<ChatRepository> { ChatRepositoryImpl(get(), get()) }
    factory { CheckModelAvailabilityUseCase(get()) }
    factory { SendMessageUseCase(get()) }
    factory { ChatMessageUiMapper() }
    viewModel { ChatViewModel(get(), get(), get(), get()) }
}
