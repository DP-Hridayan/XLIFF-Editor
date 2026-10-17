package `in`.hridayan.xliffeditor.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.xliffeditor.domain.model.FileFormat
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument
import `in`.hridayan.xliffeditor.domain.model.SingleStringItem
import `in`.hridayan.xliffeditor.domain.model.TranslationStatus
import `in`.hridayan.xliffeditor.domain.repository.DocumentSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentSessionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DocumentSessionRepository {

    private val sessionFile: File
        get() = File(context.filesDir, SESSION_FILE_NAME)

    override suspend fun saveSession(document: LoadedDocument): Unit = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject().apply {
                if (document.uri != null) {
                    put("uri", document.uri.toString())
                }
                put("fileName", document.fileName)
                put("format", document.format.name)
                put("sourceLocale", document.sourceLocale)
                put("targetLocale", document.targetLocale)

                val itemsArray = JSONArray()
                for (item in document.items) {
                    val itemObj = JSONObject().apply {
                        put("key", item.key)
                        put("sourceValue", item.sourceValue)
                        if (item.targetValue != null) put("targetValue", item.targetValue)
                        if (item.comment != null) put("comment", item.comment)
                        put("isTranslatable", item.isTranslatable)
                        put("isFormatted", item.isFormatted)
                        put("status", item.status.name)
                    }
                    itemsArray.put(itemObj)
                }
                put("items", itemsArray)
            }

            val tempFile = File(context.filesDir, "$SESSION_FILE_NAME.tmp")
            tempFile.writeText(root.toString(), Charsets.UTF_8)
            if (!tempFile.renameTo(sessionFile)) {
                tempFile.copyTo(sessionFile, overwrite = true)
                tempFile.delete()
            }
        } catch (e: Exception) {
            android.util.Log.e("DocumentSessionRepo", "Failed to save document session", e)
        }
    }

    override suspend fun loadSession(): LoadedDocument? = withContext(Dispatchers.IO) {
        try {
            val file = sessionFile
            if (!file.exists()) return@withContext null
            val content = file.readText(Charsets.UTF_8)
            if (content.isBlank()) return@withContext null

            val root = JSONObject(content)
            val uriStr = if (root.has("uri") && !root.isNull("uri")) root.getString("uri") else null
            val fileName = root.optString("fileName", "strings.xml")
            val formatStr = root.optString("format", "XML")
            val format = runCatching { FileFormat.valueOf(formatStr) }.getOrDefault(FileFormat.XML)
            val sourceLocale = root.optString("sourceLocale", "EN-US")
            val targetLocale = root.optString("targetLocale", "VI-VN")

            val itemsArray = root.optJSONArray("items") ?: JSONArray()
            val items = ArrayList<SingleStringItem>(itemsArray.length())
            for (i in 0 until itemsArray.length()) {
                val itemObj = itemsArray.getJSONObject(i)
                val key = itemObj.getString("key")
                val sourceValue = itemObj.getString("sourceValue")
                val targetValue = if (itemObj.has("targetValue") && !itemObj.isNull("targetValue")) {
                    itemObj.getString("targetValue")
                } else null
                val comment = if (itemObj.has("comment") && !itemObj.isNull("comment")) {
                    itemObj.getString("comment")
                } else null
                val isTranslatable = itemObj.optBoolean("isTranslatable", true)
                val isFormatted = itemObj.optBoolean("isFormatted", true)
                val statusStr = itemObj.optString("status", TranslationStatus.UNTRANSLATED.name)
                val status = runCatching { TranslationStatus.valueOf(statusStr) }.getOrDefault(TranslationStatus.UNTRANSLATED)

                items.add(
                    SingleStringItem(
                        key = key,
                        sourceValue = sourceValue,
                        targetValue = targetValue,
                        comment = comment,
                        isTranslatable = isTranslatable,
                        isFormatted = isFormatted,
                        status = status
                    )
                )
            }

            LoadedDocument(
                uri = uriStr?.let { Uri.parse(it) },
                fileName = fileName,
                format = format,
                sourceLocale = sourceLocale,
                targetLocale = targetLocale,
                items = items
            )
        } catch (e: Exception) {
            android.util.Log.e("DocumentSessionRepo", "Failed to load document session", e)
            null
        }
    }

    override suspend fun clearSession(): Unit = withContext(Dispatchers.IO) {
        try {
            val file = sessionFile
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            android.util.Log.e("DocumentSessionRepo", "Failed to clear document session", e)
        }
    }

    private companion object {
        private const val SESSION_FILE_NAME = "current_session.json"
    }
}
