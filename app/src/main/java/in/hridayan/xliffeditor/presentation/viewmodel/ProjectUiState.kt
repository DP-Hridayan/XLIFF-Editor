package `in`.hridayan.xliffeditor.presentation.viewmodel

import `in`.hridayan.xliffeditor.domain.model.FileFormat
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.presentation.navigation.BottomNavDestination

data class ProjectUiState(
    val document: LoadedDocument = LoadedDocument(
        fileName = "strings.xliff",
        format = FileFormat.XLIFF,
        sourceLocale = "EN-US",
        targetLocale = "VI-VN",
        items = listOf(
            SingleStringItem(
                key = "app_title",
                sourceValue = "My Application",
                targetValue = "Ứng dụng của tôi",
                status = TranslationStatus.TRANSLATED
            ),
            SingleStringItem(
                key = "welcome_msg",
                sourceValue = "Welcome to our app, users!",
                targetValue = "Chào mừng đến với ứng dụng của chúng tôi!",
                status = TranslationStatus.FUZZY
            ),
            SingleStringItem(
                key = "login_button",
                sourceValue = "Log In",
                targetValue = null,
                status = TranslationStatus.UNTRANSLATED
            )
        )
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
