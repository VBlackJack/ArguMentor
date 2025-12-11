package com.argumentor.app.ui.screens.claim

import app.cash.turbine.test
import com.argumentor.app.R
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Fallacy
import com.argumentor.app.data.repository.ClaimRepository
import com.argumentor.app.data.repository.FallacyRepository
import com.argumentor.app.util.ResourceProvider
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
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class ClaimCreateEditViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var claimRepository: ClaimRepository
    private lateinit var fallacyRepository: FallacyRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var viewModel: ClaimCreateEditViewModel

    private val testClaim = Claim(
        id = "claim-1",
        text = "Test claim text",
        stance = Claim.Stance.PRO,
        strength = Claim.Strength.HIGH,
        topics = listOf("topic-1"),
        fallacyIds = listOf("ad_hominem")
    )

    private val testFallacy = Fallacy(
        id = "ad_hominem",
        name = "Ad Hominem",
        description = "Attacking the person",
        example = "You can't trust John's opinion because he's a terrible person."
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        claimRepository = mock()
        fallacyRepository = mock()
        resourceProvider = mock()

        whenever(fallacyRepository.getAllFallacies()).thenReturn(flowOf(listOf(testFallacy)))
        whenever(resourceProvider.getString(R.string.error_claim_text_empty)).thenReturn("Claim text cannot be empty")
        whenever(resourceProvider.getString(R.string.error_text_too_long)).thenReturn("Text is too long")
        whenever(resourceProvider.getString(R.string.error_unknown)).thenReturn("Unknown error")
        whenever(resourceProvider.getString(eq(R.string.error_save_claim), any())).thenReturn("Failed to save claim")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ClaimCreateEditViewModel {
        return ClaimCreateEditViewModel(
            claimRepository = claimRepository,
            fallacyRepository = fallacyRepository,
            resourceProvider = resourceProvider
        )
    }

    @Test
    fun `initial state has empty text and default values`() = runTest {
        viewModel = createViewModel()

        assertThat(viewModel.text.value).isEmpty()
        assertThat(viewModel.stance.value).isEqualTo(Claim.Stance.NEUTRAL)
        assertThat(viewModel.strength.value).isEqualTo(Claim.Strength.MEDIUM)
        assertThat(viewModel.selectedTopics.value).isEmpty()
        assertThat(viewModel.selectedFallacies.value).isEmpty()
        assertThat(viewModel.isSaving.value).isFalse()
        assertThat(viewModel.isLoading.value).isFalse()
        assertThat(viewModel.errorMessage.value).isNull()
    }

    @Test
    fun `loadClaim with null claimId initializes for new claim`() = runTest {
        viewModel = createViewModel()

        viewModel.loadClaim(null, "topic-1")

        assertThat(viewModel.selectedTopics.value).containsExactly("topic-1")
        assertThat(viewModel.text.value).isEmpty()
    }

    @Test
    fun `loadClaim with claimId loads existing claim`() = runTest {
        whenever(claimRepository.getClaimByIdSync("claim-1")).thenReturn(testClaim)

        viewModel = createViewModel()
        viewModel.loadClaim("claim-1", null)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.text.value).isEqualTo("Test claim text")
        assertThat(viewModel.stance.value).isEqualTo(Claim.Stance.PRO)
        assertThat(viewModel.strength.value).isEqualTo(Claim.Strength.HIGH)
        assertThat(viewModel.selectedTopics.value).containsExactly("topic-1")
        assertThat(viewModel.selectedFallacies.value).containsExactly("ad_hominem")
    }

    @Test
    fun `onTextChange updates text state`() = runTest {
        viewModel = createViewModel()

        viewModel.onTextChange("New text")

        assertThat(viewModel.text.value).isEqualTo("New text")
    }

    @Test
    fun `onStanceChange updates stance state`() = runTest {
        viewModel = createViewModel()

        viewModel.onStanceChange(Claim.Stance.CON)

        assertThat(viewModel.stance.value).isEqualTo(Claim.Stance.CON)
    }

    @Test
    fun `onStrengthChange updates strength state`() = runTest {
        viewModel = createViewModel()

        viewModel.onStrengthChange(Claim.Strength.LOW)

        assertThat(viewModel.strength.value).isEqualTo(Claim.Strength.LOW)
    }

    @Test
    fun `addTopic adds topic to selection`() = runTest {
        viewModel = createViewModel()

        viewModel.addTopic("topic-1")
        viewModel.addTopic("topic-2")

        assertThat(viewModel.selectedTopics.value).containsExactly("topic-1", "topic-2")
    }

    @Test
    fun `addTopic does not add duplicate`() = runTest {
        viewModel = createViewModel()

        viewModel.addTopic("topic-1")
        viewModel.addTopic("topic-1")

        assertThat(viewModel.selectedTopics.value).hasSize(1)
    }

    @Test
    fun `removeTopic removes topic from selection`() = runTest {
        viewModel = createViewModel()
        viewModel.addTopic("topic-1")
        viewModel.addTopic("topic-2")

        viewModel.removeTopic("topic-1")

        assertThat(viewModel.selectedTopics.value).containsExactly("topic-2")
    }

    @Test
    fun `addFallacy adds fallacy to selection`() = runTest {
        viewModel = createViewModel()

        viewModel.addFallacy("ad_hominem")
        viewModel.addFallacy("straw_man")

        assertThat(viewModel.selectedFallacies.value).containsExactly("ad_hominem", "straw_man")
    }

    @Test
    fun `addFallacy does not add duplicate`() = runTest {
        viewModel = createViewModel()

        viewModel.addFallacy("ad_hominem")
        viewModel.addFallacy("ad_hominem")

        assertThat(viewModel.selectedFallacies.value).hasSize(1)
    }

    @Test
    fun `removeFallacy removes fallacy from selection`() = runTest {
        viewModel = createViewModel()
        viewModel.addFallacy("ad_hominem")
        viewModel.addFallacy("straw_man")

        viewModel.removeFallacy("ad_hominem")

        assertThat(viewModel.selectedFallacies.value).containsExactly("straw_man")
    }

    @Test
    fun `hasUnsavedChanges returns false initially for new claim`() = runTest {
        viewModel = createViewModel()
        viewModel.loadClaim(null, null)

        assertThat(viewModel.hasUnsavedChanges()).isFalse()
    }

    @Test
    fun `hasUnsavedChanges returns true after text change`() = runTest {
        viewModel = createViewModel()
        viewModel.loadClaim(null, null)

        viewModel.onTextChange("New text")

        assertThat(viewModel.hasUnsavedChanges()).isTrue()
    }

    @Test
    fun `hasUnsavedChanges returns true after stance change`() = runTest {
        viewModel = createViewModel()
        viewModel.loadClaim(null, null)

        viewModel.onStanceChange(Claim.Stance.PRO)

        assertThat(viewModel.hasUnsavedChanges()).isTrue()
    }

    @Test
    fun `hasUnsavedChanges returns false for loaded claim without changes`() = runTest {
        whenever(claimRepository.getClaimByIdSync("claim-1")).thenReturn(testClaim)

        viewModel = createViewModel()
        viewModel.loadClaim("claim-1", null)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.hasUnsavedChanges()).isFalse()
    }

    @Test
    fun `saveClaim fails with empty text`() = runTest {
        viewModel = createViewModel()
        viewModel.loadClaim(null, "topic-1")

        var callbackCalled = false
        viewModel.saveClaim { callbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(callbackCalled).isFalse()
        assertThat(viewModel.errorMessage.value).isEqualTo("Claim text cannot be empty")
    }

    @Test
    fun `saveClaim fails with blank text`() = runTest {
        viewModel = createViewModel()
        viewModel.loadClaim(null, "topic-1")
        viewModel.onTextChange("   ")

        var callbackCalled = false
        viewModel.saveClaim { callbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(callbackCalled).isFalse()
        assertThat(viewModel.errorMessage.value).isEqualTo("Claim text cannot be empty")
    }

    @Test
    fun `saveClaim creates new claim when not in edit mode`() = runTest {
        viewModel = createViewModel()
        viewModel.loadClaim(null, "topic-1")
        viewModel.onTextChange("New claim text")
        viewModel.onStanceChange(Claim.Stance.PRO)

        var callbackCalled = false
        viewModel.saveClaim { callbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        verify(claimRepository).insertClaim(argThat { claim ->
            claim.text == "New claim text" &&
            claim.stance == Claim.Stance.PRO &&
            claim.topics.contains("topic-1")
        })
        assertThat(callbackCalled).isTrue()
    }

    @Test
    fun `saveClaim updates existing claim in edit mode`() = runTest {
        whenever(claimRepository.getClaimByIdSync("claim-1")).thenReturn(testClaim)

        viewModel = createViewModel()
        viewModel.loadClaim("claim-1", null)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onTextChange("Updated claim text")

        var callbackCalled = false
        viewModel.saveClaim { callbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        verify(claimRepository).updateClaim(argThat { claim ->
            claim.id == "claim-1" && claim.text == "Updated claim text"
        })
        assertThat(callbackCalled).isTrue()
    }

    @Test
    fun `saveClaim handles repository error`() = runTest {
        whenever(claimRepository.insertClaim(any())).thenThrow(RuntimeException("Database error"))

        viewModel = createViewModel()
        viewModel.loadClaim(null, "topic-1")
        viewModel.onTextChange("New claim")

        var callbackCalled = false
        viewModel.saveClaim { callbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(callbackCalled).isFalse()
        assertThat(viewModel.errorMessage.value).isNotNull()
    }

    @Test
    fun `saveClaim sets isSaving during save operation`() = runTest {
        viewModel = createViewModel()
        viewModel.loadClaim(null, "topic-1")
        viewModel.onTextChange("New claim")

        viewModel.isSaving.test {
            assertThat(awaitItem()).isFalse()

            viewModel.saveClaim { }
            assertThat(awaitItem()).isTrue()

            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(awaitItem()).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearError clears error message`() = runTest {
        viewModel = createViewModel()
        viewModel.loadClaim(null, null)
        viewModel.saveClaim { } // Will fail with empty text
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.errorMessage.value).isNotNull()

        viewModel.clearError()

        assertThat(viewModel.errorMessage.value).isNull()
    }

    @Test
    fun `allFallacies emits from repository`() = runTest {
        viewModel = createViewModel()

        viewModel.allFallacies.test {
            assertThat(awaitItem()).isEmpty() // Initial value

            testDispatcher.scheduler.advanceUntilIdle()

            val fallacies = awaitItem()
            assertThat(fallacies).hasSize(1)
            assertThat(fallacies.first().id).isEqualTo("ad_hominem")

            cancelAndIgnoreRemainingEvents()
        }
    }
}
