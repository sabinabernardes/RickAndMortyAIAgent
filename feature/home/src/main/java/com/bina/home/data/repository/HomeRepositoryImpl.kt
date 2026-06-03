package com.bina.home.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.bina.home.data.datasource.CharacterDataSource
import com.bina.home.data.mapper.CharacterMapper
import com.bina.home.data.pagingSource.CharacterPagingSource
import com.bina.home.domain.model.CharacterDomain
import com.bina.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeRepositoryImpl(
    private val dataSource: CharacterDataSource
) : HomeRepository {
    override fun getCharacters(query: String): Flow<PagingData<CharacterDomain>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { CharacterPagingSource(dataSource, query) }
        ).flow.map { pagingData ->
            pagingData.map(CharacterMapper::toDomain)
        }
    }
}