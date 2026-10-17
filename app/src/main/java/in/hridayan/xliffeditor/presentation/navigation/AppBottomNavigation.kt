package `in`.hridayan.xliffeditor.presentation.navigation

import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import `in`.hridayan.xliffeditor.R

enum class BottomNavDestination(
    val titleRes: Int,
    val icon: ImageVector
) {
    FILES(R.string.files, Icons.Outlined.Folder),
    STRINGS(R.string.strings, Icons.AutoMirrored.Outlined.FormatListBulleted),
    TRANSLATE(R.string.translate, Icons.Outlined.Translate),
    SETTINGS(R.string.settings, Icons.Outlined.Settings)
}

@Composable
fun AppBottomNavigation(
    currentDestination: BottomNavDestination,
    onDestinationSelected: (BottomNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        for (destination in BottomNavDestination.entries) {
            val selected = destination == currentDestination
            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = stringResource(destination.titleRes)
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.titleRes),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
