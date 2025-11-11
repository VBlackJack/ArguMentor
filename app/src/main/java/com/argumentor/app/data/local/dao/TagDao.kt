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

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getTagCount(): Int
}
