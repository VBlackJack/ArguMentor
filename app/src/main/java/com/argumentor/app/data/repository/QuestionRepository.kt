package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.QuestionDao
import com.argumentor.app.data.model.Question
import com.argumentor.app.util.SearchUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepository @Inject constructor(
    private val questionDao: QuestionDao
) {
    fun getQuestionsByTargetId(targetId: String): Flow<List<Question>> =
        questionDao.getQuestionsByTargetId(targetId)

    suspend fun getQuestionById(questionId: String): Question? =
        questionDao.getQuestionById(questionId)

    suspend fun getQuestionsForTopic(topicId: String): List<Question> =
        questionDao.getQuestionsForTopic(topicId)

    suspend fun insertQuestion(question: Question) =
        questionDao.insertQuestion(question)

    suspend fun updateQuestion(question: Question) =
        questionDao.updateQuestion(question)

    suspend fun deleteQuestion(question: Question) =
        questionDao.deleteQuestion(question)

    /**
     * Search questions using FTS with automatic fallback to LIKE search if FTS fails.
     */
    fun searchQuestions(query: String): Flow<List<Question>> {
        return searchWithFtsFallback(
            query = query,
            ftsSearch = { questionDao.searchQuestionsFts(it) },
            likeSearch = { questionDao.searchQuestionsLike(it) }
        )
    }

    /**
     * Deletes orphan questions where targetId doesn't reference any existing Topic or Claim.
     * This helps maintain referential integrity in the database.
     *
     * @return The number of orphan questions deleted
     */
    suspend fun deleteOrphanQuestions(): Int = questionDao.deleteOrphanQuestions()
}
