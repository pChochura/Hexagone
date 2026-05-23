package com.pointlessgames.hexagone.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.GameViewModel
import com.pointlessgames.hexagone.game.model.Particle
import com.pointlessgames.hexagone.game.model.Perk
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
internal fun GameScreen(viewModel: GameViewModel) {
    val gridState by viewModel.gridState.collectAsState()
    val previewState by viewModel.previewState.collectAsState()
    val pendingMerge by viewModel.pendingMerge.collectAsState()
    val score by viewModel.score.collectAsState()
    val bestScore by viewModel.bestScore.collectAsState()
    val level by viewModel.level.collectAsState()
    val isStuck by viewModel.isStuck.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()
    val perkOptions by viewModel.perkOptions.collectAsState()
    val collectedPerks by viewModel.collectedPerks.collectAsState()
    val activePerk by viewModel.activePerk.collectAsState()
    val selectedCellId by viewModel.selectedCellId.collectAsState()
    val density = LocalDensity.current

    var finishedMergeCount by remember { mutableStateOf(0) }
    var particles by remember { mutableStateOf(emptyList<Particle>()) }

    LaunchedEffect(particles.isNotEmpty()) {
        if (particles.isEmpty()) return@LaunchedEffect
        var lastTime = withFrameNanos { it }
        while (particles.isNotEmpty()) {
            val currentTime = withFrameNanos { it }
            val dt = (currentTime - lastTime) / 1_000_000_000f
            lastTime = currentTime

            particles = particles.mapNotNull { p ->
                if (p.life <= 0) null
                else p.copy(
                    x = p.x + p.vx * dt,
                    y = p.y + p.vy * dt,
                    life = p.life - dt * 2f,
                )
            }
        }
    }

    LaunchedEffect(pendingMerge) {
        if (pendingMerge != null) {
            finishedMergeCount = 0
        }
    }

    LaunchedEffect(finishedMergeCount, pendingMerge) {
        val merge = pendingMerge
        if (merge != null && finishedMergeCount >= merge.mergingCells.size) {
            viewModel.onMergeAnimationFinished()
        }
    }

    val moveAnimationSpec = remember {
        spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
            visibilityThreshold = IntOffset(1, 1),
        )
    }

    val gridAlpha by animateFloatAsState(
        targetValue = if (isGameOver) 0.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "grid_alpha",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1C1C24), Color(0xFF0A0A0E)),
                ),
            )
            .systemBarsPadding()
            .graphicsLayer { alpha = gridAlpha }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Score section
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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

        Spacer(Modifier.height(24.dp))

        if (activePerk != null) {
            Text(
                text = "ACTIVE PERK: ${activePerk?.displayName}",
                color = Color(0xFFF06292),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Text(
                text = if (activePerk == Perk.MOVE_TILE && selectedCellId == null) "Select a tile to move"
                else if (activePerk == Perk.MOVE_TILE) "Select empty spot"
                else if (activePerk == Perk.REMOVE_TILE) "Select a tile to remove"
                else "Select an empty spot for fusion",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
            )
        } else {
            // Next Piece section
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

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            val columns = 5
            val rows = 4
            val itemGap = 4.dp

            BoxWithConstraints {
                val gapPx = with(density) { itemGap.toPx() }
                val cellWidth = constraints.maxWidth / (1f + (columns - 1) * 0.75f)
                val cellHeight = cellWidth * (sqrt(3f) / 2f)

                val itemWidth = (cellWidth - gapPx).coerceAtLeast(0f)
                val itemHeight = (cellHeight - gapPx).coerceAtLeast(0f)

                LaunchedEffect(finishedMergeCount, pendingMerge) {
                    val merge = pendingMerge
                    if (merge != null && finishedMergeCount >= merge.mergingCells.size) {
                        val targetOffset = HexagonGridDefaults.calculateOffset(
                            merge.targetX,
                            merge.targetY,
                            cellWidth,
                            cellHeight,
                            gapPx,
                        )
                        val center = Offset(
                            targetOffset.x + itemWidth / 2,
                            targetOffset.y + itemHeight / 2,
                        )
                        val color = HexagonGridDefaults.getColorForValue(merge.newValue)

                        val newParticles = List(30) {
                            val angle = Random.nextFloat() * 2 * PI.toFloat()
                            val speed = Random.nextFloat() * 400f + 200f
                            Particle(
                                id = Random.nextLong(),
                                x = center.x,
                                y = center.y,
                                vx = cos(angle) * speed,
                                vy = sin(angle) * speed,
                                color = color,
                                life = 1f,
                                size = Random.nextFloat() * 8f + 4f,
                            )
                        }
                        particles = particles + newParticles
                    }
                }

                HexagonGrid(
                    columns = columns,
                    rows = rows,
                    itemGap = itemGap,
                    outlineContent = { col, row ->
                        Hexagon(
                            modifier = Modifier.fillMaxSize(),
                            onClick = { viewModel.onEmptySpaceClicked(col, row) },
                            isOutline = true,
                        )
                    },
                ) {
                    // Draw preview hexagons (ghosts)
                    previewState.forEach { preview ->
                        key(preview.id) {
                            val targetOffset = HexagonGridDefaults.calculateOffset(
                                preview.x,
                                preview.y,
                                cellWidth,
                                cellHeight,
                                gapPx,
                            )
                            val animatedOffset by animateIntOffsetAsState(
                                targetValue = targetOffset,
                                animationSpec = moveAnimationSpec,
                                label = "ghost_offset",
                            )
                            val targetScale = when (preview.rank) {
                                0 -> 0.8f
                                1 -> 0.6f
                                else -> 0.4f
                            }
                            val animatedScale by animateFloatAsState(
                                targetValue = targetScale,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = 1800f,
                                ),
                                label = "ghost_scale",
                            )
                            Hexagon(
                                value = preview.value.toString(),
                                backgroundColor = HexagonGridDefaults.getColorForValue(preview.value)
                                    .copy(alpha = 0.3f),
                                modifier = Modifier
                                    .size(
                                        width = with(density) { itemWidth.toDp() },
                                        height = with(density) { itemHeight.toDp() },
                                    )
                                    .offset { animatedOffset }
                                    .graphicsLayer {
                                        scaleX = animatedScale
                                        scaleY = animatedScale
                                    },
                                onClick = if (activePerk != null) {
                                    { viewModel.onPreviewClicked(preview) }
                                } else null,
                            )
                        }
                    }

                    // Draw actual grid tiles
                    gridState.forEach { cell ->
                        key(cell.id) {
                            val targetOffset = HexagonGridDefaults.calculateOffset(
                                cell.x,
                                cell.y,
                                cellWidth,
                                cellHeight,
                                gapPx,
                            )
                            val animatedOffset by animateIntOffsetAsState(
                                targetValue = targetOffset,
                                animationSpec = moveAnimationSpec,
                                label = "cell_offset",
                                finishedListener = {
                                    if (pendingMerge?.mergingCells?.any { it.id == cell.id } == true) {
                                        finishedMergeCount++
                                    }
                                },
                            )

                            var targetScale by remember { mutableStateOf(0f) }
                            val scale by animateFloatAsState(
                                targetValue = targetScale,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium,
                                ),
                                label = "cell_scale",
                            )

                            val alpha by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessMedium,
                                ),
                                label = "cell_alpha",
                            )
                            LaunchedEffect(Unit) {
                                targetScale = 1f
                            }

                            val isSelected = selectedCellId == cell.id

                            Hexagon(
                                value = cell.value.toString(),
                                backgroundColor = HexagonGridDefaults.getColorForValue(cell.value),
                                modifier = Modifier
                                    .size(
                                        width = with(density) { itemWidth.toDp() },
                                        height = with(density) { itemHeight.toDp() },
                                    )
                                    .offset { animatedOffset }
                                    .graphicsLayer {
                                        this.alpha = alpha
                                        scaleX = scale * (if (isSelected) 1.2f else 1f)
                                        scaleY = scale * (if (isSelected) 1.2f else 1f)
                                    }
                                    .then(
                                        if (isSelected) Modifier.border(
                                            2.dp,
                                            Color.White,
                                            FlatTopHexagonShape(),
                                        ) else Modifier,
                                    ),
                                onClick = if (activePerk != null) {
                                    { viewModel.onCellClicked(cell) }
                                } else null,
                            )
                        }
                    }
                }

                Canvas(modifier = Modifier.matchParentSize()) {
                    particles.forEach { p ->
                        drawCircle(
                            color = p.color,
                            radius = p.size * p.life,
                            center = Offset(p.x, p.y),
                            alpha = p.life,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Perk Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            collectedPerks.distinct().forEach { perk ->
                val count = collectedPerks.count { it == perk }
                val isActive = activePerk == perk

                PerkButton(
                    perk = perk,
                    count = count,
                    isActive = isActive,
                    onClick = { viewModel.onUsePerkClicked(perk) },
                )

                Spacer(Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Bottom section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "COMBO X2",
                color = Color(0xFFF06292),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "LEVEL $level",
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }

        Spacer(Modifier.height(8.dp))

        val currentLevelThreshold = 20 * (2.0.pow(level - 1) - 1).toFloat()
        val nextLevelThreshold = 20 * (2.0.pow(level) - 1).toFloat()
        val progress =
            ((score - currentLevelThreshold) / (nextLevelThreshold - currentLevelThreshold)).coerceIn(
                0f,
                1f,
            )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = Color(0xFFF06292),
            trackColor = Color.White.copy(alpha = 0.1f),
            strokeCap = StrokeCap.Round,
        )

        Spacer(Modifier.height(16.dp))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = perkOptions.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            PerkSelectionDialog(
                options = perkOptions,
                onPerkSelected = { viewModel.onPerkSelected(it) },
            )
        }

        AnimatedVisibility(
            visible = (isStuck || isGameOver) && perkOptions.isEmpty(),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            StatusDialog(
                isGameOver = isGameOver,
                collectedPerks = collectedPerks,
                onUsePerk = { viewModel.onUsePerkClicked(it) },
                onRestart = { viewModel.onRestartClicked() },
            )
        }
    }
}

@Composable
fun PerkButton(
    perk: Perk,
    count: Int,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (isActive) Color(0xFFF06292).copy(alpha = 0.2f) else Color(0xFF2A2A36),
                    CircleShape,
                )
                .border(
                    width = if (isActive) 2.dp else 1.dp,
                    color = if (isActive) Color(0xFFF06292) else Color.White.copy(alpha = 0.1f),
                    shape = CircleShape,
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(24.dp)) {
                val strokeWidth = 2.dp.toPx()
                when (perk) {
                    Perk.ADVANCE_QUEUE -> {
                        // Double arrow
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width * 0.5f, size.height * 0.5f)
                            lineTo(0f, size.height)
                            moveTo(size.width * 0.5f, 0f)
                            lineTo(size.width, size.height * 0.5f)
                            lineTo(size.width * 0.5f, size.height)
                        }
                        drawPath(path, color = Color.White, style = Stroke(width = strokeWidth))
                    }

                    Perk.MOVE_TILE -> {
                        // Plus with arrows
                        drawLine(
                            Color.White,
                            Offset(size.width * 0.5f, 0f),
                            Offset(size.width * 0.5f, size.height),
                            strokeWidth,
                        )
                        drawLine(
                            Color.White,
                            Offset(0f, size.height * 0.5f),
                            Offset(size.width, size.height * 0.5f),
                            strokeWidth,
                        )
                    }

                    Perk.REMOVE_TILE -> {
                        // Trash bin simple
                        drawRect(
                            Color.White,
                            Offset(size.width * 0.2f, size.height * 0.3f),
                            size.copy(width = size.width * 0.6f, height = size.height * 0.6f),
                            style = Stroke(width = strokeWidth),
                        )
                        drawLine(
                            Color.White,
                            Offset(size.width * 0.1f, size.height * 0.2f),
                            Offset(size.width * 0.9f, size.height * 0.2f),
                            strokeWidth,
                        )
                    }

                    Perk.FUSION -> {
                        // Concentric circles/implosion
                        drawCircle(
                            Color.White,
                            radius = size.minDimension * 0.4f,
                            style = Stroke(width = strokeWidth),
                        )
                        drawCircle(Color.White, radius = size.minDimension * 0.2f)
                    }
                }
            }

            // Count badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .background(Color(0xFFF06292), CircleShape)
                    .size(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            perk.displayName.split(" ").first(),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun PerkSelectionDialog(
    options: List<Perk>,
    onPerkSelected: (Perk) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                ),
            )
            .padding(top = 64.dp), // Give space to see board
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(Color(0xFF1C1C24), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "LEVEL UP!",
                color = Color(0xFFF06292),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Choose your perk",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                options.forEach { perk ->
                    Button(
                        onClick = { onPerkSelected(perk) },
                        modifier = Modifier.weight(1f).height(100.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2A2A36),
                            contentColor = Color.White,
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                perk.displayName.split(" ").first(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                perk.description,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 11.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusDialog(
    isGameOver: Boolean,
    collectedPerks: List<Perk>,
    onUsePerk: (Perk) -> Unit,
    onRestart: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                ),
            )
            .padding(top = 64.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(Color(0xFF1C1C24), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Canvas(
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, Color(0xFFF06292).copy(alpha = 0.3f), CircleShape)
                        .padding(8.dp)
                        .background(Color(0xFFF06292).copy(alpha = 0.1f), CircleShape),
                ) {
                    val strokeWidth = 2.dp.toPx()
                    drawCircle(
                        color = Color(0xFFF06292),
                        radius = size.minDimension / 2.5f,
                        style = Stroke(width = strokeWidth),
                    )
                    val radius = size.minDimension / 2.5f
                    val angle = 45f * (PI.toFloat() / 180f)
                    drawLine(
                        color = Color(0xFFF06292),
                        start = center + Offset(cos(angle) * radius, sin(angle) * radius),
                        end = center - Offset(cos(angle) * radius, sin(angle) * radius),
                        strokeWidth = strokeWidth,
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = if (isGameOver) "GAME OVER" else "NO MORE MOVES",
                        color = Color(0xFFD1C4E9),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                    Text(
                        text = if (isGameOver) "Better luck next time!" else "Try using a perk.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (!isGameOver) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    collectedPerks.distinct().forEach { perk ->
                        val count = collectedPerks.count { it == perk }
                        Button(
                            onClick = { onUsePerk(perk) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4F6BFF),
                                contentColor = Color.White,
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                        ) {
                            Text(
                                "${perk.displayName.split(" ").first()} ($count)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.3f),
                ),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            ) {
                Text("RESTART GAME", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ScoreCard(
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
