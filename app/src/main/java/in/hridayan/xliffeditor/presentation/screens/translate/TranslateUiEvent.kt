package `in`.hridayan.xliffeditor.presentation.screens.translate

import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus

sealed interface TranslateUiEvent {
    data class OnSearchQueryChanged(val query: String) : TranslateUiEvent
    data class OnStatusFilterSelected(val status: TranslationStatus?) : TranslateUiEvent
    data class OnEditItemClicked(val item: SingleStringItem) : TranslateUiEvent
    data object OnDismissEditor : TranslateUiEvent
    data class OnSaveTranslation(
        val key: String,
        val newTarget: String,
        val newStatus: TranslationStatus
    ) : TranslateUiEvent
    data object OnOpenFileRequested : TranslateUiEvent
}
