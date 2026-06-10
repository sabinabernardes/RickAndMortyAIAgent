package com.bina.network

import com.bina.domain.DomainResult
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

fun <T> NetworkResult<T>.toDomain(): DomainResult<T> = when (this) {
    is NetworkResult.Success -> DomainResult.Success(data)
    is NetworkResult.BusinessError -> DomainResult.Error(message, code)
    is NetworkResult.NetworkError -> DomainResult.Error(exception.message ?: "Erro de rede")
    is NetworkResult.Unauthorized -> DomainResult.Unauthorized
    is NetworkResult.Empty -> DomainResult.Empty
    is NetworkResult.Loading -> DomainResult.Loading
}
