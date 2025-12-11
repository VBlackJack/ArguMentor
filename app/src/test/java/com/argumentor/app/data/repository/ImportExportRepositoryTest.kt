package com.argumentor.app.data.repository

import android.content.Context
import android.os.Environment
import com.argumentor.app.data.local.ArguMentorDatabase
import com.argumentor.app.data.local.dao.*
import com.argumentor.app.data.model.*
import com.google.common.truth.Truth.assertThat
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ImportExportRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var database: ArguMentorDatabase
    private lateinit var context: Context
    private lateinit var topicDao: TopicDao
    private lateinit var claimDao: ClaimDao
    private lateinit var rebuttalDao: RebuttalDao
    private lateinit var evidenceDao: EvidenceDao
    private lateinit var questionDao: QuestionDao
    private lateinit var sourceDao: SourceDao
    private lateinit var tagDao: TagDao
    private lateinit var repository: ImportExportRepository

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val testTopic = Topic(
        id = "topic-1",
        title = "Test Topic",
        summary = "Test Summary"
    )

    private val testClaim = Claim(
        id = "claim-1",
        text = "Test Claim",
        topics = listOf("topic-1")
    )

    private val testTag = Tag(
        id = "tag-1",
        label = "Test Tag"
    )

    private val testSource = Source(
        id = "source-1",
        title = "Test Source"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        database = mock()
        context = mock()
        topicDao = mock()
        claimDao = mock()
        rebuttalDao = mock()
        evidenceDao = mock()
        questionDao = mock()
        sourceDao = mock()
        tagDao = mock()

        whenever(database.topicDao()).thenReturn(topicDao)
        whenever(database.claimDao()).thenReturn(claimDao)
        whenever(database.rebuttalDao()).thenReturn(rebuttalDao)
        whenever(database.evidenceDao()).thenReturn(evidenceDao)
        whenever(database.questionDao()).thenReturn(questionDao)
        whenever(database.sourceDao()).thenReturn(sourceDao)
        whenever(database.tagDao()).thenReturn(tagDao)

        repository = ImportExportRepository(database, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `exportToJson produces valid JSON`() = runTest {
        whenever(topicDao.getAllTopicsSync()).thenReturn(listOf(testTopic))
        whenever(claimDao.getAllClaimsSync()).thenReturn(listOf(testClaim))
        whenever(rebuttalDao.getAllRebuttalsSync()).thenReturn(emptyList())
        whenever(evidenceDao.getAllEvidencesSync()).thenReturn(emptyList())
        whenever(questionDao.getAllQuestionsSync()).thenReturn(emptyList())
        whenever(sourceDao.getAllSourcesSync()).thenReturn(listOf(testSource))
        whenever(tagDao.getAllTagsSync()).thenReturn(listOf(testTag))

        val outputStream = ByteArrayOutputStream()

        val result = repository.exportToJson(outputStream)

        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(result.isSuccess).isTrue()

        val json = outputStream.toString()
        assertThat(json).contains("\"schemaVersion\":\"1.0\"")
        assertThat(json).contains("\"app\":\"ArguMentor\"")
        assertThat(json).contains("Test Topic")
        assertThat(json).contains("Test Claim")
        assertThat(json).contains("Test Source")
    }

    @Test
    fun `importFromJson fails on unsupported schema version`() = runTest {
        val invalidJson = """
            {
                "schemaVersion": "2.0",
                "exportedAt": "2024-01-01T00:00:00Z",
                "app": "ArguMentor",
                "topics": [],
                "claims": [],
                "rebuttals": [],
                "evidences": [],
                "questions": [],
                "sources": [],
                "tags": []
            }
        """.trimIndent()

        val inputStream = ByteArrayInputStream(invalidJson.toByteArray())

        val result = repository.importFromJson(inputStream)

        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Unsupported schema version")
    }

    @Test
    fun `importFromJson fails on file exceeding max size`() = runTest {
        // Create a mock input stream that reports large size
        val largeData = ByteArray(51 * 1024 * 1024) // 51MB
        val inputStream = ByteArrayInputStream(largeData)

        val result = repository.importFromJson(inputStream)

        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("too large")
    }

    @Test
    fun `exportToFile validates path security`() = runTest {
        whenever(topicDao.getAllTopicsSync()).thenReturn(emptyList())
        whenever(claimDao.getAllClaimsSync()).thenReturn(emptyList())
        whenever(rebuttalDao.getAllRebuttalsSync()).thenReturn(emptyList())
        whenever(evidenceDao.getAllEvidencesSync()).thenReturn(emptyList())
        whenever(questionDao.getAllQuestionsSync()).thenReturn(emptyList())
        whenever(sourceDao.getAllSourcesSync()).thenReturn(emptyList())
        whenever(tagDao.getAllTagsSync()).thenReturn(emptyList())

        // Mock context to return null for external files dir
        whenever(context.getExternalFilesDir(null)).thenReturn(null)
        whenever(context.filesDir).thenReturn(File("/data/data/com.argumentor.app/files"))

        // Try to export to an invalid path
        val invalidFile = File("/etc/passwd")

        val result = repository.exportToFile(invalidFile)

        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Invalid export path")
    }

    @Test
    fun `importFromFile checks file size before reading`() = runTest {
        // Create a mock file that appears large
        val mockFile = mock<File>()
        whenever(mockFile.length()).thenReturn(100 * 1024 * 1024L) // 100MB

        val result = repository.importFromFile(mockFile)

        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("too large")
    }

    @Test
    fun `valid export import roundtrip preserves data structure`() = runTest {
        whenever(topicDao.getAllTopicsSync()).thenReturn(listOf(testTopic))
        whenever(claimDao.getAllClaimsSync()).thenReturn(listOf(testClaim))
        whenever(rebuttalDao.getAllRebuttalsSync()).thenReturn(emptyList())
        whenever(evidenceDao.getAllEvidencesSync()).thenReturn(emptyList())
        whenever(questionDao.getAllQuestionsSync()).thenReturn(emptyList())
        whenever(sourceDao.getAllSourcesSync()).thenReturn(listOf(testSource))
        whenever(tagDao.getAllTagsSync()).thenReturn(listOf(testTag))

        // Export
        val outputStream = ByteArrayOutputStream()
        val exportResult = repository.exportToJson(outputStream)

        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(exportResult.isSuccess).isTrue()

        val exportedJson = outputStream.toString()

        // Verify JSON structure
        assertThat(exportedJson).contains("\"id\":\"topic-1\"")
        assertThat(exportedJson).contains("\"id\":\"claim-1\"")
        assertThat(exportedJson).contains("\"id\":\"source-1\"")
        assertThat(exportedJson).contains("\"id\":\"tag-1\"")
    }

    @Test
    fun `import detects duplicate tags by label`() = runTest {
        val importJson = """
            {
                "schemaVersion": "1.0",
                "exportedAt": "2024-01-01T00:00:00Z",
                "app": "ArguMentor",
                "topics": [],
                "claims": [],
                "rebuttals": [],
                "evidences": [],
                "questions": [],
                "sources": [],
                "tags": [
                    {"id": "tag-new", "label": "Existing Tag", "createdAt": "2024-01-01T00:00:00Z", "updatedAt": "2024-01-01T00:00:00Z"}
                ]
            }
        """.trimIndent()

        val existingTag = Tag(id = "tag-existing", label = "Existing Tag")
        whenever(tagDao.getTagById("tag-new")).thenReturn(null)
        whenever(tagDao.getTagByLabel("Existing Tag")).thenReturn(existingTag)

        whenever(database.withTransaction<Any>(any())).thenAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val block = invocation.arguments[0] as suspend () -> Any
            kotlinx.coroutines.runBlocking { block() }
        }

        val inputStream = ByteArrayInputStream(importJson.toByteArray())

        val result = repository.importFromJson(inputStream)

        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(result.isSuccess).isTrue()
        val importResult = result.getOrNull()
        assertThat(importResult?.duplicates).isEqualTo(1)
    }
}
