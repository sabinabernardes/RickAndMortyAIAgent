package com.bina.network

sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: Throwable) : NetworkResult<Nothing>()
    object Empty : NetworkResult<Nothing>()
    object Unauthorized : NetworkResult<Nothing>()
}
