package com.bina.character_details.data.repository

import com.bina.character_details.data.datasource.CharacterDetailsDataSource
import com.bina.character_details.data.mapper.CharacterDetailsMapper
import com.bina.character_details.domain.model.CharacterDetailsDomain
import com.bina.character_details.domain.repository.CharacterDetailsRepository
import com.bina.domain.DomainResult
import com.bina.network.mapSuccess
import com.bina.network.safeApiCall
import com.bina.network.toDomain

class CharacterDetailsRepositoryImpl(
    private val dataSource: CharacterDetailsDataSource
) : CharacterDetailsRepository {
    override suspend fun getCharacterDetails(id: Int): DomainResult<CharacterDetailsDomain> =
        safeApiCall { dataSource.getCharacterDetails(id) }
            .mapSuccess { CharacterDetailsMapper.toDomain(it) }
            .toDomain()
}
