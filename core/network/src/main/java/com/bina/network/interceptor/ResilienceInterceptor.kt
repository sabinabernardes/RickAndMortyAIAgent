package com.bina.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import kotlin.math.pow

/**
 * Interceptor que lida com instabilidades da API e limites de taxa (Rate Limiting).
 * Implementa uma estratégia de retry com Exponential Backoff e respeita o header 'Retry-After'.
 */
class ResilienceInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelay: Long = 1000L
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)
        var tryCount = 0

        // Retenta se a resposta não for bem-sucedida e o código for passível de retry (429 ou 5xx)
        while (!response.isSuccessful && shouldRetry(response.code) && tryCount < maxRetries) {
            val waitTime = getWaitTime(response, tryCount)

            tryCount++
            response.close() // Fecha a resposta anterior para evitar vazamento de memória

            try {
                Thread.sleep(waitTime)
            } catch (e: InterruptedException) {
                // Se a thread for interrompida, retorna a última resposta obtida
                return response
            }

            response = chain.proceed(request)
        }

        return response
    }

    /**
     * Determina se a requisição deve ser repetida baseada no status code.
     * 429: Too Many Requests
     * 5xx: Server Errors (500 Internal Server Error, 503 Service Unavailable, etc)
     */
    private fun shouldRetry(code: Int) = code == HTTP_TOO_MANY_REQUESTS || code >= HTTP_SERVER_ERROR_THRESHOLD

    /**
     * Calcula o tempo de espera antes da próxima tentativa.
     * Prioriza o header 'Retry-After' se estiver presente, caso contrário usa Exponential Backoff.
     */
    private fun getWaitTime(response: Response, attempt: Int): Long {
        val retryAfter = response.header("Retry-After")?.toLongOrNull()
        return if (retryAfter != null) {
            retryAfter * MILLIS_PER_SECOND
        } else {
            // Exponential backoff: 1s, 2s, 4s...
            initialDelay * (2.0.pow(attempt.toDouble()).toLong())
        }
    }

    companion object {
        private const val HTTP_TOO_MANY_REQUESTS = 429
        private const val HTTP_SERVER_ERROR_THRESHOLD = 500
        private const val MILLIS_PER_SECOND = 1000L
    }
}
