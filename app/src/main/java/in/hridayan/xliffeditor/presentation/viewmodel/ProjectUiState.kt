package `in`.hridayan.xliffeditor.presentation.viewmodel

import `in`.hridayan.xliffeditor.domain.model.FileFormat
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.presentation.navigation.BottomNavDestination

enum class AppThemeMode(val title: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark"),
    AMOLED("AMOLED Black")
}

data class ProjectUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val document: LoadedDocument = LoadedDocument(
        fileName = "strings.xml",
        format = FileFormat.XML,
        sourceLocale = "EN-US",
        targetLocale = "VI-VN",
        items = emptyList()
    ),
    val searchQuery: String = "",
    val selectedStatusFilter: TranslationStatus? = null,
    val filteredItems: List<SingleStringItem> = emptyList(),
    val editingItem: SingleStringItem? = null,
    val isTranslating: Boolean = false,
    val translatingItemKey: String? = null,
    val statusMessage: String? = null,
    val currentDestination: BottomNavDestination = BottomNavDestination.TRANSLATE,
    val availableTargetLocales: List<String> = listOf("VI-VN", "ES-ES", "FR-FR", "DE-DE", "HI-IN", "JA-JP", "ZH-CN", "IT-IT", "RU-RU"),
    val availableSourceLocales: List<String> = listOf("EN-US", "ES-ES", "FR-FR", "DE-DE", "ZH-CN")
) {
    val totalCount: Int get() = document.items.size
    val translatedCount: Int get() = document.items.count { it.status == TranslationStatus.TRANSLATED }
    val fuzzyCount: Int get() = document.items.count { it.status == TranslationStatus.FUZZY }
    val untranslatedCount: Int get() = document.items.count { it.status == TranslationStatus.UNTRANSLATED }
}
