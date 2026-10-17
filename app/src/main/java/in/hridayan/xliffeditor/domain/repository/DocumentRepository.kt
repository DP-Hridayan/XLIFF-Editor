package `in`.hridayan.xliffeditor.domain.repository

import android.content.ContentResolver
import android.net.Uri
import `in`.hridayan.xliffeditor.domain.model.LoadedDocument

interface DocumentRepository {
    suspend fun loadDocument(uri: Uri, contentResolver: ContentResolver): Result<LoadedDocument>
    suspend fun saveDocument(uri: Uri, document: LoadedDocument, contentResolver: ContentResolver): Result<Unit>
    suspend fun exportDocument(uri: Uri, document: LoadedDocument, contentResolver: ContentResolver): Result<Unit>
}
