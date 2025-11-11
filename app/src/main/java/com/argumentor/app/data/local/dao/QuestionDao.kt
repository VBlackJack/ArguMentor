package com.argumentor.app.data.local.dao

import androidx.room.*
import com.argumentor.app.data.model.Question
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY updatedAt DESC")
    fun getAllQuestions(): Flow<List<Question>>

    @Query("SELECT * FROM questions ORDER BY updatedAt DESC")
    suspend fun getAllQuestionsSync(): List<Question>

    @Query("SELECT * FROM questions WHERE targetId = :targetId ORDER BY updatedAt DESC")
    fun getQuestionsByTargetId(targetId: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE targetId = :topicId ORDER BY updatedAt DESC")
    suspend fun getQuestionsByTopicId(topicId: String): List<Question>

    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: String): Question?

    @Query("SELECT * FROM questions WHERE id = :questionId")
    fun observeQuestionById(questionId: String): Flow<Question?>

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

    @Query("DELETE FROM questions WHERE targetId = :targetId")
    suspend fun deleteQuestionsByTargetId(targetId: String)

    // Full-text search
    @Query("""
        SELECT questions.* FROM questions
        JOIN questions_fts ON questions.rowid = questions_fts.rowid
        WHERE questions_fts MATCH :query
        ORDER BY updatedAt DESC
    """)
    fun searchQuestionsFts(query: String): Flow<List<Question>>

    // Fallback search using LIKE (for when FTS query contains invalid operators)
    // SECURITY FIX (SEC-004): Added ESCAPE '\' clause to prevent wildcard injection
    @Query("""
        SELECT * FROM questions
        WHERE text LIKE '%' || :query || '%' ESCAPE '\'
        ORDER BY updatedAt DESC
    """)
    fun searchQuestionsLike(query: String): Flow<List<Question>>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int

    /**
     * Deletes orphan questions where targetId doesn't reference any existing Topic or Claim.
     * Returns the number of orphan questions deleted.
     */
    @Query("""
        DELETE FROM questions
        WHERE targetId NOT IN (SELECT id FROM topics)
        AND targetId NOT IN (SELECT id FROM claims)
    """)
    suspend fun deleteOrphanQuestions(): Int
}
