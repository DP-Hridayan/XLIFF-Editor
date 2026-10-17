package `in`.hridayan.xliffeditor.presentation.navigation

import android.content.ContentResolver
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.xliffeditor.presentation.screens.files.FilesScreen
import `in`.hridayan.xliffeditor.presentation.screens.settings.SettingsScreen
import `in`.hridayan.xliffeditor.presentation.screens.strings.StringsScreen
import `in`.hridayan.xliffeditor.presentation.screens.translate.TranslateScreen
import `in`.hridayan.xliffeditor.presentation.viewmodel.ProjectViewModel

@Composable
fun AppNavHost(
    viewModel: ProjectViewModel,
    contentResolver: ContentResolver,
    onOpenFilePicker: () -> Unit,
    onExportFilePicker: () -> Unit,
    onTranslateDirect: suspend (text: String) -> String?,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onClearStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            AppBottomNavigation(
                currentDestination = uiState.currentDestination,
                onDestinationSelected = viewModel::onTabSelected
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.currentDestination,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tab_transition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { destination ->
            when (destination) {
                BottomNavDestination.FILES -> {
                    FilesScreen(
                        uiState = uiState,
                        onOpenFileClick = onOpenFilePicker,
                        onSaveFileClick = { viewModel.onSaveFile(contentResolver) },
                        onExportFileClick = onExportFilePicker,
                        onCreateNewFile = { name, format, src, tgt ->
                            viewModel.onCreateNewFile(name, format, src, tgt)
                        },
                        onSourceLocaleChanged = viewModel::onSourceLocaleChanged,
                        onTargetLocaleChanged = viewModel::onTargetLocaleChanged
                    )
                }
                BottomNavDestination.STRINGS -> {
                    StringsScreen(
                        uiState = uiState,
                        onSearchQueryChanged = viewModel::onSearchQueryChanged,
                        onAddNewString = viewModel::onAddNewString,
                        onUpdateString = viewModel::onUpdateStringDetails,
                        onDeleteString = viewModel::onDeleteString
                    )
                }
                BottomNavDestination.TRANSLATE -> {
                    TranslateScreen(
                        uiState = uiState,
                        onOpenFileClick = onOpenFilePicker,
                        onSearchQueryChanged = viewModel::onSearchQueryChanged,
                        onStatusFilterSelected = viewModel::onStatusFilterSelected,
                        onEditItemClicked = viewModel::onEditItemClicked,
                        onDismissEditor = viewModel::onDismissEditor,
                        onSaveTranslation = viewModel::onSaveTranslation,
                        onAutoTranslateItem = viewModel::onAutoTranslateItem,
                        onAutoTranslateAllUntranslated = viewModel::onAutoTranslateAllUntranslated,
                        onTranslateSingleItemDirect = onTranslateDirect
                    )
                }
                BottomNavDestination.SETTINGS -> {
                    SettingsScreen()
                }
            }
        }
    }
}
