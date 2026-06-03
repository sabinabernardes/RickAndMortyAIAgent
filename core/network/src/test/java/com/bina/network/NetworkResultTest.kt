package com.bina.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class NetworkResultTest {

    @Test
    fun `Success should hold correct data`() {
        val result = NetworkResult.Success("Rick")
        assertEquals("Rick", result.data)
    }

    @Test
    fun `Error should hold correct exception`() {
        val exception = IllegalStateException("Something went wrong")
        val result = NetworkResult.Error(exception)
        assertEquals(exception, result.exception)
    }

    @Test
    fun `Loading should be singleton`() {
        val result1 = NetworkResult.Loading
        val result2 = NetworkResult.Loading
        assertSame(result1, result2)
    }

    @Test
    fun `Empty should be singleton`() {
        val result1 = NetworkResult.Empty
        val result2 = NetworkResult.Empty
        assertSame(result1, result2)
    }

    @Test
    fun `Unauthorized should be singleton`() {
        val result1 = NetworkResult.Unauthorized
        val result2 = NetworkResult.Unauthorized
        assertSame(result1, result2)
    }
}
