package `in`.hridayan.xliffeditor.domain.usecase

import `in`.hridayan.xliffeditor.data.parser.XmlStringSerializer
import `in`.hridayan.xliffeditor.data.parser.XliffSerializer
import `in`.hridayan.xliffeditor.domain.model.FileFormat
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.domain.model.XliffDocument
import `in`.hridayan.xliffeditor.domain.model.XliffFile
import `in`.hridayan.xliffeditor.domain.model.XliffUnit
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class ExportZipUseCase(
    private val xmlSerializer: XmlStringSerializer,
    private val xliffSerializer: XliffSerializer
) {

    @Inject
    constructor() : this(XmlStringSerializer(), XliffSerializer())

    constructor(xmlSerializer: XmlStringSerializer) : this(xmlSerializer, XliffSerializer())

    operator fun invoke(
        document: LoadedDocument,
        selectedLocales: Set<String>,
        outputStream: OutputStream
    ): Result<Int> {
        return runCatching {
            var exportedCount = 0
            val baseName = document.fileName.substringBeforeLast(".")
            val ext = document.fileName.substringAfterLast(".", "xml")

            ZipOutputStream(outputStream).use { zipOut ->
                for (locale in selectedLocales) {
                    val itemsForLocale = document.items.map { item ->
                        val targetForLocale = item.translationsByLocale[locale]
                            ?: (if (document.targetLocale.equals(locale, ignoreCase = true)) item.targetValue else null)
                        val statusForLocale = item.statusesByLocale[locale]
                            ?: (if (document.targetLocale.equals(locale, ignoreCase = true)) item.status else TranslationStatus.UNTRANSLATED)

                        item.copy(
                            targetValue = targetForLocale,
                            status = statusForLocale
                        )
                    }

                    val serializedContent = when (document.format) {
                        FileFormat.XML -> {
                            xmlSerializer.serialize(itemsForLocale)
                        }
                        FileFormat.XLIFF -> {
                            val units = itemsForLocale.map { item ->
                                val state = when (item.status) {
                                    TranslationStatus.TRANSLATED -> STATE_TRANSLATED
                                    TranslationStatus.FUZZY -> STATE_NEEDS_REVIEW
                                    TranslationStatus.UNTRANSLATED -> STATE_NEW
                                }
                                XliffUnit(
                                    id = item.key,
                                    source = item.sourceValue,
                                    target = item.targetValue,
                                    state = state,
                                    approved = item.status == TranslationStatus.TRANSLATED,
                                    translate = item.isTranslatable,
                                    note = item.comment
                                )
                            }
                            val xliffFile = XliffFile(
                                original = document.fileName,
                                sourceLanguage = document.sourceLocale.lowercase(Locale.US),
                                targetLanguage = locale.lowercase(Locale.US),
                                units = units
                            )
                            val xliffDoc = XliffDocument(
                                version = XLIFF_VERSION,
                                files = listOf(xliffFile)
                            )
                            xliffSerializer.serialize(xliffDoc)
                        }
                    }

                    val entryName = "${baseName}_$locale.$ext"
                    zipOut.putNextEntry(ZipEntry(entryName))
                    zipOut.write(serializedContent.toByteArray(StandardCharsets.UTF_8))
                    zipOut.closeEntry()
                    exportedCount++
                }
                zipOut.finish()
            }

            exportedCount
        }
    }

    private companion object {
        private const val XLIFF_VERSION = "1.2"
        private const val STATE_TRANSLATED = "translated"
        private const val STATE_NEEDS_REVIEW = "needs-review-translation"
        private const val STATE_NEW = "new"
    }
}
