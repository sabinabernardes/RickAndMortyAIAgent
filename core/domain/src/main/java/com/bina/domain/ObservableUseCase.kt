package com.bina.domain

import kotlin.time.TimeSource

abstract class ObservableUseCase<in P, out T>(
    private val observer: UseCaseObserver = NoOpUseCaseObserver
) : UseCase<P, T> {

    protected abstract suspend fun execute(params: P): DomainResult<T>

    final override suspend operator fun invoke(params: P): DomainResult<T> {
        val name = this::class.simpleName ?: "UnknownUseCase"
        val mark = TimeSource.Monotonic.markNow()
        observer.onStart(name)
        return execute(params).also { result ->
            observer.onComplete(name, result, mark.elapsedNow().inWholeMilliseconds)
        }
    }
}
