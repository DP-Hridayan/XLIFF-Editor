package `in`.hridayan.xliffeditor.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XmlSanitizerTest {

    @Test
    fun sanitize_rawAmpersand_escapesToAmpEntity() {
        val input = "<string name=\"test\">Terms & Conditions</string>"
        val expected = "<string name=\"test\">Terms &amp; Conditions</string>"
        val actual = XmlSanitizer.sanitize(input)
        assertEquals(expected, actual)
    }

    @Test
    fun sanitize_urlWithAmpersands_escapesCorrectly() {
        val input = "<string name=\"url\">https://example.com?a=1&b=2&c=3</string>"
        val expected = "<string name=\"url\">https://example.com?a=1&amp;b=2&amp;c=3</string>"
        val actual = XmlSanitizer.sanitize(input)
        assertEquals(expected, actual)
    }

    @Test
    fun sanitize_validEntities_remainsUntouched() {
        val input = "<string name=\"valid\">A &amp; B &lt; C &gt; D &quot; E &apos; F &#160; G &#x20;</string>"
        val actual = XmlSanitizer.sanitize(input)
        assertEquals(input, actual)
    }

    @Test
    fun sanitize_htmlNamedEntities_convertsToNumericEntities() {
        val input = "<string name=\"html\">Space&nbsp;Copyright&copy;Trade&trade;</string>"
        val expected = "<string name=\"html\">Space&#160;Copyright&#169;Trade&#8482;</string>"
        val actual = XmlSanitizer.sanitize(input)
        assertEquals(expected, actual)
    }

    @Test
    fun sanitize_cdataSection_preservesContent() {
        val input = "<string name=\"cdata\"><![CDATA[Terms & Conditions &nbsp;]]></string>"
        val actual = XmlSanitizer.sanitize(input)
        assertEquals(input, actual)
    }

    @Test
    fun sanitize_utf8Bom_stripsBom() {
        val input = "\uFEFF<resources><string name=\"a\">b</string></resources>"
        val actual = XmlSanitizer.sanitize(input)
        assertTrue(actual.startsWith("<resources>"))
    }
}
