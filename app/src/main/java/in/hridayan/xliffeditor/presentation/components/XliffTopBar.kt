package `in`.hridayan.xliffeditor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.xliffeditor.R
import `in`.hridayan.xliffeditor.core.ui.haptic.LocalHapticsEnabled

@Composable
fun XliffTopBar(
    fileName: String,
    onOpenFileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val hapticsEnabled = LocalHapticsEnabled.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
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
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                FilePill(fileName = fileName)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (hapticsEnabled) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                            }
                            onOpenFileClick()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Folder,
                    contentDescription = stringResource(R.string.open_file),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        FilePill(fileName = fileName)
    }
}

@Composable
private fun FilePill(
    fileName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = fileName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
