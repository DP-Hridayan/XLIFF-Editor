package `in`.hridayan.xliffeditor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportedLocalesProviderTest {

    @Test
    fun normalizeCustomLocale_handlesSingleLanguageCode() {
        val result = SupportedLocalesProvider.normalizeCustomLocale("it")
        assertEquals("IT-IT", result)
    }

    @Test
    fun normalizeCustomLocale_handlesLanguageWithRegion() {
        val result = SupportedLocalesProvider.normalizeCustomLocale("pt-br")
        assertEquals("PT-BR", result)
    }

    @Test
    fun normalizeCustomLocale_handlesUnderscore() {
        val result = SupportedLocalesProvider.normalizeCustomLocale("es_us")
        assertEquals("ES-US", result)
    }

    @Test
    fun isMlKitSupported_returnsTrueForSupportedLanguage() {
        assertTrue(SupportedLocalesProvider.isMlKitSupported("it"))
        assertTrue(SupportedLocalesProvider.isMlKitSupported("IT-IT"))
        assertTrue(SupportedLocalesProvider.isMlKitSupported("PT-BR"))
        assertTrue(SupportedLocalesProvider.isMlKitSupported("es"))
    }

    @Test
    fun isMlKitSupported_returnsFalseForUnknownLanguage() {
        assertFalse(SupportedLocalesProvider.isMlKitSupported("xyz"))
        assertFalse(SupportedLocalesProvider.isMlKitSupported("zzz-zz"))
    }

    @Test
    fun getAllSupportedLocales_returnsNonEmptySortedList() {
        val locales = SupportedLocalesProvider.getAllSupportedLocales()
        assertTrue(locales.isNotEmpty())
        assertTrue(locales.any { it.code == "IT-IT" })
        assertTrue(locales.any { it.code == "PT-BR" })
    }
}
