package com.pointlessgames.hexagone.auth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing

@Composable
internal fun AuthButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White.copy(alpha = 0.1f),
) {
    val playSound = com.pointlessgames.hexagone.utils.rememberPlayButtonSound()
    val spacing = MaterialTheme.spacing
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(containerColor)
            .border(1.dp.scaled, borderColor, CircleShape)
            .clickable { playSound(); onClick() }
            .padding(spacing.medium.scaled),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            color = contentColor,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp.scaled,
            letterSpacing = 1.sp.scaled,
            textAlign = TextAlign.Center,
        )
    }
}
