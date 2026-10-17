package `in`.hridayan.xliffeditor.presentation.viewmodel

import android.content.ContentResolver
import android.net.TestUri
import android.net.Uri
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument
import `in`.hridayan.xliffeditor.domain.model.PendingImportDocument
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.domain.repository.AppPreferences
import `in`.hridayan.xliffeditor.domain.repository.DocumentRepository
import `in`.hridayan.xliffeditor.domain.repository.DocumentSessionRepository
import `in`.hridayan.xliffeditor.domain.translation.TranslationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeRepo = object : DocumentRepository {
        override suspend fun loadDocument(uri: Uri, contentResolver: ContentResolver): Result<LoadedDocument> =
            Result.success(
                LoadedDocument(
                    uri = uri,
                    fileName = "test_import.xml",
                    sourceLocale = "EN-US",
                    targetLocale = "VI-VN",
                    items = listOf(
                        SingleStringItem(key = "hello", sourceValue = "Hello")
                    )
                )
            )
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
        var savedProjects: List<LoadedDocument> = emptyList()
        var savedActiveId: String? = null

        override suspend fun saveProjects(projects: List<LoadedDocument>, activeId: String?) {
            savedProjects = projects
            savedActiveId = activeId
        }

        override suspend fun loadProjects(): Pair<List<LoadedDocument>, String?> {
            return Pair(savedProjects, savedActiveId)
        }

        override suspend fun saveSession(document: LoadedDocument) {
            savedProjects = listOf(document)
            savedActiveId = document.id
        }

        override suspend fun loadSession(): LoadedDocument? {
            return savedProjects.firstOrNull()
        }

        override suspend fun clearSession() {
            savedProjects = emptyList()
            savedActiveId = null
        }
    }

    private lateinit var viewModel: ProjectViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSessionRepo.savedProjects = emptyList()
        fakeSessionRepo.savedActiveId = null
        viewModel = ProjectViewModel(fakeRepo, fakeTranslator, fakePrefs, fakeSessionRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testScreenNavigation() {
        assertEquals(AppScreen.HOME, viewModel.uiState.value.currentScreen)
        viewModel.onNavigateToScreen(AppScreen.SETTINGS)
        assertEquals(AppScreen.SETTINGS, viewModel.uiState.value.currentScreen)
        viewModel.onNavigateBack()
        assertEquals(AppScreen.HOME, viewModel.uiState.value.currentScreen)
    }

    @Test
    fun testInitialProjectsEmpty() {
        assertTrue(viewModel.uiState.value.projects.isEmpty())
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
        assertEquals(1, fakeSessionRepo.savedProjects.size)

        viewModel.onAutoTranslateItem("btn_submit")
        testScheduler.advanceUntilIdle()

        val translated = viewModel.uiState.value.document.items.find { it.key == "btn_submit" }
        assertEquals("Translated: Submit Now", translated?.targetValue)
        assertEquals(TranslationStatus.TRANSLATED, translated?.status)
    }

    @Test
    fun testSessionRestorationOnLaunch() = runTest {
        fakeSessionRepo.savedProjects = listOf(
            LoadedDocument(
                fileName = "app_strings.xml",
                items = listOf(
                    SingleStringItem(key = "title", sourceValue = "Hello World", status = TranslationStatus.UNTRANSLATED)
                )
            )
        )

        val newViewModel = ProjectViewModel(fakeRepo, fakeTranslator, fakePrefs, fakeSessionRepo)
        testScheduler.advanceUntilIdle()

        assertEquals("app_strings.xml", newViewModel.uiState.value.document.fileName)
        assertEquals(1, newViewModel.uiState.value.projects.size)
        assertEquals("title", newViewModel.uiState.value.document.items[0].key)
    }

    @Test
    fun testImportConfirmationFlow() = runTest {
        val dummyUri = TestUri("/res/values-es/strings.xml")
        viewModel.onFilePicked(dummyUri, object : ContentResolver(null) {})
        testScheduler.advanceUntilIdle()

        val pending = viewModel.uiState.value.pendingImportDocument
        assertNotNull(pending)
        assertEquals("test_import.xml", pending?.fileName)
        assertEquals("ES-ES", pending?.detectedSourceLocale)

        viewModel.onConfirmImport("FR-FR")
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingImportDocument)
        assertEquals(1, viewModel.uiState.value.projects.size)
        assertEquals(AppScreen.PROJECT_DETAIL, viewModel.uiState.value.currentScreen)
        assertEquals("FR-FR", viewModel.uiState.value.activeProject.sourceLocale)
    }

    @Test
    fun testDeleteProject() = runTest {
        viewModel.onAddNewString("k1", "v1", null, true)
        testScheduler.advanceUntilIdle()
        val docId = viewModel.uiState.value.activeProject.id
        assertEquals(1, viewModel.uiState.value.projects.size)

        viewModel.onDeleteProject(docId)
        testScheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.projects.size)
        assertEquals(AppScreen.HOME, viewModel.uiState.value.currentScreen)
    }

    @Test
    fun testNonTranslatableStringsExcludedFromAutoTranslate() = runTest {
        viewModel.onAddNewString("not_translatable", "Do Not Translate", "Comment", false)
        viewModel.onAddNewString("translatable", "Translate Me", null, true)
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.nonTranslatableCount)
        assertEquals(1, viewModel.uiState.value.needsTranslationCount)

        viewModel.onAutoTranslateItem("not_translatable")
        testScheduler.advanceUntilIdle()

        val nonTransItem = viewModel.uiState.value.document.items.find { it.key == "not_translatable" }
        assertEquals(null, nonTransItem?.targetValue)
        assertEquals(TranslationStatus.UNTRANSLATED, nonTransItem?.status)
        assertTrue(viewModel.uiState.value.statusMessage?.contains("Cannot translate") == true)

        viewModel.onAutoTranslateAllUntranslated()
        testScheduler.advanceUntilIdle()

        val transItem = viewModel.uiState.value.document.items.find { it.key == "translatable" }
        assertEquals(TranslationStatus.TRANSLATED, transItem?.status)

        val nonTransItemAfter = viewModel.uiState.value.document.items.find { it.key == "not_translatable" }
        assertEquals(null, nonTransItemAfter?.targetValue)
        assertEquals(TranslationStatus.UNTRANSLATED, nonTransItemAfter?.status)
    }

    @Test
    fun testMultiLocaleTranslationSwitching() = runTest {
        viewModel.onAddNewString("greeting", "Hello", "Context note", true)
        testScheduler.advanceUntilIdle()

        viewModel.onSaveTranslation("greeting", "Xin chao", TranslationStatus.TRANSLATED)
        testScheduler.advanceUntilIdle()

        val itemVi = viewModel.uiState.value.document.items.first()
        assertEquals("Xin chao", itemVi.targetValue)
        assertEquals(TranslationStatus.TRANSLATED, itemVi.status)

        viewModel.onTargetLocaleChanged("ES-ES")
        testScheduler.advanceUntilIdle()

        val itemEs = viewModel.uiState.value.document.items.first()
        assertEquals(null, itemEs.targetValue)
        assertEquals(TranslationStatus.UNTRANSLATED, itemEs.status)

        viewModel.onSaveTranslation("greeting", "Hola", TranslationStatus.TRANSLATED)
        testScheduler.advanceUntilIdle()

        val itemEsSaved = viewModel.uiState.value.document.items.first()
        assertEquals("Hola", itemEsSaved.targetValue)
        assertEquals(TranslationStatus.TRANSLATED, itemEsSaved.status)

        viewModel.onTargetLocaleChanged("VI-VN")
        testScheduler.advanceUntilIdle()

        val itemViRestored = viewModel.uiState.value.document.items.first()
        assertEquals("Xin chao", itemViRestored.targetValue)
        assertEquals(TranslationStatus.TRANSLATED, itemViRestored.status)

        viewModel.onTargetLocaleChanged("ES-ES")
        testScheduler.advanceUntilIdle()

        val itemEsRestored = viewModel.uiState.value.document.items.first()
        assertEquals("Hola", itemEsRestored.targetValue)
        assertEquals(TranslationStatus.TRANSLATED, itemEsRestored.status)
    }
}
