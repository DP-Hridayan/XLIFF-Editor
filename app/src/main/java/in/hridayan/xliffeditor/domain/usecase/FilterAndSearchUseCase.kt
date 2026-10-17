package `in`.hridayan.xliffeditor.domain.usecase

import `in`.hridayan.xliffeditor.domain.model.PluralItem
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.StringArrayItem
import `in`.hridayan.xliffeditor.domain.model.TranslatableItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus

class FilterAndSearchUseCase {

    operator fun invoke(
        items: List<TranslatableItem>,
        query: String = "",
        statusFilter: TranslationStatus? = null
    ): List<TranslatableItem> {
        val trimmedQuery = query.trim()

        return items.filter { item ->
            val matchesStatus = statusFilter == null || item.status == statusFilter
            if (!matchesStatus) return@filter false

            if (trimmedQuery.isEmpty()) return@filter true

            val matchesKey = item.key.contains(trimmedQuery, ignoreCase = true)
            val matchesComment = item.comment?.contains(trimmedQuery, ignoreCase = true) == true

            val matchesContent = when (item) {
                is SingleStringItem -> {
                    item.sourceValue.contains(trimmedQuery, ignoreCase = true) ||
                        item.targetValue?.contains(trimmedQuery, ignoreCase = true) == true
                }
                is PluralItem -> {
                    item.sourceQuantities.values.any { it.contains(trimmedQuery, ignoreCase = true) } ||
                        item.targetQuantities.values.any { it.contains(trimmedQuery, ignoreCase = true) }
                }
                is StringArrayItem -> {
                    item.sourceItems.any { it.contains(trimmedQuery, ignoreCase = true) } ||
                        item.targetItems.any { it.contains(trimmedQuery, ignoreCase = true) }
                }
            }

            matchesKey || matchesComment || matchesContent
        }
    }
}
