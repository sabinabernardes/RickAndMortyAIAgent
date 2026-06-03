package com.bina.home.data.mapper

import com.bina.home.data.model.CharacterData
import com.bina.home.data.model.LocationData
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterMapperTest {

    private fun characterData(
        id: Int = 1,
        name: String = "Rick Sanchez",
        status: String = "Alive",
        species: String = "Human",
        image: String = "https://img.png",
        locationName: String = "Earth"
    ) = CharacterData(id, name, status, species, image, LocationData(locationName, ""))

    @Test
    fun `GIVEN character data WHEN toDomain THEN all fields are mapped correctly`() {
        val data = characterData()

        val domain = CharacterMapper.toDomain(data)

        assertEquals(1, domain.id)
        assertEquals("Rick Sanchez", domain.name)
        assertEquals("Alive", domain.status)
        assertEquals("Human", domain.species)
        assertEquals("https://img.png", domain.image)
        assertEquals("Earth", domain.location)
    }

    @Test
    fun `GIVEN character data WHEN toDomain THEN location is extracted from nested LocationData name`() {
        val data = characterData(locationName = "Citadel of Ricks")

        val domain = CharacterMapper.toDomain(data)

        assertEquals("Citadel of Ricks", domain.location)
    }

    @Test
    fun `GIVEN dead character WHEN toDomain THEN status is preserved`() {
        val data = characterData(status = "Dead")

        val domain = CharacterMapper.toDomain(data)

        assertEquals("Dead", domain.status)
    }
}
