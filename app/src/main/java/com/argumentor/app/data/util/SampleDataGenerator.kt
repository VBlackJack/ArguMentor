package com.argumentor.app.data.util

import com.argumentor.app.data.model.*
import com.argumentor.app.data.repository.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleDataGenerator @Inject constructor(
    private val topicRepository: TopicRepository,
    private val claimRepository: ClaimRepository,
    private val rebuttalRepository: RebuttalRepository,
    private val evidenceRepository: EvidenceRepository,
    private val questionRepository: QuestionRepository,
    private val sourceRepository: SourceRepository
) {

    suspend fun generateSampleData() {
        // Check if sample data already exists
        val existingTopics = topicRepository.getAllTopics().first()
        if (existingTopics.isNotEmpty()) {
            // Sample data already exists, don't duplicate
            return
        }

        // Create topic
        val topic = Topic(
            title = "Faut-il apprendre à coder ?",
            summary = "Un débat sur l'importance de l'apprentissage de la programmation dans l'éducation moderne",
            posture = Topic.Posture.NEUTRAL_CRITIQUE,
            tags = listOf("éducation", "technologie", "compétences")
        )
        topicRepository.insertTopic(topic)

        // Create PRO claims
        val claim1 = Claim(
            topics = listOf(topic.id),
            text = "Apprendre à coder développe la pensée logique et la résolution de problèmes",
            stance = Claim.Stance.PRO,
            strength = Claim.Strength.HIGH
        )
        claimRepository.insertClaim(claim1)

        val claim2 = Claim(
            topics = listOf(topic.id),
            text = "La programmation est une compétence de plus en plus demandée sur le marché du travail",
            stance = Claim.Stance.PRO,
            strength = Claim.Strength.MEDIUM
        )
        claimRepository.insertClaim(claim2)

        // Create CON claims
        val claim3 = Claim(
            topics = listOf(topic.id),
            text = "Tout le monde n'a pas besoin de programmer, il faut prioriser d'autres compétences",
            stance = Claim.Stance.CON,
            strength = Claim.Strength.MEDIUM
        )
        claimRepository.insertClaim(claim3)

        val claim4 = Claim(
            topics = listOf(topic.id),
            text = "L'apprentissage du code peut être frustrant et démotivant pour certains",
            stance = Claim.Stance.CON,
            strength = Claim.Strength.LOW
        )
        claimRepository.insertClaim(claim4)

        // Add rebuttals (counter-arguments)
        val rebuttal1 = Rebuttal(
            claimId = claim3.id,
            text = "Même sans devenir programmeur, la pensée computationnelle aide dans de nombreux domaines",
            fallacyTag = null
        )
        rebuttalRepository.insertRebuttal(rebuttal1)

        val rebuttal2 = Rebuttal(
            claimId = claim4.id,
            text = "Avec les bonnes méthodes pédagogiques, l'apprentissage peut être ludique et motivant",
            fallacyTag = null
        )
        rebuttalRepository.insertRebuttal(rebuttal2)

        // Add evidence
        val source1 = Source(
            title = "Rapport sur l'éducation numérique",
            citation = "L'apprentissage de la programmation améliore les compétences en résolution de problèmes de 35%",
            url = "https://example.com/education-numerique"
        )
        sourceRepository.insertSource(source1)

        val evidence1 = Evidence(
            claimId = claim1.id,
            content = "Plusieurs études montrent que l'apprentissage du code renforce la pensée logique",
            type = Evidence.EvidenceType.STUDY,
            sourceId = source1.id
        )
        evidenceRepository.insertEvidence(evidence1)

        val evidence2 = Evidence(
            claimId = claim2.id,
            content = "Les offres d'emploi en informatique ont augmenté de 40% ces 5 dernières années",
            type = Evidence.EvidenceType.STAT,
            sourceId = null
        )
        evidenceRepository.insertEvidence(evidence2)

        // Add questions
        val question1 = Question(
            targetId = topic.id,
            text = "Quel est le meilleur âge pour commencer à apprendre la programmation ?",
            kind = Question.QuestionKind.CLARIFYING
        )
        questionRepository.insertQuestion(question1)

        val question2 = Question(
            targetId = claim1.id,
            text = "Peut-on développer la pensée logique par d'autres moyens que la programmation ?",
            kind = Question.QuestionKind.SOCRATIC
        )
        questionRepository.insertQuestion(question2)

        val question3 = Question(
            targetId = claim2.id,
            text = "Cette tendance du marché du travail est-elle durable à long terme ?",
            kind = Question.QuestionKind.CHALLENGE
        )
        questionRepository.insertQuestion(question3)
    }
}
