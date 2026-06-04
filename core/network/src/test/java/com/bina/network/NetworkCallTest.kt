package com.bina.network

import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class NetworkCallTest {

    private fun httpException(code: Int) = HttpException(
        Response.error<Any>(code, "error".toResponseBody("text/plain".toMediaType()))
    )

    @Test
    fun `GIVEN successful call WHEN safeApiCall THEN returns Success with data`() = runTest {
        val result = safeApiCall { "data" }

        assertTrue(result is NetworkResult.Success)
        assertEquals("data", (result as NetworkResult.Success).data)
    }

    @Test
    fun `GIVEN call throws HttpException 401 WHEN safeApiCall THEN returns Unauthorized`() = runTest {
        val result = safeApiCall<String> { throw httpException(401) }

        assertTrue(result is NetworkResult.Unauthorized)
    }

    @Test
    fun `GIVEN call throws HttpException 500 WHEN safeApiCall THEN returns Error`() = runTest {
        val result = safeApiCall<String> { throw httpException(500) }

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `GIVEN call throws HttpException 404 WHEN safeApiCall THEN returns Error`() = runTest {
        val result = safeApiCall<String> { throw httpException(404) }

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `GIVEN call throws IOException WHEN safeApiCall THEN returns Error with message`() = runTest {
        val result = safeApiCall<String> { throw java.io.IOException("network error") }

        assertTrue(result is NetworkResult.Error)
        assertEquals("network error", (result as NetworkResult.Error).exception.message)
    }

    @Test
    fun `GIVEN call returns null WHEN safeApiCall THEN returns Success with null`() = runTest {
        val result = safeApiCall<String?> { null }

        assertTrue(result is NetworkResult.Success)
        assertEquals(null, (result as NetworkResult.Success).data)
    }
}
