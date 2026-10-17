package `in`.hridayan.xliffeditor.domain.usecase

import `in`.hridayan.xliffeditor.domain.model.PluralItem
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.StringArrayItem
import `in`.hridayan.xliffeditor.domain.model.TranslatableItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus

class SyncLocalesUseCase {

    operator fun invoke(
        baseItems: List<TranslatableItem>,
        targetItems: List<TranslatableItem>
    ): List<TranslatableItem> {
        val targetMap = targetItems.associateBy { it.key }

        return baseItems.map { baseItem ->
            val targetMatch = targetMap[baseItem.key]

            when (baseItem) {
                is SingleStringItem -> {
                    val targetVal = (targetMatch as? SingleStringItem)?.sourceValue
                    val status = when {
                        targetVal.isNullOrBlank() -> TranslationStatus.UNTRANSLATED
                        targetMatch.status == TranslationStatus.FUZZY -> TranslationStatus.FUZZY
                        else -> TranslationStatus.TRANSLATED
                    }

                    baseItem.copy(
                        targetValue = targetVal,
                        status = status
                    )
                }
                is PluralItem -> {
                    val targetQuantities = (targetMatch as? PluralItem)?.sourceQuantities ?: emptyMap()
                    val status = when {
                        targetQuantities.isEmpty() -> TranslationStatus.UNTRANSLATED
                        targetMatch?.status == TranslationStatus.FUZZY -> TranslationStatus.FUZZY
                        else -> TranslationStatus.TRANSLATED
                    }

                    baseItem.copy(
                        targetQuantities = targetQuantities,
                        status = status
                    )
                }
                is StringArrayItem -> {
                    val targetList = (targetMatch as? StringArrayItem)?.sourceItems ?: emptyList()
                    val status = when {
                        targetList.isEmpty() -> TranslationStatus.UNTRANSLATED
                        targetMatch?.status == TranslationStatus.FUZZY -> TranslationStatus.FUZZY
                        else -> TranslationStatus.TRANSLATED
                    }

                    baseItem.copy(
                        targetItems = targetList,
                        status = status
                    )
                }
            }
        }
    }
}
