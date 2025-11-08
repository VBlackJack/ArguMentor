package com.argumentor.app.data.model

import org.junit.Assert.*
import org.junit.Test

class TopicTest {

    @Test
    fun `Topic Posture fromString parses correctly`() {
        assertEquals(
            Topic.Posture.NEUTRAL_CRITIQUE,
            Topic.Posture.fromString("neutral_critique")
        )
        assertEquals(
            Topic.Posture.SCEPTIQUE,
            Topic.Posture.fromString("sceptique")
        )
        assertEquals(
            Topic.Posture.COMPARATIF_ACADEMIQUE,
            Topic.Posture.fromString("comparatif_academique")
        )
    }

    @Test
    fun `Topic Posture fromString handles case insensitivity`() {
        assertEquals(
            Topic.Posture.NEUTRAL_CRITIQUE,
            Topic.Posture.fromString("NEUTRAL_CRITIQUE")
        )
        assertEquals(
            Topic.Posture.SCEPTIQUE,
            Topic.Posture.fromString("SCEPTIQUE")
        )
    }

    @Test
    fun `Topic Posture fromString returns default for invalid input`() {
        assertEquals(
            Topic.Posture.NEUTRAL_CRITIQUE,
            Topic.Posture.fromString("invalid")
        )
    }

    @Test
    fun `Topic Posture toString returns correct value`() {
        assertEquals("neutral_critique", Topic.Posture.NEUTRAL_CRITIQUE.toString())
        assertEquals("sceptique", Topic.Posture.SCEPTIQUE.toString())
        assertEquals("comparatif_academique", Topic.Posture.COMPARATIF_ACADEMIQUE.toString())
    }

    @Test
    fun `Topic creates with default values`() {
        val topic = Topic(
            title = "Test Topic",
            summary = "Test Summary"
        )

        assertNotNull(topic.id)
        assertTrue(topic.id.isNotEmpty())
        assertEquals(Topic.Posture.NEUTRAL_CRITIQUE, topic.posture)
        assertTrue(topic.tags.isEmpty())
        assertNotNull(topic.createdAt)
        assertNotNull(topic.updatedAt)
    }

    @Test
    fun `Topic generates unique IDs`() {
        val topic1 = Topic(title = "Topic 1", summary = "Summary 1")
        val topic2 = Topic(title = "Topic 2", summary = "Summary 2")

        assertNotEquals(topic1.id, topic2.id)
    }
}
