package `in`.hridayan.xliffeditor.domain.repository

import `in`.hridayan.xliffeditor.domain.model.LoadedDocument

interface DocumentSessionRepository {
    suspend fun saveSession(document: LoadedDocument)
    suspend fun loadSession(): LoadedDocument?
    suspend fun clearSession()
}
