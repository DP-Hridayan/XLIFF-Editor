package `in`.hridayan.xliffeditor.presentation.screens.translate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.domain.usecase.FilterAndSearchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslateViewModel @Inject constructor() : ViewModel() {

    private val filterAndSearchUseCase = FilterAndSearchUseCase()

    private val defaultInitialItems = listOf(
        SingleStringItem(
            key = "app_title",
            sourceValue = "My Application",
            targetValue = "Ứng dụng của tôi",
            status = TranslationStatus.TRANSLATED
        ),
        SingleStringItem(
            key = "welcome_msg",
            sourceValue = "Welcome to our app, users!",
            targetValue = "Chào mừng đến với ứng dụng của chúng tôi!",
            status = TranslationStatus.FUZZY
        ),
        SingleStringItem(
            key = "login_button",
            sourceValue = "Log In",
            targetValue = null,
            status = TranslationStatus.UNTRANSLATED
        )
    )

    private val _uiState = MutableStateFlow(
        TranslateUiState(
            items = defaultInitialItems,
            filteredItems = defaultInitialItems
        )
    )
    val uiState: StateFlow<TranslateUiState> = _uiState.asStateFlow()

    fun onEvent(event: TranslateUiEvent) {
        when (event) {
            is TranslateUiEvent.OnSearchQueryChanged -> {
                _uiState.update { current ->
                    val filtered = applyFilter(current.items, event.query, current.selectedStatusFilter)
                    current.copy(searchQuery = event.query, filteredItems = filtered)
                }
            }
            is TranslateUiEvent.OnStatusFilterSelected -> {
                _uiState.update { current ->
                    val filtered = applyFilter(current.items, current.searchQuery, event.status)
                    current.copy(selectedStatusFilter = event.status, filteredItems = filtered)
                }
            }
            is TranslateUiEvent.OnEditItemClicked -> {
                _uiState.update { it.copy(editingItem = event.item) }
            }
            is TranslateUiEvent.OnDismissEditor -> {
                _uiState.update { it.copy(editingItem = null) }
            }
            is TranslateUiEvent.OnSaveTranslation -> {
                _uiState.update { current ->
                    val updatedItems = current.items.map { item ->
                        if (item.key == event.key) {
                            item.copy(
                                targetValue = event.newTarget.ifBlank { null },
                                status = event.newStatus
                            )
                        } else {
                            item
                        }
                    }
                    val filtered = applyFilter(updatedItems, current.searchQuery, current.selectedStatusFilter)
                    current.copy(
                        items = updatedItems,
                        filteredItems = filtered,
                        editingItem = null
                    )
                }
            }
            is TranslateUiEvent.OnOpenFileRequested -> {
            }
        }
    }

    private fun applyFilter(
        items: List<SingleStringItem>,
        query: String,
        statusFilter: TranslationStatus?
    ): List<SingleStringItem> {
        val result = filterAndSearchUseCase(items, query, statusFilter)
        return result.filterIsInstance<SingleStringItem>()
    }
}
