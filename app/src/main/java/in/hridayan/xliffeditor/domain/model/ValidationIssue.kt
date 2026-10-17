package `in`.hridayan.xliffeditor.domain.model

enum class ValidationSeverity {
    ERROR,
    WARNING
}

enum class ValidationType {
    MISSING_FORMAT_SPECIFIER,
    UNESCAPED_APOSTROPHE,
    UNCLOSED_TAG,
    TRAILING_WHITESPACE_MISMATCH,
    LENGTH_ANOMALY
}

data class ValidationIssue(
    val type: ValidationType,
    val message: String,
    val severity: ValidationSeverity
)
