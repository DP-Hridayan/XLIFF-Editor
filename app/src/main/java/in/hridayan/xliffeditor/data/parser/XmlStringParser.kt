package `in`.hridayan.xliffeditor.data.parser

import `in`.hridayan.xliffeditor.domain.model.PluralItem
import `in`.hridayan.xliffeditor.domain.model.PluralQuantity
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslatableItem
import org.w3c.dom.Comment
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

class XmlStringParser {

    fun parse(xmlContent: String): List<TranslatableItem> {
        val sanitizedContent = XmlSanitizer.sanitize(xmlContent)
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val builder = factory.newDocumentBuilder()
        val stream = ByteArrayInputStream(sanitizedContent.toByteArray(StandardCharsets.UTF_8))
        val document = builder.parse(stream)
        document.documentElement.normalize()

        val items = mutableListOf<TranslatableItem>()
        val root = document.documentElement
        val children = root.childNodes

        for (i in 0 until children.length) {
            val node = children.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element
                val precedingComment = findPrecedingComment(node)

                when (element.tagName) {
                    "string" -> {
                        parseSingleString(element, precedingComment)?.let { items.add(it) }
                    }
                    "plurals" -> {
                        parsePlurals(element, precedingComment)?.let { items.add(it) }
                    }
                }
            }
        }

        return items
    }

    private fun parseSingleString(element: Element, comment: String?): SingleStringItem? {
        val key = element.getAttribute("name")
        if (key.isBlank()) return null

        val translatableAttr = element.getAttribute("translatable")
        val isTranslatable = if (translatableAttr.isNotBlank()) {
            translatableAttr.toBoolean()
        } else {
            true
        }

        val formattedAttr = element.getAttribute("formatted")
        val isFormatted = if (formattedAttr.isNotBlank()) {
            formattedAttr.toBoolean()
        } else {
            true
        }

        val rawText = element.textContent ?: ""
        val unescapedText = AndroidXmlEscaper.unescape(rawText)

        val commentAttr = element.getAttribute("comment").takeIf { it.isNotBlank() }
            ?: element.getAttribute("description").takeIf { it.isNotBlank() }
            ?: element.getAttribute("tools:comment").takeIf { it.isNotBlank() }
            ?: element.getAttribute("tools:description").takeIf { it.isNotBlank() }
        val finalComment = commentAttr ?: comment

        return SingleStringItem(
            key = key,
            sourceValue = unescapedText,
            comment = finalComment,
            isTranslatable = isTranslatable,
            isFormatted = isFormatted
        )
    }

    private fun parsePlurals(element: Element, comment: String?): PluralItem? {
        val key = element.getAttribute("name")
        if (key.isBlank()) return null

        val translatableAttr = element.getAttribute("translatable")
        val isTranslatable = if (translatableAttr.isNotBlank()) {
            translatableAttr.toBoolean()
        } else {
            true
        }

        val quantityMap = mutableMapOf<PluralQuantity, String>()
        val itemNodes = element.getElementsByTagName("item")

        for (i in 0 until itemNodes.length) {
            val itemElem = itemNodes.item(i) as Element
            val qtyStr = itemElem.getAttribute("quantity").uppercase(Locale.US)
            val qty = runCatching { PluralQuantity.valueOf(qtyStr) }.getOrNull()
            if (qty != null) {
                val raw = itemElem.textContent ?: ""
                quantityMap[qty] = AndroidXmlEscaper.unescape(raw)
            }
        }

        val commentAttr = element.getAttribute("comment").takeIf { it.isNotBlank() }
            ?: element.getAttribute("description").takeIf { it.isNotBlank() }
            ?: element.getAttribute("tools:comment").takeIf { it.isNotBlank() }
            ?: element.getAttribute("tools:description").takeIf { it.isNotBlank() }
        val finalComment = commentAttr ?: comment

        return PluralItem(
            key = key,
            sourceQuantities = quantityMap,
            comment = finalComment,
            isTranslatable = isTranslatable
        )
    }

    private fun findPrecedingComment(node: Node): String? {
        var prev = node.previousSibling
        while (prev != null) {
            if (prev.nodeType == Node.COMMENT_NODE) {
                return (prev as Comment).data.trim()
            }
            if (prev.nodeType == Node.ELEMENT_NODE) {
                return null
            }
            prev = prev.previousSibling
        }
        return null
    }
}
