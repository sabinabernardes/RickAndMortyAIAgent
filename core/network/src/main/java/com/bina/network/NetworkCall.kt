package com.bina.network

import retrofit2.HttpException

suspend fun <T> safeApiCall(call: suspend () -> T): NetworkResult<T> = try {
    NetworkResult.Success(call())
} catch (e: HttpException) {
    if (e.code() == HTTP_UNAUTHORIZED) NetworkResult.Unauthorized else NetworkResult.Error(e)
} catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
    // safeApiCall é uma borda de segurança — qualquer exceção não-HTTP deve virar NetworkResult.Error
    NetworkResult.Error(e)
}

private const val HTTP_UNAUTHORIZED = 401
