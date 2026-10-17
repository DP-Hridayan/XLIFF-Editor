package `in`.hridayan.xliffeditor.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
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
import `in`.hridayan.xliffeditor.domain.model.PendingImportDocument

private val SheetPadding: Dp = 24.dp
private val SheetCornerRadius: Dp = 28.dp
private val HeaderSpacing: Dp = 12.dp
private val TitleFontSize = 20.sp
private val SectionSpacing: Dp = 16.dp
private val CardPadding: Dp = 16.dp
private val CardContentSpacing: Dp = 8.dp
private val BadgeHorizontalSpacing: Dp = 8.dp
private val BadgeVerticalSpacing: Dp = 6.dp
private val SourceLangSpacing: Dp = 8.dp
private val ChipSpacing: Dp = 8.dp
private val ButtonRowSpacing: Dp = 24.dp
private val ActionButtonSpacing: Dp = 12.dp
private const val ButtonCornerPercent = 100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportConfirmationBottomSheet(
    pendingDoc: PendingImportDocument,
    availableLocales: List<String>,
    onConfirm: (chosenSourceLocale: String) -> Unit,
    onDismiss: () -> Unit,
    onAddCustomLocale: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    var selectedSourceLocale by remember(pendingDoc) {
        mutableStateOf(pendingDoc.detectedSourceLocale)
    }
    var showAddLocaleSheet by remember { mutableStateOf(false) }

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
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.import_file),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = TitleFontSize
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(SectionSpacing))

            CustomCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CardCornerShape.SINGLE_CARD,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                clickable = false
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(CardPadding)
                ) {
                    Text(
                        text = pendingDoc.fileName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(CardContentSpacing))

                    val translatableCount = remember(pendingDoc.items) {
                        pendingDoc.items.count { it.isTranslatable }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(BadgeHorizontalSpacing),
                        verticalArrangement = Arrangement.spacedBy(BadgeVerticalSpacing),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LocaleBadge(locale = pendingDoc.format.displayName)
                        LocaleBadge(
                            locale = stringResource(
                                R.string.strings_count_format,
                                pendingDoc.items.size
                            )
                        )
                        LocaleBadge(
                            locale = stringResource(
                                R.string.translatable_count_format,
                                translatableCount
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SectionSpacing))

            Text(
                text = stringResource(R.string.source_language),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${stringResource(R.string.detected_source)}: ${pendingDoc.detectedSourceLocale}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(SourceLangSpacing))

            val displayedLocales = remember(availableLocales, pendingDoc.detectedSourceLocale) {
                if (availableLocales.contains(pendingDoc.detectedSourceLocale)) {
                    availableLocales
                } else {
                    listOf(pendingDoc.detectedSourceLocale) + availableLocales
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(ChipSpacing)
            ) {
                for (locale in displayedLocales) {
                    FilterChip(
                        selected = selectedSourceLocale == locale,
                        onClick = withHaptic { selectedSourceLocale = locale },
                        label = { Text(text = locale) },
                        leadingIcon = if (selectedSourceLocale == locale) {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else null
                    )
                }

                FilterChip(
                    selected = false,
                    onClick = withHaptic { showAddLocaleSheet = true },
                    label = {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.add_language),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            if (showAddLocaleSheet) {
                AddLocaleBottomSheet(
                    onLocaleSelected = { code ->
                        onAddCustomLocale(code)
                        selectedSourceLocale = code
                        showAddLocaleSheet = false
                    },
                    onDismiss = { showAddLocaleSheet = false }
                )
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

                Button(
                    onClick = withHaptic { onConfirm(selectedSourceLocale) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(ButtonCornerPercent)
                ) {
                    AutoResizeableText(text = stringResource(R.string.confirm))
                }
            }
        }
    }
}
