package com.argumentor.app.data.local.dao

import androidx.room.*
import com.argumentor.app.data.model.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY label ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags ORDER BY label ASC")
    suspend fun getAllTagsSync(): List<Tag>

    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: String): Tag?

    @Query("SELECT * FROM tags WHERE id = :tagId")
    fun observeTagById(tagId: String): Flow<Tag?>

    @Query("SELECT * FROM tags WHERE label = :label")
    suspend fun getTagByLabel(label: String): Tag?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<Tag>)

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTagById(tagId: String)

    /**
     * Full-text search on tags using FTS4 index.
     * Searches in label field with relevance ranking.
     * @param query Search query (supports FTS4 operators like OR, AND, *)
     * @return Flow of matching tags ordered by label
     */
    @Query("""
        SELECT tags.* FROM tags
        JOIN tags_fts ON tags.rowid = tags_fts.rowid
        WHERE tags_fts MATCH :query
        ORDER BY tags.label ASC
    """)
    fun searchTagsFts(query: String): Flow<List<Tag>>

    /**
     * Fallback search using LIKE (for when FTS query contains invalid operators).
     * Searches in label field.
     * SECURITY FIX (SEC-004): Added ESCAPE '\' clause to prevent wildcard injection
     * @param query Search query string
     * @return Flow of matching tags ordered by label
     */
    @Query("""
        SELECT * FROM tags
        WHERE label LIKE '%' || :query || '%' ESCAPE '\'
        ORDER BY label ASC
    """)
    fun searchTagsLike(query: String): Flow<List<Tag>>

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getTagCount(): Int
}
