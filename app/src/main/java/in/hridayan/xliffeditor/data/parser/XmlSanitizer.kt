package `in`.hridayan.xliffeditor.data.parser

import java.util.regex.Pattern

object XmlSanitizer {

    private val CDATA_PATTERN: Pattern = Pattern.compile("<!\\[CDATA\\[(.*?)\\]\\]>", Pattern.DOTALL)
    private val UNESCAPED_AMPERSAND_REGEX: Regex = Regex("&(?!(amp|lt|gt|quot|apos|#[0-9]{1,7}|#[xX][0-9a-fA-F]{1,6});)")

    private val HTML_NAMED_ENTITIES: Map<String, String> = mapOf(
        "&nbsp;" to "&#160;",
        "&copy;" to "&#169;",
        "&reg;" to "&#174;",
        "&trade;" to "&#8482;",
        "&ndash;" to "&#8211;",
        "&mdash;" to "&#8212;",
        "&hellip;" to "&#8230;",
        "&bull;" to "&#8226;",
        "&lsquo;" to "&#8216;",
        "&rsquo;" to "&#8217;",
        "&ldquo;" to "&#8220;",
        "&rdquo;" to "&#8221;",
        "&laquo;" to "&#171;",
        "&raquo;" to "&#187;",
        "&euro;" to "&#8364;",
        "&pound;" to "&#163;",
        "&deg;" to "&#176;"
    )

    private const val BOM_CHAR: Char = '\uFEFF'

    fun sanitize(xml: String): String {
        val cleanXml = xml.trim().removePrefix(BOM_CHAR.toString())
        if (!cleanXml.contains("<![CDATA[")) {
            return sanitizeSegment(cleanXml)
        }

        val matcher = CDATA_PATTERN.matcher(cleanXml)
        val sb = StringBuilder()
        var lastIdx = 0

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            if (start > lastIdx) {
                sb.append(sanitizeSegment(cleanXml.substring(lastIdx, start)))
            }
            sb.append(matcher.group())
            lastIdx = end
        }

        if (lastIdx < cleanXml.length) {
            sb.append(sanitizeSegment(cleanXml.substring(lastIdx)))
        }

        return sb.toString()
    }

    private fun sanitizeSegment(segment: String): String {
        var processed = segment
        for ((namedEntity, numericEntity) in HTML_NAMED_ENTITIES) {
            processed = processed.replace(namedEntity, numericEntity)
        }
        return UNESCAPED_AMPERSAND_REGEX.replace(processed, "&amp;")
    }
}
