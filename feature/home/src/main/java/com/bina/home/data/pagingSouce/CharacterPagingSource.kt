package com.bina.home.data.pagingSouce

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bina.home.data.datasource.CharacterDataSource
import com.bina.home.data.model.CharacterData

internal class CharacterPagingSource(
    private val dataSource: CharacterDataSource,
    private val query: String
) : PagingSource<Int, CharacterData>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CharacterData> {
        val page = params.key ?: 1
        return try {
            val characters = dataSource.getCharacters(query, page)
            LoadResult.Page(
                data = characters,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (characters.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CharacterData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}