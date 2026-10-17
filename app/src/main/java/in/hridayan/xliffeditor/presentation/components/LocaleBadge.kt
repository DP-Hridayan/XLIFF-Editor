package `in`.hridayan.xliffeditor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BadgeHorizontalPadding: Dp = 8.dp
private val BadgeVerticalPadding: Dp = 2.dp
private const val CORNER_PERCENT = 100

@Composable
fun LocaleBadge(
    locale: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(CORNER_PERCENT))
            .background(containerColor)
            .padding(horizontal = BadgeHorizontalPadding, vertical = BadgeVerticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = locale,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = contentColor
        )
    }
}
