package com.bina.domain

interface UseCaseObserver {
    fun onStart(useCaseName: String)
    fun onComplete(useCaseName: String, outcome: DomainResult<*>, durationMs: Long)
}

object NoOpUseCaseObserver : UseCaseObserver {
    override fun onStart(useCaseName: String) = Unit
    override fun onComplete(useCaseName: String, outcome: DomainResult<*>, durationMs: Long) = Unit
}
