@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.xliffeditor.core.ui.theme

import android.app.Activity
import android.os.Build
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import `in`.hridayan.xliffeditor.core.ui.haptic.LocalHapticsEnabled

@Composable
fun XliffEditorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    amoled: Boolean = false,
    hapticsEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current

    LaunchedEffect(darkTheme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val flags = if (darkTheme) 0 else APPEARANCE_LIGHT_STATUS_BARS or APPEARANCE_LIGHT_NAVIGATION_BARS
            view.windowInsetsController?.setSystemBarsAppearance(
                flags,
                APPEARANCE_LIGHT_STATUS_BARS or APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        } else {
            val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val colorScheme = if (darkTheme && amoled) {
        baseColorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceContainer = Color(0xFF121212)
        )
    } else {
        baseColorScheme
    }

    val semanticStatusColors = if (darkTheme) {
        DarkSemanticStatusColors
    } else {
        LightSemanticStatusColors
    }

    CompositionLocalProvider(
        LocalSemanticStatusColors provides semanticStatusColors,
        LocalHapticsEnabled provides hapticsEnabled
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
