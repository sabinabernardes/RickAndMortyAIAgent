package com.bina.character_details.presentation.mapper

import com.bina.character_details.domain.model.EpisodeDomain
import org.junit.Assert.assertEquals
import org.junit.Test

class EpisodeUiMapperTest {

    private val mapper = EpisodeUiMapper()

    @Test
    fun `GIVEN domain episode WHEN map THEN all fields are mapped`() {
        val domain = EpisodeDomain(id = 1, name = "Pilot", code = "S01E01", airDate = "December 2, 2013")

        val ui = mapper.map(domain)

        assertEquals(1, ui.id)
        assertEquals("Pilot", ui.name)
        assertEquals("S01E01", ui.code)
        assertEquals("December 2, 2013", ui.airDate)
    }
}
