package `in`.hridayan.xliffeditor.presentation.viewmodel

import `in`.hridayan.xliffeditor.domain.model.FileFormat
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument
import `in`.hridayan.xliffeditor.domain.model.PendingImportDocument
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus

enum class AppThemeMode(val title: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark"),
    AMOLED("AMOLED Black")
}

enum class AppScreen {
    HOME,
    PROJECT_DETAIL,
    SETTINGS
}

enum class DetailTab {
    TRANSLATE,
    STRINGS
}

data class ProjectUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val projects: List<LoadedDocument> = emptyList(),
    val activeProjectId: String? = null,
    val currentScreen: AppScreen = AppScreen.HOME,
    val activeDetailTab: DetailTab = DetailTab.TRANSLATE,
    val pendingImportDocument: PendingImportDocument? = null,
    val exportingProject: LoadedDocument? = null,
    val searchQuery: String = "",
    val selectedStatusFilter: TranslationStatus? = null,
    val filteredItems: List<SingleStringItem> = emptyList(),
    val editingItem: SingleStringItem? = null,
    val isTranslating: Boolean = false,
    val translatingItemKey: String? = null,
    val translationProgress: Float = 0f,
    val translationProgressPercent: Int = 0,
    val translatedBatchCount: Int = 0,
    val totalBatchCount: Int = 0,
    val statusMessage: String? = null,
    val availableTargetLocales: List<String> = listOf("VI-VN", "ES-ES", "FR-FR", "DE-DE", "HI-IN", "JA-JP", "ZH-CN", "IT-IT", "RU-RU", "KO-KR", "AR-SA", "PT-BR"),
    val availableSourceLocales: List<String> = listOf("EN-US", "ES-ES", "FR-FR", "DE-DE", "ZH-CN", "VI-VN", "JA-JP", "HI-IN", "IT-IT", "RU-RU")
) {
    val activeProject: LoadedDocument
        get() = projects.find { it.id == activeProjectId } ?: projects.firstOrNull() ?: DEFAULT_EMPTY_DOC

    val document: LoadedDocument
        get() = activeProject

    val totalCount: Int get() = document.items.size
    val translatableCount: Int get() = document.items.count { it.isTranslatable }
    val nonTranslatableCount: Int get() = document.items.count { !it.isTranslatable }
    val translatedCount: Int get() = document.items.count { it.status == TranslationStatus.TRANSLATED }
    val fuzzyCount: Int get() = document.items.count { it.isTranslatable && it.status == TranslationStatus.FUZZY }
    val untranslatedCount: Int get() = document.items.count { it.isTranslatable && it.status == TranslationStatus.UNTRANSLATED }
    val needsTranslationCount: Int get() = untranslatedCount + fuzzyCount

    private companion object {
        private val DEFAULT_EMPTY_DOC = LoadedDocument(
            fileName = "strings.xml",
            format = FileFormat.XML,
            sourceLocale = "EN-US",
            targetLocale = "VI-VN",
            items = emptyList()
        )
    }
}
