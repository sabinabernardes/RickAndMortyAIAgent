package com.bina.network

import okhttp3.Headers

data class ResponseEnvelope<T>(val data: T, val headers: Headers)

sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()
    data class Success<T>(val envelope: ResponseEnvelope<T>) : NetworkResult<T>()
    data class BusinessError(val code: Int, val message: String) : NetworkResult<Nothing>()
    data class NetworkError(val exception: Throwable) : NetworkResult<Nothing>()
    object Empty : NetworkResult<Nothing>()
    object Unauthorized : NetworkResult<Nothing>()
}