package `in`.hridayan.xliffeditor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.xliffeditor.R
import `in`.hridayan.xliffeditor.core.ui.theme.LocalSemanticStatusColors
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus

@Composable
fun StatusPill(
    status: TranslationStatus,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalSemanticStatusColors.current

    val (containerColor, contentColor, icon, textRes) = when (status) {
        TranslationStatus.TRANSLATED -> {
            Tuple4(
                semanticColors.successContainer,
                semanticColors.onSuccessContainer,
                Icons.Rounded.Check,
                R.string.translated
            )
        }
        TranslationStatus.FUZZY -> {
            Tuple4(
                semanticColors.warningContainer,
                semanticColors.onWarningContainer,
                Icons.Rounded.WarningAmber,
                R.string.fuzzy_needs_review
            )
        }
        TranslationStatus.UNTRANSLATED -> {
            Tuple4(
                semanticColors.errorContainer,
                semanticColors.onErrorContainer,
                Icons.Rounded.WarningAmber,
                R.string.untranslated
            )
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = contentColor
        )
    }
}

private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
