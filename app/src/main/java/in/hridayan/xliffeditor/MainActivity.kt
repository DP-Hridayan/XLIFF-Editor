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
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
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
                uri?.let { projectViewModel.onOpenFile(it, contentResolver) }
            }

            val exportDocumentLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/xml")
            ) { uri ->
                uri?.let { projectViewModel.onExportFile(it, contentResolver) }
            }

            XliffEditorTheme(
                darkTheme = isDark,
                dynamicColor = uiState.dynamicColor,
                amoled = isAmoled
            ) {
                AppNavHost(
                    viewModel = projectViewModel,
                    contentResolver = contentResolver,
                    onOpenFilePicker = {
                        openDocumentLauncher.launch(
                            arrayOf(
                                "*/*",
                                "text/xml",
                                "application/xml",
                                "application/x-xliff+xml"
                            )
                        )
                    },
                    onExportFilePicker = {
                        val fileName = uiState.document.fileName
                        exportDocumentLauncher.launch(fileName)
                    },
                    onTranslateDirect = projectViewModel::translateDirect
                )
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
            projectViewModel.onOpenFile(uri, contentResolver)
        }
    }
}
