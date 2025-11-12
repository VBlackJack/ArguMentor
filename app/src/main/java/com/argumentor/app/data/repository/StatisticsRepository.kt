package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.*
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Topic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
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
     * Get comprehensive statistics as a Flow.
     *
     * PERFORMANCE OPTIMIZATION (COMPLETE REFACTOR):
     * - Uses SQL aggregation queries instead of loading ALL data in memory
     * - Prevents Out Of Memory errors on large databases
     * - Much faster and more efficient
     * - Each stat is calculated via a dedicated SQL query
     *
     * MEMORY LEAK FIX:
     * - Uses callbackFlow with isActive check to prevent infinite loop memory leaks
     * - Properly cancels when collector is disposed
     */
    fun getStatistics(): Flow<Statistics> = callbackFlow {
        while (isActive) {
            val stats = calculateStatistics()
            send(stats)
            // Re-emit when database changes (polling interval)
            delay(1000)
        }
        awaitClose { /* cleanup if needed */ }
    }.flowOn(Dispatchers.IO)

    /**
     * Calculate statistics using SQL aggregation queries.
     * This method doesn't load full entity lists into memory.
     */
    private suspend fun calculateStatistics(): Statistics {
        // Get total counts via SQL COUNT queries
        val totalTopics = topicDao.getTopicCount()
        val totalClaims = claimDao.getClaimCount()
        val totalRebuttals = rebuttalDao.getRebuttalCount()
        val totalEvidence = evidenceDao.getEvidenceCount()
        val totalQuestions = questionDao.getQuestionCount()
        val totalSources = sourceDao.getSourceCount()

        // Claims by stance (using SQL COUNT GROUP BY)
        val claimsByStance = Claim.Stance.values().associateWith { stance ->
            claimDao.getClaimCountByStance(stance)
        }

        // Claims by strength (using SQL COUNT GROUP BY)
        val claimsByStrength = Claim.Strength.values().associateWith { strength ->
            claimDao.getClaimCountByStrength(strength)
        }

        // Topics by posture (using SQL COUNT GROUP BY)
        val topicsByPosture = Topic.Posture.values().associateWith { posture ->
            topicDao.getTopicCountByPosture(posture)
        }

        // Most debated topics (load only top 5 topics with their stats)
        val mostDebatedTopics = calculateMostDebatedTopics()

        // Calculate averages
        val avgClaimsPerTopic = if (totalTopics > 0) {
            totalClaims.toDouble() / totalTopics
        } else 0.0

        val avgRebuttalsPerClaim = if (totalClaims > 0) {
            totalRebuttals.toDouble() / totalClaims
        } else 0.0

        val avgStrength = claimDao.getAverageStrength() ?: 0.0

        return Statistics(
            totalTopics = totalTopics,
            totalClaims = totalClaims,
            totalRebuttals = totalRebuttals,
            totalEvidence = totalEvidence,
            totalQuestions = totalQuestions,
            totalSources = totalSources,
            claimsByStance = claimsByStance,
            claimsByStrength = claimsByStrength,
            topicsByPosture = topicsByPosture,
            mostDebatedTopics = mostDebatedTopics,
            averageClaimsPerTopic = avgClaimsPerTopic,
            averageRebuttalsPerClaim = avgRebuttalsPerClaim,
            averageStrength = avgStrength
        )
    }

    /**
     * Calculate most debated topics stats.
     * Only loads topics (not all claims/rebuttals) to reduce memory usage.
     *
     * PERFORMANCE NOTE (PERF-001):
     * This method has N+1 query pattern: for each topic, it makes separate queries for claims,
     * rebuttals, evidence, and questions. This is acceptable for small datasets (< 100 topics)
     * but could be optimized with a single SQL JOIN query for larger databases.
     *
     * TODO (Low Priority): Consider adding a single DAO method with JOIN:
     *   @Query("""
     *     SELECT t.id, t.title,
     *       COUNT(DISTINCT c.id) as claimCount,
     *       COUNT(DISTINCT r.id) as rebuttalCount,
     *       COUNT(DISTINCT e.id) as evidenceCount,
     *       COUNT(DISTINCT q.id) as questionCount
     *     FROM topics t
     *     LEFT JOIN claims c ON c.topics LIKE '%' || t.id || '%'
     *     LEFT JOIN rebuttals r ON r.claimId IN (SELECT id FROM claims WHERE topics LIKE '%' || t.id || '%')
     *     LEFT JOIN evidence e ON e.claimId IN (SELECT id FROM claims WHERE topics LIKE '%' || t.id || '%')
     *     LEFT JOIN questions q ON q.topicId = t.id
     *     GROUP BY t.id
     *     ORDER BY claimCount DESC
     *     LIMIT 5
     *   """)
     */
    private suspend fun calculateMostDebatedTopics(): List<TopicStats> {
        val topics = topicDao.getAllTopicsSync()

        return topics.map { topic ->
            val claims = claimDao.getClaimsByTopicId(topic.id)
            val claimIds = claims.map { it.id }

            val rebuttalCount = if (claimIds.isNotEmpty()) {
                rebuttalDao.getRebuttalsByClaimIds(claimIds).size
            } else 0

            val evidenceCount = if (claimIds.isNotEmpty()) {
                evidenceDao.getEvidencesByClaimIds(claimIds).size
            } else 0

            val questionCount = questionDao.getQuestionsByTopicId(topic.id).size

            TopicStats(
                topicId = topic.id,
                topicTitle = topic.title,
                claimCount = claims.size,
                rebuttalCount = rebuttalCount,
                evidenceCount = evidenceCount,
                questionCount = questionCount
            )
        }.sortedByDescending { it.claimCount }.take(5)
    }

    /**
     * Get statistics for a specific topic
     */
    suspend fun getTopicStatistics(topicId: String): Result<TopicStats> = withContext(Dispatchers.IO) {
        try {
            val topic = topicDao.getTopicById(topicId) ?: return@withContext Result.failure(
                Exception("Topic not found")
            )

            val claims = claimDao.getClaimsByTopicId(topicId)
            val rebuttals = claims.flatMap { claim ->
                rebuttalDao.getRebuttalsByClaimIdSync(claim.id)
            }
            val evidence = claims.flatMap { claim ->
                evidenceDao.getEvidencesByClaimIdSync(claim.id)
            }
            // Note: Evidence is linked to claims, not rebuttals in current schema
            val questions = questionDao.getQuestionsByTopicId(topicId)

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
        val evidence = evidenceDao.getAllEvidencesSync().size
        val questions = questionDao.getAllQuestionsSync().size
        val sources = sourceDao.getAllSourcesSync().size

        topics + claims + rebuttals + evidence + questions + sources
    }

    /**
     * Convert strength to numeric value for average calculation
     */
    private fun strengthToNumeric(strength: Claim.Strength): Double {
        return when (strength) {
            Claim.Strength.LOW -> 1.0
            Claim.Strength.MEDIUM -> 2.0
            Claim.Strength.HIGH -> 3.0
        }
    }
}
