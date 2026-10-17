package `in`.hridayan.xliffeditor.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import `in`.hridayan.xliffeditor.data.parser.XliffParser
import `in`.hridayan.xliffeditor.data.parser.XliffSerializer
import `in`.hridayan.xliffeditor.data.parser.XmlStringParser
import `in`.hridayan.xliffeditor.data.parser.XmlStringSerializer
import `in`.hridayan.xliffeditor.domain.model.FileFormat
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.domain.model.XliffDocument
import `in`.hridayan.xliffeditor.domain.model.XliffFile
import `in`.hridayan.xliffeditor.domain.model.XliffUnit
import `in`.hridayan.xliffeditor.domain.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepositoryImpl @Inject constructor() : DocumentRepository {

    private val xmlParser = XmlStringParser()
    private val xmlSerializer = XmlStringSerializer()
    private val xliffParser = XliffParser()
    private val xliffSerializer = XliffSerializer()

    override suspend fun loadDocument(
        uri: Uri,
        contentResolver: ContentResolver
    ): Result<LoadedDocument> = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = queryDisplayName(uri, contentResolver) ?: uri.lastPathSegment ?: DEFAULT_FILE_NAME
            val format = if (fileName.endsWith(".xliff", ignoreCase = true) || fileName.endsWith(".xlf", ignoreCase = true)) {
                FileFormat.XLIFF
            } else {
                FileFormat.XML
            }

            val content = readUriContent(uri, contentResolver)
            when (format) {
                FileFormat.XML -> parseXmlDocument(uri, fileName, content)
                FileFormat.XLIFF -> parseXliffDocument(uri, fileName, content)
            }
        }
    }

    override suspend fun saveDocument(
        uri: Uri,
        document: LoadedDocument,
        contentResolver: ContentResolver
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val outputContent = serializeDocument(document)
            writeUriContent(uri, outputContent, contentResolver)
        }
    }

    override suspend fun exportDocument(
        uri: Uri,
        document: LoadedDocument,
        contentResolver: ContentResolver
    ): Result<Unit> = saveDocument(uri, document, contentResolver)

    private fun parseXmlDocument(uri: Uri, fileName: String, content: String): LoadedDocument {
        val parsedItems = xmlParser.parse(content)
        val singleItems = parsedItems.filterIsInstance<SingleStringItem>()
        return LoadedDocument(
            uri = uri,
            fileName = fileName,
            format = FileFormat.XML,
            sourceLocale = DEFAULT_SOURCE_LOCALE,
            targetLocale = DEFAULT_TARGET_LOCALE,
            items = singleItems
        )
    }

    private fun parseXliffDocument(uri: Uri, fileName: String, content: String): LoadedDocument {
        val document = xliffParser.parse(content)
        val firstFile = document.files.firstOrNull()
        val sourceLocale = firstFile?.sourceLanguage ?: DEFAULT_SOURCE_LOCALE
        val targetLocale = firstFile?.targetLanguage ?: DEFAULT_TARGET_LOCALE

        val items = firstFile?.units?.map { unit ->
            val status = when {
                unit.target.isNullOrBlank() -> TranslationStatus.UNTRANSLATED
                unit.state.equals("needs-review-translation", ignoreCase = true) ||
                    unit.state.equals("new", ignoreCase = true) -> TranslationStatus.FUZZY
                else -> TranslationStatus.TRANSLATED
            }
            SingleStringItem(
                key = unit.id,
                sourceValue = unit.source,
                targetValue = unit.target,
                comment = unit.note,
                status = status
            )
        } ?: emptyList()

        return LoadedDocument(
            uri = uri,
            fileName = fileName,
            format = FileFormat.XLIFF,
            sourceLocale = sourceLocale.uppercase(),
            targetLocale = targetLocale.uppercase(),
            items = items
        )
    }

    private fun serializeDocument(document: LoadedDocument): String {
        return when (document.format) {
            FileFormat.XML -> {
                xmlSerializer.serialize(document.items)
            }
            FileFormat.XLIFF -> {
                val units = document.items.map { item ->
                    val state = when (item.status) {
                        TranslationStatus.TRANSLATED -> "translated"
                        TranslationStatus.FUZZY -> "needs-review-translation"
                        TranslationStatus.UNTRANSLATED -> "new"
                    }
                    XliffUnit(
                        id = item.key,
                        source = item.sourceValue,
                        target = item.targetValue,
                        state = state,
                        approved = item.status == TranslationStatus.TRANSLATED,
                        note = item.comment
                    )
                }
                val xliffFile = XliffFile(
                    original = document.fileName,
                    sourceLanguage = document.sourceLocale.lowercase(),
                    targetLanguage = document.targetLocale.lowercase(),
                    units = units
                )
                val xliffDoc = XliffDocument(
                    version = "1.2",
                    files = listOf(xliffFile)
                )
                xliffSerializer.serialize(xliffDoc)
            }
        }
    }

    private fun readUriContent(uri: Uri, contentResolver: ContentResolver): String {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open input stream for $uri")
        return inputStream.use { stream ->
            BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                reader.readText()
            }
        }
    }

    private fun writeUriContent(uri: Uri, content: String, contentResolver: ContentResolver) {
        val outputStream = contentResolver.openOutputStream(uri, "wt")
            ?: throw IllegalStateException("Cannot open output stream for $uri")
        outputStream.use { stream ->
            stream.write(content.toByteArray(StandardCharsets.UTF_8))
            stream.flush()
        }
    }

    private fun queryDisplayName(uri: Uri, contentResolver: ContentResolver): String? {
        val cursor = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) it.getString(index) else null
            } else {
                null
            }
        }
    }

    private companion object {
        private const val DEFAULT_FILE_NAME = "strings.xml"
        private const val DEFAULT_SOURCE_LOCALE = "EN-US"
        private const val DEFAULT_TARGET_LOCALE = "VI-VN"
    }
}
