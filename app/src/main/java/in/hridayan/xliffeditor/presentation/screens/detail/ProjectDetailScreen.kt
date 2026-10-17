package `in`.hridayan.xliffeditor.presentation.screens.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.xliffeditor.R
import `in`.hridayan.xliffeditor.core.ui.components.haptic.withHaptic
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.presentation.components.LocaleBadge
import `in`.hridayan.xliffeditor.presentation.screens.strings.StringsScreen
import `in`.hridayan.xliffeditor.presentation.screens.translate.TranslateScreen
import `in`.hridayan.xliffeditor.presentation.viewmodel.DetailTab
import `in`.hridayan.xliffeditor.presentation.viewmodel.ProjectUiState

private val BadgeSpacing: Dp = 6.dp
private val TopBarEndPadding: Dp = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    uiState: ProjectUiState,
    onNavigateBack: () -> Unit,
    onSelectDetailTab: (DetailTab) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onStatusFilterSelected: (TranslationStatus?) -> Unit,
    onEditItemClicked: (SingleStringItem) -> Unit,
    onDismissEditor: () -> Unit,
    onSaveTranslation: (key: String, newTarget: String, newStatus: TranslationStatus) -> Unit,
    onAutoTranslateItem: (key: String) -> Unit,
    onAutoTranslateAllUntranslated: () -> Unit,
    onTranslateSingleItemDirect: suspend (text: String) -> String?,
    onTargetLocaleChanged: (String) -> Unit,
    onAddNewString: (key: String, sourceValue: String, comment: String?, isTranslatable: Boolean) -> Unit,
    onUpdateString: (oldKey: String, newKey: String, newSource: String, newComment: String?, isTranslatable: Boolean) -> Unit,
    onDeleteString: (key: String) -> Unit,
    onAddCustomTargetLocale: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.activeProject.fileName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                actions = {
                    Row(
                        modifier = Modifier.padding(end = TopBarEndPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LocaleBadge(locale = uiState.activeProject.format.displayName)
                        Spacer(modifier = Modifier.width(BadgeSpacing))
                        LocaleBadge(locale = uiState.activeProject.sourceLocale)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val selectedTabIndex = when (uiState.activeDetailTab) {
                DetailTab.TRANSLATE -> 0
                DetailTab.STRINGS -> 1
            }

            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = uiState.activeDetailTab == DetailTab.TRANSLATE,
                    onClick = withHaptic { onSelectDetailTab(DetailTab.TRANSLATE) },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Translate,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.translate),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )

                Tab(
                    selected = uiState.activeDetailTab == DetailTab.STRINGS,
                    onClick = withHaptic { onSelectDetailTab(DetailTab.STRINGS) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.FormatListBulleted,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.strings),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = uiState.activeDetailTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "detail_tab_content",
                    modifier = Modifier.fillMaxSize()
                ) { tab ->
                    when (tab) {
                        DetailTab.TRANSLATE -> {
                            TranslateScreen(
                                uiState = uiState,
                                onSearchQueryChanged = onSearchQueryChanged,
                                onStatusFilterSelected = onStatusFilterSelected,
                                onEditItemClicked = onEditItemClicked,
                                onDismissEditor = onDismissEditor,
                                onSaveTranslation = onSaveTranslation,
                                onAutoTranslateItem = onAutoTranslateItem,
                                onAutoTranslateAllUntranslated = onAutoTranslateAllUntranslated,
                                onTranslateSingleItemDirect = onTranslateSingleItemDirect,
                                onTargetLocaleChanged = onTargetLocaleChanged,
                                onAddCustomTargetLocale = onAddCustomTargetLocale,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        DetailTab.STRINGS -> {
                            StringsScreen(
                                uiState = uiState,
                                onSearchQueryChanged = onSearchQueryChanged,
                                onAddNewString = onAddNewString,
                                onUpdateString = onUpdateString,
                                onDeleteString = onDeleteString,
                                showTitleHeader = false,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
