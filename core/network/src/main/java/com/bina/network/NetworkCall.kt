package com.bina.network

import retrofit2.HttpException

suspend fun <T> safeApiCall(call: suspend () -> T): NetworkResult<T> = try {
    NetworkResult.Success(call())
} catch (e: HttpException) {
    if (e.code() == HTTP_UNAUTHORIZED) NetworkResult.Unauthorized else NetworkResult.Error(e)
} catch (e: Exception) {
    NetworkResult.Error(e)
}

private const val HTTP_UNAUTHORIZED = 401
