package `in`.hridayan.xliffeditor.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class XliffParserTest {

    private val parser = XliffParser()
    private val serializer = XliffSerializer()

    @Test
    fun parse_xliff12_extractsUnitsProperly() {
        val xliffXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <xliff xmlns="urn:oasis:names:tc:xliff:document:1.2" version="1.2">
              <file original="res/values/strings.xml" source-language="en-US" target-language="vi-VN" datatype="plaintext">
                <body>
                  <trans-unit id="app_title" approved="yes">
                    <source>My Application</source>
                    <target state="translated">Ứng dụng của tôi</target>
                    <note from="developer">Application title</note>
                  </trans-unit>
                  <trans-unit id="welcome_msg">
                    <source>Welcome to our app, users!</source>
                    <target state="needs-review-translation">Chào mừng đến với ứng dụng của chúng tôi!</target>
                  </trans-unit>
                  <trans-unit id="login_button">
                    <source>Log In</source>
                  </trans-unit>
                </body>
              </file>
            </xliff>
        """.trimIndent()

        val doc = parser.parse(xliffXml)

        assertEquals("1.2", doc.version)
        assertEquals(1, doc.files.size)

        val file = doc.files[0]
        assertEquals("en-US", file.sourceLanguage)
        assertEquals("vi-VN", file.targetLanguage)
        assertEquals(3, file.units.size)

        val u1 = file.units[0]
        assertEquals("app_title", u1.id)
        assertEquals("My Application", u1.source)
        assertEquals("Ứng dụng của tôi", u1.target)
        assertEquals("Application title", u1.note)
        assertTrue(u1.approved)

        val u2 = file.units[1]
        assertEquals("welcome_msg", u2.id)
        assertEquals("needs-review-translation", u2.state)

        val u3 = file.units[2]
        assertEquals("login_button", u3.id)
        assertEquals(null, u3.target)
    }

    @Test
    fun serialize_xliff12_generatesCompliantXml() {
        val doc = parser.parse("""
            <xliff version="1.2">
              <file original="res/values/strings.xml" source-language="en-US" target-language="vi-VN">
                <body>
                  <trans-unit id="app_title">
                    <source>My Application</source>
                    <target state="translated">Ứng dụng của tôi</target>
                  </trans-unit>
                </body>
              </file>
            </xliff>
        """.trimIndent())

        val xml = serializer.serialize(doc)

        assertTrue(xml.contains("<xliff"))
        assertTrue(xml.contains("trans-unit id=\"app_title\""))
        assertTrue(xml.contains("<target state=\"translated\">Ứng dụng của tôi</target>"))
    }
}
