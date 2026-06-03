package com.bina.home.domain.repository

import androidx.paging.PagingData
import com.bina.home.domain.model.CharacterDomain
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getCharacters(query: String): Flow<PagingData<CharacterDomain>>
}
