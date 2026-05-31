package com.bina.home.data.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bina.home.data.datasource.CharacterDataSource
import com.bina.home.data.model.CharacterData
import com.bina.home.data.model.LocationData
import com.bina.home.data.pagingSouce.CharacterPagingSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterPagingSourceTest {
    private lateinit var dataSource: CharacterDataSource
    private lateinit var pagingSource: CharacterPagingSource

    @Before
    fun setUp() {
        dataSource = mockk()
        pagingSource = CharacterPagingSource(dataSource, "")
    }

    @Test
    fun `given dataSource returns data when load then returns page`() = runTest {
        // Given
        val characters = listOf(CharacterData(1, "Rick", "Alive", "Human", "img", LocationData("Earth", "")))
        coEvery { dataSource.getCharacters("", 1) } returns characters

        // When
        val result = pagingSource.load(PagingSource.LoadParams.Refresh(key = 1, loadSize = 20, placeholdersEnabled = false))

        // Then
        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(characters, page.data)
        assertEquals(null, page.prevKey)
        assertEquals(2, page.nextKey)
        coVerify { dataSource.getCharacters("", 1) }
    }

    @Test
    fun `given dataSource throws exception when load then returns error`() = runTest {
        // Given
        coEvery { dataSource.getCharacters("", 1) } throws RuntimeException("API error")

        // When
        val result = pagingSource.load(PagingSource.LoadParams.Refresh(key = 1, loadSize = 20, placeholdersEnabled = false))

        // Then
        assertTrue(result is PagingSource.LoadResult.Error)
        val error = result as PagingSource.LoadResult.Error
        assertEquals("API error", error.throwable.message)
        coVerify { dataSource.getCharacters("", 1) }
    }

    @Test
    fun `GIVEN null key WHEN load THEN defaults to page 1`() = runTest {
        val characters = listOf(CharacterData(1, "Rick", "Alive", "Human", "img", LocationData("Earth", "")))
        coEvery { dataSource.getCharacters("", 1) } returns characters

        val result = pagingSource.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))

        assertTrue(result is PagingSource.LoadResult.Page)
        coVerify { dataSource.getCharacters("", 1) }
    }

    @Test
    fun `GIVEN empty results WHEN load THEN nextKey is null`() = runTest {
        coEvery { dataSource.getCharacters("", 1) } returns emptyList()

        val result = pagingSource.load(PagingSource.LoadParams.Refresh(key = 1, loadSize = 20, placeholdersEnabled = false))

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(null, page.nextKey)
    }

    @Test
    fun `GIVEN page 2 WHEN load THEN prevKey is 1`() = runTest {
        val characters = listOf(CharacterData(2, "Morty", "Alive", "Human", "img", LocationData("Earth", "")))
        coEvery { dataSource.getCharacters("", 2) } returns characters

        val result = pagingSource.load(PagingSource.LoadParams.Refresh(key = 2, loadSize = 20, placeholdersEnabled = false))

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.prevKey)
        assertEquals(3, page.nextKey)
    }

    @Test
    fun `GIVEN null anchorPosition WHEN getRefreshKey THEN returns null`() {
        val state = PagingState<Int, CharacterData>(pages = emptyList(), anchorPosition = null, config = PagingConfig(20), leadingPlaceholderCount = 0)

        val result = pagingSource.getRefreshKey(state)

        assertEquals(null, result)
    }
}

