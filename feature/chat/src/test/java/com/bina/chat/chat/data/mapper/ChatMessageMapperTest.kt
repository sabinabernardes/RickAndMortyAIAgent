package com.bina.chat.chat.data.mapper

import com.bina.chat.chat.data.model.ChatMessageData
import com.bina.chat.chat.domain.model.MessageRole
import org.junit.Assert.assertEquals
import org.junit.Test

class ChatMessageMapperTest {

    @Test
    fun `GIVEN data with role "user" WHEN toDomain THEN role is USER`() {
        val data = ChatMessageData(role = "user", text = "Hello", timestampMs = 1000L)

        val domain = ChatMessageMapper.toDomain(data)

        assertEquals(MessageRole.USER, domain.role)
    }

    @Test
    fun `GIVEN data with role other than "user" WHEN toDomain THEN role is AI`() {
        val data = ChatMessageData(role = "model", text = "Response", timestampMs = 1000L)

        val domain = ChatMessageMapper.toDomain(data)

        assertEquals(MessageRole.AI, domain.role)
    }

    @Test
    fun `GIVEN data with text and timestamp WHEN toDomain THEN values are preserved`() {
        val data = ChatMessageData(role = "user", text = "Wubba lubba", timestampMs = 42000L)

        val domain = ChatMessageMapper.toDomain(data)

        assertEquals("Wubba lubba", domain.text)
        assertEquals(42000L, domain.timestampMs)
    }
}