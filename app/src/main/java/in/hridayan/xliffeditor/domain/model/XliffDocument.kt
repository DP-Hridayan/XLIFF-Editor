package `in`.hridayan.xliffeditor.domain.model

data class XliffDocument(
    val version: String,
    val files: List<XliffFile>
)

data class XliffFile(
    val original: String,
    val sourceLanguage: String,
    val targetLanguage: String? = null,
    val datatype: String = "plaintext",
    val units: List<XliffUnit>
)

data class XliffUnit(
    val id: String,
    val source: String,
    val target: String? = null,
    val state: String? = null,
    val approved: Boolean = false,
    val note: String? = null
)
