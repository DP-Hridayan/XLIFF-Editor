package `in`.hridayan.xliffeditor.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.settingsgraph.ui.SettingsColumn
import `in`.hridayan.xliffeditor.R
import `in`.hridayan.xliffeditor.core.ui.components.haptic.withHaptic
import `in`.hridayan.xliffeditor.presentation.viewmodel.AppThemeMode
import `in`.hridayan.xliffeditor.presentation.viewmodel.ProjectUiState

private val ContentBottomPadding: Dp = 24.dp
private val OptionRowVerticalPadding: Dp = 8.dp
private val OptionRowSpacing: Dp = 8.dp
private const val SupportUrl = "https://buymeacoffee.com/hridayan"
private const val SupportLinkDisplay = "buymeacoffee.com/hridayan"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: ProjectUiState,
    onNavigateBack: () -> Unit,
    onThemeModeChanged: (AppThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = withHaptic { onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        SettingsColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = ContentBottomPadding),
            hapticsEnabled = true
        ) {
            group(title = "Appearance") {
                clickableItem(key = "theme_setting") {
                    title(R.string.theme)
                    description(uiState.themeMode.title)
                    icon(Icons.Outlined.DarkMode)
                    onClick { showThemeDialog = true }
                }

                switchItem(key = "dynamic_color_setting") {
                    title(R.string.dynamic_color)
                    description("Material You dynamic color palettes")
                    icon(Icons.Outlined.Palette)
                    isChecked { uiState.dynamicColor }
                    onCheckedChange { _, isChecked ->
                        onDynamicColorChanged(isChecked)
                    }
                }
            }

            group(titleResId = R.string.offline_models) {
                clickableItem(key = "mlkit_info") {
                    title(R.string.offline_models)
                    description(R.string.offline_models_desc)
                    icon(Icons.Outlined.Translate)
                }
            }

            group(titleResId = R.string.about) {
                clickableItem(key = "version_info") {
                    title(R.string.app_name)
                    description(R.string.version)
                    icon(Icons.Outlined.Info)
                }

                clickableItem(key = "about_desc") {
                    title(R.string.about)
                    description(R.string.app_description)
                    icon(Icons.Outlined.Code)
                }

                clickableItem(key = "support_link") {
                    title(R.string.support_development)
                    description(SupportLinkDisplay)
                    icon(Icons.Outlined.LocalCafe)
                    onClick {
                        try {
                            uriHandler.openUri(SupportUrl)
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.theme),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    for (mode in AppThemeMode.entries) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    onClick = withHaptic {
                                        onThemeModeChanged(mode)
                                        showThemeDialog = false
                                    }
                                )
                                .padding(vertical = OptionRowVerticalPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(OptionRowSpacing)
                        ) {
                            RadioButton(
                                selected = uiState.themeMode == mode,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = mode.title,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = withHaptic { showThemeDialog = false }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}
