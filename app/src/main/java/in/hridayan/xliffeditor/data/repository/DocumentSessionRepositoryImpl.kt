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
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentSessionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DocumentSessionRepository {

    private val sessionFile: File
        get() = File(context.filesDir, SESSION_FILE_NAME)

    override suspend fun saveProjects(projects: List<LoadedDocument>, activeId: String?): Unit = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject().apply {
                if (activeId != null) {
                    put("activeProjectId", activeId)
                }
                val projArray = JSONArray()
                for (doc in projects) {
                    projArray.put(serializeDocument(doc))
                }
                put("projects", projArray)
            }

            val tempFile = File(context.filesDir, "$SESSION_FILE_NAME.tmp")
            tempFile.writeText(root.toString(), Charsets.UTF_8)
            if (!tempFile.renameTo(sessionFile)) {
                tempFile.copyTo(sessionFile, overwrite = true)
                tempFile.delete()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to save projects session", e)
        }
    }

    override suspend fun loadProjects(): Pair<List<LoadedDocument>, String?> = withContext(Dispatchers.IO) {
        try {
            val file = sessionFile
            if (!file.exists()) return@withContext Pair(emptyList(), null)
            val content = file.readText(Charsets.UTF_8)
            if (content.isBlank()) return@withContext Pair(emptyList(), null)

            val root = JSONObject(content)
            if (root.has("projects")) {
                val activeId = if (root.has("activeProjectId") && !root.isNull("activeProjectId")) {
                    root.getString("activeProjectId")
                } else null
                val projArray = root.optJSONArray("projects") ?: JSONArray()
                val list = ArrayList<LoadedDocument>(projArray.length())
                for (i in 0 until projArray.length()) {
                    val docObj = projArray.getJSONObject(i)
                    list.add(deserializeDocument(docObj))
                }
                Pair(list, activeId)
            } else {
                val singleDoc = deserializeDocument(root)
                Pair(listOf(singleDoc), singleDoc.id)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to load projects session", e)
            Pair(emptyList(), null)
        }
    }

    override suspend fun saveSession(document: LoadedDocument) {
        saveProjects(listOf(document), document.id)
    }

    override suspend fun loadSession(): LoadedDocument? {
        return loadProjects().first.firstOrNull()
    }

    override suspend fun clearSession(): Unit = withContext(Dispatchers.IO) {
        try {
            val file = sessionFile
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to clear document session", e)
        }
    }

    private fun serializeDocument(document: LoadedDocument): JSONObject {
        return JSONObject().apply {
            put("id", document.id)
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

                    val transObj = JSONObject()
                    for ((loc, trans) in item.translationsByLocale) {
                        transObj.put(loc, trans)
                    }
                    put("translationsByLocale", transObj)

                    val statObj = JSONObject()
                    for ((loc, stat) in item.statusesByLocale) {
                        statObj.put(loc, stat.name)
                    }
                    put("statusesByLocale", statObj)
                }
                itemsArray.put(itemObj)
            }
            put("items", itemsArray)
        }
    }

    private fun deserializeDocument(docObj: JSONObject): LoadedDocument {
        val id = docObj.optString("id", UUID.randomUUID().toString())
        val uriStr = if (docObj.has("uri") && !docObj.isNull("uri")) docObj.getString("uri") else null
        val fileName = docObj.optString("fileName", "strings.xml")
        val formatStr = docObj.optString("format", "XML")
        val format = runCatching { FileFormat.valueOf(formatStr) }.getOrDefault(FileFormat.XML)
        val sourceLocale = docObj.optString("sourceLocale", "EN-US")
        val targetLocale = docObj.optString("targetLocale", "VI-VN")

        val itemsArray = docObj.optJSONArray("items") ?: JSONArray()
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

            val translationsByLocale = mutableMapOf<String, String>()
            val transObj = itemObj.optJSONObject("translationsByLocale")
            if (transObj != null) {
                val keys = transObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    translationsByLocale[k] = transObj.getString(k)
                }
            }

            val statusesByLocale = mutableMapOf<String, TranslationStatus>()
            val statObj = itemObj.optJSONObject("statusesByLocale")
            if (statObj != null) {
                val keys = statObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val s = runCatching { TranslationStatus.valueOf(statObj.getString(k)) }.getOrDefault(TranslationStatus.UNTRANSLATED)
                    statusesByLocale[k] = s
                }
            }

            items.add(
                SingleStringItem(
                    key = key,
                    sourceValue = sourceValue,
                    targetValue = targetValue,
                    comment = comment,
                    isTranslatable = isTranslatable,
                    isFormatted = isFormatted,
                    status = status,
                    translationsByLocale = translationsByLocale.toMap(),
                    statusesByLocale = statusesByLocale.toMap()
                )
            )
        }

        return LoadedDocument(
            id = id,
            uri = uriStr?.let { Uri.parse(it) },
            fileName = fileName,
            format = format,
            sourceLocale = sourceLocale,
            targetLocale = targetLocale,
            items = items
        )
    }

    private companion object {
        private const val TAG = "DocumentSessionRepo"
        private const val SESSION_FILE_NAME = "current_session.json"
    }
}
