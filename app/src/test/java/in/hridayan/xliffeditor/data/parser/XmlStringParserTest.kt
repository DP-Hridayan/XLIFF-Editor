package `in`.hridayan.xliffeditor.data.parser

import `in`.hridayan.xliffeditor.domain.model.PluralItem
import `in`.hridayan.xliffeditor.domain.model.PluralQuantity
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class XmlStringParserTest {

    private val parser = XmlStringParser()
    private val serializer = XmlStringSerializer()

    @Test
    fun parse_standardStrings_extractsKeysValuesAndComments() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <!-- Title of the application -->
                <string name="app_title">My Application</string>
                <string name="login_btn" translatable="false">Log In</string>
                <!-- Welcome greeting for registered users -->
                <string name="welcome_msg">Welcome, user\'s app!</string>
            </resources>
        """.trimIndent()

        val items = parser.parse(xml)

        assertEquals(3, items.size)

        val appTitle = items[0] as SingleStringItem
        assertEquals("app_title", appTitle.key)
        assertEquals("My Application", appTitle.sourceValue)
        assertEquals("Title of the application", appTitle.comment)
        assertTrue(appTitle.isTranslatable)
        assertEquals(TranslationStatus.UNTRANSLATED, appTitle.status)

        val loginBtn = items[1] as SingleStringItem
        assertEquals("login_btn", loginBtn.key)
        assertEquals("Log In", loginBtn.sourceValue)
        assertFalse(loginBtn.isTranslatable)

        val welcomeMsg = items[2] as SingleStringItem
        assertEquals("welcome_msg", welcomeMsg.key)
        assertEquals("Welcome, user's app!", welcomeMsg.sourceValue)
        assertEquals("Welcome greeting for registered users", welcomeMsg.comment)
    }

    @Test
    fun parse_plurals_extractsQuantityMap() {
        val xml = """
            <resources>
                <!-- Quantity of items in the cart -->
                <plurals name="cart_items">
                    <item quantity="one">1 item</item>
                    <item quantity="other">%d items</item>
                </plurals>
            </resources>
        """.trimIndent()

        val items = parser.parse(xml)
        assertEquals(1, items.size)

        val plural = items[0] as PluralItem
        assertEquals("cart_items", plural.key)
        assertEquals("Quantity of items in the cart", plural.comment)
        assertEquals("1 item", plural.sourceQuantities[PluralQuantity.ONE])
        assertEquals("%d items", plural.sourceQuantities[PluralQuantity.OTHER])
    }

    @Test
    fun serialize_escapesApostrophesAndFormatsProperly() {
        val items = listOf(
            SingleStringItem(
                key = "greeting",
                sourceValue = "Don't worry, user!",
                comment = "Greeting message"
            )
        )

        val xml = serializer.serialize(items)

        assertTrue(xml.contains("<string name=\"greeting\">Don\\'t worry, user!</string>"))
        assertTrue(xml.contains("<!-- Greeting message -->"))
    }
}
