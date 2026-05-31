package com.bina.home.presentation.mapper

import com.bina.home.domain.model.CharacterDomain
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterUiMapperTest {

    private val mapper = CharacterUiMapper()

    private fun domain(
        id: Int = 1,
        name: String = "Morty Smith",
        status: String = "Alive",
        species: String = "Human",
        image: String = "https://img.png",
        location: String = "Earth"
    ) = CharacterDomain(id, name, status, species, image, location)

    @Test
    fun `GIVEN domain model WHEN map THEN all fields are mapped to UI model`() {
        val domain = domain()

        val ui = mapper.map(domain)

        assertEquals(1, ui.id)
        assertEquals("Morty Smith", ui.name)
        assertEquals("Alive", ui.status)
        assertEquals("Human", ui.species)
        assertEquals("https://img.png", ui.imageUrl)
        assertEquals("Earth", ui.location)
    }

    @Test
    fun `GIVEN domain with unknown status WHEN map THEN status is preserved`() {
        val domain = domain(status = "unknown")

        val ui = mapper.map(domain)

        assertEquals("unknown", ui.status)
    }
}