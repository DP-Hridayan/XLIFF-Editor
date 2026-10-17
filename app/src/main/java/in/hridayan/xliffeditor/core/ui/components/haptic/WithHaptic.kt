package `in`.hridayan.xliffeditor.core.ui.components.haptic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun withHaptic(
    type: HapticFeedbackType = HapticFeedbackType.ContextClick,
    block: () -> Unit
): () -> Unit {
    val haptic = LocalHapticFeedback.current
    val latestBlock = rememberUpdatedState(block)

    return remember(type, haptic) {
        {
            haptic.performHapticFeedback(type)
            latestBlock.value()
        }
    }
}
