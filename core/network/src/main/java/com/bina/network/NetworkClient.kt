package com.bina.network

import com.bina.network.interceptor.ResilienceInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object NetworkClient {
    private const val BASE_URL = "https://rickandmortyapi.com/api/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val resilienceInterceptor = ResilienceInterceptor()

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(resilienceInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()
}
