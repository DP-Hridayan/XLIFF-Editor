package `in`.hridayan.xliffeditor.domain.translation

interface TranslationEngine {
    suspend fun translate(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<String>

    suspend fun downloadModelIfNeeded(
        sourceLanguage: String,
        targetLanguage: String
    ): Result<Unit>
}
