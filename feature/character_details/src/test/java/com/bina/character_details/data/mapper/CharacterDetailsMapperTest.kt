package com.bina.character_details.data.mapper

import com.bina.character_details.data.model.CharacterDetailsData
import com.bina.character_details.data.model.LocationDetailsData
import com.bina.character_details.data.model.OriginData
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterDetailsMapperTest {

    private fun data(
        id: Int = 1,
        name: String = "Rick Sanchez",
        status: String = "Alive",
        species: String = "Human",
        gender: String = "Male",
        originName: String = "Earth (C-137)",
        locationName: String = "Citadel of Ricks",
        image: String = "https://img.png",
        episodes: List<String> = listOf("https://rickandmortyapi.com/api/episode/1")
    ) = CharacterDetailsData(
        id = id, name = name, status = status, species = species,
        type = "", gender = gender,
        origin = OriginData(originName, ""),
        location = LocationDetailsData(locationName, ""),
        image = image, episode = episodes, url = "", created = ""
    )

    @Test
    fun `GIVEN character data WHEN toDomain THEN all scalar fields are mapped`() {
        val domain = CharacterDetailsMapper.toDomain(data())

        assertEquals(1, domain.id)
        assertEquals("Rick Sanchez", domain.name)
        assertEquals("Alive", domain.status)
        assertEquals("Human", domain.species)
        assertEquals("Male", domain.gender)
        assertEquals("https://img.png", domain.image)
    }

    @Test
    fun `GIVEN character data WHEN toDomain THEN origin extracted from nested OriginData`() {
        val domain = CharacterDetailsMapper.toDomain(data(originName = "Earth (C-137)"))

        assertEquals("Earth (C-137)", domain.origin)
    }

    @Test
    fun `GIVEN character data WHEN toDomain THEN location extracted from nested LocationDetailsData`() {
        val domain = CharacterDetailsMapper.toDomain(data(locationName = "Citadel of Ricks"))

        assertEquals("Citadel of Ricks", domain.location)
    }

    @Test
    fun `GIVEN character data with episode urls WHEN toDomain THEN episodeUrls are preserved`() {
        val urls = listOf("https://rickandmortyapi.com/api/episode/1", "https://rickandmortyapi.com/api/episode/2")
        val domain = CharacterDetailsMapper.toDomain(data(episodes = urls))

        assertEquals(urls, domain.episodeUrls)
    }

    @Test
    fun `GIVEN character with no episodes WHEN toDomain THEN episodeUrls is empty`() {
        val domain = CharacterDetailsMapper.toDomain(data(episodes = emptyList()))

        assertEquals(emptyList<String>(), domain.episodeUrls)
    }
}
