package `in`.hridayan.xliffeditor.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.xliffeditor.domain.repository.AppPreferences
import `in`.hridayan.xliffeditor.presentation.viewmodel.AppThemeMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppPreferences {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getThemeMode(): AppThemeMode {
        val ordinal = prefs.getInt(KEY_THEME_MODE, AppThemeMode.SYSTEM.ordinal)
        return AppThemeMode.entries.getOrElse(ordinal) { AppThemeMode.SYSTEM }
    }

    override fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode.ordinal).apply()
    }

    override fun isDynamicColor(): Boolean {
        return prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
    }

    override fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
    }

    private companion object {
        private const val PREFS_NAME = "xliff_preferences"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
    }
}
