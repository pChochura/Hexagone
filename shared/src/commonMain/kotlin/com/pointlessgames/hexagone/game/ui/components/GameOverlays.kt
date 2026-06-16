package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.model.ConfettiPiece
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.RankingInfo
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.view_stats_button
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
internal fun GameOverlays(
    modifier: Modifier = Modifier,
    isGameOverProvider: () -> Boolean,
    scoreProvider: () -> Int,
    bestScoreProvider: () -> Int,
    sessionBestScoreProvider: () -> Int,
    levelProvider: () -> Int,
    diamondsProvider: () -> Int = { 0 },
    maxComboProvider: () -> Int,
    totalMergesProvider: () -> Int,
    highestValueProvider: () -> Int,
    showBoardProvider: () -> Boolean,
    perkOptionsProvider: () -> List<Perk>,
    pendingLevelUpsProvider: () -> Int,
    canRerollProvider: () -> Boolean,
    onPerkSelected: (Perk) -> Unit,
    onRerollClicked: () -> Unit,
    onRestart: () -> Unit,
    onViewBoardToggle: () -> Unit,
    onShare: () -> Unit,
    onLeaderboard: () -> Unit,
    activeTierReward: Pair<com.pointlessgames.hexagone.game.model.ComboTier, com.pointlessgames.hexagone.game.model.Perk>?,
    onTierRewardFinished: () -> Unit,
    activeChallengeReward: com.pointlessgames.hexagone.game.model.GameEffect.DailyChallengeComplete?,
    persistentCompletedMissionIdsProvider: () -> Set<String> = { emptySet() },
    onChallengeRewardFinished: () -> Unit,
    rankingInfoProvider: () -> RankingInfo?,
    showReviveOptionProvider: () -> Boolean = { false },
    vouchersProvider: () -> Map<PerkCategory, Int> = { emptyMap() },
    onRevive: (PerkCategory) -> Unit = {},
    onBuyAndRevive: (PerkCategory) -> Unit = {},
    onOpenShop: () -> Unit = {},
    onDeclineRevive: () -> Unit = {},
    debugUsedProvider: () -> Boolean = { false },
    finalResultProvider: () -> com.pointlessgames.hexagone.game.model.DetailedGameResult? = { null },
) {
    val isGameOver = isGameOverProvider()
    val showReviveOption = showReviveOptionProvider()
    val vouchers = vouchersProvider()
    val bestScore = bestScoreProvider()
    val sessionBestScore = sessionBestScoreProvider()
    val level = levelProvider()
    val maxCombo = maxComboProvider()
    val totalMerges = totalMergesProvider()
    val highestValue = highestValueProvider()
    val showBoard = showBoardProvider()
    val perkOptions = perkOptionsProvider()
    val pendingLevelUps = pendingLevelUpsProvider()
    val canReroll = canRerollProvider()
    val rankingInfo = rankingInfoProvider()
    val finalResult = finalResultProvider()
    val isAnyOverlayVisible =
        perkOptions.isNotEmpty() || isGameOver || showReviveOption || activeTierReward != null || activeChallengeReward != null
    val dimAlphaState = animateFloatAsState(
        targetValue = if (activeTierReward != null || activeChallengeReward != null) 0.85f else if (isAnyOverlayVisible && !showBoard) 0.6f else 0f,
        animationSpec = tween(500),
        label = "dim_alpha",
    )

    val currentScore = scoreProvider()
    val isNewBest = isGameOver && currentScore >= bestScore && currentScore > 0
    val confettiPieces = remember { mutableStateListOf<ConfettiPiece>() }
    var hasSpawnedConfetti by remember(isGameOver) { mutableStateOf(false) }

    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius
    val primaryColor = MaterialTheme.colorScheme.primary
    val playButtonSound = com.pointlessgames.hexagone.utils.rememberPlayButtonSound()

    LaunchedEffect(isNewBest) {
        if (isNewBest && !hasSpawnedConfetti) {
            repeat(60) {
                val angle = Random.nextFloat() * PI.toFloat() + PI.toFloat()
                val speed = 200f + Random.nextFloat() * 500f
                confettiPieces.add(
                    ConfettiPiece(
                        x = 0f,
                        y = 0f,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        rotation = Random.nextFloat() * 360f,
                        rotationSpeed = (-200..200).random().toFloat(),
                        color = if (Random.nextBoolean()) primaryColor else Color.White,
                        life = 2.5f + Random.nextFloat() * 2f,
                        size = 8f + Random.nextFloat() * 8f,
                        flipSpeed = 3f + Random.nextFloat() * 5f,
                    ),
                )
            }
            hasSpawnedConfetti = true

            while (confettiPieces.isNotEmpty()) {
                delay(16)
                val dt = 0.016f
                val iterator = confettiPieces.listIterator()
                while (iterator.hasNext()) {
                    val c = iterator.next()
                    if (c.life <= 0) {
                        iterator.remove()
                    } else {
                        iterator.set(
                            c.copy(
                                x = c.x + c.vx * dt + sin(c.life * 5f) * 2f,
                                y = c.y + c.vy * dt,
                                vy = c.vy + 600f * dt,
                                vx = c.vx * 0.98f,
                                rotation = c.rotation + c.rotationSpeed * dt,
                                life = c.life - dt,
                            ),
                        )
                    }
                }
            }
        } else if (!isGameOver) {
            confettiPieces.clear()
        }
    }

    if (isAnyOverlayVisible || dimAlphaState.value > 0f) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = dimAlphaState.value }
                    .background(Color.Black)
                    .clickable(
                        enabled = isAnyOverlayVisible && !showBoard,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                    },
            )

            AnimatedVisibility(
                visible = activeTierReward != null,
                enter = fadeIn(tween(600)) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(600, easing = EaseOutExpo),
                ),
                exit = fadeOut(tween(400)) + scaleOut(
                    targetScale = 1.2f,
                    animationSpec = tween(400, easing = EaseInExpo),
                ),
                modifier = Modifier.align(Alignment.Center),
            ) {
                activeTierReward?.let { (tier, perk) ->
                    TierRewardOverlay(
                        tier = tier,
                        perk = perk,
                        onFinished = onTierRewardFinished,
                    )
                }
            }

            AnimatedVisibility(
                visible = activeChallengeReward != null,
                enter = fadeIn(tween(600)) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(600, easing = EaseOutExpo),
                ),
                exit = fadeOut(tween(400)) + scaleOut(
                    targetScale = 1.2f,
                    animationSpec = tween(400, easing = EaseInExpo),
                ),
                modifier = Modifier.align(Alignment.Center),
            ) {
                activeChallengeReward?.let { effect ->
                    DailyChallengeRewardOverlay(
                        challenge = effect.challenge,
                        isFirstTimeToday = effect.isFirstTimeToday,
                        isDayCompleted = effect.isDayCompleted,
                        newStreak = effect.newStreak,
                        onFinished = onChallengeRewardFinished,
                    )
                }
            }

            if (perkOptions.isNotEmpty() && activeTierReward == null && activeChallengeReward == null && !showReviveOption) {
                PerkSelectionDialog(
                    options = perkOptions,
                    pendingLevelUps = pendingLevelUps,
                    canReroll = canReroll,
                    onPerkSelected = onPerkSelected,
                    onRerollClicked = onRerollClicked,
                )
            }

            AnimatedVisibility(
                visible = showReviveOption && !showBoard,
                enter = fadeIn(tween(600)) + scaleIn(initialScale = 0.9f),
                exit = fadeOut(tween(300)) + scaleOut(targetScale = 1.1f),
                modifier = Modifier.align(Alignment.Center),
            ) {
                ReviveDialog(
                    score = scoreProvider(),
                    level = level,
                    diamonds = diamondsProvider(),
                    vouchers = vouchers,
                    onRevive = onRevive,
                    onBuyAndRevive = onBuyAndRevive,
                    onOpenShop = onOpenShop,
                    onDecline = onDeclineRevive
                )
            }

            AnimatedVisibility(
                visible = isGameOver && !showBoard && !showReviveOption,
                enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { -it / 8 }),
                exit = fadeOut(tween(400)) + scaleOut(targetScale = 0.9f),
                modifier = Modifier.align(Alignment.Center),
            ) {
                GameOverDialog(
                    score = scoreProvider(),
                    bestScore = sessionBestScore,
                    level = level,
                    maxCombo = maxCombo,
                    totalMerges = totalMerges,
                    highestValue = highestValue,
                    rankingInfo = rankingInfo,
                    dailyChallenges = finalResult?.dailyChallenges ?: emptyList(),
                    persistentCompletedMissionIds = persistentCompletedMissionIdsProvider(),
                    debugUsed = debugUsedProvider(),
                    onViewBoard = onViewBoardToggle,
                    onRestart = onRestart,
                    onShare = onShare,
                    onLeaderboard = onLeaderboard,
                )
            }


            AnimatedVisibility(
                visible = !isLandscape && isGameOver && !showBoard,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                GameOverBottomActions(
                    onRestart = onRestart,
                    onShare = onShare,
                    onLeaderboard = onLeaderboard,
                )
            }

            if (confettiPieces.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            withTransform(
                                {
                                    translate(size.width / 2, size.height / 3)
                                },
                            ) {
                                confettiPieces.forEach { c ->
                                    rotate(c.rotation, Offset(c.x, c.y)) {
                                        val flipScale = abs(sin(c.life * c.flipSpeed))
                                        drawRect(
                                            color = c.color.copy(
                                                alpha = (c.life * 1.5f).coerceIn(
                                                    0f,
                                                    1f,
                                                ),
                                            ),
                                            topLeft = Offset(
                                                c.x - c.size / 2,
                                                c.y - (c.size * flipScale) / 2,
                                            ),
                                            size = Size(c.size, c.size * flipScale),
                                        )
                                    }
                                }
                            }
                        },
                )
            }

            if (isGameOver && showBoard) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                        .padding(bottom = if (isLandscape) spacing.medium.scaled else spacing.extraHuge.scaled),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(cornerRadius.full))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(
                                spacing.extraTiny,
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(cornerRadius.full),
                            )
                            .clickable { 
                                playButtonSound()
                                onViewBoardToggle() 
                            }
                            .padding(horizontal = spacing.extraLarge.scaled, vertical = spacing.medium.scaled),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.view_stats_button).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp.scaled,
                            letterSpacing = 1.sp.scaled,
                        )
                    }
                }
            }
        }
    }
}
