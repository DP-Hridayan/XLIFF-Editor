package `in`.hridayan.xliffeditor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.settingsgraph.ui.LocalSettingGraphState
import `in`.hridayan.settingsgraph.ui.rememberSettingsGraphState
import `in`.hridayan.xliffeditor.core.ui.theme.XliffEditorTheme
import `in`.hridayan.xliffeditor.presentation.navigation.AppNavHost
import `in`.hridayan.xliffeditor.presentation.viewmodel.AppThemeMode
import `in`.hridayan.xliffeditor.presentation.viewmodel.ProjectViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val projectViewModel: ProjectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val uiState by projectViewModel.uiState.collectAsStateWithLifecycle()

            val isSystemDark = isSystemInDarkTheme()
            val isDark = when (uiState.themeMode) {
                AppThemeMode.SYSTEM -> isSystemDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK, AppThemeMode.AMOLED -> true
            }
            val isAmoled = uiState.themeMode == AppThemeMode.AMOLED

            val openDocumentLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                uri?.let {
                    try {
                        contentResolver.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    } catch (_: Exception) {
                    }
                    projectViewModel.onOpenFile(it, contentResolver)
                }
            }

            var pendingZipLocales by remember { mutableStateOf<Set<String>?>(null) }

            val exportZipLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/zip")
            ) { uri ->
                val locales = pendingZipLocales
                if (uri != null && locales != null) {
                    val outputStream = contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        projectViewModel.onExportZip(outputStream, locales)
                    }
                } else {
                    projectViewModel.onDismissExportDialog()
                }
                pendingZipLocales = null
            }

            val settingsGraphState = rememberSettingsGraphState()

            XliffEditorTheme(
                darkTheme = isDark,
                dynamicColor = uiState.dynamicColor,
                amoled = isAmoled
            ) {
                CompositionLocalProvider(
                    LocalSettingGraphState provides settingsGraphState
                ) {
                    AppNavHost(
                        viewModel = projectViewModel,
                        contentResolver = contentResolver,
                        onOpenFilePicker = {
                            openDocumentLauncher.launch(arrayOf("*/*"))
                        },
                        onRequestExportZip = { suggestedFileName, locales ->
                            pendingZipLocales = locales
                            exportZipLauncher.launch(suggestedFileName)
                        },
                        onTranslateDirect = projectViewModel::translateDirect
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: intent?.clipData?.getItemAt(0)?.uri
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            projectViewModel.onOpenFile(uri, contentResolver)
        }
    }
}
