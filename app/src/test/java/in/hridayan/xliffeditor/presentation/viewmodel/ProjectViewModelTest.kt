package `in`.hridayan.xliffeditor.presentation.viewmodel

import android.content.ContentResolver
import android.net.Uri
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.domain.repository.DocumentRepository
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

    private lateinit var viewModel: ProjectViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProjectViewModel(fakeRepo, fakeTranslator)
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
    fun testAddNewStringAndTranslate() = runTest {
        viewModel.onAddNewString("btn_submit", "Submit Now", "Comment", true)
        val added = viewModel.uiState.value.document.items.find { it.key == "btn_submit" }
        assertNotNull(added)
        assertEquals("Submit Now", added?.sourceValue)
        assertEquals(TranslationStatus.UNTRANSLATED, added?.status)

        viewModel.onAutoTranslateItem("btn_submit")
        testScheduler.advanceUntilIdle()

        val translated = viewModel.uiState.value.document.items.find { it.key == "btn_submit" }
        assertEquals("Translated: Submit Now", translated?.targetValue)
        assertEquals(TranslationStatus.TRANSLATED, translated?.status)
    }

    @Test
    fun testFilterByStatus() {
        viewModel.onStatusFilterSelected(TranslationStatus.TRANSLATED)
        val state = viewModel.uiState.value
        assertTrue(state.filteredItems.all { it.status == TranslationStatus.TRANSLATED })
    }
}
