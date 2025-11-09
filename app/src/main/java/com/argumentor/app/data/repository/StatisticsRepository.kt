package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.*
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Topic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Statistics data model
 */
data class Statistics(
    val totalTopics: Int = 0,
    val totalClaims: Int = 0,
    val totalRebuttals: Int = 0,
    val totalEvidence: Int = 0,
    val totalQuestions: Int = 0,
    val totalSources: Int = 0,
    val claimsByStance: Map<Claim.Stance, Int> = emptyMap(),
    val claimsByStrength: Map<Claim.Strength, Int> = emptyMap(),
    val topicsByPosture: Map<Topic.Posture, Int> = emptyMap(),
    val mostDebatedTopics: List<TopicStats> = emptyList(),
    val averageClaimsPerTopic: Double = 0.0,
    val averageRebuttalsPerClaim: Double = 0.0,
    val averageStrength: Double = 0.0
)

/**
 * Topic statistics
 */
data class TopicStats(
    val topicId: String,
    val topicTitle: String,
    val claimCount: Int,
    val rebuttalCount: Int,
    val evidenceCount: Int,
    val questionCount: Int
)

/**
 * Repository for calculating and providing statistics about the argumentation data.
 */
@Singleton
class StatisticsRepository @Inject constructor(
    private val topicDao: TopicDao,
    private val claimDao: ClaimDao,
    private val rebuttalDao: RebuttalDao,
    private val evidenceDao: EvidenceDao,
    private val questionDao: QuestionDao,
    private val sourceDao: SourceDao
) {

    /**
     * Get comprehensive statistics as a Flow
     */
    fun getStatistics(): Flow<Statistics> {
        return combine(
            topicDao.getAllTopics(),
            claimDao.getAllClaims(),
            rebuttalDao.getAllRebuttals(),
            evidenceDao.getAllEvidence(),
            questionDao.getAllQuestions(),
            sourceDao.getAllSources()
        ) { topics, claims, rebuttals, evidence, questions, sources ->

            // Claims by stance
            val claimsByStance = claims.groupingBy { it.stance }.eachCount()

            // Claims by strength
            val claimsByStrength = claims.groupingBy { it.strength }.eachCount()

            // Topics by posture
            val topicsByPosture = topics.groupingBy { it.posture }.eachCount()

            // Most debated topics (by claim count)
            val topicStats = topics.map { topic ->
                val topicClaims = claims.filter { topic.id in it.topics }
                val topicRebuttals = topicClaims.flatMap { claim ->
                    rebuttals.filter { it.claimId == claim.id }
                }
                val topicEvidence = evidence.filter { ev ->
                    topicClaims.any { it.id == ev.claimId } ||
                            topicRebuttals.any { it.id == ev.rebuttalId }
                }
                val topicQuestions = questions.filter { it.topicId == topic.id }

                TopicStats(
                    topicId = topic.id,
                    topicTitle = topic.title,
                    claimCount = topicClaims.size,
                    rebuttalCount = topicRebuttals.size,
                    evidenceCount = topicEvidence.size,
                    questionCount = topicQuestions.size
                )
            }.sortedByDescending { it.claimCount }

            // Calculate averages
            val avgClaimsPerTopic = if (topics.isNotEmpty()) {
                claims.size.toDouble() / topics.size
            } else 0.0

            val avgRebuttalsPerClaim = if (claims.isNotEmpty()) {
                rebuttals.size.toDouble() / claims.size
            } else 0.0

            val avgStrength = if (claims.isNotEmpty()) {
                claims.map { strengthToNumeric(it.strength) }.average()
            } else 0.0

            Statistics(
                totalTopics = topics.size,
                totalClaims = claims.size,
                totalRebuttals = rebuttals.size,
                totalEvidence = evidence.size,
                totalQuestions = questions.size,
                totalSources = sources.size,
                claimsByStance = claimsByStance,
                claimsByStrength = claimsByStrength,
                topicsByPosture = topicsByPosture,
                mostDebatedTopics = topicStats.take(5),
                averageClaimsPerTopic = avgClaimsPerTopic,
                averageRebuttalsPerClaim = avgRebuttalsPerClaim,
                averageStrength = avgStrength
            )
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Get statistics for a specific topic
     */
    suspend fun getTopicStatistics(topicId: String): Result<TopicStats> = withContext(Dispatchers.IO) {
        try {
            val topic = topicDao.getTopicById(topicId) ?: return@withContext Result.failure(
                Exception("Topic not found")
            )

            val claims = claimDao.getClaimsForTopic(topicId)
            val rebuttals = claims.flatMap { claim ->
                rebuttalDao.getRebuttalsForClaim(claim.id)
            }
            val evidence = claims.flatMap { claim ->
                evidenceDao.getEvidenceForClaim(claim.id)
            } + rebuttals.flatMap { rebuttal ->
                evidenceDao.getEvidenceForRebuttal(rebuttal.id)
            }
            val questions = questionDao.getQuestionsForTopic(topicId)

            val stats = TopicStats(
                topicId = topic.id,
                topicTitle = topic.title,
                claimCount = claims.size,
                rebuttalCount = rebuttals.size,
                evidenceCount = evidence.size,
                questionCount = questions.size
            )

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get total count of all entities
     */
    suspend fun getTotalEntityCount(): Int = withContext(Dispatchers.IO) {
        val topics = topicDao.getAllTopicsSync().size
        val claims = claimDao.getAllClaimsSync().size
        val rebuttals = rebuttalDao.getAllRebuttalsSync().size
        val evidence = evidenceDao.getAllEvidenceSync().size
        val questions = questionDao.getAllQuestionsSync().size
        val sources = sourceDao.getAllSourcesSync().size

        topics + claims + rebuttals + evidence + questions + sources
    }

    /**
     * Convert strength to numeric value for average calculation
     */
    private fun strengthToNumeric(strength: Strength): Double {
        return when (strength) {
            Strength.LOW -> 1.0
            Strength.MEDIUM -> 2.0
            Strength.HIGH -> 3.0
        }
    }
}
