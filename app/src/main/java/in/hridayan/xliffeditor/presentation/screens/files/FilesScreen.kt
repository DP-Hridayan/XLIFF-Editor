package `in`.hridayan.xliffeditor.presentation.screens.files

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.xliffeditor.R
import `in`.hridayan.xliffeditor.core.ui.components.card.CustomCard
import `in`.hridayan.xliffeditor.core.ui.components.haptic.withHaptic
import `in`.hridayan.xliffeditor.core.ui.theme.CardCornerShape
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.presentation.components.ExportBottomSheet
import `in`.hridayan.xliffeditor.presentation.components.ImportConfirmationBottomSheet
import `in`.hridayan.xliffeditor.presentation.components.LocaleBadge
import `in`.hridayan.xliffeditor.presentation.viewmodel.ProjectUiState

private val ScreenPaddingHorizontal: Dp = 16.dp
private val CardContentPadding: Dp = 18.dp
private val ItemSpacing: Dp = 12.dp
private val EmptyStateIconSize: Dp = 64.dp
private val EmptyStateSpacing: Dp = 16.dp
private val ProgressBarHeight: Dp = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    uiState: ProjectUiState,
    onOpenFilePicker: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSelectProject: (projectId: String) -> Unit,
    onDeleteProject: (projectId: String) -> Unit,
    onOpenExportDialog: (project: LoadedDocument) -> Unit,
    onConfirmImport: (chosenSourceLocale: String) -> Unit,
    onCancelImport: () -> Unit,
    onDismissExportDialog: () -> Unit,
    onConfirmExportZip: (selectedLocales: Set<String>) -> Unit,
    onAddCustomSourceLocale: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var projectPendingDelete by remember { mutableStateOf<LoadedDocument?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(onClick = withHaptic { onNavigateToSettings() }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = withHaptic { onOpenFilePicker() },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.import_file),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.projects.isEmpty()) {
                EmptyProjectsView(
                    onImportClick = onOpenFilePicker,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                ProjectsListView(
                    projects = uiState.projects,
                    onSelectProject = onSelectProject,
                    onOpenExportDialog = onOpenExportDialog,
                    onAskDeleteProject = { projectPendingDelete = it },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (uiState.pendingImportDocument != null) {
        ImportConfirmationBottomSheet(
            pendingDoc = uiState.pendingImportDocument,
            availableLocales = uiState.availableSourceLocales,
            onConfirm = onConfirmImport,
            onDismiss = onCancelImport,
            onAddCustomLocale = onAddCustomSourceLocale
        )
    }

    if (uiState.exportingProject != null) {
        ExportBottomSheet(
            project = uiState.exportingProject,
            availableLocales = uiState.availableTargetLocales,
            onConfirmExport = onConfirmExportZip,
            onDismiss = onDismissExportDialog
        )
    }

    projectPendingDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectPendingDelete = null },
            title = {
                Text(
                    text = stringResource(R.string.delete_project),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${stringResource(R.string.confirm_delete_project)} \"${project.fileName}\"?"
                )
            },
            confirmButton = {
                Button(
                    onClick = withHaptic {
                        onDeleteProject(project.id)
                        projectPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(text = stringResource(R.string.delete_string))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = withHaptic { projectPendingDelete = null }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun EmptyProjectsView(
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(EmptyStateIconSize),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(EmptyStateSpacing))

        Text(
            text = stringResource(R.string.no_projects),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.no_projects_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = withHaptic { onImportClick() },
            shape = RoundedCornerShape(100)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.import_file))
        }
    }
}

@Composable
private fun ProjectsListView(
    projects: List<LoadedDocument>,
    onSelectProject: (projectId: String) -> Unit,
    onOpenExportDialog: (project: LoadedDocument) -> Unit,
    onAskDeleteProject: (project: LoadedDocument) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ScreenPaddingHorizontal,
            end = ScreenPaddingHorizontal,
            top = 8.dp,
            bottom = 88.dp
        ),
        verticalArrangement = Arrangement.spacedBy(ItemSpacing)
    ) {
        items(
            items = projects,
            key = { it.id }
        ) { project ->
            ProjectCard(
                project = project,
                onClick = { onSelectProject(project.id) },
                onExportClick = { onOpenExportDialog(project) },
                onDeleteClick = { onAskDeleteProject(project) }
            )
        }
    }
}

@Composable
private fun ProjectCard(
    project: LoadedDocument,
    onClick: () -> Unit,
    onExportClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = project.items.size
    val translatedCount = project.items.count { it.status == TranslationStatus.TRANSLATED }
    val progressFraction = if (totalCount > 0) {
        translatedCount.toFloat() / totalCount.toFloat()
    } else {
        0f
    }
    val progressPercent = (progressFraction * 100).toInt()

    CustomCard(
        modifier = modifier.fillMaxWidth(),
        shape = CardCornerShape.SINGLE_CARD,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        clickable = true,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardContentPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.fileName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(8.dp))

                LocaleBadge(locale = project.format.displayName)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source: ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LocaleBadge(locale = project.sourceLocale)
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ProgressBarHeight),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$translatedCount of $totalCount strings translated ($progressPercent%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = withHaptic { onExportClick() },
                    shape = RoundedCornerShape(100),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FolderZip,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.export_zip),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = withHaptic { onDeleteClick() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.delete_string),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
