package com.bina.character_details.data.mapper

import com.bina.character_details.data.model.EpisodeData
import org.junit.Assert.assertEquals
import org.junit.Test

class EpisodeMapperTest {

    @Test
    fun `GIVEN episode data WHEN toDomain THEN all fields are mapped`() {
        val data = EpisodeData(id = 1, name = "Pilot", episode = "S01E01", airDate = "December 2, 2013")

        val domain = EpisodeMapper.toDomain(data)

        assertEquals(1, domain.id)
        assertEquals("Pilot", domain.name)
        assertEquals("S01E01", domain.code)
        assertEquals("December 2, 2013", domain.airDate)
    }

    @Test
    fun `GIVEN episode data WHEN toDomain THEN episode field maps to code`() {
        val data = EpisodeData(id = 5, name = "Meeseeks and Destroy", episode = "S01E05", airDate = "January 6, 2014")

        val domain = EpisodeMapper.toDomain(data)

        assertEquals("S01E05", domain.code)
    }
}
