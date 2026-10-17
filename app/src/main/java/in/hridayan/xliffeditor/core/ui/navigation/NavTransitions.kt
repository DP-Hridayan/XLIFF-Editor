package `in`.hridayan.xliffeditor.core.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

private const val ENTER_SLIDE_DURATION_MS = 250
private const val ENTER_FADE_DURATION_MS = 200
private const val ENTER_FADE_DELAY_MS = 66
private const val EXIT_SLIDE_DURATION_MS = 250
private const val EXIT_FADE_DURATION_MS = 50

private const val PREDICTIVE_ENTER_SLIDE_DURATION_MS = 350
private const val PREDICTIVE_ENTER_FADE_DURATION_MS = 75
private const val PREDICTIVE_ENTER_SCALE_DURATION_MS = 350
private const val PREDICTIVE_EXIT_SLIDE_DURATION_MS = 200
private const val PREDICTIVE_EXIT_SCALE_DURATION_MS = 250

private const val SLIDE_OFFSET_FRACTION_RIGHT = 0.15f
private const val SLIDE_OFFSET_FRACTION_LEFT = 0.15f
private const val SLIDE_OFFSET_FRACTION_EXIT = 0.10f
private const val PREDICTIVE_OFFSET_FRACTION = 0.25f

private const val INITIAL_ALPHA_STANDARD = 0.5f
private const val INITIAL_ALPHA_PREDICTIVE = 0.75f
private const val INITIAL_SCALE_PREDICTIVE = 0.9f
private const val TARGET_SCALE_PREDICTIVE = 0.9f

fun slideFadeInFromRight(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { (it * SLIDE_OFFSET_FRACTION_RIGHT).toInt() },
        animationSpec = tween(ENTER_SLIDE_DURATION_MS, easing = FastOutSlowInEasing)
    ) + fadeIn(
        initialAlpha = INITIAL_ALPHA_STANDARD,
        animationSpec = tween(ENTER_FADE_DURATION_MS, delayMillis = ENTER_FADE_DELAY_MS, easing = LinearEasing)
    )
}

fun slideFadeOutToRight(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { (it * SLIDE_OFFSET_FRACTION_EXIT).toInt() },
        animationSpec = tween(EXIT_SLIDE_DURATION_MS, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(EXIT_FADE_DURATION_MS, easing = LinearEasing)
    )
}

fun slideFadeInFromLeft(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { -(it * SLIDE_OFFSET_FRACTION_LEFT).toInt() },
        animationSpec = tween(PREDICTIVE_ENTER_SLIDE_DURATION_MS, easing = FastOutSlowInEasing)
    ) + fadeIn(
        initialAlpha = INITIAL_ALPHA_STANDARD,
        animationSpec = tween(EXIT_FADE_DURATION_MS, delayMillis = ENTER_FADE_DELAY_MS, easing = LinearEasing)
    )
}

fun slideFadeOutToLeft(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -(it * SLIDE_OFFSET_FRACTION_EXIT).toInt() },
        animationSpec = tween(EXIT_SLIDE_DURATION_MS, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(EXIT_FADE_DURATION_MS, easing = LinearEasing)
    )
}

fun predictiveEnter(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { -(it * PREDICTIVE_OFFSET_FRACTION).toInt() },
        animationSpec = tween(PREDICTIVE_ENTER_SLIDE_DURATION_MS, easing = FastOutSlowInEasing)
    ) + fadeIn(
        initialAlpha = INITIAL_ALPHA_PREDICTIVE,
        animationSpec = tween(PREDICTIVE_ENTER_FADE_DURATION_MS, delayMillis = ENTER_FADE_DELAY_MS, easing = LinearEasing)
    ) + scaleIn(
        initialScale = INITIAL_SCALE_PREDICTIVE,
        animationSpec = tween(PREDICTIVE_ENTER_SCALE_DURATION_MS, delayMillis = ENTER_FADE_DELAY_MS, easing = LinearEasing)
    )
}

fun predictiveExit(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(PREDICTIVE_EXIT_SLIDE_DURATION_MS, easing = FastOutSlowInEasing)
    ) + scaleOut(
        targetScale = TARGET_SCALE_PREDICTIVE,
        animationSpec = tween(PREDICTIVE_EXIT_SCALE_DURATION_MS, easing = LinearEasing)
    )
}
