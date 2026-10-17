package `in`.hridayan.xliffeditor.core.ui.components.card

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import `in`.hridayan.xliffeditor.core.ui.haptic.LocalHapticsEnabled
import `in`.hridayan.xliffeditor.core.ui.theme.AshellYouAnimationSpecs
import `in`.hridayan.xliffeditor.core.ui.theme.CustomCardShape
import `in`.hridayan.xliffeditor.core.ui.theme.toDp

private val MinCornerDp = 0.dp
private val MaxCornerDp = 100.dp

object CustomCardDefaults {
    val pressedCornerRadius: Dp = 28.dp
    const val pressedScale: Float = 0.98f

    fun shape(): CustomCardShape = CustomCardShape(24.dp)
}

@Composable
fun CustomCard(
    modifier: Modifier = Modifier,
    shape: CustomCardShape = CustomCardDefaults.shape(),
    pressedCornerRadius: Dp = CustomCardDefaults.pressedCornerRadius,
    pressedScale: Float = CustomCardDefaults.pressedScale,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface
    ),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    clickable: Boolean = true,
    onClick: () -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val density = LocalDensity.current
    var heightPx by remember { mutableFloatStateOf(0f) }

    val isPressedState = interactionSource.collectIsPressedAsState()
    val isHoveredState = interactionSource.collectIsHoveredAsState()

    val isPressed = isPressedState.value
    val isHovered = isHoveredState.value
    val doCardInteractions = isPressed || isHovered

    val resolvedTopStart = shape.topStart.toDp(heightPx, density)
    val resolvedTopEnd = shape.topEnd.toDp(heightPx, density)
    val resolvedBottomStart = shape.bottomStart.toDp(heightPx, density)
    val resolvedBottomEnd = shape.bottomEnd.toDp(heightPx, density)

    val topStart by animateDpAsState(
        targetValue = if (doCardInteractions) pressedCornerRadius else resolvedTopStart,
        animationSpec = AshellYouAnimationSpecs.springDp,
        label = "corner_top_start"
    )

    val topEnd by animateDpAsState(
        targetValue = if (doCardInteractions) pressedCornerRadius else resolvedTopEnd,
        animationSpec = AshellYouAnimationSpecs.springDp,
        label = "corner_top_end"
    )

    val bottomStart by animateDpAsState(
        targetValue = if (doCardInteractions) pressedCornerRadius else resolvedBottomStart,
        animationSpec = AshellYouAnimationSpecs.springDp,
        label = "corner_bottom_start"
    )

    val bottomEnd by animateDpAsState(
        targetValue = if (doCardInteractions) pressedCornerRadius else resolvedBottomEnd,
        animationSpec = AshellYouAnimationSpecs.springDp,
        label = "corner_bottom_end"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = AshellYouAnimationSpecs.springFloat,
        label = "scale_anim"
    )

    val animatedShape = remember {
        AnimatedCornerShape(
            topStart = { topStart },
            topEnd = { topEnd },
            bottomStart = { bottomStart },
            bottomEnd = { bottomEnd }
        )
    }

    val isHapticEnabled = LocalHapticsEnabled.current
    val hapticFeedback = LocalHapticFeedback.current

    val handleCardClick = remember(clickable, onClick, isHapticEnabled, hapticFeedback) {
        {
            if (isHapticEnabled) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
            }
            onClick()
        }
    }

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(animatedShape)
            .onGloballyPositioned { coordinates ->
                heightPx = coordinates.size.height.toFloat()
            }
            .then(
                if (clickable) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = handleCardClick
                    )
                } else {
                    Modifier
                }
            ),
        shape = animatedShape,
        colors = colors,
        elevation = elevation,
        border = border
    ) {
        Column(content = content)
    }
}

private class AnimatedCornerShape(
    private val topStart: () -> Dp,
    private val topEnd: () -> Dp,
    private val bottomStart: () -> Dp,
    private val bottomEnd: () -> Dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val ts = with(density) { topStart().coerceIn(MinCornerDp, MaxCornerDp).toPx() }
        val te = with(density) { topEnd().coerceIn(MinCornerDp, MaxCornerDp).toPx() }
        val bs = with(density) { bottomStart().coerceIn(MinCornerDp, MaxCornerDp).toPx() }
        val be = with(density) { bottomEnd().coerceIn(MinCornerDp, MaxCornerDp).toPx() }
        return Outline.Rounded(
            RoundRect(
                rect = Rect(Offset.Zero, size),
                topLeft = CornerRadius(ts, ts),
                topRight = CornerRadius(te, te),
                bottomRight = CornerRadius(be, be),
                bottomLeft = CornerRadius(bs, bs)
            )
        )
    }
}
