package `in`.hridayan.xliffeditor.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val PrimaryLight = Color(0xFF5A5A90)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFE2E0FF)
val OnPrimaryContainerLight = Color(0xFF16154B)

val SecondaryLight = Color(0xFF5E5D72)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE3E0F9)
val OnSecondaryContainerLight = Color(0xFF1A1A2C)

val TertiaryLight = Color(0xFF7A5368)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFD7EC)
val OnTertiaryContainerLight = Color(0xFF301124)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFFBF8FD)
val OnBackgroundLight = Color(0xFF1B1B21)
val SurfaceLight = Color(0xFFFBF8FD)
val OnSurfaceLight = Color(0xFF1B1B21)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF6F3F8)
val SurfaceContainerLight = Color(0xFFF0EDF2)
val SurfaceContainerHighLight = Color(0xFFEAE7EC)
val SurfaceContainerHighestLight = Color(0xFFE4E1E6)
val OutlineLight = Color(0xFF777680)
val OutlineVariantLight = Color(0xFFC8C5D0)

val PrimaryDark = Color(0xFFC3C0FF)
val OnPrimaryDark = Color(0xFF2C2B60)
val PrimaryContainerDark = Color(0xFF434277)
val OnPrimaryContainerDark = Color(0xFFE2E0FF)

val SecondaryDark = Color(0xFFC7C4DD)
val OnSecondaryDark = Color(0xFF2F2F42)
val SecondaryContainerDark = Color(0xFF464559)
val OnSecondaryContainerDark = Color(0xFFE3E0F9)

val TertiaryDark = Color(0xFFEBB9D2)
val OnTertiaryDark = Color(0xFF472639)
val TertiaryContainerDark = Color(0xFF603C50)
val OnTertiaryContainerDark = Color(0xFFFFD7EC)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF131318)
val OnBackgroundDark = Color(0xFFE4E1E6)
val SurfaceDark = Color(0xFF131318)
val OnSurfaceDark = Color(0xFFE4E1E6)
val SurfaceContainerLowestDark = Color(0xFF0E0E13)
val SurfaceContainerLowDark = Color(0xFF1B1B21)
val SurfaceContainerDark = Color(0xFF1F1F25)
val SurfaceContainerHighDark = Color(0xFF2A292F)
val SurfaceContainerHighestDark = Color(0xFF35343A)
val OutlineDark = Color(0xFF918F9A)
val OutlineVariantDark = Color(0xFF46464F)

val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

@Immutable
data class SemanticStatusColors(
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val errorContainer: Color,
    val onErrorContainer: Color
)

val LightSemanticStatusColors = SemanticStatusColors(
    successContainer = Color(0xFFD7E8CD),
    onSuccessContainer = Color(0xFF121F0E),
    warningContainer = Color(0xFFFFF3CD),
    onWarningContainer = Color(0xFF664D03),
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

val DarkSemanticStatusColors = SemanticStatusColors(
    successContainer = Color(0xFF283524),
    onSuccessContainer = Color(0xFFD7E8CD),
    warningContainer = Color(0xFF4D3800),
    onWarningContainer = Color(0xFFFFE69C),
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

val LocalSemanticStatusColors = staticCompositionLocalOf {
    LightSemanticStatusColors
}
