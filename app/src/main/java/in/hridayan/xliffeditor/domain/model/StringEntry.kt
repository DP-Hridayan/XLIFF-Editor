package `in`.hridayan.xliffeditor.domain.model

sealed interface TranslatableItem {
    val key: String
    val comment: String?
    val isTranslatable: Boolean
    val status: TranslationStatus
}

data class SingleStringItem(
    override val key: String,
    val sourceValue: String,
    val targetValue: String? = null,
    override val comment: String? = null,
    override val isTranslatable: Boolean = true,
    val isFormatted: Boolean = true,
    override val status: TranslationStatus = if (targetValue.isNullOrBlank()) {
        TranslationStatus.UNTRANSLATED
    } else {
        TranslationStatus.TRANSLATED
    },
    val translationsByLocale: Map<String, String> = emptyMap(),
    val statusesByLocale: Map<String, TranslationStatus> = emptyMap()
) : TranslatableItem

enum class PluralQuantity {
    ZERO,
    ONE,
    TWO,
    FEW,
    MANY,
    OTHER
}

data class PluralItem(
    override val key: String,
    val sourceQuantities: Map<PluralQuantity, String>,
    val targetQuantities: Map<PluralQuantity, String> = emptyMap(),
    override val comment: String? = null,
    override val isTranslatable: Boolean = true,
    override val status: TranslationStatus = if (targetQuantities.isEmpty()) {
        TranslationStatus.UNTRANSLATED
    } else {
        TranslationStatus.TRANSLATED
    }
) : TranslatableItem

data class StringArrayItem(
    override val key: String,
    val sourceItems: List<String>,
    val targetItems: List<String> = emptyList(),
    override val comment: String? = null,
    override val isTranslatable: Boolean = true,
    override val status: TranslationStatus = if (targetItems.isEmpty()) {
        TranslationStatus.UNTRANSLATED
    } else {
        TranslationStatus.TRANSLATED
    }
) : TranslatableItem
