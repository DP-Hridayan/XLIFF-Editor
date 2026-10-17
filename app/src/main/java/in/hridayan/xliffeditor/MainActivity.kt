package `in`.hridayan.xliffeditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.xliffeditor.core.ui.theme.XliffEditorTheme
import `in`.hridayan.xliffeditor.presentation.screens.translate.TranslateScreen
import `in`.hridayan.xliffeditor.presentation.screens.translate.TranslateViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val translateViewModel: TranslateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XliffEditorTheme {
                val uiState by translateViewModel.uiState.collectAsStateWithLifecycle()
                TranslateScreen(
                    uiState = uiState,
                    onEvent = translateViewModel::onEvent
                )
            }
        }
    }
}
