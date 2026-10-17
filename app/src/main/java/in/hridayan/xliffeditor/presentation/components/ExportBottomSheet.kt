package `in`.hridayan.xliffeditor.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.xliffeditor.R
import `in`.hridayan.xliffeditor.core.ui.components.card.CustomCard
import `in`.hridayan.xliffeditor.core.ui.components.haptic.withHaptic
import `in`.hridayan.xliffeditor.core.ui.theme.CardCornerShape
import `in`.hridayan.xliffeditor.core.ui.theme.CustomCardShape
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus

private val SheetPadding: Dp = 24.dp
private val HeaderSpacing: Dp = 12.dp
private val SubtitleSpacing: Dp = 6.dp
private val SectionSpacing: Dp = 16.dp
private val ListMaxHeight: Dp = 320.dp
private val ListClipRadius: Dp = 16.dp
private val CardVerticalPadding: Dp = 1.dp
private val CardHorizontalPadding: Dp = 15.dp
private val CardContentVerticalPadding: Dp = 10.dp
private val ItemRowSpacing: Dp = 12.dp
private val IconSize: Dp = 20.dp
private val ActionButtonSpacing: Dp = 12.dp

private const val PILL_CORNER_PERCENT = 50

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportBottomSheet(
    project: LoadedDocument,
    availableLocales: List<String>,
    onConfirmExport: (selectedLocales: Set<String>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    var selectedLocales by remember {
        val initialSelected = if (project.targetLocale.isNotBlank()) {
            setOf(project.targetLocale)
        } else {
            setOf(availableLocales.firstOrNull() ?: "VI-VN")
        }
        mutableStateOf(initialSelected)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SheetPadding)
                .padding(bottom = SheetPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HeaderSpacing)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderZip,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.export_zip),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(SubtitleSpacing))

            Text(
                text = "${project.fileName} • ${project.items.size} strings",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(SectionSpacing))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.target_languages),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row {
                    TextButton(
                        onClick = withHaptic { selectedLocales = availableLocales.toSet() }
                    ) {
                        Text(text = stringResource(R.string.select_all), fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = withHaptic { selectedLocales = emptySet() }
                    ) {
                        Text(text = stringResource(R.string.deselect_all), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(SubtitleSpacing))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = ListMaxHeight)
                    .clip(RoundedCornerShape(ListClipRadius))
            ) {
                itemsIndexed(
                    items = availableLocales,
                    key = { _, locale -> locale }
                ) { index, locale ->
                    val isSelected = selectedLocales.contains(locale)
                    val shape = CardCornerShape.getRoundedShape(index, availableLocales.size)
                    val finalShape = if (isSelected) {
                        CustomCardShape(PILL_CORNER_PERCENT)
                    } else {
                        shape
                    }

                    val containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    }

                    val contentColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }

                    val translatedCount = project.items.count { item ->
                        val translation = item.translationsByLocale[locale]
                        if (translation != null) {
                            translation.isNotBlank()
                        } else {
                            project.targetLocale.equals(locale, ignoreCase = true) && item.status == TranslationStatus.TRANSLATED
                        }
                    }

                    val percent = if (project.items.isNotEmpty()) {
                        (translatedCount * 100) / project.items.size
                    } else {
                        0
                    }

                    val badgeContainerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    }

                    val badgeContentColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    CustomCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = CardVerticalPadding),
                        shape = finalShape,
                        colors = CardDefaults.cardColors(
                            containerColor = containerColor,
                            contentColor = contentColor
                        ),
                        onClick = withHaptic(HapticFeedbackType.ToggleOn) {
                            selectedLocales = if (isSelected) {
                                selectedLocales - locale
                            } else {
                                selectedLocales + locale
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = CardHorizontalPadding,
                                    vertical = CardContentVerticalPadding
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(ItemRowSpacing)
                            ) {
                                LocaleBadge(
                                    locale = locale,
                                    containerColor = badgeContainerColor,
                                    contentColor = badgeContentColor
                                )
                                Text(
                                    text = "$translatedCount/${project.items.size} ($percent%)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = contentColor
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(IconSize)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(SectionSpacing))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ActionButtonSpacing)
            ) {
                OutlinedButton(
                    onClick = withHaptic { onDismiss() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(100)
                ) {
                    Text(text = stringResource(R.string.cancel))
                }

                Button(
                    onClick = withHaptic { onConfirmExport(selectedLocales) },
                    enabled = selectedLocales.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(100)
                ) {
                    Text(text = "${stringResource(R.string.export_zip)} (${selectedLocales.size})")
                }
            }
        }
    }
}
