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
        val sanitizedQuery = SearchUtils.sanitizeLikeQuery(query)

        return if (SearchUtils.isSafeFtsQuery(query)) {
            // Try FTS first
            questionDao.searchQuestionsFts(query).catch { error ->
                // If FTS fails (e.g., invalid query syntax), fall back to LIKE
                emitAll(questionDao.searchQuestionsLike(sanitizedQuery))
            }
        } else {
            // Query looks unsafe for FTS, use LIKE directly
            questionDao.searchQuestionsLike(sanitizedQuery)
        }
    }
}
