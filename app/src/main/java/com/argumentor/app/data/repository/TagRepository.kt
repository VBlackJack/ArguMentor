package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.TagDao
import com.argumentor.app.data.model.Tag
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {
    fun getAllTags(): Flow<List<Tag>> = tagDao.getAllTags()

    suspend fun getTagById(tagId: String): Tag? = tagDao.getTagById(tagId)

    suspend fun getTagByLabel(label: String): Tag? = tagDao.getTagByLabel(label)

    suspend fun insertTag(tag: Tag) = tagDao.insertTag(tag)

    suspend fun updateTag(tag: Tag) = tagDao.updateTag(tag)

    suspend fun deleteTag(tag: Tag) = tagDao.deleteTag(tag)

    suspend fun getOrCreateTag(label: String, color: String? = null): Tag {
        return getTagByLabel(label) ?: Tag(label = label, color = color).also {
            insertTag(it)
        }
    }
}
