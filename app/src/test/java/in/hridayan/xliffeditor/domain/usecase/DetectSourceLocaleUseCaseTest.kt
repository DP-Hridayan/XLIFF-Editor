package `in`.hridayan.xliffeditor.domain.usecase
import org.junit.Assert.assertEquals
import org.junit.Test

class DetectSourceLocaleUseCaseTest {

    private val useCase = DetectSourceLocaleUseCase()

    @Test
    fun detectFromValuesFolder() {
        assertEquals("ES-ES", useCase(path = "res/values-es/strings.xml", content = null))
        assertEquals("FR-FR", useCase(path = "res/values-fr/strings.xml", content = null))
        assertEquals("VI-VN", useCase(path = "res/values-vi/strings.xml", content = null))
        assertEquals("DE-DE", useCase(path = "res/values-de/strings.xml", content = null))
        assertEquals("ZH-CN", useCase(path = "res/values-zh-rCN/strings.xml", content = null))
        assertEquals("EN-US", useCase(path = "res/values/strings.xml", content = null))
    }

    @Test
    fun detectFromXmlAttributes() {
        val xmlWithToolsLocale = """<resources xmlns:tools="http://schemas.android.com/tools" tools:locale="es"></resources>"""
        assertEquals("ES-ES", useCase(path = null, content = xmlWithToolsLocale))

        val xmlWithXmlLang = """<resources xml:lang="fr"></resources>"""
        assertEquals("FR-FR", useCase(path = null, content = xmlWithXmlLang))
    }

    @Test
    fun detectFromXliffSourceLanguage() {
        val xliff = """<xliff version="1.2"><file source-language="de" target-language="en"></file></xliff>"""
        assertEquals("DE-DE", useCase(path = null, content = xliff))
    }

    @Test
    fun fallbackToEnUsWhenUndetected() {
        assertEquals("EN-US", useCase(path = "some/random/file.xml", content = "<resources></resources>"))
    }
}
