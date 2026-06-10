package com.bina.analytics.event

data class UseCaseEvent(
    val useCaseName: String,
    val outcome: String,
    val durationMs: Long,
    val errorCode: String? = null
) : AnalyticsEvent {
    override val name = "use_case_completed"
    override val properties = buildMap {
        put("use_case", useCaseName)
        put("outcome", outcome)
        put("duration_ms", durationMs.toString())
        errorCode?.let { put("error_code", it) }
    }
}
