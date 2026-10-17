package `in`.hridayan.xliffeditor.domain.model

import android.net.Uri
import java.util.UUID

enum class FileFormat(val extension: String, val displayName: String) {
    XML("xml", "Android strings.xml"),
    XLIFF("xliff", "XLIFF 1.2 Localization")
}

data class LoadedDocument(
    val id: String = UUID.randomUUID().toString(),
    val uri: Uri? = null,
    val fileName: String = "strings.xml",
    val format: FileFormat = FileFormat.XML,
    val sourceLocale: String = "EN-US",
    val targetLocale: String = "VI-VN",
    val items: List<SingleStringItem> = emptyList()
)

data class PendingImportDocument(
    val uri: Uri,
    val fileName: String,
    val format: FileFormat,
    val detectedSourceLocale: String,
    val items: List<SingleStringItem>
)
