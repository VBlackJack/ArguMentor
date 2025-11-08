package com.argumentor.app.data.constants

import org.junit.Assert.*
import org.junit.Test

class TemplateLibraryTest {

    @Test
    fun `TemplateLibrary contains expected templates`() {
        assertTrue(TemplateLibrary.TEMPLATES.isNotEmpty())
        assertTrue(TemplateLibrary.TEMPLATES.size >= 6)
    }

    @Test
    fun `getTemplateById returns correct template`() {
        val template = TemplateLibrary.getTemplateById("doctrinal_claim")
        assertNotNull(template)
        assertEquals("doctrinal_claim", template?.id)
        assertEquals("Affirmation Doctrinale", template?.name)
    }

    @Test
    fun `getTemplateById returns null for invalid id`() {
        val template = TemplateLibrary.getTemplateById("non_existent")
        assertNull(template)
    }

    @Test
    fun `searchTemplates finds by name`() {
        val results = TemplateLibrary.searchTemplates("scientifique")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.id == "scientific_fact" })
    }

    @Test
    fun `searchTemplates finds by description`() {
        val results = TemplateLibrary.searchTemplates("expert")
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun `all templates have required fields`() {
        TemplateLibrary.TEMPLATES.forEach { template ->
            assertFalse(template.id.isBlank())
            assertFalse(template.name.isBlank())
            assertFalse(template.description.isBlank())
            assertTrue(template.fields.isNotEmpty())
        }
    }

    @Test
    fun `all template IDs are unique`() {
        val ids = TemplateLibrary.TEMPLATES.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `template fields have required properties`() {
        TemplateLibrary.TEMPLATES.forEach { template ->
            template.fields.forEach { field ->
                assertFalse(field.name.isBlank())
                assertFalse(field.description.isBlank())
            }
        }
    }

    @Test
    fun `doctrinal_claim template has expected fields`() {
        val template = TemplateLibrary.getTemplateById("doctrinal_claim")
        assertNotNull(template)
        assertTrue(template!!.fields.any { it.name == "Définition" })
        assertTrue(template.fields.any { it.name == "Textes cités" })
        assertTrue(template.fields.any { it.name == "Contexte historique" })
    }

    @Test
    fun `scientific_fact template has expected fields`() {
        val template = TemplateLibrary.getTemplateById("scientific_fact")
        assertNotNull(template)
        assertTrue(template!!.fields.any { it.name == "Hypothèse" })
        assertTrue(template.fields.any { it.name == "Méthode" })
        assertTrue(template.fields.any { it.name == "Résultats" })
    }
}
