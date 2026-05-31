package com.bina.chat.chat.presentation.mapper

import com.bina.chat.chat.domain.model.ChatMessageDomain
import com.bina.chat.chat.domain.model.MessageRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ChatMessageUiMapperTest {

    private val mapper = ChatMessageUiMapper()

    @Test
    fun `GIVEN domain with USER role WHEN map THEN ui model has USER role`() {
        val domain = ChatMessageDomain(role = MessageRole.USER, text = "Oi Rick", timestampMs = 1000L)

        val ui = mapper.map(domain)

        assertEquals(MessageRole.USER, ui.role)
    }

    @Test
    fun `GIVEN domain with AI role WHEN map THEN ui model has AI role`() {
        val domain = ChatMessageDomain(role = MessageRole.AI, text = "Morty!", timestampMs = 2000L)

        val ui = mapper.map(domain)

        assertEquals(MessageRole.AI, ui.role)
    }

    @Test
    fun `GIVEN domain message WHEN map THEN text is preserved and isStreaming is false by default`() {
        val domain = ChatMessageDomain(role = MessageRole.AI, text = "Wubba lubba dub dub", timestampMs = 1000L)

        val ui = mapper.map(domain)

        assertEquals("Wubba lubba dub dub", ui.text)
        assertFalse(ui.isStreaming)
    }
}