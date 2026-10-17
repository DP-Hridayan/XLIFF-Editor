package `in`.hridayan.xliffeditor.presentation.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.xliffeditor.domain.model.FileFormat
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.domain.repository.DocumentRepository
import `in`.hridayan.xliffeditor.domain.translation.TranslationEngine
import `in`.hridayan.xliffeditor.domain.usecase.FilterAndSearchUseCase
import `in`.hridayan.xliffeditor.domain.repository.AppPreferences
import `in`.hridayan.xliffeditor.presentation.navigation.BottomNavDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val translationEngine: TranslationEngine,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val filterAndSearchUseCase = FilterAndSearchUseCase()

    private val _uiState = MutableStateFlow(
        ProjectUiState(
            themeMode = appPreferences.getThemeMode(),
            dynamicColor = appPreferences.isDynamicColor()
        ).let { initial ->
            initial.copy(filteredItems = initial.document.items)
        }
    )
    val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

    fun onThemeModeChanged(mode: AppThemeMode) {
        appPreferences.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun onDynamicColorChanged(enabled: Boolean) {
        appPreferences.setDynamicColor(enabled)
        _uiState.update { it.copy(dynamicColor = enabled) }
    }

    fun onTabSelected(destination: BottomNavDestination) {
        _uiState.update { it.copy(currentDestination = destination) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val filtered = filterAndSearchUseCase(current.document.items, query, current.selectedStatusFilter)
            current.copy(searchQuery = query, filteredItems = filtered)
        }
    }

    fun onStatusFilterSelected(status: TranslationStatus?) {
        _uiState.update { current ->
            val filtered = filterAndSearchUseCase(current.document.items, current.searchQuery, status)
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
        _uiState.update { current ->
            val updatedItems = current.document.items.map { item ->
                if (item.key == key) {
                    item.copy(
                        targetValue = newTarget.ifBlank { null },
                        status = newStatus
                    )
                } else {
                    item
                }
            }
            val updatedDoc = current.document.copy(items = updatedItems)
            val filtered = filterAndSearchUseCase(updatedItems, current.searchQuery, current.selectedStatusFilter)
            current.copy(
                document = updatedDoc,
                filteredItems = filtered,
                editingItem = null
            )
        }
    }

    fun onAutoTranslateItem(key: String) {
        val current = _uiState.value
        val item = current.document.items.find { it.key == key } ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(translatingItemKey = key) }
            val result = translationEngine.translate(
                text = item.sourceValue,
                sourceLanguage = current.document.sourceLocale,
                targetLanguage = current.document.targetLocale
            )

            result.onSuccess { translatedText ->
                onSaveTranslation(
                    key = key,
                    newTarget = translatedText,
                    newStatus = TranslationStatus.TRANSLATED
                )
            }.onFailure { error ->
                Log.e("ProjectViewModel", "Translation failed for key: $key", error)
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
        val untranslated = current.document.items.filter {
            it.status == TranslationStatus.UNTRANSLATED || it.status == TranslationStatus.FUZZY
        }
        if (untranslated.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isTranslating = true) }

            val downloadResult = translationEngine.downloadModelIfNeeded(
                sourceLanguage = current.document.sourceLocale,
                targetLanguage = current.document.targetLocale
            )
            downloadResult.onFailure { error ->
                Log.e("ProjectViewModel", "Model download failed", error)
                val msg = error.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: error.message?.takeIf { it.isNotBlank() }
                    ?: error.javaClass.simpleName
                _uiState.update {
                    it.copy(
                        isTranslating = false,
                        statusMessage = "Model download failed: $msg"
                    )
                }
                return@launch
            }

            var successCount = 0
            for (item in untranslated) {
                val result = translationEngine.translate(
                    text = item.sourceValue,
                    sourceLanguage = current.document.sourceLocale,
                    targetLanguage = current.document.targetLocale
                )
                result.onSuccess { translatedText ->
                    onSaveTranslation(
                        key = item.key,
                        newTarget = translatedText,
                        newStatus = TranslationStatus.TRANSLATED
                    )
                    successCount++
                }
            }

            _uiState.update {
                it.copy(
                    isTranslating = false,
                    statusMessage = "Translated $successCount string(s) successfully"
                )
            }
        }
    }

    fun onAddNewString(key: String, sourceValue: String, comment: String?, isTranslatable: Boolean) {
        val trimmedKey = key.trim()
        if (trimmedKey.isBlank()) return

        _uiState.update { current ->
            if (current.document.items.any { it.key == trimmedKey }) {
                current.copy(statusMessage = "Key '$trimmedKey' already exists")
            } else {
                val newItem = SingleStringItem(
                    key = trimmedKey,
                    sourceValue = sourceValue,
                    targetValue = null,
                    comment = comment?.ifBlank { null },
                    isTranslatable = isTranslatable,
                    status = TranslationStatus.UNTRANSLATED
                )
                val updatedItems = current.document.items + newItem
                val updatedDoc = current.document.copy(items = updatedItems)
                val filtered = filterAndSearchUseCase(updatedItems, current.searchQuery, current.selectedStatusFilter)
                current.copy(
                    document = updatedDoc,
                    filteredItems = filtered,
                    statusMessage = "Added string '$trimmedKey'"
                )
            }
        }
    }

    fun onDeleteString(key: String) {
        _uiState.update { current ->
            val updatedItems = current.document.items.filterNot { it.key == key }
            val updatedDoc = current.document.copy(items = updatedItems)
            val filtered = filterAndSearchUseCase(updatedItems, current.searchQuery, current.selectedStatusFilter)
            current.copy(
                document = updatedDoc,
                filteredItems = filtered,
                statusMessage = "Deleted string '$key'"
            )
        }
    }

    fun onUpdateStringDetails(oldKey: String, newKey: String, newSource: String, newComment: String?, isTranslatable: Boolean) {
        val trimmedKey = newKey.trim()
        if (trimmedKey.isBlank()) return

        _uiState.update { current ->
            val updatedItems = current.document.items.map { item ->
                if (item.key == oldKey) {
                    item.copy(
                        key = trimmedKey,
                        sourceValue = newSource,
                        comment = newComment?.ifBlank { null },
                        isTranslatable = isTranslatable
                    )
                } else {
                    item
                }
            }
            val updatedDoc = current.document.copy(items = updatedItems)
            val filtered = filterAndSearchUseCase(updatedItems, current.searchQuery, current.selectedStatusFilter)
            current.copy(document = updatedDoc, filteredItems = filtered)
        }
    }

    fun onTargetLocaleChanged(newLocale: String) {
        _uiState.update { current ->
            val updatedDoc = current.document.copy(targetLocale = newLocale)
            current.copy(document = updatedDoc)
        }
    }

    fun onSourceLocaleChanged(newLocale: String) {
        _uiState.update { current ->
            val updatedDoc = current.document.copy(sourceLocale = newLocale)
            current.copy(document = updatedDoc)
        }
    }

    fun onOpenFile(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val result = documentRepository.loadDocument(uri, contentResolver)
            result.onSuccess { doc ->
                _uiState.update { current ->
                    val filtered = filterAndSearchUseCase(doc.items, current.searchQuery, current.selectedStatusFilter)
                    current.copy(
                        document = doc,
                        filteredItems = filtered,
                        statusMessage = "Loaded ${doc.fileName} (${doc.items.size} strings)"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(statusMessage = "Failed to open file: ${error.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    fun onSaveFile(contentResolver: ContentResolver) {
        val current = _uiState.value
        val uri = current.document.uri
        if (uri == null) {
            _uiState.update { it.copy(statusMessage = "No file path. Use Export to save to a new file.") }
            return
        }

        viewModelScope.launch {
            val result = documentRepository.saveDocument(uri, current.document, contentResolver)
            result.onSuccess {
                _uiState.update { it.copy(statusMessage = "Saved ${current.document.fileName} successfully") }
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
            val result = documentRepository.exportDocument(uri, current.document, contentResolver)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        document = it.document.copy(uri = uri),
                        statusMessage = "Exported successfully"
                    )
                }
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
        _uiState.update {
            it.copy(
                document = newDoc,
                filteredItems = emptyList(),
                statusMessage = "Created new ${format.displayName} file"
            )
        }
    }

    suspend fun translateDirect(text: String): String? {
        val current = _uiState.value
        val result = translationEngine.translate(
            text = text,
            sourceLanguage = current.document.sourceLocale,
            targetLanguage = current.document.targetLocale
        )
        return result.getOrNull()
    }

    fun onClearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
