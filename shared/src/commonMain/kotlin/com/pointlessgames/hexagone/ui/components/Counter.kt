package com.pointlessgames.hexagone.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.decrement
import hexagone.shared.generated.resources.icon_add
import hexagone.shared.generated.resources.icon_remove
import hexagone.shared.generated.resources.increment

@Composable
internal fun Counter(
    count: Int,
    minValue: Int,
    maxValue: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        IconButton(
            isEnabled = count > minValue,
            iconRes = Res.drawable.icon_remove,
            contentDescription = Res.string.decrement,
            tint = MaterialTheme.colorScheme.onBackground,
            onClick = onDecrement,
        )

        AnimatedContent(
            targetState = count,
            transitionSpec = {
                val direction = if (initialState < targetState) 1 else -1

                fadeIn() + slideInHorizontally { direction * it / 2 } togetherWith
                        fadeOut() + slideOutHorizontally { -direction * it / 2 } using
                        SizeTransform(false)
            },
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        IconButton(
            isEnabled = count < maxValue,
            iconRes = Res.drawable.icon_add,
            contentDescription = Res.string.increment,
            tint = MaterialTheme.colorScheme.onBackground,
            onClick = onIncrement,
        )
    }
}
