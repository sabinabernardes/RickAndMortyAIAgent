package com.bina.network.interceptor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ResilienceInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    // initialDelay = 0 para os testes não esperarem backoff real
    private val interceptor = ResilienceInterceptor(maxRetries = 3, initialDelay = 0L)

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun get(): okhttp3.Response {
        val request = Request.Builder().url(server.url("/test")).build()
        return client.newCall(request).execute()
    }

    @Test
    fun `GIVEN 200 response WHEN intercept THEN returns immediately without retry`() {
        server.enqueue(MockResponse().setResponseCode(200))

        val response = get()

        assertEquals(200, response.code)
        assertEquals(1, server.requestCount)
        response.close()
    }

    @Test
    fun `GIVEN 404 response WHEN intercept THEN does not retry`() {
        server.enqueue(MockResponse().setResponseCode(404))

        val response = get()

        assertEquals(404, response.code)
        assertEquals(1, server.requestCount)
        response.close()
    }

    @Test
    fun `GIVEN 429 then 200 WHEN intercept THEN retries once and returns 200`() {
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setResponseCode(200))

        val response = get()

        assertEquals(200, response.code)
        assertEquals(2, server.requestCount)
        response.close()
    }

    @Test
    fun `GIVEN 503 then 200 WHEN intercept THEN retries once and returns 200`() {
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setResponseCode(200))

        val response = get()

        assertEquals(200, response.code)
        assertEquals(2, server.requestCount)
        response.close()
    }

    @Test
    fun `GIVEN 500 always WHEN intercept THEN retries maxRetries times then returns 500`() {
        repeat(4) { server.enqueue(MockResponse().setResponseCode(500)) }

        val response = get()

        assertEquals(500, response.code)
        // 1 tentativa original + 3 retries = 4 requisições
        assertEquals(4, server.requestCount)
        response.close()
    }

    @Test
    fun `GIVEN 429 twice then 200 WHEN intercept THEN retries twice and returns 200`() {
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setResponseCode(200))

        val response = get()

        assertEquals(200, response.code)
        assertEquals(3, server.requestCount)
        response.close()
    }

    @Test
    fun `GIVEN Retry-After header WHEN intercept THEN uses header value for wait time`() {
        // Com initialDelay=0 e Retry-After=0, o sleep é 0ms — só verifica que não quebra
        server.enqueue(MockResponse().setResponseCode(429).addHeader("Retry-After", "0"))
        server.enqueue(MockResponse().setResponseCode(200))

        val response = get()

        assertEquals(200, response.code)
        assertEquals(2, server.requestCount)
        response.close()
    }

    @Test
    fun `GIVEN 301 redirect code WHEN intercept THEN does not retry`() {
        server.enqueue(MockResponse().setResponseCode(301))

        val response = get()

        // OkHttp segue redirects por padrão; verificamos que não houve retry pelo interceptor
        assertEquals(1, server.requestCount)
        response.close()
    }

    @Test
    fun `GIVEN 429 with maxRetries 1 WHEN intercept THEN retries exactly once`() {
        val singleRetryInterceptor = ResilienceInterceptor(maxRetries = 1, initialDelay = 0L)
        val singleRetryClient = OkHttpClient.Builder()
            .addInterceptor(singleRetryInterceptor)
            .build()

        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setResponseCode(429))

        val request = Request.Builder().url(server.url("/test")).build()
        val response = singleRetryClient.newCall(request).execute()

        assertEquals(429, response.code)
        assertEquals(2, server.requestCount)
        response.close()
    }
}
