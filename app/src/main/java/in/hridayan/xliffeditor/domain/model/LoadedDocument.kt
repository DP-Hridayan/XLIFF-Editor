package `in`.hridayan.xliffeditor.domain.model

import android.net.Uri

enum class FileFormat(val extension: String, val displayName: String) {
    XML("xml", "Android strings.xml"),
    XLIFF("xliff", "XLIFF 1.2 Localization")
}

data class LoadedDocument(
    val uri: Uri? = null,
    val fileName: String = "strings.xml",
    val format: FileFormat = FileFormat.XML,
    val sourceLocale: String = "EN-US",
    val targetLocale: String = "VI-VN",
    val items: List<SingleStringItem> = emptyList()
)
