package com.bina.character_details.presentation.mapper

import com.bina.character_details.domain.model.CharacterDetailsDomain
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterDetailsUiMapperTest {

    private val mapper = CharacterDetailsUiMapper()

    private fun domain(
        id: Int = 1, name: String = "Morty Smith", status: String = "Alive",
        species: String = "Human", gender: String = "Male",
        origin: String = "Earth", location: String = "Earth", image: String = "img"
    ) = CharacterDetailsDomain(id, name, status, species, gender, origin, location, image, emptyList())

    @Test
    fun `GIVEN domain WHEN map THEN all fields are mapped to UI model`() {
        val ui = mapper.map(domain())

        assertEquals(1, ui.id)
        assertEquals("Morty Smith", ui.name)
        assertEquals("Alive", ui.status)
        assertEquals("Human", ui.species)
        assertEquals("Male", ui.gender)
        assertEquals("Earth", ui.origin)
        assertEquals("Earth", ui.location)
        assertEquals("img", ui.imageUrl)
    }

    @Test
    fun `GIVEN domain with unknown gender WHEN map THEN gender is preserved`() {
        val ui = mapper.map(domain(gender = "unknown"))

        assertEquals("unknown", ui.gender)
    }
}
