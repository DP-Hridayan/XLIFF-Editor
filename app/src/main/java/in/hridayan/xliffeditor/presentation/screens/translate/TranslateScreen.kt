@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.xliffeditor.presentation.screens.translate

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.xliffeditor.R
import `in`.hridayan.xliffeditor.core.ui.components.haptic.withHaptic
import `in`.hridayan.xliffeditor.core.ui.theme.AshellYouAnimationSpecs
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.presentation.components.AddLocaleBottomSheet
import `in`.hridayan.xliffeditor.presentation.components.FilterOptionItem
import `in`.hridayan.xliffeditor.presentation.components.SearchAndFilterBar
import `in`.hridayan.xliffeditor.presentation.components.TranslateStringCard
import `in`.hridayan.xliffeditor.presentation.screens.editor.EditTranslationBottomSheet
import `in`.hridayan.xliffeditor.presentation.viewmodel.ProjectUiState
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun TranslateScreen(
    uiState: ProjectUiState,
    onSearchQueryChanged: (String) -> Unit,
    onStatusFilterSelected: (TranslationStatus?) -> Unit,
    onEditItemClicked: (SingleStringItem) -> Unit,
    onDismissEditor: () -> Unit,
    onSaveTranslation: (key: String, newTarget: String, newStatus: TranslationStatus) -> Unit,
    onAutoTranslateItem: (key: String) -> Unit,
    onAutoTranslateAllUntranslated: () -> Unit,
    onTranslateSingleItemDirect: suspend (text: String) -> String?,
    onTargetLocaleChanged: (String) -> Unit,
    onAddCustomTargetLocale: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    var showAddLocaleSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        SearchAndFilterBar(
            query = uiState.searchQuery,
            onQueryChange = onSearchQueryChanged,
            onFilterClick = { showFilterSheet = true }
        )

        Spacer(modifier = Modifier.height(10.dp))

        val filteredTargetLocales = remember(uiState.availableTargetLocales, uiState.document.sourceLocale) {
            uiState.availableTargetLocales.filterNot {
                it.equals(uiState.document.sourceLocale, ignoreCase = true)
            }
        }

        TargetLocaleChipSelector(
            selectedLocale = uiState.document.targetLocale,
            locales = filteredTargetLocales,
            onLocaleSelected = onTargetLocaleChanged,
            onAddLocaleClick = { showAddLocaleSheet = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        val needsTranslationCount = uiState.untranslatedCount + uiState.fuzzyCount
        if (needsTranslationCount > 0 || uiState.isTranslating) {
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    strokeWidth = 2.5.dp,
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
                                    if (uiState.totalBatchCount > 0) {
                                        "Translating ${uiState.translatedBatchCount}/${uiState.totalBatchCount}"
                                    } else {
                                        stringResource(R.string.translating)
                                    }
                                } else {
                                    "${stringResource(R.string.auto_translate_all)} ($needsTranslationCount)"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        if (uiState.isTranslating) {
                            Text(
                                text = "${uiState.translationProgressPercent}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "ML Kit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    if (uiState.isTranslating) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { uiState.translationProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (uiState.filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Translate,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (uiState.document.items.isEmpty()) "No strings loaded" else "No matching strings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (uiState.document.items.isEmpty()) {
                            "No strings found in this file."
                        } else {
                            "Try clearing your search query or changing status filter."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
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
            sheetGesturesEnabled = false,
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

    if (showAddLocaleSheet) {
        AddLocaleBottomSheet(
            onLocaleSelected = { code ->
                onAddCustomTargetLocale(code)
                showAddLocaleSheet = false
            },
            onDismiss = { showAddLocaleSheet = false }
        )
    }
}

private const val CHIP_CONTAINER_CORNER_PERCENT = 50
private const val CHIP_CORNER_PERCENT_ROUNDED = 50
private const val CHIP_CORNER_PERCENT_DEFAULT = 25
private const val SCROLL_FADE_WIDTH_DP = 36
private const val CHIP_ROW_PADDING_DP = 12
private const val CHIP_ROW_SPACING_DP = 6
private const val CHIP_HORIZONTAL_PADDING_DP = 14
private const val CHIP_VERTICAL_PADDING_DP = 8
private const val ADD_CHIP_ICON_SIZE_DP = 16
private const val ADD_CHIP_HORIZONTAL_PADDING_DP = 12

@Composable
private fun TargetLocaleChipSelector(
    selectedLocale: String,
    locales: List<String>,
    onLocaleSelected: (String) -> Unit,
    onAddLocaleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    LaunchedEffect(selectedLocale) {
        val selectedIndex = locales.indexOf(selectedLocale)
        if (selectedIndex >= 0) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    LaunchedEffect(listState) {
        var previousStart = true
        var previousEnd = false
        var firstEmission = true

        snapshotFlow {
            Pair(
                !listState.canScrollBackward,
                !listState.canScrollForward
            )
        }
            .distinctUntilChanged()
            .collect { (atStart, atEnd) ->
                if (firstEmission) {
                    previousStart = atStart
                    previousEnd = atEnd
                    firstEmission = false
                    return@collect
                }

                val reachedStart = atStart && !previousStart
                val reachedEnd = atEnd && !previousEnd

                if (reachedStart || reachedEnd) {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                }

                previousStart = atStart
                previousEnd = atEnd
            }
    }

    val leftFadeAlpha by animateFloatAsState(
        targetValue = if (listState.canScrollBackward) 1f else 0f,
        label = "leftFadeAlpha"
    )
    val rightFadeAlpha by animateFloatAsState(
        targetValue = if (listState.canScrollForward) 1f else 0f,
        label = "rightFadeAlpha"
    )

    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceContainer

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CHIP_CONTAINER_CORNER_PERCENT))
            .background(surfaceContainerColor)
            .drawWithContent {
                drawContent()

                val fadeWidthPx = SCROLL_FADE_WIDTH_DP.dp.toPx()

                if (leftFadeAlpha > 0f) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(surfaceContainerColor, Color.Transparent),
                            startX = 0f,
                            endX = fadeWidthPx
                        ),
                        size = Size(fadeWidthPx, size.height),
                        alpha = leftFadeAlpha
                    )
                }

                if (rightFadeAlpha > 0f) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, surfaceContainerColor),
                            startX = size.width - fadeWidthPx,
                            endX = size.width
                        ),
                        topLeft = Offset(size.width - fadeWidthPx, 0f),
                        size = Size(fadeWidthPx, size.height),
                        alpha = rightFadeAlpha
                    )
                }
            }
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(all = CHIP_ROW_PADDING_DP.dp),
            horizontalArrangement = Arrangement.spacedBy(CHIP_ROW_SPACING_DP.dp)
        ) {
            itemsIndexed(locales, key = { _, locale -> locale }) { index, locale ->
                val isSelected = selectedLocale == locale
                TargetLocaleChip(
                    locale = locale,
                    isSelected = isSelected,
                    isFirst = index == 0,
                    isLast = false,
                    onClick = { onLocaleSelected(locale) }
                )
            }
            item(key = "add_locale_chip") {
                AddLocaleChip(onClick = onAddLocaleClick)
            }
        }
    }
}

@Composable
private fun AddLocaleChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(CHIP_CORNER_PERCENT_ROUNDED))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(onClick = withHaptic { onClick() })
            .padding(
                horizontal = ADD_CHIP_HORIZONTAL_PADDING_DP.dp,
                vertical = CHIP_VERTICAL_PADDING_DP.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = stringResource(R.string.add_language),
            modifier = Modifier.size(ADD_CHIP_ICON_SIZE_DP.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TargetLocaleChip(
    locale: String,
    isSelected: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val startCornerPercent by animateIntAsState(
        targetValue = if (isSelected || isFirst) {
            CHIP_CORNER_PERCENT_ROUNDED
        } else {
            CHIP_CORNER_PERCENT_DEFAULT
        },
        animationSpec = AshellYouAnimationSpecs.springInt,
        label = "chipStartCornerPercent"
    )

    val endCornerPercent by animateIntAsState(
        targetValue = if (isSelected || isLast) {
            CHIP_CORNER_PERCENT_ROUNDED
        } else {
            CHIP_CORNER_PERCENT_DEFAULT
        },
        animationSpec = AshellYouAnimationSpecs.springInt,
        label = "chipEndCornerPercent"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        label = "chipContainerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "chipContentColor"
    )

    Box(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStartPercent = startCornerPercent,
                    topEndPercent = endCornerPercent,
                    bottomEndPercent = endCornerPercent,
                    bottomStartPercent = startCornerPercent
                )
            )
            .background(containerColor)
            .clickable(onClick = withHaptic { onClick() })
            .padding(
                horizontal = CHIP_HORIZONTAL_PADDING_DP.dp,
                vertical = CHIP_VERTICAL_PADDING_DP.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatLocaleChipLabel(locale),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            ),
            color = contentColor
        )
    }
}

private fun formatLocaleChipLabel(locale: String): String {
    val parts = locale.split("-", "_")
    return if (parts.size == 2 && parts[0].equals(parts[1], ignoreCase = true)) {
        parts[0].uppercase()
    } else {
        locale.uppercase()
    }
}
