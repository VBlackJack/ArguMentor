package com.argumentor.app.data.constants

import android.content.Context

/**
 * Library of templates for different types of argument structures.
 * Helps users create well-structured arguments following academic patterns.
 *
 * All template texts are internationalized and loaded from string resources
 * to support multiple languages (FR/EN).
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

    /**
     * Returns the list of all available templates with localized strings.
     *
     * @param context Android context used to access string resources
     * @return List of templates with internationalized content
     */
    fun getTemplates(context: Context): List<Template> = listOf(
        Template(
            id = "doctrinal_claim",
            name = context.getString(com.argumentor.app.R.string.template_doctrinal_claim_name),
            description = context.getString(com.argumentor.app.R.string.template_doctrinal_claim_description),
            fields = listOf(
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_definition_name),
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_definition_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_texts_name),
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_texts_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_historical_context_name),
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_historical_context_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_variants_name),
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_variants_description),
                    false,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_primary_sources_name),
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_primary_sources_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_secondary_sources_name),
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_secondary_sources_description),
                    false,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_counterarguments_name),
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_counterarguments_description),
                    false,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_socratic_questions_name),
                    context.getString(com.argumentor.app.R.string.template_doctrinal_claim_field_socratic_questions_description),
                    false,
                    true
                )
            )
        ),
        Template(
            id = "argument_from_authority",
            name = context.getString(com.argumentor.app.R.string.template_argument_from_authority_name),
            description = context.getString(com.argumentor.app.R.string.template_argument_from_authority_description),
            fields = listOf(
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_argument_from_authority_field_authority_name),
                    context.getString(com.argumentor.app.R.string.template_argument_from_authority_field_authority_description),
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_argument_from_authority_field_expertise_name),
                    context.getString(com.argumentor.app.R.string.template_argument_from_authority_field_expertise_description),
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_argument_from_authority_field_qualifications_name),
                    context.getString(com.argumentor.app.R.string.template_argument_from_authority_field_qualifications_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_argument_from_authority_field_conflicts_name),
                    context.getString(com.argumentor.app.R.string.template_argument_from_authority_field_conflicts_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_argument_from_authority_field_consensus_name),
                    context.getString(com.argumentor.app.R.string.template_argument_from_authority_field_consensus_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_argument_from_authority_field_contradictory_sources_name),
                    context.getString(com.argumentor.app.R.string.template_argument_from_authority_field_contradictory_sources_description),
                    false,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_argument_from_authority_field_date_name),
                    context.getString(com.argumentor.app.R.string.template_argument_from_authority_field_date_description),
                    false
                )
            )
        ),
        Template(
            id = "scientific_fact",
            name = context.getString(com.argumentor.app.R.string.template_scientific_fact_name),
            description = context.getString(com.argumentor.app.R.string.template_scientific_fact_description),
            fields = listOf(
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_hypothesis_name),
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_hypothesis_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_method_name),
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_method_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_sample_name),
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_sample_description),
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_results_name),
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_results_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_publication_date_name),
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_publication_date_description),
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_limits_name),
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_limits_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_replications_name),
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_replications_description),
                    false,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_meta_analyses_name),
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_meta_analyses_description),
                    false,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_evidence_level_name),
                    context.getString(com.argumentor.app.R.string.template_scientific_fact_field_evidence_level_description),
                    true
                )
            )
        ),
        Template(
            id = "testimony",
            name = context.getString(com.argumentor.app.R.string.template_testimony_name),
            description = context.getString(com.argumentor.app.R.string.template_testimony_description),
            fields = listOf(
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_testimony_field_witness_name),
                    context.getString(com.argumentor.app.R.string.template_testimony_field_witness_description),
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_testimony_field_testimony_date_name),
                    context.getString(com.argumentor.app.R.string.template_testimony_field_testimony_date_description),
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_testimony_field_event_date_name),
                    context.getString(com.argumentor.app.R.string.template_testimony_field_event_date_description),
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_testimony_field_circumstances_name),
                    context.getString(com.argumentor.app.R.string.template_testimony_field_circumstances_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_testimony_field_verifiability_name),
                    context.getString(com.argumentor.app.R.string.template_testimony_field_verifiability_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_testimony_field_corroborations_name),
                    context.getString(com.argumentor.app.R.string.template_testimony_field_corroborations_description),
                    false,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_testimony_field_contradictions_name),
                    context.getString(com.argumentor.app.R.string.template_testimony_field_contradictions_description),
                    false,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_testimony_field_alternative_explanations_name),
                    context.getString(com.argumentor.app.R.string.template_testimony_field_alternative_explanations_description),
                    false,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_testimony_field_motivations_name),
                    context.getString(com.argumentor.app.R.string.template_testimony_field_motivations_description),
                    false,
                    true
                )
            )
        ),
        Template(
            id = "academic_comparison",
            name = context.getString(com.argumentor.app.R.string.template_academic_comparison_name),
            description = context.getString(com.argumentor.app.R.string.template_academic_comparison_description),
            fields = listOf(
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_thesis_a_name),
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_thesis_a_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_thesis_b_name),
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_thesis_b_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_criteria_name),
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_criteria_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_arguments_for_a_name),
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_arguments_for_a_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_arguments_against_a_name),
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_arguments_against_a_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_arguments_for_b_name),
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_arguments_for_b_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_arguments_against_b_name),
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_arguments_against_b_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_academic_consensus_name),
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_academic_consensus_description),
                    false,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_bibliography_name),
                    context.getString(com.argumentor.app.R.string.template_academic_comparison_field_bibliography_description),
                    true,
                    true
                )
            )
        ),
        Template(
            id = "historical_claim",
            name = context.getString(com.argumentor.app.R.string.template_historical_claim_name),
            description = context.getString(com.argumentor.app.R.string.template_historical_claim_description),
            fields = listOf(
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_event_name),
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_event_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_date_name),
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_date_description),
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_primary_sources_name),
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_primary_sources_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_source_dating_name),
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_source_dating_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_secondary_sources_name),
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_secondary_sources_description),
                    false,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_historical_consensus_name),
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_historical_consensus_description),
                    true,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_debates_name),
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_debates_description),
                    false,
                    true
                ),
                TemplateField(
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_context_name),
                    context.getString(com.argumentor.app.R.string.template_historical_claim_field_context_description),
                    true,
                    true
                )
            )
        )
    )

    /**
     * Finds a template by its ID.
     *
     * @param context Android context used to access string resources
     * @param id The unique identifier of the template
     * @return The template with the given ID, or null if not found
     */
    fun getTemplateById(context: Context, id: String): Template? =
        getTemplates(context).find { it.id == id }

    /**
     * Searches templates by name or description.
     *
     * @param context Android context used to access string resources
     * @param query The search query (case-insensitive)
     * @return List of templates matching the query
     */
    fun searchTemplates(context: Context, query: String): List<Template> {
        val lowerQuery = query.lowercase()
        return getTemplates(context).filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.description.lowercase().contains(lowerQuery)
        }
    }
}
