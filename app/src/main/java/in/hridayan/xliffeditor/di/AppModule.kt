package `in`.hridayan.xliffeditor.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.xliffeditor.data.repository.DocumentRepositoryImpl
import `in`.hridayan.xliffeditor.data.translation.MlKitTranslationService
import `in`.hridayan.xliffeditor.domain.repository.DocumentRepository
import `in`.hridayan.xliffeditor.domain.translation.TranslationEngine
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(
        impl: DocumentRepositoryImpl
    ): DocumentRepository

    @Binds
    @Singleton
    abstract fun bindTranslationEngine(
        impl: MlKitTranslationService
    ): TranslationEngine
}
