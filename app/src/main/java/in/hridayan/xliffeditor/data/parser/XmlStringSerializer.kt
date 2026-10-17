package `in`.hridayan.xliffeditor.data.parser

import `in`.hridayan.xliffeditor.domain.model.PluralItem
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.StringArrayItem
import `in`.hridayan.xliffeditor.domain.model.TranslatableItem
import java.util.Locale

class XmlStringSerializer(
    private val indent: String = "    "
) {

    fun serialize(items: List<TranslatableItem>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        sb.append("<resources>\n")

        for (item in items) {
            item.comment?.takeIf { it.isNotBlank() }?.let { comment ->
                sb.append(indent).append("<!-- ").append(comment).append(" -->\n")
            }

            when (item) {
                is SingleStringItem -> {
                    val value = item.targetValue ?: item.sourceValue
                    val escaped = AndroidXmlEscaper.escape(value)
                    sb.append(indent)
                        .append("<string name=\"")
                        .append(item.key)
                        .append("\"")
                    if (!item.isTranslatable) {
                        sb.append(" translatable=\"false\"")
                    }
                    if (!item.isFormatted) {
                        sb.append(" formatted=\"false\"")
                    }
                    sb.append(">")
                        .append(escaped)
                        .append("</string>\n")
                }
                is PluralItem -> {
                    sb.append(indent)
                        .append("<plurals name=\"")
                        .append(item.key)
                        .append("\">\n")
                    val map = if (item.targetQuantities.isNotEmpty()) item.targetQuantities else item.sourceQuantities
                    for ((qty, text) in map) {
                        val escaped = AndroidXmlEscaper.escape(text)
                        sb.append(indent).append(indent)
                            .append("<item quantity=\"")
                            .append(qty.name.lowercase(Locale.US))
                            .append("\">")
                            .append(escaped)
                            .append("</item>\n")
                    }
                    sb.append(indent).append("</plurals>\n")
                }
                is StringArrayItem -> {
                    sb.append(indent)
                        .append("<string-array name=\"")
                        .append(item.key)
                        .append("\">\n")
                    val list = if (item.targetItems.isNotEmpty()) item.targetItems else item.sourceItems
                    for (text in list) {
                        val escaped = AndroidXmlEscaper.escape(text)
                        sb.append(indent).append(indent)
                            .append("<item>")
                            .append(escaped)
                            .append("</item>\n")
                    }
                    sb.append(indent).append("</string-array>\n")
                }
            }
        }

        sb.append("</resources>\n")
        return sb.toString()
    }
}
