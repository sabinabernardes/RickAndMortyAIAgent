package com.bina.network

import okhttp3.Headers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class NetworkResultTest {

    @Test
    fun `Success should hold correct data via extension property`() {
        val result = successOf("Rick")
        assertEquals("Rick", result.data)
    }

    @Test
    fun `Success envelope should expose headers`() {
        val headers = Headers.headersOf("X-Request-Id", "abc")
        val result = NetworkResult.Success(ResponseEnvelope("Rick", headers))
        assertEquals("abc", result.envelope.headers["X-Request-Id"])
    }

    @Test
    fun `NetworkError should hold correct exception`() {
        val exception = IllegalStateException("Something went wrong")
        val result = NetworkResult.NetworkError(exception)
        assertEquals(exception, result.exception)
    }

    @Test
    fun `BusinessError should hold code and message`() {
        val result = NetworkResult.BusinessError(422, "email already taken")
        assertEquals(422, result.code)
        assertEquals("email already taken", result.message)
    }

    @Test
    fun `Loading should be singleton`() {
        assertSame(NetworkResult.Loading, NetworkResult.Loading)
    }

    @Test
    fun `Empty should be singleton`() {
        assertSame(NetworkResult.Empty, NetworkResult.Empty)
    }

    @Test
    fun `Unauthorized should be singleton`() {
        assertSame(NetworkResult.Unauthorized, NetworkResult.Unauthorized)
    }

    @Test
    fun `mapSuccess transforms data and preserves headers`() {
        val headers = Headers.headersOf("X-Request-Id", "xyz")
        val result: NetworkResult<Int> = NetworkResult.Success(ResponseEnvelope(42, headers))

        val mapped = result.mapSuccess { it * 2 }

        assert(mapped is NetworkResult.Success)
        assertEquals(84, (mapped as NetworkResult.Success).data)
        assertEquals("xyz", mapped.envelope.headers["X-Request-Id"])
    }

    @Test
    fun `mapSuccess on NetworkError returns same NetworkError`() {
        val error = NetworkResult.NetworkError(RuntimeException("oops"))
        val mapped = error.mapSuccess { "never" }
        assertSame(error, mapped)
    }

    @Test
    fun `mapSuccess on BusinessError returns same BusinessError`() {
        val error = NetworkResult.BusinessError(409, "conflict")
        val mapped = error.mapSuccess { "never" }
        assertSame(error, mapped)
    }
}
