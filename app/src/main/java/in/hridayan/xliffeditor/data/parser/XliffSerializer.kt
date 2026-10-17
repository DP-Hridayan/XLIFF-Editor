package `in`.hridayan.xliffeditor.data.parser

import `in`.hridayan.xliffeditor.domain.model.XliffDocument

class XliffSerializer(
    private val indent: String = "  "
) {

    fun serialize(document: XliffDocument): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        sb.append("<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" version=\"")
            .append(document.version)
            .append("\">\n")

        for (file in document.files) {
            sb.append(indent)
                .append("<file original=\"")
                .append(file.original)
                .append("\" source-language=\"")
                .append(file.sourceLanguage)
                .append("\"")
            file.targetLanguage?.let {
                sb.append(" target-language=\"").append(it).append("\"")
            }
            sb.append(" datatype=\"").append(file.datatype).append("\">\n")
            sb.append(indent).append(indent).append("<body>\n")

            for (unit in file.units) {
                sb.append(indent).append(indent).append(indent)
                    .append("<trans-unit id=\"").append(unit.id).append("\"")
                if (unit.approved) {
                    sb.append(" approved=\"yes\"")
                }
                if (!unit.translate) {
                    sb.append(" translate=\"no\"")
                }
                sb.append(">\n")

                sb.append(indent).append(indent).append(indent).append(indent)
                    .append("<source>").append(unit.source).append("</source>\n")

                unit.target?.let { targetVal ->
                    sb.append(indent).append(indent).append(indent).append(indent)
                        .append("<target")
                    unit.state?.let {
                        sb.append(" state=\"").append(it).append("\"")
                    }
                    sb.append(">").append(targetVal).append("</target>\n")
                }

                unit.note?.let { noteVal ->
                    sb.append(indent).append(indent).append(indent).append(indent)
                        .append("<note from=\"developer\">").append(noteVal).append("</note>\n")
                }

                sb.append(indent).append(indent).append(indent)
                    .append("</trans-unit>\n")
            }

            sb.append(indent).append(indent).append("</body>\n")
            sb.append(indent).append("</file>\n")
        }

        sb.append("</xliff>\n")
        return sb.toString()
    }
}
