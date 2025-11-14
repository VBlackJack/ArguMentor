package com.argumentor.app.data.model

import org.junit.Assert.*
import org.junit.Test

class TopicTest {

    @Test
    fun `Topic Posture fromString parses correctly`() {
        assertEquals(
            Topic.Posture.NEUTRAL_CRITICAL,
            Topic.Posture.fromString("neutral_critique")
        )
        assertEquals(
            Topic.Posture.SKEPTICAL,
            Topic.Posture.fromString("sceptique")
        )
        assertEquals(
            Topic.Posture.ACADEMIC_COMPARATIVE,
            Topic.Posture.fromString("comparatif_academique")
        )
    }

    @Test
    fun `Topic Posture fromString handles case insensitivity`() {
        assertEquals(
            Topic.Posture.NEUTRAL_CRITICAL,
            Topic.Posture.fromString("NEUTRAL_CRITIQUE")
        )
        assertEquals(
            Topic.Posture.SKEPTICAL,
            Topic.Posture.fromString("SCEPTIQUE")
        )
    }

    @Test
    fun `Topic Posture fromString returns default for invalid input`() {
        assertEquals(
            Topic.Posture.NEUTRAL_CRITICAL,
            Topic.Posture.fromString("invalid")
        )
    }

    @Test
    fun `Topic Posture toString returns correct value`() {
        assertEquals("neutral_critical", Topic.Posture.NEUTRAL_CRITICAL.toString())
        assertEquals("skeptical", Topic.Posture.SKEPTICAL.toString())
        assertEquals("academic_comparative", Topic.Posture.ACADEMIC_COMPARATIVE.toString())
    }

    @Test
    fun `Topic creates with default values`() {
        val topic = Topic(
            title = "Test Topic",
            summary = "Test Summary"
        )

        assertNotNull(topic.id)
        assertTrue(topic.id.isNotEmpty())
        assertEquals(Topic.Posture.NEUTRAL_CRITICAL, topic.posture)
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
