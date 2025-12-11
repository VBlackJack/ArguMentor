package com.argumentor.app.ui.screens.settings

import app.cash.turbine.test
import com.argumentor.app.data.datastore.SettingsDataStore
import com.argumentor.app.data.preferences.AppLanguage
import com.argumentor.app.data.preferences.LanguagePreferences
import com.argumentor.app.data.util.TutorialManager
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var languagePreferences: LanguagePreferences
    private lateinit var tutorialManager: TutorialManager
    private lateinit var viewModel: SettingsViewModel

    private val isDarkThemeFlow = MutableStateFlow(false)
    private val isImmersiveModeFlow = MutableStateFlow(false)
    private val fontSizeFlow = MutableStateFlow("MEDIUM")
    private val showEthicsWarningFlow = MutableStateFlow(true)
    private val defaultPostureFlow = MutableStateFlow("neutral_critique")
    private val tutorialEnabledFlow = MutableStateFlow(true)
    private val demoTopicIdFlow = MutableStateFlow<String?>(null)
    private val languageFlow = MutableStateFlow(AppLanguage.FRENCH)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        settingsDataStore = mock()
        languagePreferences = mock()
        tutorialManager = mock()

        whenever(settingsDataStore.isDarkTheme).thenReturn(isDarkThemeFlow)
        whenever(settingsDataStore.isImmersiveMode).thenReturn(isImmersiveModeFlow)
        whenever(settingsDataStore.fontSize).thenReturn(fontSizeFlow)
        whenever(settingsDataStore.showEthicsWarning).thenReturn(showEthicsWarningFlow)
        whenever(settingsDataStore.defaultPosture).thenReturn(defaultPostureFlow)
        whenever(settingsDataStore.tutorialEnabled).thenReturn(tutorialEnabledFlow)
        whenever(settingsDataStore.demoTopicId).thenReturn(demoTopicIdFlow)
        whenever(languagePreferences.languageFlow).thenReturn(languageFlow)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            settingsDataStore = settingsDataStore,
            languagePreferences = languagePreferences,
            tutorialManager = tutorialManager
        )
    }

    @Test
    fun `initial state has correct default values`() = runTest {
        viewModel = createViewModel()

        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.isDarkTheme.value).isFalse()
        assertThat(viewModel.isImmersiveMode.value).isFalse()
        assertThat(viewModel.fontSize.value).isEqualTo(SettingsViewModel.FontSize.MEDIUM)
        assertThat(viewModel.showEthicsWarning.value).isTrue()
        assertThat(viewModel.tutorialEnabled.value).isTrue()
        assertThat(viewModel.language.value).isEqualTo(AppLanguage.FRENCH)
    }

    @Test
    fun `toggleDarkTheme calls datastore with inverted value`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleDarkTheme()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsDataStore).setDarkTheme(true)
    }

    @Test
    fun `toggleImmersiveMode calls datastore with inverted value`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleImmersiveMode()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsDataStore).setImmersiveMode(true)
    }

    @Test
    fun `setFontSize calls datastore with correct value`() = runTest {
        viewModel = createViewModel()

        viewModel.setFontSize(SettingsViewModel.FontSize.LARGE)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsDataStore).setFontSize("LARGE")
    }

    @Test
    fun `toggleEthicsWarning calls datastore with inverted value`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleEthicsWarning()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsDataStore).setShowEthicsWarning(false)
    }

    @Test
    fun `setDefaultPosture calls datastore with correct value`() = runTest {
        viewModel = createViewModel()

        viewModel.setDefaultPosture("skeptical")
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsDataStore).setDefaultPosture("skeptical")
    }

    @Test
    fun `selectLanguage sets pending language when different`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.pendingLanguage.value).isNull()

        viewModel.selectLanguage(AppLanguage.ENGLISH)

        assertThat(viewModel.pendingLanguage.value).isEqualTo(AppLanguage.ENGLISH)
    }

    @Test
    fun `selectLanguage clears pending when selecting current language`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectLanguage(AppLanguage.ENGLISH)
        assertThat(viewModel.pendingLanguage.value).isEqualTo(AppLanguage.ENGLISH)

        viewModel.selectLanguage(AppLanguage.FRENCH) // Current language
        assertThat(viewModel.pendingLanguage.value).isNull()
    }

    @Test
    fun `applyLanguageChange saves language and clears pending`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectLanguage(AppLanguage.ENGLISH)

        var callbackCalled = false
        viewModel.applyLanguageChange { callbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        verify(languagePreferences).setLanguage(AppLanguage.ENGLISH)
        assertThat(viewModel.pendingLanguage.value).isNull()
        assertThat(callbackCalled).isTrue()
    }

    @Test
    fun `applyLanguageChange does nothing when no pending language`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        var callbackCalled = false
        viewModel.applyLanguageChange { callbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(callbackCalled).isFalse()
    }

    @Test
    fun `cancelLanguageChange clears pending language`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectLanguage(AppLanguage.ENGLISH)
        assertThat(viewModel.pendingLanguage.value).isNotNull()

        viewModel.cancelLanguageChange()
        assertThat(viewModel.pendingLanguage.value).isNull()
    }

    @Test
    fun `toggleTutorialEnabled calls datastore and tutorialManager`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleTutorialEnabled()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsDataStore).setTutorialEnabled(false)
        verify(tutorialManager).handleTutorialToggle(false)
    }

    @Test
    fun `fontSize maps correctly from string`() = runTest {
        fontSizeFlow.value = "LARGE"
        viewModel = createViewModel()

        viewModel.fontSize.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val item = expectMostRecentItem()
            assertThat(item).isEqualTo(SettingsViewModel.FontSize.LARGE)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `FontSize enum has correct scale values`() {
        assertThat(SettingsViewModel.FontSize.SMALL.scale).isEqualTo(0.85f)
        assertThat(SettingsViewModel.FontSize.MEDIUM.scale).isEqualTo(1.0f)
        assertThat(SettingsViewModel.FontSize.LARGE.scale).isEqualTo(1.15f)
        assertThat(SettingsViewModel.FontSize.EXTRA_LARGE.scale).isEqualTo(1.3f)
    }

    @Test
    fun `demoTopicId emits correct value`() = runTest {
        demoTopicIdFlow.value = "demo-123"
        viewModel = createViewModel()

        viewModel.demoTopicId.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val item = expectMostRecentItem()
            assertThat(item).isEqualTo("demo-123")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
