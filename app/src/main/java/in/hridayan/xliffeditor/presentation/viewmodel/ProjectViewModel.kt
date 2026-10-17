package `in`.hridayan.xliffeditor.presentation.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.xliffeditor.domain.model.FileFormat
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument
import `in`.hridayan.xliffeditor.domain.model.PendingImportDocument
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.SupportedLocalesProvider
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.domain.repository.AppPreferences
import `in`.hridayan.xliffeditor.domain.repository.DocumentRepository
import `in`.hridayan.xliffeditor.domain.repository.DocumentSessionRepository
import `in`.hridayan.xliffeditor.domain.translation.TranslationEngine
import `in`.hridayan.xliffeditor.domain.usecase.DetectSourceLocaleUseCase
import `in`.hridayan.xliffeditor.domain.usecase.ExportZipUseCase
import `in`.hridayan.xliffeditor.domain.usecase.FilterAndSearchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val translationEngine: TranslationEngine,
    private val appPreferences: AppPreferences,
    private val documentSessionRepository: DocumentSessionRepository,
    private val detectSourceLocaleUseCase: DetectSourceLocaleUseCase = DetectSourceLocaleUseCase(),
    private val exportZipUseCase: ExportZipUseCase = ExportZipUseCase()
) : ViewModel() {

    private val filterAndSearchUseCase = FilterAndSearchUseCase()

    private val _uiState = MutableStateFlow(
        ProjectUiState(
            themeMode = appPreferences.getThemeMode(),
            dynamicColor = appPreferences.isDynamicColor()
        )
    )
    val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val (savedProjects, savedActiveId) = documentSessionRepository.loadProjects()
            if (savedProjects.isNotEmpty()) {
                val activeId = savedActiveId ?: savedProjects.first().id
                val activeDoc = savedProjects.find { it.id == activeId } ?: savedProjects.first()
                val filtered = filterAndSearchUseCase(activeDoc.items, "", null)
                _uiState.update { current ->
                    current.copy(
                        projects = savedProjects,
                        activeProjectId = activeId,
                        filteredItems = filtered
                    )
                }
            }
        }
    }

    private fun persistProjects(projects: List<LoadedDocument>, activeId: String?) {
        viewModelScope.launch {
            documentSessionRepository.saveProjects(projects, activeId)
        }
    }

    private fun updateActiveProject(transform: (LoadedDocument) -> LoadedDocument) {
        _uiState.update { current ->
            val active = current.activeProject
            val updatedActive = transform(active)
            val updatedProjects = if (current.projects.any { it.id == active.id }) {
                current.projects.map { if (it.id == active.id) updatedActive else it }
            } else {
                listOf(updatedActive)
            }
            persistProjects(updatedProjects, updatedActive.id)
            val filtered = filterAndSearchUseCase(updatedActive.items, current.searchQuery, current.selectedStatusFilter)
            current.copy(
                projects = updatedProjects,
                activeProjectId = updatedActive.id,
                filteredItems = filtered
            )
        }
    }

    fun onThemeModeChanged(mode: AppThemeMode) {
        appPreferences.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun onDynamicColorChanged(enabled: Boolean) {
        appPreferences.setDynamicColor(enabled)
        _uiState.update { it.copy(dynamicColor = enabled) }
    }

    fun onNavigateToScreen(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun onNavigateBack() {
        _uiState.update { current ->
            when (current.currentScreen) {
                AppScreen.PROJECT_DETAIL, AppScreen.SETTINGS -> current.copy(currentScreen = AppScreen.HOME)
                AppScreen.HOME -> current
            }
        }
    }

    fun onSelectProject(projectId: String) {
        _uiState.update { current ->
            val selected = current.projects.find { it.id == projectId } ?: current.activeProject
            val filtered = filterAndSearchUseCase(selected.items, current.searchQuery, current.selectedStatusFilter)
            current.copy(
                activeProjectId = selected.id,
                filteredItems = filtered,
                activeDetailTab = DetailTab.TRANSLATE,
                currentScreen = AppScreen.PROJECT_DETAIL
            )
        }
    }

    fun onSelectDetailTab(tab: DetailTab) {
        _uiState.update { it.copy(activeDetailTab = tab) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val filtered = filterAndSearchUseCase(current.activeProject.items, query, current.selectedStatusFilter)
            current.copy(searchQuery = query, filteredItems = filtered)
        }
    }

    fun onStatusFilterSelected(status: TranslationStatus?) {
        _uiState.update { current ->
            val filtered = filterAndSearchUseCase(current.activeProject.items, current.searchQuery, status)
            current.copy(selectedStatusFilter = status, filteredItems = filtered)
        }
    }

    fun onEditItemClicked(item: SingleStringItem) {
        _uiState.update { it.copy(editingItem = item) }
    }

    fun onDismissEditor() {
        _uiState.update { it.copy(editingItem = null) }
    }

    fun onSaveTranslation(key: String, newTarget: String, newStatus: TranslationStatus) {
        val currentTargetLocale = _uiState.value.activeProject.targetLocale
        val targetVal = newTarget.ifBlank { null }
        updateActiveProject { doc ->
            val updatedItems = doc.items.map { item ->
                if (item.key == key) {
                    val updatedTranslations = if (targetVal != null) {
                        item.translationsByLocale + (currentTargetLocale to targetVal)
                    } else {
                        item.translationsByLocale - currentTargetLocale
                    }
                    val updatedStatuses = item.statusesByLocale + (currentTargetLocale to newStatus)
                    item.copy(
                        targetValue = targetVal,
                        status = newStatus,
                        translationsByLocale = updatedTranslations,
                        statusesByLocale = updatedStatuses
                    )
                } else {
                    item
                }
            }
            doc.copy(items = updatedItems)
        }
        _uiState.update { it.copy(editingItem = null) }
    }

    fun onAutoTranslateItem(key: String) {
        val current = _uiState.value
        val item = current.activeProject.items.find { it.key == key } ?: return
        if (!item.isTranslatable) {
            _uiState.update { it.copy(statusMessage = "Cannot translate: '$key' is marked translatable=false") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(translatingItemKey = key) }
            val result = translationEngine.translate(
                text = item.sourceValue,
                sourceLanguage = current.activeProject.sourceLocale,
                targetLanguage = current.activeProject.targetLocale
            )

            result.onSuccess { translatedText ->
                onSaveTranslation(
                    key = key,
                    newTarget = translatedText,
                    newStatus = TranslationStatus.TRANSLATED
                )
            }.onFailure { error ->
                Log.e(TAG, "Translation failed for key: $key", error)
                val msg = error.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: error.message?.takeIf { it.isNotBlank() }
                    ?: error.javaClass.simpleName
                _uiState.update {
                    it.copy(statusMessage = "Translation failed: $msg")
                }
            }

            _uiState.update { it.copy(translatingItemKey = null) }
        }
    }

    fun onAutoTranslateAllUntranslated() {
        val current = _uiState.value
        val untranslated = current.activeProject.items.filter {
            it.isTranslatable && (it.status == TranslationStatus.UNTRANSLATED || it.status == TranslationStatus.FUZZY)
        }
        if (untranslated.isEmpty()) return

        val total = untranslated.size

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isTranslating = true,
                    translationProgress = 0f,
                    translationProgressPercent = 0,
                    translatedBatchCount = 0,
                    totalBatchCount = total
                )
            }

            val downloadResult = translationEngine.downloadModelIfNeeded(
                sourceLanguage = current.activeProject.sourceLocale,
                targetLanguage = current.activeProject.targetLocale
            )
            downloadResult.onFailure { error ->
                Log.e(TAG, "Model download failed", error)
                val msg = error.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: error.message?.takeIf { it.isNotBlank() }
                    ?: error.javaClass.simpleName
                _uiState.update {
                    it.copy(
                        isTranslating = false,
                        translationProgress = 0f,
                        translationProgressPercent = 0,
                        statusMessage = "Model download failed: $msg"
                    )
                }
                return@launch
            }

            var successCount = 0
            for ((index, item) in untranslated.withIndex()) {
                val result = translationEngine.translate(
                    text = item.sourceValue,
                    sourceLanguage = current.activeProject.sourceLocale,
                    targetLanguage = current.activeProject.targetLocale
                )
                if (result.isSuccess) {
                    val translatedText = result.getOrThrow()
                    onSaveTranslation(
                        key = item.key,
                        newTarget = translatedText,
                        newStatus = TranslationStatus.TRANSLATED
                    )
                    successCount++
                }
                val processed = index + 1
                val progressFraction = (processed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                val percent = (progressFraction * 100).toInt()
                _uiState.update {
                    it.copy(
                        translationProgress = progressFraction,
                        translationProgressPercent = percent,
                        translatedBatchCount = processed,
                        totalBatchCount = total
                    )
                }
            }

            _uiState.update {
                it.copy(
                    isTranslating = false,
                    translationProgress = 1f,
                    translationProgressPercent = 100,
                    statusMessage = "Translated $successCount string(s) successfully"
                )
            }
        }
    }

    fun onAddNewString(key: String, sourceValue: String, comment: String?, isTranslatable: Boolean) {
        val trimmedKey = key.trim()
        if (trimmedKey.isBlank()) return

        updateActiveProject { doc ->
            val newItem = SingleStringItem(
                key = trimmedKey,
                sourceValue = sourceValue,
                targetValue = null,
                comment = comment?.takeIf { it.isNotBlank() },
                isTranslatable = isTranslatable,
                status = TranslationStatus.UNTRANSLATED
            )
            doc.copy(items = doc.items + newItem)
        }
    }

    fun onUpdateStringDetails(
        originalKey: String,
        newKey: String,
        newSource: String,
        newComment: String?,
        isTranslatable: Boolean
    ) {
        updateActiveProject { doc ->
            val updatedItems = doc.items.map { item ->
                if (item.key == originalKey) {
                    item.copy(
                        key = newKey.trim(),
                        sourceValue = newSource,
                        comment = newComment?.takeIf { it.isNotBlank() },
                        isTranslatable = isTranslatable
                    )
                } else {
                    item
                }
            }
            doc.copy(items = updatedItems)
        }
    }

    fun onDeleteString(key: String) {
        updateActiveProject { doc ->
            doc.copy(items = doc.items.filterNot { it.key == key })
        }
    }

    fun onTargetLocaleChanged(newLocale: String) {
        val oldLocale = _uiState.value.activeProject.targetLocale
        if (oldLocale == newLocale) return

        updateActiveProject { doc ->
            val updatedItems = doc.items.map { item ->
                val savedTranslations = if (item.targetValue != null) {
                    item.translationsByLocale + (oldLocale to item.targetValue)
                } else {
                    item.translationsByLocale
                }
                val savedStatuses = item.statusesByLocale + (oldLocale to item.status)

                val newTarget = savedTranslations[newLocale]
                val newStatus = savedStatuses[newLocale] ?: if (newTarget.isNullOrBlank()) {
                    TranslationStatus.UNTRANSLATED
                } else {
                    TranslationStatus.TRANSLATED
                }

                item.copy(
                    targetValue = newTarget,
                    status = newStatus,
                    translationsByLocale = savedTranslations,
                    statusesByLocale = savedStatuses
                )
            }
            doc.copy(targetLocale = newLocale, items = updatedItems)
        }
    }

    fun onSourceLocaleChanged(newLocale: String) {
        updateActiveProject { doc ->
            doc.copy(sourceLocale = newLocale)
        }
    }

    fun onAddCustomTargetLocale(localeCode: String) {
        val normalized = SupportedLocalesProvider.normalizeCustomLocale(localeCode)
        if (normalized.isBlank()) return
        _uiState.update { current ->
            val updatedLocales = listOf(normalized) + current.availableTargetLocales.filter { it != normalized }
            current.copy(availableTargetLocales = updatedLocales)
        }
        onTargetLocaleChanged(normalized)
    }

    fun onAddCustomSourceLocale(localeCode: String) {
        val normalized = SupportedLocalesProvider.normalizeCustomLocale(localeCode)
        if (normalized.isBlank()) return
        _uiState.update { current ->
            val updatedLocales = listOf(normalized) + current.availableSourceLocales.filter { it != normalized }
            current.copy(availableSourceLocales = updatedLocales)
        }
    }

    fun onOpenFile(uri: Uri, contentResolver: ContentResolver) {
        onFilePicked(uri, contentResolver)
    }

    fun onFilePicked(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val result = documentRepository.loadDocument(uri, contentResolver)
            result.onSuccess { doc ->
                val path = uri.path ?: doc.fileName
                val detected = detectSourceLocaleUseCase(path, null)
                _uiState.update { current ->
                    current.copy(
                        pendingImportDocument = PendingImportDocument(
                            uri = uri,
                            fileName = doc.fileName,
                            format = doc.format,
                            detectedSourceLocale = detected,
                            items = doc.items
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(statusMessage = "Failed to parse file: ${error.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    fun onConfirmImport(chosenSourceLocale: String) {
        val pending = _uiState.value.pendingImportDocument ?: return
        val newDoc = LoadedDocument(
            uri = pending.uri,
            fileName = pending.fileName,
            format = pending.format,
            sourceLocale = chosenSourceLocale,
            targetLocale = if (chosenSourceLocale == "VI-VN") "EN-US" else "VI-VN",
            items = pending.items
        )

        _uiState.update { current ->
            val updatedProjects = current.projects + newDoc
            persistProjects(updatedProjects, newDoc.id)
            val filtered = filterAndSearchUseCase(newDoc.items, current.searchQuery, current.selectedStatusFilter)
            current.copy(
                projects = updatedProjects,
                activeProjectId = newDoc.id,
                filteredItems = filtered,
                pendingImportDocument = null,
                activeDetailTab = DetailTab.TRANSLATE,
                currentScreen = AppScreen.PROJECT_DETAIL,
                statusMessage = "Imported ${newDoc.fileName} (${newDoc.items.size} strings)"
            )
        }
    }

    fun onCancelImport() {
        _uiState.update { it.copy(pendingImportDocument = null) }
    }

    fun onOpenExportDialog(project: LoadedDocument) {
        _uiState.update { it.copy(exportingProject = project) }
    }

    fun onDismissExportDialog() {
        _uiState.update { it.copy(exportingProject = null) }
    }

    fun onExportZip(outputStream: OutputStream, selectedLocales: Set<String>) {
        val project = _uiState.value.exportingProject ?: return
        viewModelScope.launch {
            val result = exportZipUseCase(project, selectedLocales, outputStream)
            result.onSuccess { count ->
                _uiState.update {
                    it.copy(
                        exportingProject = null,
                        statusMessage = "Exported $count language(s) to ZIP successfully"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        exportingProject = null,
                        statusMessage = "Export failed: ${error.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun onDeleteProject(projectId: String) {
        _uiState.update { current ->
            val updatedProjects = current.projects.filterNot { it.id == projectId }
            val newActiveId = if (current.activeProjectId == projectId) {
                updatedProjects.firstOrNull()?.id
            } else {
                current.activeProjectId
            }
            persistProjects(updatedProjects, newActiveId)
            val newActiveDoc = updatedProjects.find { it.id == newActiveId }
            val filtered = filterAndSearchUseCase(
                newActiveDoc?.items ?: emptyList(),
                current.searchQuery,
                current.selectedStatusFilter
            )
            current.copy(
                projects = updatedProjects,
                activeProjectId = newActiveId,
                filteredItems = filtered,
                currentScreen = if (updatedProjects.isEmpty() && current.currentScreen == AppScreen.PROJECT_DETAIL) {
                    AppScreen.HOME
                } else {
                    current.currentScreen
                },
                statusMessage = "Project removed"
            )
        }
    }

    fun onSaveFile(contentResolver: ContentResolver) {
        val current = _uiState.value
        val uri = current.activeProject.uri
        if (uri == null) {
            _uiState.update { it.copy(statusMessage = "No file path. Use Export to save to a new file.") }
            return
        }

        viewModelScope.launch {
            val result = documentRepository.saveDocument(uri, current.activeProject, contentResolver)
            result.onSuccess {
                _uiState.update { it.copy(statusMessage = "Saved ${current.activeProject.fileName} successfully") }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(statusMessage = "Save failed: ${error.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    fun onExportFile(uri: Uri, contentResolver: ContentResolver) {
        val current = _uiState.value
        viewModelScope.launch {
            val result = documentRepository.exportDocument(uri, current.activeProject, contentResolver)
            result.onSuccess {
                updateActiveProject { it.copy(uri = uri) }
                _uiState.update { it.copy(statusMessage = "Exported successfully") }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(statusMessage = "Export failed: ${error.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    fun onCreateNewFile(fileName: String, format: FileFormat, sourceLocale: String, targetLocale: String) {
        val newDoc = LoadedDocument(
            uri = null,
            fileName = fileName,
            format = format,
            sourceLocale = sourceLocale,
            targetLocale = targetLocale,
            items = emptyList()
        )
        _uiState.update { current ->
            val updatedProjects = current.projects + newDoc
            persistProjects(updatedProjects, newDoc.id)
            current.copy(
                projects = updatedProjects,
                activeProjectId = newDoc.id,
                filteredItems = emptyList(),
                activeDetailTab = DetailTab.TRANSLATE,
                currentScreen = AppScreen.PROJECT_DETAIL,
                statusMessage = "Created new ${format.displayName} file"
            )
        }
    }

    suspend fun translateDirect(text: String): String? {
        val current = _uiState.value
        val result = translationEngine.translate(
            text = text,
            sourceLanguage = current.activeProject.sourceLocale,
            targetLanguage = current.activeProject.targetLocale
        )
        return result.getOrNull()
    }

    fun onClearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    private companion object {
        private const val TAG = "ProjectViewModel"
    }
}
