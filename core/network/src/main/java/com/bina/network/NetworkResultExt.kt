package com.bina.network

import okhttp3.Headers

val <T> NetworkResult.Success<T>.data: T get() = envelope.data

fun <T, R> NetworkResult<T>.mapSuccess(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success ->
        NetworkResult.Success(ResponseEnvelope(transform(envelope.data), envelope.headers))
    else ->
        @Suppress("UNCHECKED_CAST")
        (this as NetworkResult<R>)
}

fun <T> successOf(data: T, headers: Headers = Headers.headersOf()): NetworkResult.Success<T> =
    NetworkResult.Success(ResponseEnvelope(data, headers))
