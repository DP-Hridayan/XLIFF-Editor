package `in`.hridayan.xliffeditor.presentation.navigation

import android.content.ContentResolver
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import `in`.hridayan.xliffeditor.core.ui.navigation.predictiveEnter
import `in`.hridayan.xliffeditor.core.ui.navigation.predictiveExit
import `in`.hridayan.xliffeditor.core.ui.navigation.slideFadeInFromRight
import `in`.hridayan.xliffeditor.core.ui.navigation.slideFadeOutToLeft
import `in`.hridayan.xliffeditor.presentation.screens.detail.ProjectDetailScreen
import `in`.hridayan.xliffeditor.presentation.screens.files.FilesScreen
import `in`.hridayan.xliffeditor.presentation.screens.settings.SettingsScreen
import `in`.hridayan.xliffeditor.presentation.viewmodel.AppScreen
import `in`.hridayan.xliffeditor.presentation.viewmodel.ProjectViewModel

@Composable
fun AppNavHost(
    viewModel: ProjectViewModel,
    contentResolver: ContentResolver,
    onOpenFilePicker: () -> Unit,
    onRequestExportZip: (suggestedFileName: String, selectedLocales: Set<String>) -> Unit,
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

    BackHandler(enabled = uiState.currentScreen != AppScreen.HOME) {
        viewModel.onNavigateBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { _ ->
        AnimatedContent(
            targetState = uiState.currentScreen,
            transitionSpec = {
                if (targetState == AppScreen.HOME) {
                    predictiveEnter() togetherWith predictiveExit()
                } else {
                    slideFadeInFromRight() togetherWith slideFadeOutToLeft()
                }
            },
            label = "screen_transition",
            modifier = Modifier.fillMaxSize()
        ) { screen ->
            when (screen) {
                AppScreen.HOME -> {
                    FilesScreen(
                        uiState = uiState,
                        onOpenFilePicker = onOpenFilePicker,
                        onNavigateToSettings = { viewModel.onNavigateToScreen(AppScreen.SETTINGS) },
                        onSelectProject = viewModel::onSelectProject,
                        onDeleteProject = viewModel::onDeleteProject,
                        onOpenExportDialog = viewModel::onOpenExportDialog,
                        onConfirmImport = viewModel::onConfirmImport,
                        onCancelImport = viewModel::onCancelImport,
                        onDismissExportDialog = viewModel::onDismissExportDialog,
                        onConfirmExportZip = { selectedLocales ->
                            val doc = uiState.exportingProject ?: uiState.activeProject
                            val baseName = doc.fileName.substringBeforeLast(".")
                            onRequestExportZip("${baseName}_translations.zip", selectedLocales)
                        },
                        onAddCustomSourceLocale = viewModel::onAddCustomSourceLocale
                    )
                }
                AppScreen.PROJECT_DETAIL -> {
                    ProjectDetailScreen(
                        uiState = uiState,
                        onNavigateBack = viewModel::onNavigateBack,
                        onSelectDetailTab = viewModel::onSelectDetailTab,
                        onSearchQueryChanged = viewModel::onSearchQueryChanged,
                        onStatusFilterSelected = viewModel::onStatusFilterSelected,
                        onEditItemClicked = viewModel::onEditItemClicked,
                        onDismissEditor = viewModel::onDismissEditor,
                        onSaveTranslation = viewModel::onSaveTranslation,
                        onAutoTranslateItem = viewModel::onAutoTranslateItem,
                        onAutoTranslateAllUntranslated = viewModel::onAutoTranslateAllUntranslated,
                        onTranslateSingleItemDirect = onTranslateDirect,
                        onTargetLocaleChanged = viewModel::onTargetLocaleChanged,
                        onAddNewString = viewModel::onAddNewString,
                        onUpdateString = viewModel::onUpdateStringDetails,
                        onDeleteString = viewModel::onDeleteString,
                        onAddCustomTargetLocale = viewModel::onAddCustomTargetLocale
                    )
                }
                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        uiState = uiState,
                        onNavigateBack = viewModel::onNavigateBack,
                        onThemeModeChanged = viewModel::onThemeModeChanged,
                        onDynamicColorChanged = viewModel::onDynamicColorChanged
                    )
                }
            }
        }
    }
}
