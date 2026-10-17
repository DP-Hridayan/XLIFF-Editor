package `in`.hridayan.xliffeditor.domain.usecase

import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.ValidationSeverity
import `in`.hridayan.xliffeditor.domain.model.ValidationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateTranslationUseCaseTest {

    private val validateUseCase = ValidateTranslationUseCase()

    @Test
    fun invoke_matchingFormatSpecifiers_returnsNoIssues() {
        val item = SingleStringItem(
            key = "user_points",
            sourceValue = "User %1\$s has %2\$d points",
            targetValue = "Người dùng %1\$s có %2\$d điểm"
        )

        val issues = validateUseCase(item)
        assertTrue(issues.isEmpty())
    }

    @Test
    fun invoke_missingFormatSpecifier_returnsError() {
        val item = SingleStringItem(
            key = "user_points",
            sourceValue = "User %1\$s has %2\$d points",
            targetValue = "Người dùng %1\$s có điểm"
        )

        val issues = validateUseCase(item)
        assertEquals(1, issues.size)
        assertEquals(ValidationType.MISSING_FORMAT_SPECIFIER, issues[0].type)
        assertEquals(ValidationSeverity.ERROR, issues[0].severity)
    }

    @Test
    fun invoke_unclosedHtmlTag_returnsError() {
        val item = SingleStringItem(
            key = "bold_text",
            sourceValue = "<b>Welcome</b> to app",
            targetValue = "<b>Chào mừng đến ứng dụng"
        )

        val issues = validateUseCase(item)
        assertTrue(issues.any { it.type == ValidationType.UNCLOSED_TAG })
    }

    @Test
    fun invoke_trailingWhitespaceMismatch_returnsWarning() {
        val item = SingleStringItem(
            key = "trailing_space",
            sourceValue = "Search: ",
            targetValue = "Tìm kiếm:"
        )

        val issues = validateUseCase(item)
        assertTrue(issues.any { it.type == ValidationType.TRAILING_WHITESPACE_MISMATCH })
    }
}
