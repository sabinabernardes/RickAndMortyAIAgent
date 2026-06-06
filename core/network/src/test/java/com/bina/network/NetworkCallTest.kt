package com.bina.network

import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class NetworkCallTest {

    @Test
    fun `GIVEN successful response WHEN safeApiCall THEN returns Success with data and headers`() = runTest {
        val headers = Headers.headersOf("X-Request-Id", "abc")
        val result = safeApiCall { Response.success("data", headers) }

        assertTrue(result is NetworkResult.Success)
        assertEquals("data", (result as NetworkResult.Success).data)
        assertEquals("abc", result.envelope.headers["X-Request-Id"])
    }

    @Test
    fun `GIVEN response with null body WHEN safeApiCall THEN returns Empty`() = runTest {
        val result = safeApiCall { Response.success<String?>(null) }

        assertTrue(result is NetworkResult.Empty)
    }

    @Test
    fun `GIVEN 401 response WHEN safeApiCall THEN returns Unauthorized`() = runTest {
        val result = safeApiCall { Response.error<String>(401, "".toResponseBody()) }

        assertTrue(result is NetworkResult.Unauthorized)
    }

    @Test
    fun `GIVEN 404 response WHEN safeApiCall THEN returns BusinessError`() = runTest {
        val result = safeApiCall {
            Response.error<String>(404, "not found".toResponseBody("text/plain".toMediaType()))
        }

        assertTrue(result is NetworkResult.BusinessError)
        assertEquals(404, (result as NetworkResult.BusinessError).code)
    }

    @Test
    fun `GIVEN 422 response WHEN safeApiCall THEN returns BusinessError with code`() = runTest {
        val result = safeApiCall { Response.error<String>(422, "validation error".toResponseBody()) }

        assertTrue(result is NetworkResult.BusinessError)
        assertEquals(422, (result as NetworkResult.BusinessError).code)
    }

    @Test
    fun `GIVEN 500 response WHEN safeApiCall THEN returns NetworkError`() = runTest {
        val result = safeApiCall { Response.error<String>(500, "".toResponseBody()) }

        assertTrue(result is NetworkResult.NetworkError)
    }

    @Test
    fun `GIVEN IOException thrown WHEN safeApiCall THEN returns NetworkError with message`() = runTest {
        val result = safeApiCall<String> { throw java.io.IOException("network error") }

        assertTrue(result is NetworkResult.NetworkError)
        assertEquals("network error", (result as NetworkResult.NetworkError).exception.message)
    }
}
