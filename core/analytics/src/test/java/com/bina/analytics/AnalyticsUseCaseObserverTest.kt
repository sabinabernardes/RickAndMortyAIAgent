package com.bina.analytics

import com.bina.analytics.event.UseCaseEvent
import com.bina.domain.DomainResult
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class AnalyticsUseCaseObserverTest {

    private val analytics: AnalyticsTracker = mockk(relaxed = true)
    private val performance: PerformanceTracker = mockk(relaxed = true)
    private lateinit var observer: AnalyticsUseCaseObserver

    @Before
    fun setUp() {
        observer = AnalyticsUseCaseObserver(analytics, performance)
    }

    @Test
    fun `onStart inicia trace com o nome do use case`() {
        observer.onStart("GetCharacterDetailsUseCase")
        verify { performance.startTrace("GetCharacterDetailsUseCase") }
    }

    @Test
    fun `onComplete para trace e rastreia evento de sucesso`() {
        observer.onComplete("GetCharacterDetailsUseCase", DomainResult.Success("data"), 120L)

        verify { performance.stopTrace("GetCharacterDetailsUseCase") }
        verify {
            analytics.track(match { it is UseCaseEvent && it.outcome == "success" && it.durationMs == 120L })
        }
    }

    @Test
    fun `onComplete rastreia evento de erro com codigo`() {
        observer.onComplete("GetCharacterDetailsUseCase", DomainResult.Error("not found", 404), 80L)

        verify {
            analytics.track(
                match {
                    it is UseCaseEvent &&
                        it.outcome == "error" &&
                        it.errorCode == "404" &&
                        it.durationMs == 80L
                }
            )
        }
    }

    @Test
    fun `onComplete rastreia evento de erro sem codigo`() {
        observer.onComplete("GetCharacterDetailsUseCase", DomainResult.Error("timeout"), 200L)

        verify {
            analytics.track(
                match {
                    it is UseCaseEvent &&
                        it.outcome == "error" &&
                        it.errorCode == null
                }
            )
        }
    }

    @Test
    fun `onComplete rastreia evento unauthorized`() {
        observer.onComplete("GetCharacterDetailsUseCase", DomainResult.Unauthorized, 50L)

        verify {
            analytics.track(match { it is UseCaseEvent && it.outcome == "unauthorized" })
        }
    }

    @Test
    fun `onComplete rastreia evento empty`() {
        observer.onComplete("GetCharacterDetailsUseCase", DomainResult.Empty, 30L)

        verify {
            analytics.track(match { it is UseCaseEvent && it.outcome == "empty" })
        }
    }

    @Test
    fun `onComplete nao rastreia evento Loading`() {
        observer.onComplete("GetCharacterDetailsUseCase", DomainResult.Loading, 0L)

        verify(exactly = 0) { analytics.track(any()) }
    }
}
