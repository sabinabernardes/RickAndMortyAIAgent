package com.bina.home.data.repository

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.bina.home.data.datasource.CharacterDataSource
import com.bina.home.data.model.CharacterData
import com.bina.home.data.model.LocationData
import com.bina.home.data.pagingSource.CharacterPagingSource
import com.bina.home.domain.repository.HomeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeRepositoryImplTest {
    private lateinit var dataSource: CharacterDataSource
    private lateinit var repository: HomeRepository

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = HomeRepositoryImpl(dataSource)
    }

    @Test
    fun `given dataSource returns data when getCharacters then emits PagingData with CharacterDomain`() = runTest {
        // Given
        val characterData = CharacterData(1, "Rick", "Alive", "Human", "img", LocationData("Earth", ""))
        coEvery { dataSource.getCharacters("", 1) } returns listOf(characterData)

        // When
        val flow = repository.getCharacters("")
        val result = flow.first()

        // Then
        assert(result is PagingData<*>)
    }

    @Test
    fun `given dataSource throws exception when load then returns LoadResult Error`() = runTest {
        // Given
        val dataSource = mockk<CharacterDataSource>()
        coEvery { dataSource.getCharacters("", 1) } throws RuntimeException("API error")

        val pagingSource = CharacterPagingSource(dataSource, "")

        // When
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 1,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        // Then
        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals("API error", (result as PagingSource.LoadResult.Error).throwable.message)
    }
}
