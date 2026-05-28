package com.pointlessgames.hexagone.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.MaterialTheme
import com.pointlessgames.hexagone.ui.theme.iconsSize
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

internal const val DEFAULT_ANIMATION_DURATION = 2000

@Composable
internal fun LoadingIcon(
    isLoading: Boolean,
    icon: DrawableResource?,
    contentColor: Color,
    contentDescription: String? = null,
    size: Dp = MaterialTheme.iconsSize.small,
    animationDuration: Int = DEFAULT_ANIMATION_DURATION,
) {
    AnimatedContent(isLoading, contentAlignment = Alignment.Center) { isLoading ->
        if (isLoading) {
            InlineLoader(
                size = MaterialTheme.iconsSize.smaller(size),
                animationDuration = animationDuration,
            )
        } else if (icon != null) {
            Icon(
                modifier = Modifier.size(size),
                painter = painterResource(icon),
                contentDescription = contentDescription,
                tint = contentColor,
            )
        }
    }
}
