@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.xliffeditor.presentation.screens.translate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.xliffeditor.R
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.presentation.components.SearchAndFilterBar
import `in`.hridayan.xliffeditor.presentation.components.TranslateStringCard
import `in`.hridayan.xliffeditor.presentation.components.XliffTopBar
import `in`.hridayan.xliffeditor.presentation.navigation.AppBottomNavigation
import `in`.hridayan.xliffeditor.presentation.navigation.BottomNavDestination
import `in`.hridayan.xliffeditor.presentation.screens.editor.EditTranslationBottomSheet

@Composable
fun TranslateScreen(
    uiState: TranslateUiState,
    onEvent: (TranslateUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            AppBottomNavigation(
                currentDestination = BottomNavDestination.TRANSLATE,
                onDestinationSelected = { }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            XliffTopBar(
                fileName = uiState.activeFileName,
                onOpenFileClick = { onEvent(TranslateUiEvent.OnOpenFileRequested) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SearchAndFilterBar(
                query = uiState.searchQuery,
                onQueryChange = { onEvent(TranslateUiEvent.OnSearchQueryChanged(it)) },
                onFilterClick = { showFilterSheet = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = uiState.filteredItems,
                    key = { it.key }
                ) { item ->
                    TranslateStringCard(
                        item = item,
                        sourceLocale = uiState.sourceLocale,
                        targetLocale = uiState.targetLocale,
                        onEditClick = { onEvent(TranslateUiEvent.OnEditItemClicked(item)) }
                    )
                }
            }
        }
    }

    uiState.editingItem?.let { item ->
        EditTranslationBottomSheet(
            item = item,
            sourceLocale = uiState.sourceLocale,
            targetLocale = uiState.targetLocale,
            onDismiss = { onEvent(TranslateUiEvent.OnDismissEditor) },
            onSave = { newTarget, newStatus ->
                onEvent(
                    TranslateUiEvent.OnSaveTranslation(
                        key = item.key,
                        newTarget = newTarget,
                        newStatus = newStatus
                    )
                )
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

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedStatusFilter == null,
                        onClick = {
                            onEvent(TranslateUiEvent.OnStatusFilterSelected(null))
                            showFilterSheet = false
                        },
                        label = { Text(text = "All") }
                    )
                    FilterChip(
                        selected = uiState.selectedStatusFilter == TranslationStatus.TRANSLATED,
                        onClick = {
                            onEvent(TranslateUiEvent.OnStatusFilterSelected(TranslationStatus.TRANSLATED))
                            showFilterSheet = false
                        },
                        label = { Text(text = stringResource(R.string.translated)) }
                    )
                    FilterChip(
                        selected = uiState.selectedStatusFilter == TranslationStatus.FUZZY,
                        onClick = {
                            onEvent(TranslateUiEvent.OnStatusFilterSelected(TranslationStatus.FUZZY))
                            showFilterSheet = false
                        },
                        label = { Text(text = stringResource(R.string.fuzzy_needs_review)) }
                    )
                    FilterChip(
                        selected = uiState.selectedStatusFilter == TranslationStatus.UNTRANSLATED,
                        onClick = {
                            onEvent(TranslateUiEvent.OnStatusFilterSelected(TranslationStatus.UNTRANSLATED))
                            showFilterSheet = false
                        },
                        label = { Text(text = stringResource(R.string.untranslated)) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
