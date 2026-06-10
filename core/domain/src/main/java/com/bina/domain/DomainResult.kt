package com.bina.domain

sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()
    data class Error(val message: String, val code: Int? = null) : DomainResult<Nothing>()
    object Loading : DomainResult<Nothing>()
    object Empty : DomainResult<Nothing>()
    object Unauthorized : DomainResult<Nothing>()
}
