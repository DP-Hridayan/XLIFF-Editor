package `in`.hridayan.xliffeditor.domain.repository

import `in`.hridayan.xliffeditor.presentation.viewmodel.AppThemeMode

interface AppPreferences {
    fun getThemeMode(): AppThemeMode
    fun setThemeMode(mode: AppThemeMode)
    fun isDynamicColor(): Boolean
    fun setDynamicColor(enabled: Boolean)
}
