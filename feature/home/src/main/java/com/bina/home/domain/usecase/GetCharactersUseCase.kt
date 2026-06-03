package com.bina.home.domain.usecase

import androidx.paging.PagingData
import com.bina.home.domain.model.CharacterDomain
import com.bina.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow

class GetCharactersUseCase(
    private val repository: HomeRepository
) {
    operator fun invoke(query: String): Flow<PagingData<CharacterDomain>> {
        return repository.getCharacters(query)
    }
}
