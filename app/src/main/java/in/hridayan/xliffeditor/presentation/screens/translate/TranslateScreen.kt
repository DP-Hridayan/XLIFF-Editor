@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.xliffeditor.presentation.screens.translate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.xliffeditor.R
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.presentation.components.FilterOptionItem
import `in`.hridayan.xliffeditor.presentation.components.SearchAndFilterBar
import `in`.hridayan.xliffeditor.presentation.components.TranslateStringCard
import `in`.hridayan.xliffeditor.presentation.components.XliffTopBar
import `in`.hridayan.xliffeditor.presentation.screens.editor.EditTranslationBottomSheet
import `in`.hridayan.xliffeditor.presentation.viewmodel.ProjectUiState

@Composable
fun TranslateScreen(
    uiState: ProjectUiState,
    onOpenFileClick: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onStatusFilterSelected: (TranslationStatus?) -> Unit,
    onEditItemClicked: (SingleStringItem) -> Unit,
    onDismissEditor: () -> Unit,
    onSaveTranslation: (key: String, newTarget: String, newStatus: TranslationStatus) -> Unit,
    onAutoTranslateItem: (key: String) -> Unit,
    onAutoTranslateAllUntranslated: () -> Unit,
    onTranslateSingleItemDirect: suspend (text: String) -> String?,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        XliffTopBar(
            fileName = uiState.document.fileName,
            onOpenFileClick = onOpenFileClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        SearchAndFilterBar(
            query = uiState.searchQuery,
            onQueryChange = onSearchQueryChanged,
            onFilterClick = { showFilterSheet = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        val needsTranslationCount = uiState.untranslatedCount + uiState.fuzzyCount
        if (needsTranslationCount > 0) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = !uiState.isTranslating,
                        onClick = onAutoTranslateAllUntranslated
                    ),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (uiState.isTranslating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = if (uiState.isTranslating) {
                                stringResource(R.string.translating)
                            } else {
                                "${stringResource(R.string.auto_translate_all)} ($needsTranslationCount)"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Text(
                        text = "ML Kit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = uiState.filteredItems,
                key = { it.key }
            ) { item ->
                TranslateStringCard(
                    item = item,
                    sourceLocale = uiState.document.sourceLocale,
                    targetLocale = uiState.document.targetLocale,
                    onEditClick = { onEditItemClicked(item) },
                    onAutoTranslateClick = { onAutoTranslateItem(item.key) },
                    isTranslating = uiState.translatingItemKey == item.key
                )
            }
        }
    }

    uiState.editingItem?.let { item ->
        EditTranslationBottomSheet(
            item = item,
            sourceLocale = uiState.document.sourceLocale,
            targetLocale = uiState.document.targetLocale,
            onDismiss = onDismissEditor,
            onSave = { newTarget, newStatus ->
                onSaveTranslation(item.key, newTarget, newStatus)
            },
            onAutoTranslate = {
                onTranslateSingleItemDirect(item.sourceValue)
            }
        )
    }

    if (showFilterSheet) {
        val filterSheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
        )
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = filterSheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Filter by Status",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                FilterOptionItem(
                    title = "All Strings",
                    selected = uiState.selectedStatusFilter == null,
                    onClick = {
                        onStatusFilterSelected(null)
                        showFilterSheet = false
                    }
                )
                FilterOptionItem(
                    title = stringResource(R.string.translated),
                    selected = uiState.selectedStatusFilter == TranslationStatus.TRANSLATED,
                    onClick = {
                        onStatusFilterSelected(TranslationStatus.TRANSLATED)
                        showFilterSheet = false
                    }
                )
                FilterOptionItem(
                    title = stringResource(R.string.fuzzy_needs_review),
                    selected = uiState.selectedStatusFilter == TranslationStatus.FUZZY,
                    onClick = {
                        onStatusFilterSelected(TranslationStatus.FUZZY)
                        showFilterSheet = false
                    }
                )
                FilterOptionItem(
                    title = stringResource(R.string.untranslated),
                    selected = uiState.selectedStatusFilter == TranslationStatus.UNTRANSLATED,
                    onClick = {
                        onStatusFilterSelected(TranslationStatus.UNTRANSLATED)
                        showFilterSheet = false
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
