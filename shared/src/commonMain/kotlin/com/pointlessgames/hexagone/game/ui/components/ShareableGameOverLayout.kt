package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.RankingInfo
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.app_name
import hexagone.shared.generated.resources.combo_multiplier
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.label_level
import hexagone.shared.generated.resources.label_max_combo
import hexagone.shared.generated.resources.label_max_piece
import hexagone.shared.generated.resources.rank_global
import hexagone.shared.generated.resources.rank_regional
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ShareableGameOverLayout(
    score: Int,
    level: Int,
    maxCombo: Int,
    highestValue: Int,
    rankingInfo: RankingInfo?,
    playerName: String?,
    boardContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    
    Box(
        modifier = modifier
            .width(400.dp)
            .background(Color(0xFF1E2328), RoundedCornerShape(spacing.huge))
            .border(spacing.tiny, Color.White.copy(alpha = 0.1f), RoundedCornerShape(spacing.huge))
            .padding(vertical = spacing.huge, horizontal = spacing.extraLarge),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.extraLarge)
        ) {
            // Header: App Title or Logo could go here, but we will focus on Score
            Text(
                text = stringResource(Res.string.app_name).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = 4.sp,
            )

            // Score area
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = score.toString(),
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Black,
                    fontSize = 64.sp,
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.large)
                ) {
                    if (playerName != null) {
                        Text(
                            text = playerName,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        )
                    }
                    
                    rankingInfo?.let { rank ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFD54F).copy(alpha = 0.2f), RoundedCornerShape(spacing.large))
                                .border(spacing.extraTiny, Color(0xFFFFD54F), RoundedCornerShape(spacing.large))
                                .padding(horizontal = spacing.medium, vertical = spacing.extraSmall)
                        ) {
                            Text(
                                text = if (rank.isRegional) stringResource(Res.string.rank_regional, rank.rank)
                                       else stringResource(Res.string.rank_global, rank.rank),
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }

            // Board Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(spacing.extraLarge))
                    .padding(spacing.large),
                contentAlignment = Alignment.Center
            ) {
                boardContent()
            }

            // Stats Area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                GameOverStatHexagon(
                    label = stringResource(Res.string.label_level),
                    value = level.toString(),
                    backgroundColor = Color(0xFF37474F),
                    size = spacing.extraHuge
                )

                GameOverStatHexagon(
                    label = stringResource(Res.string.label_max_piece),
                    value = highestValue.toString(),
                    backgroundColor = Color(0xFF9345C4),
                    size = spacing.giant,
                    labelColor = Color.White,
                    glowAlphaProvider = { 0.5f },
                    glowColor = Color(0xFFBB86FC)
                )

                GameOverStatHexagon(
                    label = stringResource(Res.string.label_max_combo),
                    value = stringResource(Res.string.combo_multiplier, maxCombo),
                    backgroundColor = Color(0xFF5D4037),
                    size = spacing.extraHuge
                )
            }
        }
    }
}
