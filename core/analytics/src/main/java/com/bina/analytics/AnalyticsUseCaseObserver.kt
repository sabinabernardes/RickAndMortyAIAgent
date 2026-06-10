package com.bina.analytics

import com.bina.analytics.event.UseCaseEvent
import com.bina.domain.DomainResult
import com.bina.domain.UseCaseObserver

class AnalyticsUseCaseObserver(
    private val analytics: AnalyticsTracker,
    private val performance: PerformanceTracker
) : UseCaseObserver {

    override fun onStart(useCaseName: String) {
        performance.startTrace(useCaseName)
    }

    override fun onComplete(useCaseName: String, outcome: DomainResult<*>, durationMs: Long) {
        performance.stopTrace(useCaseName)
        val event = when (outcome) {
            is DomainResult.Success -> UseCaseEvent(useCaseName, "success", durationMs)
            is DomainResult.Error -> UseCaseEvent(useCaseName, "error", durationMs, outcome.code?.toString())
            is DomainResult.Unauthorized -> UseCaseEvent(useCaseName, "unauthorized", durationMs)
            is DomainResult.Empty -> UseCaseEvent(useCaseName, "empty", durationMs)
            is DomainResult.Loading -> return
        }
        analytics.track(event)
    }
}
