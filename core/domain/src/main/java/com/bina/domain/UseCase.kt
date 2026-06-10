package com.bina.domain

object NoParams

fun interface UseCase<in P, out R> {
    suspend operator fun invoke(params: P): DomainResult<R>
}
