package com.argumentor.app.data.model

import org.junit.Assert.*
import org.junit.Test

class ClaimTest {

    @Test
    fun `Claim Stance fromString parses correctly`() {
        assertEquals(Claim.Stance.PRO, Claim.Stance.fromString("pro"))
        assertEquals(Claim.Stance.CON, Claim.Stance.fromString("con"))
        assertEquals(Claim.Stance.NEUTRAL, Claim.Stance.fromString("neutral"))
    }

    @Test
    fun `Claim Stance toString returns correct value`() {
        assertEquals("pro", Claim.Stance.PRO.toString())
        assertEquals("con", Claim.Stance.CON.toString())
        assertEquals("neutral", Claim.Stance.NEUTRAL.toString())
    }

    @Test
    fun `Claim Strength fromString parses correctly`() {
        assertEquals(Claim.Strength.LOW, Claim.Strength.fromString("low"))
        assertEquals(Claim.Strength.MEDIUM, Claim.Strength.fromString("med"))
        assertEquals(Claim.Strength.MEDIUM, Claim.Strength.fromString("medium"))
        assertEquals(Claim.Strength.HIGH, Claim.Strength.fromString("high"))
    }

    @Test
    fun `Claim Strength toString returns correct value`() {
        assertEquals("low", Claim.Strength.LOW.toString())
        assertEquals("medium", Claim.Strength.MEDIUM.toString())
        assertEquals("high", Claim.Strength.HIGH.toString())
    }

    @Test
    fun `Claim creates with default values`() {
        val claim = Claim(text = "Test Claim")

        assertNotNull(claim.id)
        assertTrue(claim.id.isNotEmpty())
        assertEquals(Claim.Stance.NEUTRAL, claim.stance)
        assertEquals(Claim.Strength.MEDIUM, claim.strength)
        assertTrue(claim.topics.isEmpty())
        assertNotNull(claim.createdAt)
        assertNotNull(claim.updatedAt)
    }

    @Test
    fun `Claim generates unique IDs`() {
        val claim1 = Claim(text = "Claim 1")
        val claim2 = Claim(text = "Claim 2")

        assertNotEquals(claim1.id, claim2.id)
    }
}
