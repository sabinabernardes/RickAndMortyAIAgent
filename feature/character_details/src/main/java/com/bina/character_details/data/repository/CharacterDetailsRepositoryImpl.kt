package com.bina.character_details.data.repository

import com.bina.character_details.data.datasource.CharacterDetailsDataSource
import com.bina.character_details.data.mapper.CharacterDetailsMapper
import com.bina.character_details.domain.model.CharacterDetailsDomain
import com.bina.character_details.domain.repository.CharacterDetailsRepository

class CharacterDetailsRepositoryImpl(
    private val dataSource: CharacterDetailsDataSource
) : CharacterDetailsRepository {
    override suspend fun getCharacterDetails(id: Int): CharacterDetailsDomain {
        val data = dataSource.getCharacterDetails(id)
        return CharacterDetailsMapper.toDomain(data)
    }
}
