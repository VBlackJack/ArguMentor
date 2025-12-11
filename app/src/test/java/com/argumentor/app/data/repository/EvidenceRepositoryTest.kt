package com.argumentor.app.data.repository

import app.cash.turbine.test
import com.argumentor.app.data.local.dao.EvidenceDao
import com.argumentor.app.data.model.Evidence
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class EvidenceRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var evidenceDao: EvidenceDao
    private lateinit var repository: EvidenceRepository

    private val testEvidence = Evidence(
        id = "evidence-1",
        claimId = "claim-1",
        content = "Test evidence content",
        type = Evidence.Type.STUDY,
        sourceId = "source-1",
        quality = Evidence.Quality.HIGH
    )

    private val testEvidence2 = Evidence(
        id = "evidence-2",
        claimId = "claim-1",
        content = "Another evidence",
        type = Evidence.Type.STATISTIC
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        evidenceDao = mock()
        repository = EvidenceRepository(evidenceDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAllEvidences returns flow from dao`() = runTest {
        val evidences = listOf(testEvidence, testEvidence2)
        whenever(evidenceDao.getAllEvidences()).thenReturn(flowOf(evidences))

        repository.getAllEvidences().test {
            assertThat(awaitItem()).isEqualTo(evidences)
            awaitComplete()
        }
    }

    @Test
    fun `getEvidencesByClaimId returns filtered evidences`() = runTest {
        val evidences = listOf(testEvidence, testEvidence2)
        whenever(evidenceDao.getEvidencesByClaimId("claim-1")).thenReturn(flowOf(evidences))

        repository.getEvidencesByClaimId("claim-1").test {
            assertThat(awaitItem()).isEqualTo(evidences)
            awaitComplete()
        }
    }

    @Test
    fun `getEvidencesBySourceId returns filtered evidences`() = runTest {
        val evidences = listOf(testEvidence)
        whenever(evidenceDao.getEvidencesBySourceId("source-1")).thenReturn(flowOf(evidences))

        repository.getEvidencesBySourceId("source-1").test {
            assertThat(awaitItem()).isEqualTo(evidences)
            awaitComplete()
        }
    }

    @Test
    fun `getEvidenceById returns evidence from dao`() = runTest {
        whenever(evidenceDao.getEvidenceById("evidence-1")).thenReturn(testEvidence)

        val result = repository.getEvidenceById("evidence-1")

        assertThat(result).isEqualTo(testEvidence)
    }

    @Test
    fun `getEvidenceById returns null for non-existent`() = runTest {
        whenever(evidenceDao.getEvidenceById("non-existent")).thenReturn(null)

        val result = repository.getEvidenceById("non-existent")

        assertThat(result).isNull()
    }

    @Test
    fun `observeEvidenceById returns flow from dao`() = runTest {
        whenever(evidenceDao.observeEvidenceById("evidence-1")).thenReturn(flowOf(testEvidence))

        repository.observeEvidenceById("evidence-1").test {
            assertThat(awaitItem()).isEqualTo(testEvidence)
            awaitComplete()
        }
    }

    @Test
    fun `insertEvidence calls dao insert`() = runTest {
        repository.insertEvidence(testEvidence)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(evidenceDao).insertEvidence(testEvidence)
    }

    @Test
    fun `updateEvidence calls dao update`() = runTest {
        val updatedEvidence = testEvidence.copy(content = "Updated content")

        repository.updateEvidence(updatedEvidence)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(evidenceDao).updateEvidence(updatedEvidence)
    }

    @Test
    fun `deleteEvidence calls dao delete`() = runTest {
        repository.deleteEvidence(testEvidence)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(evidenceDao).deleteEvidence(testEvidence)
    }

    @Test
    fun `deleteEvidencesByClaimId calls dao`() = runTest {
        repository.deleteEvidencesByClaimId("claim-1")

        testDispatcher.scheduler.advanceUntilIdle()

        verify(evidenceDao).deleteEvidencesByClaimId("claim-1")
    }

    @Test
    fun `searchEvidences uses FTS search`() = runTest {
        val evidences = listOf(testEvidence)
        whenever(evidenceDao.searchEvidencesFts("test")).thenReturn(flowOf(evidences))

        repository.searchEvidences("test").test {
            assertThat(awaitItem()).isEqualTo(evidences)
            awaitComplete()
        }
    }
}
