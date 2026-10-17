package `in`.hridayan.xliffeditor.domain.usecase

import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncLocalesUseCaseTest {

    private val syncLocalesUseCase = SyncLocalesUseCase()

    @Test
    fun invoke_pairsSourceAndTarget_identifiesTranslatedAndUntranslated() {
        val baseItems = listOf(
            SingleStringItem(key = "app_title", sourceValue = "My Application"),
            SingleStringItem(key = "login_button", sourceValue = "Log In"),
            SingleStringItem(key = "welcome_msg", sourceValue = "Welcome to our app, users!")
        )

        val targetItems = listOf(
            SingleStringItem(key = "app_title", sourceValue = "Ứng dụng của tôi"),
            SingleStringItem(key = "welcome_msg", sourceValue = "Chào mừng đến với ứng dụng của chúng tôi!")
        )

        val synced = syncLocalesUseCase(baseItems, targetItems)

        assertEquals(3, synced.size)

        val appTitle = synced.first { it.key == "app_title" } as SingleStringItem
        assertEquals("My Application", appTitle.sourceValue)
        assertEquals("Ứng dụng của tôi", appTitle.targetValue)
        assertEquals(TranslationStatus.TRANSLATED, appTitle.status)

        val loginBtn = synced.first { it.key == "login_button" } as SingleStringItem
        assertEquals("Log In", loginBtn.sourceValue)
        assertEquals(null, loginBtn.targetValue)
        assertEquals(TranslationStatus.UNTRANSLATED, loginBtn.status)
    }
}
