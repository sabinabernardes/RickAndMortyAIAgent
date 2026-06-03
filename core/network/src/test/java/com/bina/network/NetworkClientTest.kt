package com.bina.network
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.test.assertEquals

class NetworkClientTest {

    private lateinit var server: MockWebServer
    private lateinit var api: TestApi

    interface TestApi {
        @GET("character/1")
        suspend fun getCharacter(): CharacterResponse
    }

    data class CharacterResponse(val id: Int, val name: String)

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                )
            )
            .build()

        api = retrofit.create(TestApi::class.java)
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun `should return character response successfully`() = runBlocking {
        val mockJson = """{ "id": 1, "name": "Rick Sanchez" }"""
        server.enqueue(MockResponse().setBody(mockJson).setResponseCode(200))

        val result = api.getCharacter()

        assertEquals(1, result.id)
        assertEquals("Rick Sanchez", result.name)
    }
}
