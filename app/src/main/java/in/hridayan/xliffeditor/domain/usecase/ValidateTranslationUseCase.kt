package `in`.hridayan.xliffeditor.domain.usecase

import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.ValidationIssue
import `in`.hridayan.xliffeditor.domain.model.ValidationSeverity
import `in`.hridayan.xliffeditor.domain.model.ValidationType
import java.util.regex.Pattern

class ValidateTranslationUseCase {

    private val specifierPattern = Pattern.compile("%(?:[0-9]+\\$)?(?:[-+#0,(]*[0-9]*(?:\\.[0-9]+)?)?[a-zA-Z%]")
    private val tagOpenPattern = Pattern.compile("<([a-zA-Z0-9]+)[^>/]*>")
    private val tagClosePattern = Pattern.compile("</([a-zA-Z0-9]+)>")

    operator fun invoke(item: SingleStringItem): List<ValidationIssue> {
        val target = item.targetValue ?: return emptyList()
        val issues = mutableListOf<ValidationIssue>()

        val sourceSpecifiers = extractSpecifiers(item.sourceValue)
        val targetSpecifiers = extractSpecifiers(target)

        for (spec in sourceSpecifiers) {
            if (sourceSpecifiers.count { it == spec } > targetSpecifiers.count { it == spec }) {
                issues.add(
                    ValidationIssue(
                        type = ValidationType.MISSING_FORMAT_SPECIFIER,
                        message = "Missing format specifier: $spec",
                        severity = ValidationSeverity.ERROR
                    )
                )
            }
        }

        val openTags = extractTags(tagOpenPattern, target)
        val closeTags = extractTags(tagClosePattern, target)
        if (openTags.sorted() != closeTags.sorted()) {
            issues.add(
                ValidationIssue(
                    type = ValidationType.UNCLOSED_TAG,
                    message = "Unclosed or mismatched HTML/XML tags in translation",
                    severity = ValidationSeverity.ERROR
                )
            )
        }

        if (item.sourceValue.endsWith(" ") && !target.endsWith(" ")) {
            issues.add(
                ValidationIssue(
                    type = ValidationType.TRAILING_WHITESPACE_MISMATCH,
                    message = "Source string ends with a space, but target does not",
                    severity = ValidationSeverity.WARNING
                )
            )
        }

        return issues
    }

    private fun extractSpecifiers(text: String): List<String> {
        val matcher = specifierPattern.matcher(text)
        val list = mutableListOf<String>()
        while (matcher.find()) {
            list.add(matcher.group())
        }
        return list
    }

    private fun extractTags(pattern: Pattern, text: String): List<String> {
        val matcher = pattern.matcher(text)
        val list = mutableListOf<String>()
        while (matcher.find()) {
            matcher.group(1)?.let { list.add(it) }
        }
        return list
    }
}
