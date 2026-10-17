package `in`.hridayan.xliffeditor.domain.repository

import `in`.hridayan.xliffeditor.domain.model.LoadedDocument

interface DocumentSessionRepository {
    suspend fun saveProjects(projects: List<LoadedDocument>, activeId: String?)
    suspend fun loadProjects(): Pair<List<LoadedDocument>, String?>
    suspend fun saveSession(document: LoadedDocument)
    suspend fun loadSession(): LoadedDocument?
    suspend fun clearSession()
}
