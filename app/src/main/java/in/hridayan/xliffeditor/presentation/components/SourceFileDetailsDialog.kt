package `in`.hridayan.xliffeditor.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.hridayan.xliffeditor.R
import `in`.hridayan.xliffeditor.core.ui.components.haptic.withHaptic
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument

private val HeaderSpacing: Dp = 10.dp
private val ItemVerticalSpacing: Dp = 12.dp
private val ItemSpacerWidth: Dp = 12.dp
private val DialogShapeRadius: Dp = 24.dp
private const val ButtonCornerPercent = 100

@Composable
fun SourceFileDetailsDialog(
    project: LoadedDocument,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val translatableCount = remember(project.items) {
        project.items.count { it.isTranslatable }
    }
    val nonTranslatableCount = remember(project.items, translatableCount) {
        project.items.size - translatableCount
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HeaderSpacing)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.file_info),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ItemVerticalSpacing)
            ) {
                DetailInfoItem(
                    label = stringResource(R.string.file_name),
                    value = project.fileName
                )
                DetailInfoItem(
                    label = stringResource(R.string.format),
                    value = project.format.displayName
                )
                DetailInfoItem(
                    label = stringResource(R.string.source_language),
                    value = project.sourceLocale
                )
                DetailInfoItem(
                    label = stringResource(R.string.strings_count),
                    value = "${project.items.size}"
                )
                DetailInfoItem(
                    label = stringResource(R.string.translatable),
                    value = "$translatableCount"
                )
                if (nonTranslatableCount > 0) {
                    DetailInfoItem(
                        label = stringResource(R.string.non_translatable),
                        value = "$nonTranslatableCount"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = withHaptic { onDismiss() },
                shape = RoundedCornerShape(ButtonCornerPercent)
            ) {
                Text(text = stringResource(R.string.close))
            }
        },
        shape = RoundedCornerShape(DialogShapeRadius),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
    )
}

@Composable
private fun DetailInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(ItemSpacerWidth))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}
