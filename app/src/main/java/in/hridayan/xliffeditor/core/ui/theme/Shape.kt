package `in`.hridayan.xliffeditor.core.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed interface CornerSize {
    data class Absolute(val dp: Dp) : CornerSize
    data class Percentage(val percent: Float) : CornerSize
}

@Stable
data class CustomCardShape(
    val topStart: CornerSize = CornerSize.Absolute(0.dp),
    val topEnd: CornerSize = CornerSize.Absolute(0.dp),
    val bottomStart: CornerSize = CornerSize.Absolute(0.dp),
    val bottomEnd: CornerSize = CornerSize.Absolute(0.dp)
) {
    constructor(percentage: Int) : this(
        topStart = CornerSize.Percentage(percentage.toFloat()),
        topEnd = CornerSize.Percentage(percentage.toFloat()),
        bottomStart = CornerSize.Percentage(percentage.toFloat()),
        bottomEnd = CornerSize.Percentage(percentage.toFloat())
    )

    constructor(all: Dp) : this(
        topStart = CornerSize.Absolute(all),
        topEnd = CornerSize.Absolute(all),
        bottomStart = CornerSize.Absolute(all),
        bottomEnd = CornerSize.Absolute(all)
    )

    constructor(top: Dp, bottom: Dp) : this(
        topStart = CornerSize.Absolute(top),
        topEnd = CornerSize.Absolute(top),
        bottomStart = CornerSize.Absolute(bottom),
        bottomEnd = CornerSize.Absolute(bottom)
    )

    constructor(topStart: Dp, topEnd: Dp, bottomStart: Dp, bottomEnd: Dp) : this(
        topStart = CornerSize.Absolute(topStart),
        topEnd = CornerSize.Absolute(topEnd),
        bottomStart = CornerSize.Absolute(bottomStart),
        bottomEnd = CornerSize.Absolute(bottomEnd)
    )
}

fun CornerSize.toDp(heightPx: Float, density: Density): Dp {
    return when (this) {
        is CornerSize.Absolute -> dp.coerceAtLeast(0.dp)
        is CornerSize.Percentage -> {
            with(density) {
                (heightPx.coerceAtLeast(0f) * (percent.coerceIn(0f, 100f) / 100f)).toDp()
            }
        }
    }
}

object CardCornerShape {
    val SINGLE_CARD = CustomCardShape(24.dp)
    val FIRST_CARD = CustomCardShape(top = 24.dp, bottom = 4.dp)
    val MIDDLE_CARD = CustomCardShape(all = 4.dp)
    val LAST_CARD = CustomCardShape(top = 4.dp, bottom = 24.dp)

    fun getRoundedShape(index: Int, size: Int): CustomCardShape {
        if (size <= 1) return SINGLE_CARD
        return when (index.coerceIn(0, size - 1)) {
            0 -> FIRST_CARD
            size - 1 -> LAST_CARD
            else -> MIDDLE_CARD
        }
    }
}
