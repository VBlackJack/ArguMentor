package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.QuestionDao
import com.argumentor.app.data.model.Question
import kotlinx.coroutines.flow.Flow
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

    fun searchQuestions(query: String): Flow<List<Question>> =
        questionDao.searchQuestionsFts(query)
}
