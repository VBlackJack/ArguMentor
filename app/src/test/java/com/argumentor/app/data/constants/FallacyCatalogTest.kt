package com.argumentor.app.data.constants

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FallacyCatalogTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `FallacyCatalog contains expected fallacies`() {
        val fallacies = FallacyCatalog.getFallacies(context)
        assertTrue(fallacies.isNotEmpty())
        assertTrue(fallacies.size >= 15)
    }

    @Test
    fun `getFallacyById returns correct fallacy`() {
        val fallacy = FallacyCatalog.getFallacyById(context, "ad_hominem")
        assertNotNull(fallacy)
        assertEquals("ad_hominem", fallacy?.id)
        assertEquals("Ad Hominem", fallacy?.name)
    }

    @Test
    fun `getFallacyById returns null for invalid id`() {
        val fallacy = FallacyCatalog.getFallacyById(context, "non_existent")
        assertNull(fallacy)
    }

    @Test
    fun `searchFallacies finds by name`() {
        val results = FallacyCatalog.searchFallacies(context, "hominem")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.id == "ad_hominem" })
    }

    @Test
    fun `searchFallacies finds by description`() {
        val results = FallacyCatalog.searchFallacies(context, "person")
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun `searchFallacies is case insensitive`() {
        val results = FallacyCatalog.searchFallacies(context, "HOMINEM")
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun `all fallacies have required fields`() {
        FallacyCatalog.getFallacies(context).forEach { fallacy ->
            assertFalse(fallacy.id.isBlank())
            assertFalse(fallacy.name.isBlank())
            assertFalse(fallacy.description.isBlank())
            assertFalse(fallacy.example.isBlank())
        }
    }

    @Test
    fun `all fallacy IDs are unique`() {
        val ids = FallacyCatalog.getFallacies(context).map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }
}
