package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.Particle
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.components.Position
import com.pointlessgames.hexagone.ui.components.Tooltip
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.choose_your_perk
import hexagone.shared.generated.resources.game_over_subtitle
import hexagone.shared.generated.resources.game_over_title
import hexagone.shared.generated.resources.level_up_title
import hexagone.shared.generated.resources.new_best_label
import hexagone.shared.generated.resources.no_more_moves_title
import hexagone.shared.generated.resources.perk_selection_hint
import hexagone.shared.generated.resources.play_again_button
import hexagone.shared.generated.resources.reroll_perks
import hexagone.shared.generated.resources.restart_game_button
import hexagone.shared.generated.resources.stat_level
import hexagone.shared.generated.resources.stat_max_combo
import hexagone.shared.generated.resources.stat_max_piece
import hexagone.shared.generated.resources.stat_merges
import hexagone.shared.generated.resources.try_perk_subtitle
import hexagone.shared.generated.resources.view_board_button
import hexagone.shared.generated.resources.view_stats_button
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameOverlays(
    isGameOver: Boolean,
    scoreProvider: () -> Int,
    bestScore: Int,
    level: Int,
    maxCombo: Int,
    totalMerges: Int,
    highestValue: Int,
    showBoard: Boolean,
    perkOptions: List<Perk>,
    pendingLevelUps: Int,
    canReroll: Boolean,
    onPerkSelected: (Perk) -> Unit,
    onRerollClicked: () -> Unit,
    onRestart: () -> Unit,
    onViewBoardToggle: () -> Unit,
    onShare: () -> Unit,
    onLeaderboard: () -> Unit
) {
    val isAnyOverlayVisible = perkOptions.isNotEmpty() || isGameOver
    val dimAlphaState = animateFloatAsState(
        targetValue = if (isAnyOverlayVisible && !showBoard) 0.5f else 0f,
        animationSpec = tween(500),
        label = "dim_alpha"
    )

    val currentScore = scoreProvider()
    val isNewBest = isGameOver && currentScore >= bestScore && currentScore > 0
    val confettiPieces = remember { androidx.compose.runtime.mutableStateListOf<ConfettiPiece>() }
    var hasSpawnedConfetti by remember(isGameOver) { mutableStateOf(false) }

    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius
    val primaryColor = MaterialTheme.colorScheme.primary
    androidx.compose.runtime.LaunchedEffect(isNewBest) {
        if (isNewBest && !hasSpawnedConfetti) {
            repeat(60) {
                val angle = kotlin.random.Random.nextFloat() * kotlin.math.PI.toFloat() + kotlin.math.PI.toFloat()
                val speed = 200f + kotlin.random.Random.nextFloat() * 500f
                confettiPieces.add(
                    ConfettiPiece(
                        x = 0f,
                        y = 0f,
                        vx = kotlin.math.cos(angle) * speed,
                        vy = kotlin.math.sin(angle) * speed,
                        rotation = kotlin.random.Random.nextFloat() * 360f,
                        rotationSpeed = (-200..200).random().toFloat(),
                        color = if (kotlin.random.Random.nextBoolean()) primaryColor else Color.White,
                        life = 2.5f + kotlin.random.Random.nextFloat() * 2f,
                        size = 8f + kotlin.random.Random.nextFloat() * 8f,
                        flipSpeed = 3f + kotlin.random.Random.nextFloat() * 5f
                    )
                )
            }
            hasSpawnedConfetti = true
            
            while (confettiPieces.isNotEmpty()) {
                kotlinx.coroutines.delay(16)
                val dt = 0.016f
                val iterator = confettiPieces.listIterator()
                while (iterator.hasNext()) {
                    val c = iterator.next()
                    if (c.life <= 0) {
                        iterator.remove()
                    } else {
                        iterator.set(
                            c.copy(
                                x = c.x + c.vx * dt + kotlin.math.sin(c.life * 5f) * 2f,
                                y = c.y + c.vy * dt,
                                vy = c.vy + 600f * dt,
                                vx = c.vx * 0.98f,
                                rotation = c.rotation + c.rotationSpeed * dt,
                                life = c.life - dt
                            )
                        )
                    }
                }
            }
        } else if (!isGameOver) {
            confettiPieces.clear()
        }
    }

    if (isAnyOverlayVisible || dimAlphaState.value > 0f) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = dimAlphaState.value }
                    .background(Color.Black)
                    .clickable(
                        enabled = isAnyOverlayVisible && !showBoard,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Consume clicks */ }
            )

            AnimatedVisibility(
                visible = perkOptions.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn() + scaleIn(initialScale = 0.9f),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                PerkSelectionDialog(
                    options = perkOptions,
                    pendingLevelUps = pendingLevelUps,
                    canReroll = canReroll,
                    onPerkSelected = onPerkSelected,
                    onRerollClicked = onRerollClicked,
                )
            }

            AnimatedVisibility(
                visible = isGameOver && !showBoard,
                enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { -it / 8 }),
                exit = fadeOut(tween(400)) + scaleOut(targetScale = 0.9f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                GameOverDialog(
                    score = scoreProvider(),
                    bestScore = bestScore,
                    level = level,
                    maxCombo = maxCombo,
                    totalMerges = totalMerges,
                    highestValue = highestValue,
                    onViewBoard = onViewBoardToggle
                )
            }

            AnimatedVisibility(
                visible = isGameOver && !showBoard,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                GameOverBottomActions(
                    onRestart = onRestart,
                    onShare = onShare,
                    onLeaderboard = onLeaderboard
                )
            }

            if (confettiPieces.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            withTransform({
                                translate(size.width / 2, size.height / 3)
                            }) {
                                confettiPieces.forEach { c ->
                                    rotate(c.rotation, Offset(c.x, c.y)) {
                                        val flipScale = kotlin.math.abs(kotlin.math.sin(c.life * c.flipSpeed))
                                        drawRect(
                                            color = c.color.copy(alpha = (c.life * 1.5f).coerceIn(0f, 1f)),
                                            topLeft = Offset(c.x - c.size / 2, c.y - (c.size * flipScale) / 2),
                                            size = androidx.compose.ui.geometry.Size(c.size, c.size * flipScale)
                                        )
                                    }
                                }
                            }
                        }
                )
            }

            if (isGameOver && showBoard) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .padding(bottom = spacing.massive),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(cornerRadius.full))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(spacing.extraTiny, Color.White.copy(alpha = 0.2f), RoundedCornerShape(cornerRadius.full))
                            .clickable { onViewBoardToggle() }
                            .padding(horizontal = spacing.extraLarge, vertical = spacing.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(Res.string.view_stats_button).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PerkSelectionDialog(
    options: List<Perk>,
    pendingLevelUps: Int,
    canReroll: Boolean,
    onPerkSelected: (Perk) -> Unit,
    onRerollClicked: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "levelup_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                ),
            )
            .padding(top = spacing.colossal),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge))
                .border(
                    spacing.tiny,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge),
                )
                .navigationBarsPadding()
                .padding(horizontal = spacing.extraLarge, vertical = spacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .padding(bottom = spacing.extraLarge)
                    .size(width = spacing.extraHuge, height = spacing.extraSmall)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.level_up_title),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                    )

                    if (pendingLevelUps > 1) {
                        Spacer(Modifier.width(spacing.medium))
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                                .border(spacing.extraTiny, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                                .padding(horizontal = spacing.small, vertical = spacing.tiny)
                        ) {
                            Text(
                                text = "+$pendingLevelUps",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (canReroll) {
                    Tooltip(
                        position = Position.BELOW,
                        contentDescription = Res.string.reroll_perks,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(spacing.extraHuge)
                                .clip(CircleShape)
                                .clickable { onRerollClicked() }
                                .padding(spacing.small),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎲",
                                fontSize = 22.sp,
                                modifier = Modifier.graphicsLayer { alpha = 0.6f }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(spacing.small))

            Text(
                text = stringResource(Res.string.choose_your_perk),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(spacing.extraHuge))

            AnimatedContent(
                targetState = options,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.8f))
                        .togetherWith(fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.8f))
                        .using(SizeTransform(clip = false))
                },
                label = "perk_refresh"
            ) { perkOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                ) {
                    perkOptions.forEach { perk ->
                        PerkButton(
                            perk = perk,
                            onClick = { onPerkSelected(perk) },
                            modifier = Modifier.weight(1f),
                            tooltipDescription = perk.descriptionRes,
                            buttonSize = spacing.giant
                        )
                    }
                }
            }

            Spacer(Modifier.height(spacing.extraLarge))

            Text(
                text = stringResource(Res.string.perk_selection_hint),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private data class ConfettiPiece(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val color: Color,
    val life: Float,
    val size: Float,
    val flipSpeed: Float
)

@Composable
private fun GameOverDialog(
    score: Int,
    bestScore: Int,
    level: Int,
    maxCombo: Int,
    totalMerges: Int,
    highestValue: Int,
    onViewBoard: () -> Unit,
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(1500),
        label = "score_count_up"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "gameover_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val isNewBest = score >= bestScore && score > 0
    val badgeTransition = rememberInfiniteTransition(label = "new_best_pulse")
    val badgeScale by badgeTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
        label = "badge_scale"
    )
    val badgeRotation by badgeTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Reverse),
        label = "badge_rotation"
    )

    val maxPieceTransition = rememberInfiniteTransition(label = "max_piece_shift")
    val maxPieceShift by maxPieceTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "max_piece_shift"
    )

    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .drawBehind {
                val baseColor = primaryColor
                val cr = cornerRadius.extraLarge.toPx()
                val path = Path().apply {
                    addRoundRect(RoundRect(Rect(0f, 0f, size.width, size.height), cr, cr))
                }

                // Restore Layered strokes for glow
                for (i in 1..3) {
                    drawPath(
                        path = path,
                        color = baseColor.copy(alpha = glowAlpha / (i * 2f)),
                        style = Stroke(width = (spacing.extraSmall * i).toPx())
                    )
                }
            }
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f), RoundedCornerShape(cornerRadius.extraLarge))
            .border(spacing.extraTiny, Color.White.copy(alpha = 0.1f), RoundedCornerShape(cornerRadius.extraLarge))
            .padding(spacing.extraLarge),
    ) {
        // View Board Icon Button (Canvas drawn)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(spacing.extraHuge)
                .clip(CircleShape)
                .clickable { onViewBoard() }
                .padding(spacing.small),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = spacing.extraTiny.toPx() * 1.5f
                val color = Color.White.copy(alpha = 0.4f)
                // Eyeball shape
                val path = Path().apply {
                    moveTo(0f, size.height / 2)
                    quadraticTo(size.width / 2, -size.height / 4, size.width, size.height / 2)
                    quadraticTo(size.width / 2, size.height * 1.25f, 0f, size.height / 2)
                }
                drawPath(path, color, style = Stroke(stroke))
                drawCircle(color, radius = size.width / 5, center = center, style = Stroke(stroke))
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.game_over_title).uppercase(),
                color = Color.White.copy(alpha = 0.3f),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                letterSpacing = 4.sp
            )

            Spacer(Modifier.height(spacing.small))

            // Hero Section: Score & Max Piece
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Text(
                            text = animatedScore.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 72.sp,
                            textAlign = TextAlign.Center,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha * 2f),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 30f
                                )
                            ),
                            modifier = Modifier.padding(horizontal = spacing.semiLarge)
                        )
                        
                        if (isNewBest) {
                            Box(
                                modifier = Modifier
                                    .offset(x = spacing.semiMedium, y = -spacing.semiSmall)
                                    .graphicsLayer {
                                        scaleX = badgeScale
                                        scaleY = badgeScale
                                        rotationZ = badgeRotation
                                    }
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(cornerRadius.full))
                                    .padding(horizontal = spacing.small, vertical = spacing.tiny)
                            ) {
                                Text(
                                    text = stringResource(Res.string.new_best_label).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 8.sp,
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = "BEST: $bestScore",
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(Modifier.width(spacing.extraLarge))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        val colorScheme = MaterialTheme.colorScheme
                        val baseColor = HexagonGridDefaults.getColorForValue(highestValue, colorScheme)
                        val shiftColor = Color(
                            (baseColor.red * 0.6f + 0.4f).coerceIn(0f, 1f),
                            (baseColor.green * 0.6f + 0.4f).coerceIn(0f, 1f),
                            (baseColor.blue * 0.6f + 0.4f).coerceIn(0f, 1f),
                            1f
                        )
                        
                        // Interpolate colors for the gradient animation
                        val c1 = androidx.compose.ui.graphics.lerp(baseColor, shiftColor, maxPieceShift)
                        val c2 = androidx.compose.ui.graphics.lerp(shiftColor, baseColor, maxPieceShift)
                        
                        Hexagon(
                            value = highestValue.toString(),
                            backgroundColor = Color.Transparent,
                            modifier = Modifier
                                .size(spacing.giant)
                                .aspectRatio(1 / 0.866f)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(c1, c2, c1),
                                        start = Offset(0f, 0f),
                                        end = Offset.Infinite
                                    ),
                                    shape = FlatTopHexagonShape()
                                )
                                .border(spacing.extraTiny, Color.White.copy(alpha = 0.15f), FlatTopHexagonShape())
                        )
                    }
                    Spacer(Modifier.height(spacing.extraSmall))
                    Text(
                        text = "MAX",
                        color = Color.White.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(spacing.extraHuge))

            // Organic Stats Row with Canvas Icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.small),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OrganicStatItem(
                    icon = { LevelIcon(spacing) },
                    label = stringResource(Res.string.stat_level),
                    value = level.toString()
                )
                OrganicStatItem(
                    icon = { ComboIcon(spacing) },
                    label = stringResource(Res.string.stat_max_combo),
                    value = "x$maxCombo"
                )
                OrganicStatItem(
                    icon = { MergeIcon(spacing) },
                    label = stringResource(Res.string.stat_merges),
                    value = totalMerges.toString()
                )
            }
        }
    }
}

@Composable
private fun LevelIcon(spacing: com.pointlessgames.hexagone.ui.theme.Spacing) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.size(spacing.semiLarge)) {
        val stroke = spacing.tiny.toPx()
        // Staircase shape
        drawLine(color, Offset(0f, size.height), Offset(size.width, 0f), stroke)
        drawLine(color, Offset(size.width, 0f), Offset(size.width * 0.6f, 0f), stroke)
        drawLine(color, Offset(size.width, 0f), Offset(size.width, size.height * 0.4f), stroke)
    }
}

@Composable
private fun ComboIcon(spacing: com.pointlessgames.hexagone.ui.theme.Spacing) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.size(spacing.semiLarge)) {
        val stroke = spacing.tiny.toPx()
        // Lightning bolt / Flash shape
        val path = Path().apply {
            moveTo(size.width * 0.6f, 0f)
            lineTo(size.width * 0.2f, size.height * 0.6f)
            lineTo(size.width * 0.5f, size.height * 0.6f)
            lineTo(size.width * 0.4f, size.height)
            lineTo(size.width * 0.8f, size.height * 0.4f)
            lineTo(size.width * 0.5f, size.height * 0.4f)
            close()
        }
        drawPath(path, color, style = Stroke(stroke))
    }
}

@Composable
private fun MergeIcon(spacing: com.pointlessgames.hexagone.ui.theme.Spacing) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.size(spacing.semiLarge)) {
        val stroke = spacing.extraTiny.toPx() * 1.5f
        // Three merging circles
        drawCircle(color, radius = size.width / 4, center = Offset(size.width / 2, size.height / 3), style = Stroke(stroke))
        drawCircle(color, radius = size.width / 4, center = Offset(size.width / 3, size.height * 0.7f), style = Stroke(stroke))
        drawCircle(color, radius = size.width / 4, center = Offset(size.width * 0.66f, size.height * 0.7f), style = Stroke(stroke))
    }
}

@Composable
private fun OrganicStatItem(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(spacing.extraHuge)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(spacing.extraTiny, Color.White.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(Modifier.height(spacing.small))
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
        )
        Text(
            text = label.uppercase(),
            color = Color.White.copy(alpha = 0.3f),
            fontWeight = FontWeight.Bold,
            fontSize = 8.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun GameOverBottomActions(
    onRestart: () -> Unit,
    onShare: () -> Unit,
    onLeaderboard: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = spacing.extraLarge, vertical = spacing.huge)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SecondaryGameButton(
                onClick = onShare,
                icon = "⤴",
                modifier = Modifier.size(spacing.giant),
                spacing = spacing,
                cornerRadius = cornerRadius
            )

            Button(
                onClick = onRestart,
                modifier = Modifier
                    .weight(1f)
                    .height(spacing.giant)
                    .graphicsLayer {
                        shadowElevation = spacing.small.toPx()
                        shape = RoundedCornerShape(cornerRadius.medium)
                        clip = true
                    },
                shape = RoundedCornerShape(cornerRadius.medium),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = spacing.small)
            ) {
                Text(
                    text = stringResource(Res.string.play_again_button).uppercase(),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.5.sp
                )
            }

            SecondaryGameButton(
                onClick = onLeaderboard,
                icon = "🏆",
                modifier = Modifier.size(spacing.giant),
                spacing = spacing,
                cornerRadius = cornerRadius
            )
        }
    }
}


@Composable
private fun StatusDialog(
    isGameOver: Boolean,
    collectedPerks: List<Perk>,
    onUsePerk: (Perk) -> Unit,
    onRestart: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                ),
            )
            .padding(top = spacing.giant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge))
                .border(
                    spacing.extraTiny,
                    Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge),
                )
                .navigationBarsPadding()
                .padding(horizontal = spacing.extraLarge, vertical = spacing.semiLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .padding(bottom = spacing.semiLarge)
                    .size(width = spacing.extraHuge, height = spacing.extraSmall)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                Canvas(
                    modifier = Modifier
                        .size(spacing.extraHuge)
                        .border(spacing.extraTiny, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                        .padding(spacing.small)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                ) {
                    val strokeWidth = spacing.tiny.toPx()
                    drawCircle(
                        color = primaryColor,
                        radius = size.minDimension / 2.5f,
                        style = Stroke(width = strokeWidth),
                    )
                    val radius = size.minDimension / 2.5f
                    val angle = 45f * (PI.toFloat() / 180f)
                    drawLine(
                        color = primaryColor,
                        start = center + Offset(cos(angle) * radius, sin(angle) * radius),
                        end = center - Offset(cos(angle) * radius, sin(angle) * radius),
                        strokeWidth = strokeWidth,
                    )
                }

                Spacer(Modifier.width(spacing.large))

                Column {
                    Text(
                        text = if (isGameOver) stringResource(Res.string.game_over_title) else stringResource(Res.string.no_more_moves_title),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                    Text(
                        text = if (isGameOver) stringResource(Res.string.game_over_subtitle) else stringResource(Res.string.try_perk_subtitle),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                    )
                }
            }

            Spacer(Modifier.height(spacing.extraLarge))

            val displayPerks = collectedPerks.distinct().filter { it.canSaveFromStuck }

            if (displayPerks.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    displayPerks.forEach { perk ->
                        val count = collectedPerks.count { it == perk }
                        PerkButton(
                            perk = perk,
                            onClick = { onUsePerk(perk) },
                            count = count,
                            tooltipDescription = perk.descriptionRes,
                            buttonSize = spacing.extraHuge
                        )
                        Spacer(Modifier.width(spacing.medium))
                    }
                }
                Spacer(Modifier.height(spacing.medium))
            }

            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth().height(spacing.extraHuge),
                shape = RoundedCornerShape(spacing.medium),
                border = androidx.compose.foundation.BorderStroke(
                    spacing.extraTiny,
                    Color.White.copy(alpha = 0.3f),
                ),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            ) {
                Text(
                    text = stringResource(Res.string.restart_game_button),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun SecondaryGameButton(
    onClick: () -> Unit,
    icon: String,
    modifier: Modifier = Modifier,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing,
    cornerRadius: com.pointlessgames.hexagone.ui.theme.CornerRadius,
) {
    Box(
        modifier = modifier
            .height(spacing.extraHuge)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(cornerRadius.medium))
            .border(spacing.extraTiny, Color.White.copy(alpha = 0.1f), RoundedCornerShape(cornerRadius.medium))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, color = Color.White, fontSize = 20.sp)
    }
}
