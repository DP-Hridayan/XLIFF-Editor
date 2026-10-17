package `in`.hridayan.xliffeditor.domain.usecase

import `in`.hridayan.xliffeditor.data.parser.XmlStringSerializer
import `in`.hridayan.xliffeditor.domain.model.FileFormat
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

class ExportZipUseCaseTest {

    private val xmlSerializer = XmlStringSerializer()
    private val useCase = ExportZipUseCase(xmlSerializer)

    @Test
    fun exportZipContainsSelectedLanguagesWithCorrectSuffixes() {
        val doc = LoadedDocument(
            fileName = "strings.xml",
            format = FileFormat.XML,
            sourceLocale = "EN-US",
            targetLocale = "VI-VN",
            items = listOf(
                SingleStringItem(
                    key = "app_name",
                    sourceValue = "My App",
                    targetValue = "Ung dung cua toi",
                    translationsByLocale = mapOf(
                        "VI-VN" to "Ung dung cua toi",
                        "ES-ES" to "Mi Aplicacion"
                    ),
                    statusesByLocale = mapOf(
                        "VI-VN" to TranslationStatus.TRANSLATED,
                        "ES-ES" to TranslationStatus.TRANSLATED
                    )
                )
            )
        )

        val outStream = ByteArrayOutputStream()
        val result = useCase(doc, setOf("VI-VN", "ES-ES"), outStream)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow())

        val inZip = ZipInputStream(ByteArrayInputStream(outStream.toByteArray()))
        val entryNames = mutableListOf<String>()
        val contents = mutableMapOf<String, String>()

        var entry = inZip.nextEntry
        while (entry != null) {
            entryNames.add(entry.name)
            contents[entry.name] = inZip.bufferedReader(Charsets.UTF_8).readText()
            entry = inZip.nextEntry
        }

        assertEquals(2, entryNames.size)
        assertTrue(entryNames.contains("strings_VI-VN.xml"))
        assertTrue(entryNames.contains("strings_ES-ES.xml"))
        assertTrue(contents["strings_VI-VN.xml"]?.contains("Ung dung cua toi") == true)
        assertTrue(contents["strings_ES-ES.xml"]?.contains("Mi Aplicacion") == true)
    }
}
