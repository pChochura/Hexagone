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
import org.jetbrains.compose.resources.painterResource

@Composable
fun BottomSheetTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
        color = color,
        letterSpacing = 4.sp,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun DialogTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cornerRadius = MaterialTheme.cornerRadius
    val spacing = MaterialTheme.spacing
    val shape = RoundedCornerShape(cornerRadius.medium)
    val primaryColor = MaterialTheme.colorScheme.primary

    val brush = androidx.compose.ui.graphics.Brush.verticalGradient(
        if (isSelected) {
            listOf(primaryColor, primaryColor.copy(alpha = 0.7f))
        } else {
            listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.01f))
        }
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .background(brush, shape)
            .border(
                width = spacing.extraTiny,
                color = if (isSelected) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f),
                shape = shape
            )
            .clip(shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
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
    size: Dp = 64.dp
) {
    val spacing = MaterialTheme.spacing
    val content = @Composable {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = size, height = size * 0.866f)
                    .clip(FlatTopHexagonShape())
                    .background(backgroundColor)
                    .border(spacing.extraTiny, borderColor, FlatTopHexagonShape())
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(size * 0.45f),
                    tint = Color.White
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
                    lineHeight = 11.sp
                )
            }
        }
    }

    if (tooltip != null && label == null) {
        Tooltip(
            position = tooltipPosition,
            contentDescription = tooltip,
            content = content
        )
    } else {
        content()
    }
}
