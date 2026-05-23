package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScoreSection(
    score: Int,
    bestScore: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScoreCard(label = "SCORE", value = score.toString(), modifier = Modifier.weight(1f))
        ScoreCard(
            label = "BEST",
            value = bestScore.toString(),
            modifier = Modifier.weight(1f),
            isBest = true,
        )
    }
}

@Composable
private fun ScoreCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isBest: Boolean = false,
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1C1C24), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
        )
        Text(
            text = value,
            color = if (isBest) Color(0xFFF06292) else Color(0xFF9FA8DA),
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
        )
    }
}
