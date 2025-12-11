package com.argumentor.app.data.repository

import app.cash.turbine.test
import com.argumentor.app.data.local.dao.ClaimDao
import com.argumentor.app.data.model.Claim
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
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ClaimRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var claimDao: ClaimDao
    private lateinit var repository: ClaimRepository

    private val testClaim = Claim(
        id = "claim-1",
        text = "Test Claim",
        topics = listOf("topic-1"),
        stance = Claim.Stance.PRO,
        strength = Claim.Strength.MEDIUM
    )

    private val testClaimWithFallacy = Claim(
        id = "claim-2",
        text = "Fallacious Claim",
        topics = listOf("topic-1"),
        fallacyIds = listOf("ad_hominem", "straw_man")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        claimDao = mock()
        repository = ClaimRepository(claimDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAllClaims returns flow from dao`() = runTest {
        val claims = listOf(testClaim)
        whenever(claimDao.getAllClaims()).thenReturn(flowOf(claims))

        repository.getAllClaims().test {
            assertThat(awaitItem()).isEqualTo(claims)
            awaitComplete()
        }
    }

    @Test
    fun `getClaimById returns flow from dao`() = runTest {
        whenever(claimDao.observeClaimById("claim-1")).thenReturn(flowOf(testClaim))

        repository.getClaimById("claim-1").test {
            assertThat(awaitItem()).isEqualTo(testClaim)
            awaitComplete()
        }
    }

    @Test
    fun `getClaimByIdSync returns claim from dao`() = runTest {
        whenever(claimDao.getClaimById("claim-1")).thenReturn(testClaim)

        val result = repository.getClaimByIdSync("claim-1")

        assertThat(result).isEqualTo(testClaim)
    }

    @Test
    fun `getClaimsByIds returns claims from dao`() = runTest {
        val claims = listOf(testClaim)
        whenever(claimDao.getClaimsByIds(listOf("claim-1"))).thenReturn(claims)

        val result = repository.getClaimsByIds(listOf("claim-1"))

        assertThat(result).isEqualTo(claims)
    }

    @Test
    fun `getClaimsByIds returns empty list for empty input`() = runTest {
        val result = repository.getClaimsByIds(emptyList())

        assertThat(result).isEmpty()
    }

    @Test
    fun `getClaimsForTopic returns claims from dao`() = runTest {
        val claims = listOf(testClaim)
        whenever(claimDao.getClaimsByTopicId("topic-1")).thenReturn(claims)

        val result = repository.getClaimsForTopic("topic-1")

        assertThat(result).isEqualTo(claims)
    }

    @Test
    fun `getClaimsForFallacy returns claims from dao`() = runTest {
        val claims = listOf(testClaimWithFallacy)
        whenever(claimDao.getClaimsByFallacyId("ad_hominem")).thenReturn(claims)

        val result = repository.getClaimsForFallacy("ad_hominem")

        assertThat(result).isEqualTo(claims)
    }

    @Test
    fun `observeClaimsForFallacy returns flow from dao`() = runTest {
        val claims = listOf(testClaimWithFallacy)
        whenever(claimDao.observeClaimsByFallacyId("ad_hominem")).thenReturn(flowOf(claims))

        repository.observeClaimsForFallacy("ad_hominem").test {
            assertThat(awaitItem()).isEqualTo(claims)
            awaitComplete()
        }
    }

    @Test
    fun `insertClaim generates fingerprint and calls dao`() = runTest {
        repository.insertClaim(testClaim)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(claimDao).insertClaim(argThat { claim ->
            claim.id == testClaim.id &&
            claim.text == testClaim.text &&
            claim.claimFingerprint.isNotEmpty()
        })
    }

    @Test
    fun `insertClaim validates topic IDs`() = runTest {
        val invalidClaim = testClaim.copy(topics = listOf("valid-id", "invalid;id"))

        var exceptionThrown = false
        try {
            repository.insertClaim(invalidClaim)
        } catch (e: IllegalArgumentException) {
            exceptionThrown = true
            assertThat(e.message).contains("Invalid topic ID format")
        }

        assertThat(exceptionThrown).isTrue()
    }

    @Test
    fun `insertClaim validates fallacy IDs`() = runTest {
        val invalidClaim = testClaim.copy(fallacyIds = listOf("ad_hominem", "invalid;fallacy"))

        var exceptionThrown = false
        try {
            repository.insertClaim(invalidClaim)
        } catch (e: IllegalArgumentException) {
            exceptionThrown = true
            assertThat(e.message).contains("Invalid fallacy ID format")
        }

        assertThat(exceptionThrown).isTrue()
    }

    @Test
    fun `insertClaim accepts valid fallacy IDs with underscores`() = runTest {
        repository.insertClaim(testClaimWithFallacy)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(claimDao).insertClaim(any())
    }

    @Test
    fun `updateClaim generates fingerprint and calls dao`() = runTest {
        val updatedClaim = testClaim.copy(text = "Updated Text")

        repository.updateClaim(updatedClaim)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(claimDao).updateClaim(argThat { claim ->
            claim.id == testClaim.id &&
            claim.text == "Updated Text" &&
            claim.claimFingerprint.isNotEmpty()
        })
    }

    @Test
    fun `updateClaim validates topic IDs`() = runTest {
        val invalidClaim = testClaim.copy(topics = listOf("DROP TABLE claims;--"))

        var exceptionThrown = false
        try {
            repository.updateClaim(invalidClaim)
        } catch (e: IllegalArgumentException) {
            exceptionThrown = true
            assertThat(e.message).contains("Invalid topic ID format")
        }

        assertThat(exceptionThrown).isTrue()
    }

    @Test
    fun `deleteClaim calls dao delete`() = runTest {
        repository.deleteClaim(testClaim)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(claimDao).deleteClaim(testClaim)
    }

    @Test
    fun `searchClaims uses FTS search`() = runTest {
        val claims = listOf(testClaim)
        whenever(claimDao.searchClaimsFts("test")).thenReturn(flowOf(claims))

        repository.searchClaims("test").test {
            assertThat(awaitItem()).isEqualTo(claims)
            awaitComplete()
        }
    }

    @Test
    fun `getClaimsForTopic validates topic ID`() = runTest {
        var exceptionThrown = false
        try {
            repository.getClaimsForTopic("invalid;id")
        } catch (e: IllegalArgumentException) {
            exceptionThrown = true
        }

        assertThat(exceptionThrown).isTrue()
    }

    @Test
    fun `getClaimsForFallacy validates fallacy ID`() = runTest {
        var exceptionThrown = false
        try {
            repository.getClaimsForFallacy("invalid'id")
        } catch (e: IllegalArgumentException) {
            exceptionThrown = true
        }

        assertThat(exceptionThrown).isTrue()
    }

    @Test
    fun `getClaimsByIds validates all IDs`() = runTest {
        var exceptionThrown = false
        try {
            repository.getClaimsByIds(listOf("valid-id", "invalid=id"))
        } catch (e: IllegalArgumentException) {
            exceptionThrown = true
        }

        assertThat(exceptionThrown).isTrue()
    }
}
