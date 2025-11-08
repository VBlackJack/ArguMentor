package com.argumentor.app.data.constants

/**
 * Library of templates for different types of argument structures.
 * Helps users create well-structured arguments following academic patterns.
 */
object TemplateLibrary {
    data class Template(
        val id: String,
        val name: String,
        val description: String,
        val fields: List<TemplateField>
    )

    data class TemplateField(
        val name: String,
        val description: String,
        val required: Boolean = true,
        val multiline: Boolean = false
    )

    val TEMPLATES = listOf(
        Template(
            id = "doctrinal_claim",
            name = "Affirmation Doctrinale",
            description = "Pour analyser une position doctrinale religieuse, philosophique ou idéologique",
            fields = listOf(
                TemplateField("Définition", "Définition claire de la doctrine", true, true),
                TemplateField("Textes cités", "Citations des textes sources", true, true),
                TemplateField("Contexte historique", "Contexte de formulation", true, true),
                TemplateField("Variantes", "Différentes interprétations", false, true),
                TemplateField("Sources primaires", "Textes originaux", true, true),
                TemplateField("Sources secondaires", "Analyses académiques", false, true),
                TemplateField("Contre-arguments typiques", "Objections courantes", false, true),
                TemplateField("Questions socratiques", "Questions pour approfondir", false, true)
            )
        ),
        Template(
            id = "argument_from_authority",
            name = "Argument d'Autorité",
            description = "Pour évaluer un argument basé sur l'expertise d'une autorité",
            fields = listOf(
                TemplateField("Qui est l'autorité", "Nom et titre", true),
                TemplateField("Domaine d'expertise", "Champ de compétence", true),
                TemplateField("Qualifications", "Diplômes, publications, expérience", true, true),
                TemplateField("Conflits d'intérêt", "Biais potentiels", true, true),
                TemplateField("Consensus scientifique", "Position de la communauté", true, true),
                TemplateField("Sources contradictoires", "Autorités en désaccord", false, true),
                TemplateField("Date de l'affirmation", "Contexte temporel", false)
            )
        ),
        Template(
            id = "scientific_fact",
            name = "Fait Scientifique",
            description = "Pour documenter une affirmation scientifique",
            fields = listOf(
                TemplateField("Hypothèse", "Hypothèse testée", true, true),
                TemplateField("Méthode", "Protocole expérimental", true, true),
                TemplateField("Échantillon", "Taille et caractéristiques", true),
                TemplateField("Résultats", "Données obtenues", true, true),
                TemplateField("Date de publication", "Année de l'étude", true),
                TemplateField("Limites", "Limitations méthodologiques", true, true),
                TemplateField("Réplications", "Études confirmant/infirmant", false, true),
                TemplateField("Méta-analyses", "Synthèses disponibles", false, true),
                TemplateField("Niveau de preuve", "Force de l'évidence (faible/moyen/fort)", true)
            )
        ),
        Template(
            id = "testimony",
            name = "Témoignage",
            description = "Pour évaluer la fiabilité d'un témoignage",
            fields = listOf(
                TemplateField("Témoin", "Identité et contexte", true),
                TemplateField("Date du témoignage", "Quand rapporté", true),
                TemplateField("Date des faits", "Quand observés", true),
                TemplateField("Circonstances", "Conditions d'observation", true, true),
                TemplateField("Vérifiabilité", "Éléments vérifiables", true, true),
                TemplateField("Corroborations", "Témoignages concordants", false, true),
                TemplateField("Contradictions", "Témoignages divergents", false, true),
                TemplateField("Explications alternatives", "Autres interprétations possibles", false, true),
                TemplateField("Motivations", "Raisons de témoigner", false, true)
            )
        ),
        Template(
            id = "academic_comparison",
            name = "Comparatif Académique",
            description = "Pour comparer deux thèses ou positions de manière systématique",
            fields = listOf(
                TemplateField("Thèse A", "Première position", true, true),
                TemplateField("Thèse B", "Deuxième position", true, true),
                TemplateField("Critères de comparaison", "Points d'évaluation", true, true),
                TemplateField("Arguments pour A", "Forces de la thèse A", true, true),
                TemplateField("Arguments contre A", "Faiblesses de la thèse A", true, true),
                TemplateField("Arguments pour B", "Forces de la thèse B", true, true),
                TemplateField("Arguments contre B", "Faiblesses de la thèse B", true, true),
                TemplateField("Consensus académique", "Position dominante", false, true),
                TemplateField("Bibliographie", "Sources principales", true, true)
            )
        ),
        Template(
            id = "historical_claim",
            name = "Affirmation Historique",
            description = "Pour analyser une affirmation sur un événement historique",
            fields = listOf(
                TemplateField("Événement", "Description de l'événement", true, true),
                TemplateField("Date", "Datation de l'événement", true),
                TemplateField("Sources primaires", "Documents contemporains", true, true),
                TemplateField("Datation des sources", "Âge des manuscrits/artefacts", true, true),
                TemplateField("Sources secondaires", "Analyses historiques", false, true),
                TemplateField("Consensus historique", "Position des historiens", true, true),
                TemplateField("Débats", "Points de controverse", false, true),
                TemplateField("Contexte", "Contexte historique", true, true)
            )
        )
    )

    fun getTemplateById(id: String): Template? = TEMPLATES.find { it.id == id }

    fun searchTemplates(query: String): List<Template> {
        val lowerQuery = query.lowercase()
        return TEMPLATES.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.description.lowercase().contains(lowerQuery)
        }
    }
}
