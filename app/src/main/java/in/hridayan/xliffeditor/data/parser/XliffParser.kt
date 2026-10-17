package `in`.hridayan.xliffeditor.data.parser

import `in`.hridayan.xliffeditor.domain.model.XliffDocument
import `in`.hridayan.xliffeditor.domain.model.XliffFile
import `in`.hridayan.xliffeditor.domain.model.XliffUnit
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory

class XliffParser {

    fun parse(xmlContent: String): XliffDocument {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val builder = factory.newDocumentBuilder()
        val stream = ByteArrayInputStream(xmlContent.toByteArray(StandardCharsets.UTF_8))
        val document = builder.parse(stream)
        document.documentElement.normalize()

        val root = document.documentElement
        val version = root.getAttribute("version").ifBlank { "1.2" }

        val files = mutableListOf<XliffFile>()
        val fileNodes = root.getElementsByTagName("file")

        for (i in 0 until fileNodes.length) {
            val fileElem = fileNodes.item(i) as Element
            val original = fileElem.getAttribute("original")
            val sourceLang = fileElem.getAttribute("source-language")
            val targetLang = fileElem.getAttribute("target-language").ifBlank { null }
            val datatype = fileElem.getAttribute("datatype").ifBlank { "plaintext" }

            val units = mutableListOf<XliffUnit>()
            val unitNodes = fileElem.getElementsByTagName("trans-unit")

            for (j in 0 until unitNodes.length) {
                val unitElem = unitNodes.item(j) as Element
                val id = unitElem.getAttribute("id")
                val approved = unitElem.getAttribute("approved").equals("yes", ignoreCase = true)

                var sourceText = ""
                var targetText: String? = null
                var targetState: String? = null
                var noteText: String? = null

                val childNodes = unitElem.childNodes
                for (k in 0 until childNodes.length) {
                    val child = childNodes.item(k)
                    if (child.nodeType == Node.ELEMENT_NODE) {
                        val childElem = child as Element
                        when (childElem.tagName) {
                            "source" -> sourceText = childElem.textContent ?: ""
                            "target" -> {
                                targetText = childElem.textContent ?: ""
                                targetState = childElem.getAttribute("state").ifBlank { null }
                            }
                            "note" -> noteText = childElem.textContent ?: ""
                        }
                    }
                }

                units.add(
                    XliffUnit(
                        id = id,
                        source = sourceText,
                        target = targetText,
                        state = targetState,
                        approved = approved,
                        note = noteText
                    )
                )
            }

            files.add(
                XliffFile(
                    original = original,
                    sourceLanguage = sourceLang,
                    targetLanguage = targetLang,
                    datatype = datatype,
                    units = units
                )
            )
        }

        return XliffDocument(version = version, files = files)
    }
}
