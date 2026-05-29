package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.theme.spacing

@Composable
internal fun DebugOverlay(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    selectedValue: Int?,
    isGhostMode: Boolean,
    onValueSelected: (Int?) -> Unit,
    onGhostModeToggled: () -> Unit,
    onPerkClick: (Perk) -> Unit,
    onClose: () -> Unit,
) {
    if (!isVisible) return

    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .border(
                1.dp, 
                Color.White.copy(alpha = 0.15f), 
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(24.dp)
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "DEBUG PAINTER",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = onClose) {
                Text("DONE", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Select Value", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))
                Spacer(Modifier.height(4.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ghost Mode", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                Switch(
                    checked = isGhostMode,
                    onCheckedChange = { onGhostModeToggled() },
                    modifier = Modifier.padding(start = 4.dp).height(24.dp)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(130.dp)
        ) {
            item {
                ValueItem(
                    value = null,
                    isSelected = selectedValue == null,
                    onClick = { onValueSelected(null) }
                )
            }
            items((1..30).toList()) { value ->
                ValueItem(
                    value = value,
                    isSelected = selectedValue == value,
                    onClick = { onValueSelected(value) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Inject Perk", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))
        Spacer(Modifier.height(4.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(Perk.entries) { perk ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.DarkGray, CircleShape)
                        .clickable { onPerkClick(perk) },
                    contentAlignment = Alignment.Center
                ) {
                    PerkIcon(modifier = Modifier.size(22.dp), perk = perk, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ValueItem(
    value: Int?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
                RoundedCornerShape(8.dp)
            )
            .border(
                1.5.dp,
                if (isSelected) Color.White else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (value == null) {
            Text("CLR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        } else {
            Text(
                value.toString(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }
    }
}
