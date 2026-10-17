package `in`.hridayan.xliffeditor.presentation.viewmodel

import android.content.ContentResolver
import android.net.Uri
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.domain.repository.AppPreferences
import `in`.hridayan.xliffeditor.domain.repository.DocumentRepository
import `in`.hridayan.xliffeditor.domain.repository.DocumentSessionRepository
import `in`.hridayan.xliffeditor.domain.translation.TranslationEngine
import `in`.hridayan.xliffeditor.presentation.navigation.BottomNavDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeRepo = object : DocumentRepository {
        override suspend fun loadDocument(uri: Uri, contentResolver: ContentResolver): Result<LoadedDocument> =
            Result.success(LoadedDocument(uri = uri, fileName = "test.xml", items = emptyList()))
        override suspend fun saveDocument(uri: Uri, document: LoadedDocument, contentResolver: ContentResolver): Result<Unit> =
            Result.success(Unit)
        override suspend fun exportDocument(uri: Uri, document: LoadedDocument, contentResolver: ContentResolver): Result<Unit> =
            Result.success(Unit)
    }

    private val fakeTranslator = object : TranslationEngine {
        override suspend fun translate(text: String, sourceLanguage: String, targetLanguage: String): Result<String> =
            Result.success("Translated: $text")
        override suspend fun downloadModelIfNeeded(sourceLanguage: String, targetLanguage: String): Result<Unit> =
            Result.success(Unit)
    }

    private val fakePrefs = object : AppPreferences {
        private var mode = AppThemeMode.SYSTEM
        private var dynamic = true
        override fun getThemeMode(): AppThemeMode = mode
        override fun setThemeMode(mode: AppThemeMode) { this.mode = mode }
        override fun isDynamicColor(): Boolean = dynamic
        override fun setDynamicColor(enabled: Boolean) { this.dynamic = enabled }
    }

    private val fakeSessionRepo = object : DocumentSessionRepository {
        var savedDoc: LoadedDocument? = null
        override suspend fun saveSession(document: LoadedDocument) {
            savedDoc = document
        }
        override suspend fun loadSession(): LoadedDocument? = savedDoc
        override suspend fun clearSession() {
            savedDoc = null
        }
    }

    private lateinit var viewModel: ProjectViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSessionRepo.savedDoc = null
        viewModel = ProjectViewModel(fakeRepo, fakeTranslator, fakePrefs, fakeSessionRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testTabNavigation() {
        assertEquals(BottomNavDestination.TRANSLATE, viewModel.uiState.value.currentDestination)
        viewModel.onTabSelected(BottomNavDestination.FILES)
        assertEquals(BottomNavDestination.FILES, viewModel.uiState.value.currentDestination)
        viewModel.onTabSelected(BottomNavDestination.STRINGS)
        assertEquals(BottomNavDestination.STRINGS, viewModel.uiState.value.currentDestination)
    }

    @Test
    fun testInitialStringsEmpty() {
        assertTrue(viewModel.uiState.value.document.items.isEmpty())
        assertTrue(viewModel.uiState.value.filteredItems.isEmpty())
    }

    @Test
    fun testAddNewStringAndTranslate() = runTest {
        viewModel.onAddNewString("btn_submit", "Submit Now", "Comment", true)
        testScheduler.advanceUntilIdle()

        val added = viewModel.uiState.value.document.items.find { it.key == "btn_submit" }
        assertNotNull(added)
        assertEquals("Submit Now", added?.sourceValue)
        assertEquals(TranslationStatus.UNTRANSLATED, added?.status)
        assertNotNull(fakeSessionRepo.savedDoc)
        assertEquals(1, fakeSessionRepo.savedDoc?.items?.size)

        viewModel.onAutoTranslateItem("btn_submit")
        testScheduler.advanceUntilIdle()

        val translated = viewModel.uiState.value.document.items.find { it.key == "btn_submit" }
        assertEquals("Translated: Submit Now", translated?.targetValue)
        assertEquals(TranslationStatus.TRANSLATED, translated?.status)
    }

    @Test
    fun testSessionRestorationOnLaunch() = runTest {
        // Pre-populate session
        fakeSessionRepo.savedDoc = LoadedDocument(
            fileName = "app_strings.xml",
            items = listOf(
                SingleStringItem(key = "title", sourceValue = "Hello World", status = TranslationStatus.UNTRANSLATED)
            )
        )

        val newViewModel = ProjectViewModel(fakeRepo, fakeTranslator, fakePrefs, fakeSessionRepo)
        testScheduler.advanceUntilIdle()

        assertEquals("app_strings.xml", newViewModel.uiState.value.document.fileName)
        assertEquals(1, newViewModel.uiState.value.document.items.size)
        assertEquals("title", newViewModel.uiState.value.document.items[0].key)
    }

    @Test
    fun testAutoTranslateAllProgress() = runTest {
        viewModel.onAddNewString("str1", "One", null, true)
        viewModel.onAddNewString("str2", "Two", null, true)
        testScheduler.advanceUntilIdle()

        viewModel.onAutoTranslateAllUntranslated()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isTranslating)
        assertEquals(100, state.translationProgressPercent)
        assertEquals(2, state.translatedBatchCount)
        assertEquals(2, state.totalBatchCount)
        assertEquals(TranslationStatus.TRANSLATED, state.document.items[0].status)
        assertEquals(TranslationStatus.TRANSLATED, state.document.items[1].status)
    }

    @Test
    fun testThemeSwitching() {
        viewModel.onThemeModeChanged(AppThemeMode.DARK)
        assertEquals(AppThemeMode.DARK, viewModel.uiState.value.themeMode)

        viewModel.onDynamicColorChanged(false)
        assertEquals(false, viewModel.uiState.value.dynamicColor)
    }
}
