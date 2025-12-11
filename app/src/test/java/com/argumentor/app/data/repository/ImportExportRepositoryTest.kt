package com.argumentor.app.data.repository

import android.content.Context
import com.argumentor.app.data.dto.*
import com.argumentor.app.data.local.ArguMentorDatabase
import com.argumentor.app.data.local.dao.*
import com.argumentor.app.data.model.*
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
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
import org.mockito.kotlin.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Unit tests for ImportExportRepository.
 *
 * Note: Import functionality uses database.withTransaction() which requires
 * instrumented tests with a real database. Export functionality can be
 * tested with mocks. Full integration tests for import are in androidTest.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ImportExportRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var database: ArguMentorDatabase
    private lateinit var context: Context
    private lateinit var repository: ImportExportRepository

    private lateinit var topicDao: TopicDao
    private lateinit var claimDao: ClaimDao
    private lateinit var rebuttalDao: RebuttalDao
    private lateinit var evidenceDao: EvidenceDao
    private lateinit var questionDao: QuestionDao
    private lateinit var sourceDao: SourceDao
    private lateinit var tagDao: TagDao

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private val testTimestamp = "2024-01-15T10:30:00Z"

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

    // =====================
    // EXPORT TESTS
    // =====================

    @Test
    fun `exportToJson exports all entities`() = runTest {
        // Setup DAOs to return test data
        val topic = Topic(
            id = "topic-1",
            title = "Test Topic",
            summary = "Test Summary",
            createdAt = testTimestamp,
            updatedAt = testTimestamp
        )
        val claim = Claim(
            id = "claim-1",
            text = "Test Claim",
            topics = listOf("topic-1"),
            stance = Claim.Stance.PRO,
            strength = Claim.Strength.MEDIUM,
            createdAt = testTimestamp,
            updatedAt = testTimestamp
        )
        val tag = Tag(
            id = "tag-1",
            label = "Test Tag",
            createdAt = testTimestamp,
            updatedAt = testTimestamp
        )

        whenever(topicDao.getAllTopicsSync()).thenReturn(listOf(topic))
        whenever(claimDao.getAllClaimsSync()).thenReturn(listOf(claim))
        whenever(rebuttalDao.getAllRebuttalsSync()).thenReturn(emptyList())
        whenever(evidenceDao.getAllEvidencesSync()).thenReturn(emptyList())
        whenever(questionDao.getAllQuestionsSync()).thenReturn(emptyList())
        whenever(sourceDao.getAllSourcesSync()).thenReturn(emptyList())
        whenever(tagDao.getAllTagsSync()).thenReturn(listOf(tag))

        val outputStream = ByteArrayOutputStream()
        val result = repository.exportToJson(outputStream)

        assertThat(result.isSuccess).isTrue()

        val exportedJson = outputStream.toString()
        assertThat(exportedJson).contains("schemaVersion")
        assertThat(exportedJson).contains("1.0")
        assertThat(exportedJson).contains("Test Topic")
        assertThat(exportedJson).contains("Test Claim")
        assertThat(exportedJson).contains("Test Tag")
    }

    @Test
    fun `exportToJson includes schema version and app name`() = runTest {
        whenever(topicDao.getAllTopicsSync()).thenReturn(emptyList())
        whenever(claimDao.getAllClaimsSync()).thenReturn(emptyList())
        whenever(rebuttalDao.getAllRebuttalsSync()).thenReturn(emptyList())
        whenever(evidenceDao.getAllEvidencesSync()).thenReturn(emptyList())
        whenever(questionDao.getAllQuestionsSync()).thenReturn(emptyList())
        whenever(sourceDao.getAllSourcesSync()).thenReturn(emptyList())
        whenever(tagDao.getAllTagsSync()).thenReturn(emptyList())

        val outputStream = ByteArrayOutputStream()
        val result = repository.exportToJson(outputStream)

        assertThat(result.isSuccess).isTrue()

        val exportedJson = outputStream.toString()
        val exportData = gson.fromJson(exportedJson, ExportData::class.java)

        assertThat(exportData.schemaVersion).isEqualTo("1.0")
        assertThat(exportData.app).isEqualTo("ArguMentor")
    }

    @Test
    fun `exportToJson exports topics with all fields`() = runTest {
        val topic = Topic(
            id = "topic-123",
            title = "Complex Topic",
            summary = "Detailed summary here",
            posture = Topic.Posture.NEUTRAL_CRITICAL,
            tags = listOf("tag-1", "tag-2"),
            createdAt = testTimestamp,
            updatedAt = testTimestamp
        )

        whenever(topicDao.getAllTopicsSync()).thenReturn(listOf(topic))
        whenever(claimDao.getAllClaimsSync()).thenReturn(emptyList())
        whenever(rebuttalDao.getAllRebuttalsSync()).thenReturn(emptyList())
        whenever(evidenceDao.getAllEvidencesSync()).thenReturn(emptyList())
        whenever(questionDao.getAllQuestionsSync()).thenReturn(emptyList())
        whenever(sourceDao.getAllSourcesSync()).thenReturn(emptyList())
        whenever(tagDao.getAllTagsSync()).thenReturn(emptyList())

        val outputStream = ByteArrayOutputStream()
        val result = repository.exportToJson(outputStream)

        assertThat(result.isSuccess).isTrue()

        val exportedJson = outputStream.toString()
        val exportData = gson.fromJson(exportedJson, ExportData::class.java)

        assertThat(exportData.topics).hasSize(1)
        assertThat(exportData.topics[0].id).isEqualTo("topic-123")
        assertThat(exportData.topics[0].title).isEqualTo("Complex Topic")
        assertThat(exportData.topics[0].summary).isEqualTo("Detailed summary here")
        assertThat(exportData.topics[0].posture).isEqualTo("neutral_critical")
        assertThat(exportData.topics[0].tags).containsExactly("tag-1", "tag-2")
    }

    @Test
    fun `exportToJson exports claims with all fields`() = runTest {
        val claim = Claim(
            id = "claim-456",
            text = "Test Claim Text",
            topics = listOf("topic-1"),
            stance = Claim.Stance.PRO,
            strength = Claim.Strength.HIGH,
            fallacyIds = listOf("ad_hominem"),
            claimFingerprint = "fingerprint123",
            createdAt = testTimestamp,
            updatedAt = testTimestamp
        )

        whenever(topicDao.getAllTopicsSync()).thenReturn(emptyList())
        whenever(claimDao.getAllClaimsSync()).thenReturn(listOf(claim))
        whenever(rebuttalDao.getAllRebuttalsSync()).thenReturn(emptyList())
        whenever(evidenceDao.getAllEvidencesSync()).thenReturn(emptyList())
        whenever(questionDao.getAllQuestionsSync()).thenReturn(emptyList())
        whenever(sourceDao.getAllSourcesSync()).thenReturn(emptyList())
        whenever(tagDao.getAllTagsSync()).thenReturn(emptyList())

        val outputStream = ByteArrayOutputStream()
        val result = repository.exportToJson(outputStream)

        assertThat(result.isSuccess).isTrue()

        val exportedJson = outputStream.toString()
        val exportData = gson.fromJson(exportedJson, ExportData::class.java)

        assertThat(exportData.claims).hasSize(1)
        assertThat(exportData.claims[0].id).isEqualTo("claim-456")
        assertThat(exportData.claims[0].text).isEqualTo("Test Claim Text")
        assertThat(exportData.claims[0].stance).isEqualTo("pro")
        assertThat(exportData.claims[0].strength).isEqualTo("high")
        assertThat(exportData.claims[0].fallacyIds).containsExactly("ad_hominem")
    }

    @Test
    fun `exportToJson exports sources with optional fields`() = runTest {
        val source = Source(
            id = "source-789",
            title = "Research Paper",
            citation = "Author et al. (2024)",
            url = "https://example.com/paper",
            publisher = "Journal of Science",
            date = "2024-01-01",
            reliabilityScore = 0.95,
            notes = "Excellent methodology",
            createdAt = testTimestamp,
            updatedAt = testTimestamp
        )

        whenever(topicDao.getAllTopicsSync()).thenReturn(emptyList())
        whenever(claimDao.getAllClaimsSync()).thenReturn(emptyList())
        whenever(rebuttalDao.getAllRebuttalsSync()).thenReturn(emptyList())
        whenever(evidenceDao.getAllEvidencesSync()).thenReturn(emptyList())
        whenever(questionDao.getAllQuestionsSync()).thenReturn(emptyList())
        whenever(sourceDao.getAllSourcesSync()).thenReturn(listOf(source))
        whenever(tagDao.getAllTagsSync()).thenReturn(emptyList())

        val outputStream = ByteArrayOutputStream()
        val result = repository.exportToJson(outputStream)

        assertThat(result.isSuccess).isTrue()

        val exportedJson = outputStream.toString()
        val exportData = gson.fromJson(exportedJson, ExportData::class.java)

        assertThat(exportData.sources).hasSize(1)
        assertThat(exportData.sources[0].title).isEqualTo("Research Paper")
        assertThat(exportData.sources[0].citation).isEqualTo("Author et al. (2024)")
        assertThat(exportData.sources[0].url).isEqualTo("https://example.com/paper")
        assertThat(exportData.sources[0].reliabilityScore).isEqualTo(0.95)
    }

    @Test
    fun `exportToJson exports rebuttals with claim reference`() = runTest {
        val rebuttal = Rebuttal(
            id = "rebuttal-1",
            claimId = "claim-1",
            text = "Counter argument",
            fallacyIds = listOf("straw_man"),
            createdAt = testTimestamp,
            updatedAt = testTimestamp
        )

        whenever(topicDao.getAllTopicsSync()).thenReturn(emptyList())
        whenever(claimDao.getAllClaimsSync()).thenReturn(emptyList())
        whenever(rebuttalDao.getAllRebuttalsSync()).thenReturn(listOf(rebuttal))
        whenever(evidenceDao.getAllEvidencesSync()).thenReturn(emptyList())
        whenever(questionDao.getAllQuestionsSync()).thenReturn(emptyList())
        whenever(sourceDao.getAllSourcesSync()).thenReturn(emptyList())
        whenever(tagDao.getAllTagsSync()).thenReturn(emptyList())

        val outputStream = ByteArrayOutputStream()
        val result = repository.exportToJson(outputStream)

        assertThat(result.isSuccess).isTrue()

        val exportedJson = outputStream.toString()
        val exportData = gson.fromJson(exportedJson, ExportData::class.java)

        assertThat(exportData.rebuttals).hasSize(1)
        assertThat(exportData.rebuttals[0].claimId).isEqualTo("claim-1")
        assertThat(exportData.rebuttals[0].text).isEqualTo("Counter argument")
    }

    @Test
    fun `exportToJson exports evidences with source reference`() = runTest {
        val evidence = Evidence(
            id = "evidence-1",
            claimId = "claim-1",
            type = Evidence.EvidenceType.QUOTE,
            content = "Quoted text",
            sourceId = "source-1",
            quality = Evidence.Quality.HIGH,
            createdAt = testTimestamp,
            updatedAt = testTimestamp
        )

        whenever(topicDao.getAllTopicsSync()).thenReturn(emptyList())
        whenever(claimDao.getAllClaimsSync()).thenReturn(emptyList())
        whenever(rebuttalDao.getAllRebuttalsSync()).thenReturn(emptyList())
        whenever(evidenceDao.getAllEvidencesSync()).thenReturn(listOf(evidence))
        whenever(questionDao.getAllQuestionsSync()).thenReturn(emptyList())
        whenever(sourceDao.getAllSourcesSync()).thenReturn(emptyList())
        whenever(tagDao.getAllTagsSync()).thenReturn(emptyList())

        val outputStream = ByteArrayOutputStream()
        val result = repository.exportToJson(outputStream)

        assertThat(result.isSuccess).isTrue()

        val exportedJson = outputStream.toString()
        val exportData = gson.fromJson(exportedJson, ExportData::class.java)

        assertThat(exportData.evidences).hasSize(1)
        assertThat(exportData.evidences[0].claimId).isEqualTo("claim-1")
        assertThat(exportData.evidences[0].sourceId).isEqualTo("source-1")
        assertThat(exportData.evidences[0].type).isEqualTo("quote")
    }

    @Test
    fun `exportToJson exports questions`() = runTest {
        val question = Question(
            id = "question-1",
            targetId = "topic-1",
            text = "What about X?",
            kind = Question.QuestionKind.CLARIFYING,
            createdAt = testTimestamp,
            updatedAt = testTimestamp
        )

        whenever(topicDao.getAllTopicsSync()).thenReturn(emptyList())
        whenever(claimDao.getAllClaimsSync()).thenReturn(emptyList())
        whenever(rebuttalDao.getAllRebuttalsSync()).thenReturn(emptyList())
        whenever(evidenceDao.getAllEvidencesSync()).thenReturn(emptyList())
        whenever(questionDao.getAllQuestionsSync()).thenReturn(listOf(question))
        whenever(sourceDao.getAllSourcesSync()).thenReturn(emptyList())
        whenever(tagDao.getAllTagsSync()).thenReturn(emptyList())

        val outputStream = ByteArrayOutputStream()
        val result = repository.exportToJson(outputStream)

        assertThat(result.isSuccess).isTrue()

        val exportedJson = outputStream.toString()
        val exportData = gson.fromJson(exportedJson, ExportData::class.java)

        assertThat(exportData.questions).hasSize(1)
        assertThat(exportData.questions[0].targetId).isEqualTo("topic-1")
        assertThat(exportData.questions[0].kind).isEqualTo("clarifying")
    }

    @Test
    fun `exportToJson handles empty database`() = runTest {
        whenever(topicDao.getAllTopicsSync()).thenReturn(emptyList())
        whenever(claimDao.getAllClaimsSync()).thenReturn(emptyList())
        whenever(rebuttalDao.getAllRebuttalsSync()).thenReturn(emptyList())
        whenever(evidenceDao.getAllEvidencesSync()).thenReturn(emptyList())
        whenever(questionDao.getAllQuestionsSync()).thenReturn(emptyList())
        whenever(sourceDao.getAllSourcesSync()).thenReturn(emptyList())
        whenever(tagDao.getAllTagsSync()).thenReturn(emptyList())

        val outputStream = ByteArrayOutputStream()
        val result = repository.exportToJson(outputStream)

        assertThat(result.isSuccess).isTrue()

        val exportedJson = outputStream.toString()
        val exportData = gson.fromJson(exportedJson, ExportData::class.java)

        assertThat(exportData.topics).isEmpty()
        assertThat(exportData.claims).isEmpty()
        assertThat(exportData.rebuttals).isEmpty()
        assertThat(exportData.evidences).isEmpty()
        assertThat(exportData.questions).isEmpty()
        assertThat(exportData.sources).isEmpty()
        assertThat(exportData.tags).isEmpty()
    }

    // =====================
    // IMPORT TESTS - INPUT VALIDATION (No transaction needed)
    // =====================

    @Test
    fun `importFromJson rejects unsupported schema version`() = runTest {
        val invalidExport = ExportData(
            schemaVersion = "2.0", // Unsupported
            exportedAt = testTimestamp
        )
        val json = gson.toJson(invalidExport)
        val inputStream = ByteArrayInputStream(json.toByteArray())

        val result = repository.importFromJson(inputStream)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Unsupported schema version")
    }

    @Test
    fun `importFromJson rejects oversized input`() = runTest {
        // Create a mock input stream that reports large size
        val smallContent = "{}"
        val inputStream = object : ByteArrayInputStream(smallContent.toByteArray()) {
            override fun available(): Int = 60 * 1024 * 1024 // Report 60MB
        }

        val result = repository.importFromJson(inputStream)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("too large")
    }

    @Test
    fun `importFromJson rejects invalid JSON`() = runTest {
        val invalidJson = "{ invalid json }"
        val inputStream = ByteArrayInputStream(invalidJson.toByteArray())

        val result = repository.importFromJson(inputStream)

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `importFromJson rejects schema version 0_9`() = runTest {
        val invalidExport = ExportData(
            schemaVersion = "0.9",
            exportedAt = testTimestamp
        )
        val json = gson.toJson(invalidExport)
        val inputStream = ByteArrayInputStream(json.toByteArray())

        val result = repository.importFromJson(inputStream)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Unsupported schema version")
    }

    // =====================
    // DTO CONVERSION TESTS
    // =====================

    @Test
    fun `TopicDto converts to Model correctly`() {
        val dto = TopicDto(
            id = "topic-1",
            title = "Test",
            summary = "Summary",
            posture = "NEUTRAL_CRITICAL",
            tags = listOf("tag-1"),
            createdAt = testTimestamp,
            updatedAt = testTimestamp
        )

        val model = dto.toModel()

        assertThat(model.id).isEqualTo("topic-1")
        assertThat(model.title).isEqualTo("Test")
        assertThat(model.posture).isEqualTo(Topic.Posture.NEUTRAL_CRITICAL)
        assertThat(model.tags).containsExactly("tag-1")
    }

    @Test
    fun `ClaimDto converts to Model correctly`() {
        val dto = ClaimDto(
            id = "claim-1",
            text = "Claim text",
            stance = "PRO",
            strength = "HIGH",
            topics = listOf("topic-1"),
            fallacyIds = listOf("ad_hominem"),
            createdAt = testTimestamp,
            updatedAt = testTimestamp,
            claimFingerprint = "fp123"
        )

        val model = dto.toModel()

        assertThat(model.id).isEqualTo("claim-1")
        assertThat(model.text).isEqualTo("Claim text")
        assertThat(model.stance).isEqualTo(Claim.Stance.PRO)
        assertThat(model.strength).isEqualTo(Claim.Strength.HIGH)
        assertThat(model.fallacyIds).containsExactly("ad_hominem")
        assertThat(model.claimFingerprint).isEqualTo("fp123")
    }

    @Test
    fun `EvidenceDto converts to Model correctly`() {
        val dto = EvidenceDto(
            id = "evidence-1",
            claimId = "claim-1",
            type = "QUOTE",
            content = "Content",
            sourceId = "source-1",
            quality = "HIGH",
            createdAt = testTimestamp,
            updatedAt = testTimestamp
        )

        val model = dto.toModel()

        assertThat(model.id).isEqualTo("evidence-1")
        assertThat(model.claimId).isEqualTo("claim-1")
        assertThat(model.type).isEqualTo(Evidence.EvidenceType.QUOTE)
        assertThat(model.quality).isEqualTo(Evidence.Quality.HIGH)
        assertThat(model.sourceId).isEqualTo("source-1")
    }

    @Test
    fun `TagDto converts to Model correctly`() {
        val dto = TagDto(
            id = "tag-1",
            label = "Important",
            color = "#FF0000",
            createdAt = testTimestamp,
            updatedAt = testTimestamp
        )

        val model = dto.toModel()

        assertThat(model.id).isEqualTo("tag-1")
        assertThat(model.label).isEqualTo("Important")
        assertThat(model.color).isEqualTo("#FF0000")
    }
}
