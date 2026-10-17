package `in`.hridayan.xliffeditor.domain.usecase

import java.util.Locale
import java.util.regex.Pattern
import javax.inject.Inject

class DetectSourceLocaleUseCase @Inject constructor() {

    operator fun invoke(path: String?, content: String?): String {
        if (!path.isNullOrBlank()) {
            val fromPath = detectFromPath(path)
            if (fromPath != null) return fromPath
        }

        if (!content.isNullOrBlank()) {
            val fromContent = detectFromContent(content)
            if (fromContent != null) return fromContent
        }

        return DEFAULT_LOCALE
    }

    private fun detectFromPath(path: String): String? {
        val matcher = VALUES_FOLDER_PATTERN.matcher(path)
        if (matcher.find()) {
            val qualifier = matcher.group(1) ?: return DEFAULT_LOCALE
            return normalizeLocale(qualifier)
        }
        return null
    }

    private fun detectFromContent(content: String): String? {
        val toolsMatcher = TOOLS_LOCALE_PATTERN.matcher(content)
        if (toolsMatcher.find()) {
            val locale = toolsMatcher.group(1)
            if (!locale.isNullOrBlank()) return normalizeLocale(locale)
        }

        val xmlLangMatcher = XML_LANG_PATTERN.matcher(content)
        if (xmlLangMatcher.find()) {
            val locale = xmlLangMatcher.group(1)
            if (!locale.isNullOrBlank()) return normalizeLocale(locale)
        }

        val xliffSourceMatcher = XLIFF_SOURCE_PATTERN.matcher(content)
        if (xliffSourceMatcher.find()) {
            val locale = xliffSourceMatcher.group(1)
            if (!locale.isNullOrBlank()) return normalizeLocale(locale)
        }

        return null
    }

    private fun normalizeLocale(raw: String): String {
        val cleaned = raw.replace("-r", "-").trim()
        val parts = cleaned.split("-", "_")
        return when (parts.size) {
            1 -> {
                val lang = parts[0].lowercase(Locale.US)
                LOCALE_MAP[lang] ?: "${lang.uppercase(Locale.US)}-${lang.uppercase(Locale.US)}"
            }
            2 -> {
                "${parts[0].uppercase(Locale.US)}-${parts[1].uppercase(Locale.US)}"
            }
            else -> DEFAULT_LOCALE
        }
    }

    private companion object {
        private const val DEFAULT_LOCALE = "EN-US"
        private val VALUES_FOLDER_PATTERN: Pattern = Pattern.compile("values(?:-([a-zA-Z0-9_-]+))?")
        private val TOOLS_LOCALE_PATTERN: Pattern = Pattern.compile("tools:locale=[\"']([^\"']+)[\"']")
        private val XML_LANG_PATTERN: Pattern = Pattern.compile("xml:lang=[\"']([^\"']+)[\"']")
        private val XLIFF_SOURCE_PATTERN: Pattern = Pattern.compile("source-language=[\"']([^\"']+)[\"']")

        private val LOCALE_MAP: Map<String, String> = mapOf(
            "en" to "EN-US",
            "vi" to "VI-VN",
            "es" to "ES-ES",
            "fr" to "FR-FR",
            "de" to "DE-DE",
            "ja" to "JA-JP",
            "zh" to "ZH-CN",
            "hi" to "HI-IN",
            "pt" to "PT-BR",
            "ru" to "RU-RU",
            "it" to "IT-IT",
            "ko" to "KO-KR",
            "ar" to "AR-SA"
        )
    }
}
