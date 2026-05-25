package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PreviewCell

@Composable
fun NextPieceSection(
    previewState: List<PreviewCell>,
    activePerk: Perk?,
    selectedCellId: String?,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = activePerk,
        modifier = modifier.wrapContentHeight().fillMaxWidth(),
        contentAlignment = Alignment.Center,
        transitionSpec = {
            (fadeIn() togetherWith fadeOut()).using(SizeTransform(clip = false))
        },
        label = "next_piece_perk"
    ) { activePerk ->
        Column(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (activePerk != null) {
                Text(
                    text = "ACTIVE PERK: ${activePerk.displayName}",
                    color = Color(0xFFF06292),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Text(
                    text = when (activePerk) {
                        Perk.MOVE_TILE -> if (selectedCellId == null) "Select a tile to move" else "Select empty spot"
                        Perk.REMOVE_TILE -> "Select a tile to remove"
                        Perk.SWAP_TILES -> if (selectedCellId == null) "Select first tile to swap" else "Select second tile to swap"
                        else -> "Select an empty spot for fusion"
                    },
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                )
            } else {
                Text(
                    text = "NEXT PIECE",
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )

                Spacer(Modifier.height(8.dp))

                Box(modifier = Modifier.size(60.dp)) {
                    val nextValue = previewState.firstOrNull()?.value ?: 1
                    Hexagon(
                        value = nextValue.toString(),
                        backgroundColor = HexagonGridDefaults.getColorForValue(nextValue),
                        modifier = Modifier.fillMaxSize().aspectRatio(1 / 0.866f),
                    )
                }
            }
        }
    }
}
