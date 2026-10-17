package `in`.hridayan.xliffeditor.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.xliffeditor.R
import `in`.hridayan.xliffeditor.core.ui.components.card.CustomCard
import `in`.hridayan.xliffeditor.core.ui.components.haptic.withHaptic
import `in`.hridayan.xliffeditor.core.ui.components.text.AutoResizeableText
import `in`.hridayan.xliffeditor.core.ui.theme.CardCornerShape
import `in`.hridayan.xliffeditor.domain.model.SupportedLocaleItem
import `in`.hridayan.xliffeditor.domain.model.SupportedLocalesProvider

private val SheetPadding: Dp = 24.dp
private val SheetCornerRadius: Dp = 28.dp
private val HeaderSpacing: Dp = 12.dp
private val TitleFontSize = 20.sp
private val SectionSpacing: Dp = 16.dp
private val FieldCornerRadius: Dp = 12.dp
private val SearchFieldHeight: Dp = 8.dp
private val LanguageListMaxHeight: Dp = 320.dp
private val ItemRowPaddingHorizontal: Dp = 14.dp
private val ItemRowPaddingVertical: Dp = 12.dp
private val ItemSpacing: Dp = 12.dp
private val ActionButtonSpacing: Dp = 12.dp
private val ButtonRowSpacing: Dp = 16.dp
private const val ButtonCornerPercent = 100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocaleBottomSheet(
    onLocaleSelected: (code: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    var searchQuery by remember { mutableStateOf("") }
    val allLocales = remember { SupportedLocalesProvider.getAllSupportedLocales() }

    val filteredLocales = remember(searchQuery, allLocales) {
        if (searchQuery.isBlank()) {
            allLocales
        } else {
            val query = searchQuery.trim().lowercase()
            allLocales.filter {
                it.displayName.lowercase().contains(query) ||
                    it.code.lowercase().contains(query) ||
                    it.languageCode.lowercase().contains(query)
            }
        }
    }

    val normalizedCustomCode = remember(searchQuery) {
        SupportedLocalesProvider.normalizeCustomLocale(searchQuery)
    }

    val isCustomMlKitSupported = remember(normalizedCustomCode) {
        if (normalizedCustomCode.isNotBlank()) {
            SupportedLocalesProvider.isMlKitSupported(normalizedCustomCode)
        } else {
            false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        shape = RoundedCornerShape(topStart = SheetCornerRadius, topEnd = SheetCornerRadius),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SheetPadding)
                .padding(bottom = SheetPadding)
                .navigationBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HeaderSpacing)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.add_language),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = TitleFontSize
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(SectionSpacing))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(text = stringResource(R.string.search_languages_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = withHaptic { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = null
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(FieldCornerRadius),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(SearchFieldHeight))

            if (normalizedCustomCode.isNotBlank() && filteredLocales.none { it.code.equals(normalizedCustomCode, ignoreCase = true) }) {
                CustomCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = CardCornerShape.SINGLE_CARD,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    onClick = withHaptic {
                        onLocaleSelected(normalizedCustomCode)
                        onDismiss()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ItemRowPaddingHorizontal, vertical = ItemRowPaddingVertical),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ItemSpacing)
                        ) {
                            LocaleBadge(locale = normalizedCustomCode)
                            Text(
                                text = "${stringResource(R.string.add)} $normalizedCustomCode",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (isCustomMlKitSupported) {
                            LocaleBadge(
                                locale = stringResource(R.string.mlkit_supported),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = LanguageListMaxHeight),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = filteredLocales,
                    key = { it.code }
                ) { localeItem ->
                    Surface(
                        shape = RoundedCornerShape(FieldCornerRadius),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = withHaptic {
                                    onLocaleSelected(localeItem.code)
                                    onDismiss()
                                }
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = ItemRowPaddingHorizontal, vertical = ItemRowPaddingVertical),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(ItemSpacing)
                            ) {
                                LocaleBadge(locale = localeItem.code)
                                Text(
                                    text = localeItem.displayName,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(ButtonRowSpacing))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ActionButtonSpacing)
            ) {
                OutlinedButton(
                    onClick = withHaptic { onDismiss() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(ButtonCornerPercent)
                ) {
                    AutoResizeableText(text = stringResource(R.string.cancel))
                }

                if (normalizedCustomCode.isNotBlank()) {
                    Button(
                        onClick = withHaptic {
                            onLocaleSelected(normalizedCustomCode)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(ButtonCornerPercent)
                    ) {
                        AutoResizeableText(text = stringResource(R.string.add))
                    }
                }
            }
        }
    }
}
