package com.bina.domain

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObservableUseCaseTest {

    private class FakeUseCase(
        private val result: DomainResult<String>,
        observer: UseCaseObserver = NoOpUseCaseObserver
    ) : ObservableUseCase<Int, String>(observer) {
        override suspend fun execute(params: Int): DomainResult<String> = result
    }

    @Test
    fun `retorna o resultado de execute`() = runTest {
        val useCase = FakeUseCase(DomainResult.Success("Rick"))
        val result = useCase(1)
        assertIs<DomainResult.Success<String>>(result)
        assertEquals("Rick", result.data)
    }

    @Test
    fun `propaga Error sem alteracao`() = runTest {
        val useCase = FakeUseCase(DomainResult.Error("not found", 404))
        val result = useCase(1)
        assertIs<DomainResult.Error>(result)
        assertEquals("not found", result.message)
        assertEquals(404, result.code)
    }

    @Test
    fun `propaga Empty sem alteracao`() = runTest {
        val useCase = FakeUseCase(DomainResult.Empty)
        assertIs<DomainResult.Empty>(useCase(1))
    }

    @Test
    fun `propaga Unauthorized sem alteracao`() = runTest {
        val useCase = FakeUseCase(DomainResult.Unauthorized)
        assertIs<DomainResult.Unauthorized>(useCase(1))
    }

    @Test
    fun `notifica observer onStart antes de executar`() = runTest {
        val observer = mockk<UseCaseObserver>(relaxed = true)
        val useCase = FakeUseCase(DomainResult.Success("Morty"), observer)
        useCase(1)
        verify { observer.onStart("FakeUseCase") }
    }

    @Test
    fun `notifica observer onComplete com o resultado correto`() = runTest {
        val observer = mockk<UseCaseObserver>(relaxed = true)
        val expected = DomainResult.Success("Morty")
        val useCase = FakeUseCase(expected, observer)
        useCase(1)
        verify { observer.onComplete("FakeUseCase", expected, any()) }
    }

    @Test
    fun `notifica observer onComplete com Error`() = runTest {
        val observer = mockk<UseCaseObserver>(relaxed = true)
        val error = DomainResult.Error("fail", 500)
        val useCase = FakeUseCase(error, observer)
        useCase(1)
        verify { observer.onComplete("FakeUseCase", error, any()) }
    }

    @Test
    fun `NoOpUseCaseObserver nao lanca excecao`() = runTest {
        val useCase = FakeUseCase(DomainResult.Success("ok"), NoOpUseCaseObserver)
        val result = useCase(1)
        assertIs<DomainResult.Success<String>>(result)
    }
}
