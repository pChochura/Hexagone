package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentWithReceiverOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.ui.components.Position
import com.pointlessgames.hexagone.ui.components.Tooltip
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.ic_back
import org.jetbrains.compose.resources.painterResource

@Composable
fun HexBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playSound = com.pointlessgames.hexagone.utils.rememberPlayButtonSound()
    val heightScale = 0.866f
    Box(
        modifier = modifier
            .size(width = 48.dp, height = 48.dp * heightScale)
            .clip(FlatTopHexagonShape())
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), FlatTopHexagonShape())
            .clickable { playSound(); onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_back),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun HexagonIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: DrawableResource,
    label: String? = null,
    tooltip: StringResource? = null,
    tooltipPosition: Position = Position.ABOVE,
    backgroundColor: Color = Color(0xFFD63F7B).copy(alpha = 0.2f),
    borderColor: Color = Color(0xFFD63F7B).copy(alpha = 0.4f),
    size: Dp = 64.dp,
) {
    val playSound = com.pointlessgames.hexagone.utils.rememberPlayButtonSound()
    val onClick by rememberUpdatedState(onClick)
    val spacing = MaterialTheme.spacing
    val content = movableContentWithReceiverOf<Modifier> {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(width = size, height = size * 0.866f)
                    .clip(FlatTopHexagonShape())
                    .background(backgroundColor)
                    .border(spacing.extraTiny, borderColor, FlatTopHexagonShape())
                    .clickable { playSound(); onClick() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(size * 0.45f),
                    tint = Color.White,
                )
            }
            if (label != null) {
                Spacer(Modifier.height(spacing.small))
                Text(
                    text = label.uppercase(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 11.sp,
                )
            }
        }
    }

    if (tooltip != null && label == null) {
        Tooltip(
            modifier = modifier,
            position = tooltipPosition,
            contentDescription = tooltip,
            content = { content(Modifier) },
        )
    } else {
        content(modifier)
    }
}
