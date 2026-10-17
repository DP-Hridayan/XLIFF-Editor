package `in`.hridayan.xliffeditor.presentation.screens.translate

import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus

data class TranslateUiState(
    val activeFileName: String = "strings.xliff",
    val sourceLocale: String = "EN-US",
    val targetLocale: String = "VI-VN",
    val searchQuery: String = "",
    val selectedStatusFilter: TranslationStatus? = null,
    val items: List<SingleStringItem> = emptyList(),
    val filteredItems: List<SingleStringItem> = emptyList(),
    val editingItem: SingleStringItem? = null,
    val isLoading: Boolean = false
)
