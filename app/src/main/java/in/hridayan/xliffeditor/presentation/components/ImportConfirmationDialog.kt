package `in`.hridayan.xliffeditor.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.xliffeditor.R
import `in`.hridayan.xliffeditor.core.ui.components.card.CustomCard
import `in`.hridayan.xliffeditor.core.ui.components.haptic.withHaptic
import `in`.hridayan.xliffeditor.core.ui.components.text.AutoResizeableText
import `in`.hridayan.xliffeditor.core.ui.theme.CardCornerShape
import `in`.hridayan.xliffeditor.domain.model.PendingImportDocument

private val DialogMinWidth = 300.dp
private val DialogMaxWidth = 400.dp
private val DialogPadding = 24.dp
private val DialogElevation = 8.dp
private val HeaderSpacing = 12.dp
private val TitleFontSize = 20.sp
private val SectionSpacing = 16.dp
private val CardPadding = 16.dp
private val CardContentSpacing = 8.dp
private val BadgeHorizontalSpacing = 8.dp
private val BadgeVerticalSpacing = 6.dp
private val SourceLangSpacing = 8.dp
private val ChipSpacing = 8.dp
private val ButtonRowSpacing = 24.dp
private val ActionButtonSpacing = 12.dp
private const val ButtonCornerPercent = 100

@Composable
fun ImportConfirmationDialog(
    pendingDoc: PendingImportDocument,
    availableLocales: List<String>,
    onConfirm: (chosenSourceLocale: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSourceLocale by remember(pendingDoc) {
        mutableStateOf(pendingDoc.detectedSourceLocale)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = DialogElevation,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = modifier.widthIn(min = DialogMinWidth, max = DialogMaxWidth)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DialogPadding)
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
}
