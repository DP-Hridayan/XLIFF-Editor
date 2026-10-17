package `in`.hridayan.xliffeditor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.xliffeditor.core.ui.theme.XliffEditorTheme
import `in`.hridayan.xliffeditor.presentation.navigation.AppNavHost
import `in`.hridayan.xliffeditor.presentation.viewmodel.ProjectViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val projectViewModel: ProjectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
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

            XliffEditorTheme {
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
                        val fileName = projectViewModel.uiState.value.document.fileName
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
