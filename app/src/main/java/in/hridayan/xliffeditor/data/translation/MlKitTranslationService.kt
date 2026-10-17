package `in`.hridayan.xliffeditor.data.translation

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import `in`.hridayan.xliffeditor.domain.translation.TranslationEngine
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class MlKitTranslationService @Inject constructor() : TranslationEngine {

    private val translatorCache = ConcurrentHashMap<String, Translator>()

    override suspend fun translate(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<String> {
        if (text.isBlank()) return Result.success(text)

        val sourceCode = resolveLanguageCode(sourceLanguage)
        val targetCode = resolveLanguageCode(targetLanguage)

        if (sourceCode == null || targetCode == null) {
            return Result.failure(
                IllegalArgumentException("Unsupported language pair: $sourceLanguage -> $targetLanguage")
            )
        }

        return runCatching {
            val translator = getOrCreateTranslator(sourceCode, targetCode)
            val downloadConditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(downloadConditions).awaitVoid()

            val shielded = shieldPlaceholders(text)
            val translatedShielded = translator.translate(shielded.shieldedText).awaitString()
            unshieldPlaceholders(translatedShielded, shielded.placeholders)
        }
    }

    override suspend fun downloadModelIfNeeded(
        sourceLanguage: String,
        targetLanguage: String
    ): Result<Unit> {
        val sourceCode = resolveLanguageCode(sourceLanguage) ?: return Result.failure(
            IllegalArgumentException("Unsupported source language: $sourceLanguage")
        )
        val targetCode = resolveLanguageCode(targetLanguage) ?: return Result.failure(
            IllegalArgumentException("Unsupported target language: $targetLanguage")
        )

        return runCatching {
            val translator = getOrCreateTranslator(sourceCode, targetCode)
            val downloadConditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(downloadConditions).awaitVoid()
        }
    }

    private fun getOrCreateTranslator(sourceCode: String, targetCode: String): Translator {
        val key = "${sourceCode}_$targetCode"
        return translatorCache.computeIfAbsent(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceCode)
                .setTargetLanguage(targetCode)
                .build()
            Translation.getClient(options)
        }
    }

    private fun resolveLanguageCode(language: String): String? {
        val normalized = language.trim().lowercase(Locale.ROOT)
        val primaryTag = when {
            normalized.contains("-") -> normalized.substringBefore("-")
            normalized.contains("_") -> normalized.substringBefore("_")
            else -> normalized
        }
        val fromTag = TranslateLanguage.fromLanguageTag(primaryTag)
        if (fromTag != null) return fromTag

        return TranslateLanguage.getAllLanguages().find { it.equals(primaryTag, ignoreCase = true) }
    }

    private fun shieldPlaceholders(input: String): ShieldedResult {
        val placeholders = mutableListOf<String>()
        val shieldedText = PLACEHOLDER_REGEX.replace(input) { matchResult ->
            val index = placeholders.size
            placeholders.add(matchResult.value)
            "__PH_${index}__"
        }
        return ShieldedResult(shieldedText, placeholders)
    }

    private fun unshieldPlaceholders(translated: String, placeholders: List<String>): String {
        return RESTORE_REGEX.replace(translated) { matchResult ->
            val indexStr = matchResult.groupValues[1]
            val index = indexStr.toIntOrNull()
            if (index != null && index in placeholders.indices) {
                placeholders[index]
            } else {
                matchResult.value
            }
        }
    }

    private suspend fun Task<Void>.awaitVoid(): Unit = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener {
            continuation.resume(Unit)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }

    private suspend fun Task<String>.awaitString(): String = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result ?: "")
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }

    private data class ShieldedResult(
        val shieldedText: String,
        val placeholders: List<String>
    )

    private companion object {
        private val PLACEHOLDER_REGEX = Regex("""(%(\d+\$)?[-\+ #'0,(]*\d*(\.\d+)?[a-zA-Z%]|<[^>]+>|\{[0-9]+\})""")
        private val RESTORE_REGEX = Regex("""__\s*PH\s*_\s*(\d+)\s*__""")
    }
}
