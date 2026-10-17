package `in`.hridayan.xliffeditor.data.parser

object AndroidXmlEscaper {

    fun unescape(value: String): String {
        return value
            .replace("\\'", "'")
            .replace("\\\"", "\"")
            .replace("\\n", "\n")
            .replace("\\t", "\t")
    }

    fun escape(value: String): String {
        var result = value.replace("\n", "\\n").replace("\t", "\\t")
        if (!result.startsWith("\"") || !result.endsWith("\"")) {
            result = result.replace("'", "\\'")
        }
        return result
    }
}
