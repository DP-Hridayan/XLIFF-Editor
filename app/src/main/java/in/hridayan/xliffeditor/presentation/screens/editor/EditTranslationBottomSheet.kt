@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.xliffeditor.presentation.screens.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import `in`.hridayan.xliffeditor.domain.model.ValidationSeverity
import `in`.hridayan.xliffeditor.domain.usecase.ValidateTranslationUseCase
import `in`.hridayan.xliffeditor.presentation.components.LocaleBadge

import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState

@Composable
fun EditTranslationBottomSheet(
    item: SingleStringItem,
    sourceLocale: String,
    targetLocale: String,
    onDismiss: () -> Unit,
    onSave: (newTarget: String, newStatus: TranslationStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    val validateUseCase = remember { ValidateTranslationUseCase() }

    var targetText by remember(item) { mutableStateOf(item.targetValue ?: "") }
    var selectedStatus by remember(item) { mutableStateOf(item.status) }

    val issues = remember(targetText) {
        validateUseCase(item.copy(targetValue = targetText))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = item.key,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LocaleBadge(locale = sourceLocale)
                Text(
                    text = item.sourceValue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LocaleBadge(locale = targetLocale)
                Text(
                    text = stringResource(R.string.translate),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = targetText,
                onValueChange = {
                    targetText = it
                    if (it.isNotBlank() && selectedStatus == TranslationStatus.UNTRANSLATED) {
                        selectedStatus = TranslationStatus.TRANSLATED
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Enter translation…") },
                shape = RoundedCornerShape(16.dp)
            )

            for (issue in issues) {
                val color = if (issue.severity == ValidationSeverity.ERROR) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.tertiary
                }
                Text(
                    text = issue.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == TranslationStatus.TRANSLATED,
                    onClick = { selectedStatus = TranslationStatus.TRANSLATED },
                    label = { Text(text = stringResource(R.string.translated)) }
                )
                FilterChip(
                    selected = selectedStatus == TranslationStatus.FUZZY,
                    onClick = { selectedStatus = TranslationStatus.FUZZY },
                    label = { Text(text = stringResource(R.string.fuzzy_needs_review)) }
                )
                FilterChip(
                    selected = selectedStatus == TranslationStatus.UNTRANSLATED,
                    onClick = { selectedStatus = TranslationStatus.UNTRANSLATED },
                    label = { Text(text = stringResource(R.string.untranslated)) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(100)
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
                Button(
                    onClick = { onSave(targetText, selectedStatus) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(100)
                ) {
                    Text(text = stringResource(R.string.save))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
