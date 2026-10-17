package `in`.hridayan.xliffeditor.domain.model

import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.Locale

data class SupportedLocaleItem(
    val code: String,
    val displayName: String,
    val languageCode: String
)

object SupportedLocalesProvider {

    private val CanonicalRegionMap: Map<String, String> = mapOf(
        "en" to "US",
        "es" to "ES",
        "pt" to "BR",
        "zh" to "CN",
        "ja" to "JP",
        "ko" to "KR",
        "de" to "DE",
        "fr" to "FR",
        "it" to "IT",
        "ru" to "RU",
        "hi" to "IN",
        "vi" to "VN",
        "ar" to "SA",
        "tr" to "TR",
        "pl" to "PL",
        "nl" to "NL",
        "sv" to "SE",
        "da" to "DK",
        "fi" to "FI",
        "el" to "GR",
        "cs" to "CZ",
        "hu" to "HU",
        "ro" to "RO",
        "uk" to "UA",
        "th" to "TH",
        "id" to "ID",
        "ms" to "MY",
        "bn" to "IN",
        "ta" to "IN",
        "te" to "IN",
        "mr" to "IN",
        "gu" to "IN",
        "kn" to "IN",
        "ur" to "PK",
        "fa" to "IR",
        "he" to "IL",
        "no" to "NO",
        "sk" to "SK",
        "bg" to "BG",
        "hr" to "HR"
    )

    fun getAllSupportedLocales(): List<SupportedLocaleItem> {
        return TranslateLanguage.getAllLanguages().map { langCode ->
            val locale = Locale.forLanguageTag(langCode)
            val displayName = locale.getDisplayLanguage(Locale.ENGLISH).replaceFirstChar { it.uppercase() }
            val region = CanonicalRegionMap[langCode] ?: langCode.uppercase()
            val code = "${langCode.uppercase()}-$region"
            SupportedLocaleItem(
                code = code,
                displayName = displayName.ifBlank { code },
                languageCode = langCode
            )
        }.sortedBy { it.displayName }
    }

    fun normalizeCustomLocale(input: String): String {
        val trimmed = input.trim().replace('_', '-')
        if (trimmed.isBlank()) return ""
        val parts = trimmed.split("-")
        return when (parts.size) {
            1 -> {
                val lang = parts[0].lowercase()
                val region = CanonicalRegionMap[lang] ?: lang.uppercase()
                "${lang.uppercase()}-$region"
            }
            2 -> "${parts[0].uppercase()}-${parts[1].uppercase()}"
            else -> trimmed.uppercase()
        }
    }

    fun isMlKitSupported(code: String): Boolean {
        val normalized = code.trim().lowercase(Locale.ROOT)
        val primaryTag = when {
            normalized.contains("-") -> normalized.substringBefore("-")
            normalized.contains("_") -> normalized.substringBefore("_")
            else -> normalized
        }
        return TranslateLanguage.getAllLanguages().any { it.equals(primaryTag, ignoreCase = true) }
    }
}
