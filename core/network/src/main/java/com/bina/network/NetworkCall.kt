package com.bina.network

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): NetworkResult<T> = try {
    val response = call()
    when {
        response.isSuccessful -> {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(ResponseEnvelope(body, response.headers()))
            } else {
                NetworkResult.Empty
            }
        }
        response.code() == HTTP_UNAUTHORIZED -> NetworkResult.Unauthorized
        response.code() in HTTP_CLIENT_ERROR_RANGE ->
            NetworkResult.BusinessError(
                response.code(),
                response.errorBody()?.string() ?: response.message()
            )
        else -> NetworkResult.NetworkError(HttpException(response))
    }
} catch (e: CancellationException) {
    throw e
} catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
    NetworkResult.NetworkError(e)
}

private const val HTTP_UNAUTHORIZED = 401
private val HTTP_CLIENT_ERROR_RANGE = 400..499
