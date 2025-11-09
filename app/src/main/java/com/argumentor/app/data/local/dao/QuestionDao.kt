package com.argumentor.app.data.local.dao

import androidx.room.*
import com.argumentor.app.data.model.Question
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY createdAt DESC")
    fun getAllQuestions(): Flow<List<Question>>

    @Query("SELECT * FROM questions ORDER BY createdAt DESC")
    suspend fun getAllQuestionsSync(): List<Question>

    @Query("SELECT * FROM questions WHERE targetId = :targetId ORDER BY createdAt DESC")
    fun getQuestionsByTargetId(targetId: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE targetId = :topicId ORDER BY createdAt DESC")
    suspend fun getQuestionsForTopic(topicId: String): List<Question>

    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: String): Question?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Update
    suspend fun updateQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)

    @Query("DELETE FROM questions WHERE id = :questionId")
    suspend fun deleteQuestionById(questionId: String)

    // Full-text search
    @Query("""
        SELECT questions.* FROM questions
        JOIN questions_fts ON questions.rowid = questions_fts.rowid
        WHERE questions_fts MATCH :query
        ORDER BY createdAt DESC
    """)
    fun searchQuestionsFts(query: String): Flow<List<Question>>

    // Fallback search using LIKE (for when FTS query contains invalid operators)
    @Query("""
        SELECT * FROM questions
        WHERE text LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchQuestionsLike(query: String): Flow<List<Question>>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int
}
