package com.argumentor.app.data.constants

import org.junit.Assert.*
import org.junit.Test

class FallacyCatalogTest {

    @Test
    fun `FallacyCatalog contains expected fallacies`() {
        assertTrue(FallacyCatalog.FALLACIES.isNotEmpty())
        assertTrue(FallacyCatalog.FALLACIES.size >= 15)
    }

    @Test
    fun `getFallacyById returns correct fallacy`() {
        val fallacy = FallacyCatalog.getFallacyById("ad_hominem")
        assertNotNull(fallacy)
        assertEquals("ad_hominem", fallacy?.id)
        assertEquals("Ad Hominem", fallacy?.name)
    }

    @Test
    fun `getFallacyById returns null for invalid id`() {
        val fallacy = FallacyCatalog.getFallacyById("non_existent")
        assertNull(fallacy)
    }

    @Test
    fun `searchFallacies finds by name`() {
        val results = FallacyCatalog.searchFallacies("hominem")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.id == "ad_hominem" })
    }

    @Test
    fun `searchFallacies finds by description`() {
        val results = FallacyCatalog.searchFallacies("personne")
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun `searchFallacies is case insensitive`() {
        val results = FallacyCatalog.searchFallacies("HOMINEM")
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun `all fallacies have required fields`() {
        FallacyCatalog.FALLACIES.forEach { fallacy ->
            assertFalse(fallacy.id.isBlank())
            assertFalse(fallacy.name.isBlank())
            assertFalse(fallacy.description.isBlank())
            assertFalse(fallacy.example.isBlank())
        }
    }

    @Test
    fun `all fallacy IDs are unique`() {
        val ids = FallacyCatalog.FALLACIES.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }
}
