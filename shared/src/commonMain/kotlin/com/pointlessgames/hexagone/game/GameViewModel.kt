package com.pointlessgames.hexagone.game

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.hexagone.data.SettingsRepository
import com.pointlessgames.hexagone.game.logic.GameEngine
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeHint
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.OnBoardPerk
import com.pointlessgames.hexagone.game.model.Particle
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PreviewCell
import com.pointlessgames.hexagone.ui.theme.Colors
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.label_bar_raised
import hexagone.shared.generated.resources.label_cleanup
import hexagone.shared.generated.resources.label_janitor_plus
import hexagone.shared.generated.resources.label_redemption
import hexagone.shared.generated.resources.label_sacrifice
import hexagone.shared.generated.resources.label_tactical_redemption
import hexagone.shared.generated.resources.label_tactician
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.StringResource

@Immutable
internal data class GameUiState(
    val grid: List<HexagonCell> = emptyList(),
    val mergeHints: List<MergeHint> = emptyList(),
    val onBoardPerks: List<OnBoardPerk> = emptyList(),
    val preview: List<PreviewCell> = emptyList(),
    val pendingMerge: MergeTransition? = null,
    val activeMergeStepIndex: Int = 0,
    val pendingMergeScore: Int = 0,
    val hoveredMerge: MergeTransition? = null,
    val score: Int = 0,
    val bestScore: Int = 0,
    val level: Int = 1,
    val levelProgress: Float = 0f,
    val highestValue: Int = 1,
    val combo: Int = 0,
    val isBusy: Boolean = false,
    val isStuck: Boolean = false,
    val isGameOver: Boolean = false,
    val collectedPerks: List<Perk> = emptyList(),
    val perkOptions: List<Perk> = emptyList(),
    val pendingLevelUps: Int = 0,
    val activePerk: Perk? = null,
    val selectedCellId: String? = null,
    val mergeHintsEnabled: Boolean = true,
    val maxCombo: Int = 0,
    val totalMerges: Int = 0,
    val showGameOverBoard: Boolean = false,
    val reachedComboTiers: Set<ComboTier> = emptySet(),
    val perkSpawnCounter: Int = 0,
    val canReroll: Boolean = true,
    val availableChoices: Int = 0,
    val isDebugMode: Boolean = false,
    val debugSelectedValue: Int? = 1,
    val debugAddAsGhost: Boolean = false,
)

@Immutable
internal data class PotentialMerge(
    val targetX: Int,
    val targetY: Int,
    val finalValue: Int,
    val baseScore: Int,
    val participatingIds: Set<String>,
)

internal sealed interface GameEffect {
    data class Particles(val particles: List<Particle>) : GameEffect
    data class ScorePopup(
        val gridX: Int,
        val gridY: Int,
        val score: Int,
        val color: Color,
        val labelRes: StringResource? = null,
    ) : GameEffect

    data class PerkPopup(
        val gridX: Int,
        val gridY: Int,
        val perk: Perk,
    ) : GameEffect
}

@Serializable
internal enum class ComboTier(val threshold: Int) {
    SURGE(11),
    OVERDRIVE(21),
    ZENITH(31)
}

@Serializable
internal data class GameState(
    val grid: List<HexagonCell>,
    val preview: List<PreviewCell>,
    val score: Int,
    val level: Int,
    val highestValue: Int,
    val combo: Int,
    val collectedPerks: List<Perk>,
    val maxCombo: Int,
    val totalMerges: Int,
    val onBoardPerks: List<OnBoardPerk>,
    val pendingLevelUps: Int,
    val perkSpawnCounter: Int,
    val reachedComboTiers: Set<ComboTier>,
    val perkOptions: List<Perk>,
    val canReroll: Boolean,
    val sessionBestScore: Int,
    val isStuck: Boolean = false,
    val availableChoices: Int = 0,
)

internal class GameViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val engine = GameEngine()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _hoveredMerge = MutableStateFlow<MergeTransition?>(null)
    val hoveredMerge: StateFlow<MergeTransition?> = _hoveredMerge.asStateFlow()

    private val _effects = MutableSharedFlow<GameEffect>()
    val effects: SharedFlow<GameEffect> = _effects.asSharedFlow()

    private var lastLevel = 1
    private var absoluteBestScore = 0
    private var stateHistory = mutableListOf<GameState>()
    private var lastMoveScore: Int? = null
    private var lastProcessedMergeId: String? = null
    private var lastProcessedStepIndex: Int = -1

    init {
        viewModelScope.launch {
            val best = settingsRepository.getBestScore()
            val hintsEnabled = settingsRepository.getMergeHintsEnabled()
            val savedStateJson = settingsRepository.getGameState()

            if (savedStateJson != null) {
                try {
                    val savedState = Json.decodeFromString<GameState>(savedStateJson)
                    _uiState.update {
                        it.copy(
                            grid = savedState.grid,
                            preview = savedState.preview,
                            score = savedState.score,
                            level = savedState.level,
                            highestValue = savedState.highestValue,
                            combo = savedState.combo,
                            collectedPerks = savedState.collectedPerks,
                            maxCombo = savedState.maxCombo,
                            totalMerges = savedState.totalMerges,
                            onBoardPerks = savedState.onBoardPerks,
                            pendingLevelUps = savedState.pendingLevelUps,
                            perkSpawnCounter = savedState.perkSpawnCounter,
                            reachedComboTiers = savedState.reachedComboTiers,
                            perkOptions = savedState.perkOptions,
                            canReroll = savedState.canReroll,
                            bestScore = savedState.sessionBestScore,
                            mergeHintsEnabled = hintsEnabled,
                            isStuck = savedState.isStuck,
                            availableChoices = savedState.availableChoices,
                        )
                    }
                    absoluteBestScore = best
                    absoluteBestScore = maxOf(best, savedState.score)
                    lastLevel = savedState.level
                    updateLevel()
                    engine.syncCounters(savedState.grid, savedState.preview)
                    checkValidMoves()
                } catch (_: Exception) {
                    _uiState.update { it.copy(bestScore = best, mergeHintsEnabled = hintsEnabled) }
                    restartGame()
                }
            } else {
                absoluteBestScore = best
                _uiState.update { it.copy(bestScore = best, mergeHintsEnabled = hintsEnabled) }
                restartGame()
            }
            recalculateHints()
        }
    }

    fun addParticles(newParticles: List<Particle>) {
        viewModelScope.launch {
            _effects.emit(GameEffect.Particles(newParticles))
        }
    }

    fun addScorePopup(
        gridX: Int,
        gridY: Int,
        score: Int,
        color: Color,
        labelRes: StringResource? = null,
    ) {
        viewModelScope.launch {
            _effects.emit(GameEffect.ScorePopup(gridX, gridY, score, color, labelRes))
        }
    }

    fun addPerkPopup(gridX: Int, gridY: Int, perk: Perk) {
        viewModelScope.launch {
            _effects.emit(GameEffect.PerkPopup(gridX, gridY, perk))
        }
    }

    private fun persistBestScore(score: Int) {
        if (score > absoluteBestScore) {
            absoluteBestScore = score
            viewModelScope.launch {
                settingsRepository.setBestScore(score)
            }
        }
    }

    private fun saveState() {
        val currentState = getCurrentGameState()
        val choices = engine.countPossibleMoves(currentState.grid) +
                currentState.collectedPerks.count { perk ->
                    engine.canPerkResolveStuck(
                        perk = perk,
                        grid = currentState.grid,
                        previews = currentState.preview,
                        previousState = stateHistory.lastOrNull()
                    )
                } +
                (if (currentState.perkOptions.isNotEmpty()) 1 else 0)

        val stateToSave = currentState.copy(availableChoices = choices)

        stateHistory.add(stateToSave)
        if (stateHistory.size > 10) stateHistory.removeAt(0)
        persistState(stateToSave)
    }

    private fun getCurrentGameState(): GameState {
        val state = _uiState.value
        return GameState(
            grid = state.grid,
            preview = state.preview,
            score = state.score,
            level = state.level,
            highestValue = state.highestValue,
            combo = state.combo,
            collectedPerks = state.collectedPerks,
            maxCombo = state.maxCombo,
            totalMerges = state.totalMerges,
            onBoardPerks = state.onBoardPerks,
            pendingLevelUps = state.pendingLevelUps,
            perkSpawnCounter = state.perkSpawnCounter,
            reachedComboTiers = state.reachedComboTiers,
            perkOptions = state.perkOptions,
            canReroll = state.canReroll,
            sessionBestScore = state.bestScore,
            isStuck = state.isStuck,
            availableChoices = state.availableChoices,
        )
    }

    private fun persistState(state: GameState) {
        viewModelScope.launch {
            settingsRepository.setGameState(Json.encodeToString(state))
        }
    }

    private fun undoLastMove(): Boolean {
        if (stateHistory.isEmpty()) return false
        val currentState = _uiState.value
        val previousState = stateHistory.removeAt(stateHistory.size - 1)

        lastMoveScore = currentState.score - previousState.score

        _uiState.update { state ->
            state.copy(
                grid = previousState.grid,
                preview = previousState.preview,
                score = previousState.score,
                level = previousState.level,
                highestValue = previousState.highestValue,
                combo = previousState.combo,
                collectedPerks = previousState.collectedPerks,
                maxCombo = previousState.maxCombo,
                totalMerges = previousState.totalMerges,
                onBoardPerks = previousState.onBoardPerks,
                pendingMerge = null,
                activePerk = null,
                selectedCellId = null,
            )
        }

        lastLevel = previousState.level
        checkValidMoves()
        return true
    }

    private fun updateLevel() {
        val currentScore = _uiState.value.score
        val lvl = engine.calculateLevel(currentScore)

        _uiState.update { state ->
            val levelDifference = lvl - lastLevel
            if (levelDifference > 0) {
                lastLevel = lvl
                val nextPerkOptions = state.perkOptions.ifEmpty { engine.pickWeightedPerks(3) }
                state.copy(
                    level = lvl,
                    levelProgress = engine.getLevelProgress(currentScore, lvl),
                    highestValue = state.grid.maxOfOrNull { it.value } ?: 1,
                    perkOptions = nextPerkOptions,
                    pendingLevelUps = state.pendingLevelUps + levelDifference,
                    canReroll = if (state.perkOptions.isEmpty()) true else state.canReroll,
                )
            } else {
                state.copy(
                    level = lvl,
                    levelProgress = engine.getLevelProgress(currentScore, lvl),
                    highestValue = state.grid.maxOfOrNull { it.value } ?: 1,
                )
            }
        }
    }

    fun onEmptySpaceClicked(x: Int, y: Int) {
        val state = _uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || state.isStuck || state.perkOptions.isNotEmpty()) return

        saveState()
        _hoveredMerge.value = null

        val perk = state.activePerk
        val selectedId = state.selectedCellId
        val previewAtPos = state.preview.find { it.x == x && it.y == y }

        if (perk == Perk.PATH_MERGE) return
        val isTileOnlyPerk = perk == Perk.REMOVE_TILE || perk == Perk.INCREMENT_TILE || perk == Perk.SWAP_TILES
        if (isTileOnlyPerk && previewAtPos == null) return

        if (perk != null && perk != Perk.FUSION && perk != Perk.CHAIN_MERGE && previewAtPos != null) {
            if (selectedId == previewAtPos.id) {
                _uiState.update { it.copy(selectedCellId = null) }
                return
            }
            if (selectedId == null || perk == Perk.REMOVE_TILE || (perk == Perk.SWAP_TILES && selectedId != previewAtPos.id)) {
                onPreviewClicked(previewAtPos)
                return
            }
        }

        if (perk == Perk.MOVE_TILE && selectedId != null) {
            moveTile(selectedId, x, y)
            return
        }

        if (perk == Perk.DUPLICATE_TILE && selectedId != null) {
            duplicateTile(selectedId, x, y)
            return
        }

        if (state.grid.any { it.x == x && it.y == y }) return

        val merge = if (perk == Perk.FUSION) {
            engine.calculateFusion(x, y, state.grid)
        } else {
            engine.calculateMerge(x, y, state.grid)
        }

        if (merge != null) {
            val comboMultiplier = (state.combo + 1).coerceAtMost(12)
            val totalAddedScore = merge.baseScore * comboMultiplier

            _uiState.update { currentState ->
                val firstStep = merge.steps.first()
                currentState.copy(
                    grid = currentState.grid.map { cell ->
                        if (firstStep.mergingCells.any { it.id == cell.id }) cell.copy(
                            x = x,
                            y = y,
                        ) else cell
                    },
                    preview = currentState.preview.filterNot { it.x == x && it.y == y },
                    pendingMerge = merge,
                    activeMergeStepIndex = 0,
                    pendingMergeScore = totalAddedScore,
                    isBusy = true,
                )
            }
            if (perk == Perk.FUSION) {
                consumePerk(Perk.FUSION)
            }
        } else {
            lastMoveScore = null // Reset redemption if the next move isn't a merge
            if (perk == Perk.CHAIN_MERGE) {
                finishPerkAction(Perk.CHAIN_MERGE)
                _uiState.update { it.copy(activePerk = null) }
            }
        }
    }

    private fun getGameItem(id: String): GameItem? {
        val state = _uiState.value
        state.grid.find { it.id == id }?.let { return GameItem(it.id, it.x, it.y, it.value, false) }
        state.preview.find { it.id == id }?.let { return GameItem(it.id, it.x, it.y, it.value, true) }
        return null
    }

    private data class GameItem(
        val id: String,
        val x: Int,
        val y: Int,
        val value: Int,
        val isGhost: Boolean
    )

    fun onEmptySpaceTouchDown(x: Int, y: Int) {
        val state = _uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || state.isStuck || state.perkOptions.isNotEmpty()) return

        val perk = state.activePerk
        val selectedId = state.selectedCellId
        val ghostAtPos = state.preview.find { it.x == x && it.y == y }

        if (perk != null && selectedId != null && ghostAtPos?.id == selectedId) return

        if (perk == Perk.PATH_MERGE) return
        val isTileOnlyPerk = perk == Perk.REMOVE_TILE || perk == Perk.INCREMENT_TILE || perk == Perk.SWAP_TILES
        if (isTileOnlyPerk && ghostAtPos == null) return

        val merge = when (perk) {
            Perk.REMOVE_TILE -> {
                if (ghostAtPos != null) {
                    val baseCleanupScore = ghostAtPos.value * 10
                    val currentMinOnBoard = state.grid.minOfOrNull { it.value } ?: Int.MAX_VALUE
                    val isLowestValue = ghostAtPos.value <= currentMinOnBoard
                    val isLastInQueue = state.preview.count { it.value == ghostAtPos.value } == 1
                    val noneOnBoard = state.grid.none { it.value == ghostAtPos.value }
                    val barRaised = isLowestValue && isLastInQueue && noneOnBoard
                    val finalBonus = if (barRaised) baseCleanupScore + 250 else baseCleanupScore
                    MergeTransition(
                        targetX = x, targetY = y, steps = emptyList(), finalValue = 0,
                        totalCells = 1, uniqueGroups = 0, baseScore = finalBonus, resultId = "preview_remove_queue",
                        isRemoval = true, participatingIds = setOf(ghostAtPos.id)
                    )
                } else null
            }
            Perk.INCREMENT_TILE -> {
                if (ghostAtPos != null) {
                    val nextValue = ghostAtPos.value + 1
                    MergeTransition(
                        targetX = x, targetY = y, steps = emptyList(), finalValue = 0,
                        totalCells = 1, uniqueGroups = 0, baseScore = 0, resultId = "preview_increment_queue",
                        participatingIds = setOf(ghostAtPos.id),
                        previewValues = mapOf(ghostAtPos.id to nextValue)
                    )
                } else null
            }
            Perk.PATH_MERGE -> {
                val merge = engine.calculatePathMerge(x, y, state.grid)
                merge?.copy(
                    resultId = "preview_path_merge",
                    forceSolidIds = setOf("preview_path_merge"),
                    previewValues = ghostAtPos?.let { mapOf(it.id to merge.finalValue) } ?: emptyMap()
                )
            }
            Perk.MOVE_TILE, Perk.DUPLICATE_TILE -> {
                if (selectedId != null && (ghostAtPos == null || selectedId != ghostAtPos.id)) {
                    val source = getGameItem(selectedId)
                    if (source != null) {
                        val resultId = if (perk == Perk.MOVE_TILE) "preview_move" else "preview_duplicate"
                        val swaps = if (perk == Perk.MOVE_TILE) mapOf(selectedId to (x to y)) else null
                        val isSourceSolid = !source.isGhost
                        val forceSolidIds = if (isSourceSolid) setOf(resultId) else emptySet()
                        val forceGhostAtSource = if (perk == Perk.MOVE_TILE && source.isGhost) setOf(selectedId) else emptySet()
                        
                        MergeTransition(
                            targetX = x, targetY = y, steps = emptyList(), finalValue = source.value,
                            totalCells = 1, uniqueGroups = 0, baseScore = 0, resultId = resultId,
                            participatingIds = setOf(selectedId) + listOfNotNull(ghostAtPos?.id),
                            previewSwaps = swaps,
                            forceSolidIds = forceSolidIds + forceGhostAtSource,
                        )
                    } else null
                } else null
            }
            Perk.SWAP_TILES -> {
                if (selectedId != null && ghostAtPos != null && selectedId != ghostAtPos.id) {
                    val source = getGameItem(selectedId)
                    val target = ghostAtPos
                    if (source != null) {
                        val swaps = mapOf(
                            source.id to (target.x to target.y),
                            target.id to (source.x to source.y)
                        )

                        MergeTransition(
                            targetX = x, targetY = y, steps = emptyList(), finalValue = 0,
                            totalCells = 1, uniqueGroups = 0, baseScore = 0, resultId = "preview_swap",
                            previewSwaps = swaps,
                            participatingIds = setOf(source.id, target.id),
                        )
                    } else null
                } else null
            }
            Perk.FUSION -> engine.calculateFusion(x, y, state.grid)
            null, Perk.CHAIN_MERGE, Perk.SKIP_SPAWN -> {
                if (perk == Perk.CHAIN_MERGE) {
                    engine.simulateChainMerge(x, y, state.grid, state.combo)
                } else {
                    engine.calculateMerge(x, y, state.grid)
                }
            }
            else -> null
        }
        _hoveredMerge.value = merge
    }

    fun onCellTouchDown(cell: HexagonCell) {
        val state = _uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || state.isStuck || state.perkOptions.isNotEmpty()) return

        val perk = state.activePerk
        val selectedId = state.selectedCellId

        if (perk != null && selectedId == cell.id) return

        val merge = when (perk) {
            Perk.PATH_MERGE -> {
                val m = engine.calculatePathMerge(cell.x, cell.y, state.grid)
                m?.copy(
                    resultId = "preview_path_merge",
                    forceSolidIds = setOf("preview_path_merge"),
                    previewValues = mapOf(cell.id to m.finalValue)
                )
            }
            Perk.REMOVE_TILE -> {
                val baseCleanupScore = cell.value * 10
                val currentMin = state.grid.minOfOrNull { it.value } ?: Int.MAX_VALUE
                val isLowestValue = cell.value <= currentMin
                val isLastOnBoard = state.grid.count { it.value == cell.value } == 1
                val noneInQueue = state.preview.none { it.value == cell.value }
                val barRaised = isLowestValue && isLastOnBoard && noneInQueue
                val isOnlyHighest = state.grid.count { it.value == state.highestValue } == 1 && cell.value == state.highestValue
                var finalBonus = baseCleanupScore
                if (barRaised) finalBonus += 250
                if (isOnlyHighest) finalBonus *= 2

                MergeTransition(
                    targetX = cell.x, targetY = cell.y, steps = emptyList(), finalValue = 0,
                    totalCells = 1, uniqueGroups = 0, baseScore = finalBonus, resultId = "preview_remove",
                    isRemoval = true, participatingIds = setOf(cell.id)
                )
            }
            Perk.INCREMENT_TILE -> {
                val nextValue = cell.value + 1
                MergeTransition(
                    targetX = cell.x, targetY = cell.y, steps = emptyList(), finalValue = 0,
                    totalCells = 1, uniqueGroups = 0, baseScore = 0, resultId = "preview_increment",
                    participatingIds = setOf(cell.id),
                    previewValues = mapOf(cell.id to nextValue)
                )
            }
            Perk.SWAP_TILES -> {
                if (selectedId != null && selectedId != cell.id) {
                    val source = getGameItem(selectedId)
                    val target = cell
                    if (source != null) {
                        val swaps = mapOf(
                            source.id to (target.x to target.y),
                            target.id to (source.x to source.y)
                        )

                        MergeTransition(
                            targetX = cell.x, targetY = cell.y, steps = emptyList(), finalValue = 0,
                            totalCells = 1, uniqueGroups = 0, baseScore = 0, resultId = "preview_swap",
                            previewSwaps = swaps,
                            participatingIds = setOf(source.id, target.id),
                        )
                    } else null
                } else null
            }
            null -> null
            else -> null
        }
        _hoveredMerge.value = merge
    }

    fun onCellTouchUp() {
        _hoveredMerge.value = null
    }

    fun onEmptySpaceTouchUp() {
        _hoveredMerge.value = null
    }

    private fun moveTile(selectedId: String, x: Int, y: Int) {
        saveState()
        _uiState.update { state ->
            val cellToMove = state.grid.find { it.id == selectedId }
            val collectedPerk = state.onBoardPerks.find { it.x == x && it.y == y }?.perk

            if (collectedPerk != null) {
                addPerkPopup(x, y, collectedPerk)
            }

            if (cellToMove != null) {
                state.copy(
                    grid = state.grid.map {
                        if (it.id == selectedId) it.copy(
                            x = x,
                            y = y,
                            isTactical = true,
                        ) else it
                    },
                    preview = state.preview.filterNot { it.x == x && it.y == y },
                    collectedPerks = state.collectedPerks + listOfNotNull(collectedPerk),
                    onBoardPerks = state.onBoardPerks.filterNot { it.x == x && it.y == y },
                )
            } else {
                val previewToMove = state.preview.find { it.id == selectedId }
                if (previewToMove != null) {
                    state.copy(
                        preview = state.preview.map {
                            if (it.id == selectedId) it.copy(
                                x = x,
                                y = y,
                                isTactical = true,
                            ) else it
                        }.filterNot { it.id != selectedId && it.x == x && it.y == y },
                        collectedPerks = state.collectedPerks + listOfNotNull(collectedPerk),
                        onBoardPerks = state.onBoardPerks.filterNot { it.x == x && it.y == y },
                    )
                } else state
            }
        }

        finishPerkAction(Perk.MOVE_TILE)
    }

    private fun duplicateTile(selectedId: String, x: Int, y: Int) {
        saveState()
        _uiState.update { state ->
            val cellToCopy = state.grid.find { it.id == selectedId }
            val previewToCopy = state.preview.find { it.id == selectedId }
            val value = cellToCopy?.value ?: previewToCopy?.value ?: return@update state

            val collectedPerk = state.onBoardPerks.find { it.x == x && it.y == y }?.perk
            if (collectedPerk != null) {
                addPerkPopup(x, y, collectedPerk)
            }

            state.copy(
                grid = state.grid + engine.createCell(x, y, value, isTactical = true),
                preview = state.preview.filterNot { it.x == x && it.y == y },
                collectedPerks = state.collectedPerks + listOfNotNull(collectedPerk),
                onBoardPerks = state.onBoardPerks.filterNot { it.x == x && it.y == y },
            )
        }
        finishPerkAction(Perk.DUPLICATE_TILE)
    }

    private fun finishPerkAction(perk: Perk) {
        consumePerk(perk)
        _uiState.update { it.copy(activePerk = null, selectedCellId = null) }
        if (_uiState.value.preview.isEmpty()) {
            spawnFromQueue(_uiState.value.grid)
        } else {
            checkValidMoves()
        }
    }

    fun onPreviewClicked(preview: PreviewCell) {
        val state = _uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || state.isStuck || state.perkOptions.isNotEmpty()) return

        when (val perk = state.activePerk) {
            Perk.MOVE_TILE -> {
                val selectedId = state.selectedCellId
                if (selectedId != null && selectedId != preview.id) {
                    moveTile(selectedId, preview.x, preview.y)
                } else {
                    _uiState.update { it.copy(selectedCellId = preview.id) }
                }
            }

            Perk.DUPLICATE_TILE -> {
                _uiState.update { it.copy(selectedCellId = if (it.selectedCellId == preview.id) null else preview.id) }
            }

            Perk.REMOVE_TILE -> {
                saveState()
                val baseCleanupScore = preview.value * 10

                val currentMinOnBoard = state.grid.minOfOrNull { it.value } ?: Int.MAX_VALUE
                val isLowestValue = preview.value <= currentMinOnBoard
                val isLastInQueue = state.preview.count { it.value == preview.value } == 1
                val noneOnBoard = state.grid.none { it.value == preview.value }

                val barRaised = isLowestValue && isLastInQueue && noneOnBoard
                val finalBonus = if (barRaised) baseCleanupScore + 250 else baseCleanupScore
                val labelRes =
                    if (barRaised) Res.string.label_bar_raised else Res.string.label_cleanup

                val nextScore = _uiState.value.score + finalBonus
                _uiState.update { s ->
                    s.copy(
                        preview = s.preview.filter { it.id != preview.id },
                        score = nextScore,
                    )
                }
                persistBestScore(nextScore)
                addScorePopup(preview.x, preview.y, finalBonus, Colors().greyBlue, labelRes)
                finishPerkAction(Perk.REMOVE_TILE)
            }

            Perk.SWAP_TILES -> {
                val selectedId = state.selectedCellId
                if (selectedId == null) {
                    _uiState.update { it.copy(selectedCellId = preview.id) }
                } else if (selectedId != preview.id) {
                    swapTiles(selectedId, preview.id)
                }
            }

            Perk.INCREMENT_TILE -> {
                saveState()
                _uiState.update { s ->
                    s.copy(
                        preview = s.preview.map { if (it.id == preview.id) it.copy(value = it.value + 1, isTactical = true) else it }
                    )
                }
                finishPerkAction(Perk.INCREMENT_TILE)
            }

            else -> {}
        }
    }

    fun onCellClicked(cell: HexagonCell) {
        val state = _uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || state.isStuck || state.perkOptions.isNotEmpty()) return

        when (val perk = state.activePerk) {
            Perk.MOVE_TILE -> {
                _uiState.update { it.copy(selectedCellId = if (it.selectedCellId == cell.id) null else cell.id) }
            }

            Perk.DUPLICATE_TILE -> {
                _uiState.update { it.copy(selectedCellId = if (it.selectedCellId == cell.id) null else cell.id) }
            }

            Perk.INCREMENT_TILE -> {
                saveState()
                _uiState.update { s ->
                    s.copy(
                        grid = s.grid.map { if (it.id == cell.id) it.copy(value = it.value + 1, isTactical = true) else it }
                    )
                }
                finishPerkAction(Perk.INCREMENT_TILE)
            }

            Perk.PATH_MERGE -> {
                val merge = engine.calculatePathMerge(cell.x, cell.y, state.grid)
                if (merge != null) {
                    saveState()
                    val comboMultiplier = (state.combo + 1).coerceAtMost(12)
                    val totalAddedScore = merge.baseScore * comboMultiplier

                    _uiState.update { currentState ->
                        val firstStep = merge.steps.first()
                        currentState.copy(
                            grid = currentState.grid.map { c ->
                                if (firstStep.mergingCells.any { it.id == c.id }) c.copy(
                                    x = cell.x,
                                    y = cell.y,
                                ) else c
                            },
                            pendingMerge = merge.copy(resultId = "preview_path_merge"),
                            activeMergeStepIndex = 0,
                            pendingMergeScore = totalAddedScore,
                            isBusy = true,
                        )
                    }
                }
            }

            Perk.REMOVE_TILE -> {
                saveState()
                val baseCleanupScore = cell.value * 10

                val currentMin = state.grid.minOfOrNull { it.value } ?: Int.MAX_VALUE
                val isLowestValue = cell.value <= currentMin
                val isLastOnBoard = state.grid.count { it.value == cell.value } == 1
                val noneInQueue = state.preview.none { it.value == cell.value }

                val barRaised = isLowestValue && isLastOnBoard && noneInQueue
                val isOnlyHighest =
                    state.grid.count { it.value == state.highestValue } == 1 && cell.value == state.highestValue

                var finalBonus = baseCleanupScore
                if (barRaised) finalBonus += 250
                if (isOnlyHighest) finalBonus *= 2

                val labelRes = if (barRaised && isOnlyHighest) {
                    Res.string.label_janitor_plus
                } else if (barRaised) {
                    Res.string.label_bar_raised
                } else if (isOnlyHighest) {
                    Res.string.label_sacrifice
                } else {
                    Res.string.label_cleanup
                }

                val nextScore = _uiState.value.score + finalBonus
                _uiState.update {
                    it.copy(
                        grid = it.grid.filter { it.id != cell.id },
                        score = nextScore,
                    )
                }
                persistBestScore(nextScore)
                val popupColor =
                    if (barRaised) Colors().skyBlue else if (isOnlyHighest) Colors().pink else Colors().greyBlue
                addScorePopup(cell.x, cell.y, finalBonus, popupColor, labelRes)
                finishPerkAction(Perk.REMOVE_TILE)
            }

            Perk.SWAP_TILES -> {
                val selectedId = state.selectedCellId
                if (selectedId == null) {
                    _uiState.update { it.copy(selectedCellId = cell.id) }
                } else if (selectedId == cell.id) {
                    _uiState.update { it.copy(selectedCellId = null) }
                } else {
                    saveState()
                    swapTiles(selectedId, cell.id)
                }
            }

            else -> {}
        }
    }

    private fun swapTiles(id1: String, id2: String) {
        val state = _uiState.value
        val item1 = state.grid.find { it.id == id1 } ?: state.preview.find { it.id == id1 }
        val item2 = state.grid.find { it.id == id2 } ?: state.preview.find { it.id == id2 }

        if (item1 == null || item2 == null) return

        val x1 = if (item1 is HexagonCell) item1.x else (item1 as PreviewCell).x
        val y1 = if (item1 is HexagonCell) item1.y else (item1 as PreviewCell).y
        val x2 = if (item2 is HexagonCell) item2.x else (item2 as PreviewCell).x
        val y2 = if (item2 is HexagonCell) item2.y else (item2 as PreviewCell).y

        _uiState.update { currentState ->
            val updatedGrid = currentState.grid.map { cell ->
                when (cell.id) {
                    id1 -> cell.copy(x = x2, y = y2, isTactical = true)
                    id2 -> cell.copy(x = x1, y = y1, isTactical = true)
                    else -> cell
                }
            }
            val updatedPreview = currentState.preview.map { preview ->
                when (preview.id) {
                    id1 -> preview.copy(x = x2, y = y2, isTactical = true)
                    id2 -> preview.copy(x = x1, y = y1, isTactical = true)
                    else -> preview
                }
            }

            val collectedPerkAt1 = currentState.onBoardPerks.find { it.x == x1 && it.y == y1 }?.perk
            val collectedPerkAt2 = currentState.onBoardPerks.find { it.x == x2 && it.y == y2 }?.perk

            if (collectedPerkAt1 != null) addPerkPopup(x1, y1, collectedPerkAt1)
            if (collectedPerkAt2 != null) addPerkPopup(x2, y2, collectedPerkAt2)

            currentState.copy(
                grid = updatedGrid,
                preview = updatedPreview,
                collectedPerks = currentState.collectedPerks + listOfNotNull(collectedPerkAt1, collectedPerkAt2),
                onBoardPerks = currentState.onBoardPerks.filterNot { (it.x == x1 && it.y == y1) || (it.x == x2 && it.y == y2) },
            )
        }
        finishPerkAction(Perk.SWAP_TILES)
    }

    private fun consumePerk(perk: Perk) {
        _uiState.update { state ->
            val perkIndex = state.collectedPerks.indexOf(perk)
            if (perkIndex != -1) {
                val newList = state.collectedPerks.toMutableList()
                newList.removeAt(perkIndex)
                state.copy(collectedPerks = newList)
            } else state
        }
    }

    fun onUsePerkClicked(perk: Perk) {
        val state = _uiState.value
        if (state.activePerk == perk) {
            _uiState.update { it.copy(activePerk = null, selectedCellId = null) }
            recalculateHints()
            checkValidMoves()
            return
        }

        _uiState.update { it.copy(isStuck = false, isGameOver = false, selectedCellId = null) }
        when (perk) {
            Perk.ADVANCE_QUEUE -> {
                saveState()
                consumePerk(Perk.ADVANCE_QUEUE)
                spawnFromQueue(_uiState.value.grid)
            }

            Perk.UNDO -> {
                if (undoLastMove()) {
                    consumePerk(Perk.UNDO)
                    persistState(getCurrentGameState())
                }
            }

            Perk.SKIP_SPAWN -> {
                _uiState.update { it.copy(activePerk = Perk.SKIP_SPAWN) }
            }

            else -> {
                _uiState.update { it.copy(activePerk = perk) }
            }
        }
        recalculateHints()
    }

    private fun recalculateHints() {
        _uiState.update { state ->
            state.copy(
                mergeHints = if (state.mergeHintsEnabled) {
                    engine.findMergeHints(state.grid, state.preview, state.combo, state.activePerk)
                } else emptyList(),
            )
        }
    }

    fun onPerkSelected(perk: Perk) {
        _uiState.update {
            val remainingLevelUps = (it.pendingLevelUps - 1).coerceAtLeast(0)
            it.copy(
                collectedPerks = it.collectedPerks + perk,
                perkOptions = if (remainingLevelUps > 0) engine.pickWeightedPerks(3) else emptyList(),
                pendingLevelUps = remainingLevelUps,
                canReroll = true,
            )
        }
        checkValidMoves()
        persistState(getCurrentGameState())
    }

    fun onRerollClicked() {
        val state = _uiState.value
        if (!state.canReroll || state.perkOptions.isEmpty()) return

        _uiState.update {
            it.copy(
                perkOptions = engine.pickWeightedPerks(3, excludeLegendary = true),
                canReroll = false,
            )
        }
        persistState(getCurrentGameState())
    }

    fun onRestartClicked() {
        restartGame()
    }

    private fun restartGame() {
        stateHistory.clear()
        lastLevel = 1
        lastProcessedMergeId = null
        lastProcessedStepIndex = -1
        engine.syncCounters(emptyList(), emptyList())
        val initialGrid = engine.generateInitialGrid()
        val initialPreviews = engine.pickRandomPreviews(initialGrid, emptyList(), emptyList(), 3)
        _uiState.value = GameUiState(
            grid = initialGrid,
            mergeHints = if (_uiState.value.mergeHintsEnabled) engine.findMergeHints(
                initialGrid,
                initialPreviews,
                0,
                null,
            ) else emptyList(),
            mergeHintsEnabled = _uiState.value.mergeHintsEnabled,
            preview = initialPreviews,
            bestScore = absoluteBestScore,
            collectedPerks = emptyList(),
            onBoardPerks = emptyList(),
            perkSpawnCounter = 0,
        )
        updateLevel()
        checkValidMoves()
        persistState(getCurrentGameState())
    }

    fun onMergeAnimationFinished() {
        val state = _uiState.value
        val merge = state.pendingMerge ?: return
        val stepIndex = state.activeMergeStepIndex

        // Prevent multiple processing of the same merge step (e.g. if multiple cells finish at once)
        if (lastProcessedMergeId == merge.resultId && lastProcessedStepIndex == stepIndex) return
        lastProcessedMergeId = merge.resultId
        lastProcessedStepIndex = stepIndex

        val currentStep = merge.steps[stepIndex]

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }

            val currentState = _uiState.value

            val stateAfterStep = currentState.grid.filter { cell ->
                currentStep.mergingCells.none { it.id == cell.id } && (cell.x != merge.targetX || cell.y != merge.targetY)
            } + engine.createCell(
                merge.targetX,
                merge.targetY,
                currentStep.resultValue,
                merge.resultId,
            )

            val isLastStep = stepIndex >= merge.steps.lastIndex

            if (isLastStep) {
                val totalAddedScore = currentState.pendingMergeScore

                val redemptionBonus =
                    if (lastMoveScore != null && totalAddedScore > lastMoveScore!!) {
                        val bonus = 250 + ((totalAddedScore - lastMoveScore!!) * 0.5).toInt()
                        lastMoveScore = null
                        bonus
                    } else {
                        lastMoveScore = null
                        0
                    }

                val combinedScore = totalAddedScore + redemptionBonus
                val labelRes = when {
                    redemptionBonus > 0 && merge.isTactical -> Res.string.label_tactical_redemption
                    redemptionBonus > 0 -> Res.string.label_redemption
                    merge.isTactical -> Res.string.label_tactician
                    else -> null
                }

                val popupColor = when (labelRes) {
                    Res.string.label_tactical_redemption, Res.string.label_redemption -> Colors().yellow

                    Res.string.label_tactician -> Colors().purple
                    else -> Color.White
                }

                addScorePopup(
                    merge.targetX,
                    merge.targetY,
                    combinedScore,
                    popupColor,
                    labelRes,
                )

                val collectedOnBoard =
                    currentState.onBoardPerks.find { it.x == merge.targetX && it.y == merge.targetY }?.perk

                if (collectedOnBoard != null) {
                    addPerkPopup(
                        merge.targetX,
                        merge.targetY,
                        collectedOnBoard,
                    )
                }

                val remainingOnBoard =
                    currentState.onBoardPerks.filterNot { it.x == merge.targetX && it.y == merge.targetY }

                val finalCombo =
                    if (merge.uniqueGroups > 1 || currentState.activePerk == Perk.CHAIN_MERGE) {
                        currentState.combo + (if (currentState.activePerk == Perk.CHAIN_MERGE) 1 else 0) + (merge.uniqueGroups - 1)
                    } else if (currentState.activePerk == Perk.FUSION) {
                        currentState.combo
                    } else {
                        0
                    }

                val finalScore = currentState.score + totalAddedScore + redemptionBonus
                persistBestScore(finalScore)

                // Tier Reward Logic
                val newReachedTiers = mutableSetOf<ComboTier>()
                val newlyEarnedPerks = mutableListOf<Perk>()

                ComboTier.entries.forEach { tier ->
                    if (finalCombo >= tier.threshold && !currentState.reachedComboTiers.contains(
                            tier,
                        )
                    ) {
                        newReachedTiers.add(tier)
                        val reward = when (tier) {
                            ComboTier.SURGE -> Perk.entries.filter { it.baseWeight in 50..80 }
                                .random()

                            ComboTier.OVERDRIVE -> Perk.entries.filter { it.isLegendary }.random()
                            ComboTier.ZENITH -> Perk.entries.filter { it.isLegendary }.random()
                        }
                        newlyEarnedPerks.add(reward)
                    }
                }

                _uiState.update {
                    val finalGrid =
                        if (finalCombo >= ComboTier.ZENITH.threshold && !currentState.reachedComboTiers.contains(
                                ComboTier.ZENITH,
                            )
                        ) {
                            stateAfterStep.map { it.copy(value = it.value + 1) }
                        } else stateAfterStep

                    it.copy(
                        score = finalScore,
                        bestScore = currentState.bestScore,
                        levelProgress = engine.getLevelProgress(finalScore, it.level),
                        combo = finalCombo,
                        grid = finalGrid,
                        collectedPerks = it.collectedPerks + listOfNotNull(collectedOnBoard) + newlyEarnedPerks,
                        onBoardPerks = remainingOnBoard,
                        reachedComboTiers = if (finalCombo == 0) emptySet() else it.reachedComboTiers + newReachedTiers,
                        mergeHints = if (it.mergeHintsEnabled) engine.findMergeHints(
                            finalGrid,
                            it.preview,
                            finalCombo,
                            it.activePerk,
                        ) else emptyList(),
                        pendingMerge = null,
                        activeMergeStepIndex = 0,
                        pendingMergeScore = 0,
                        isBusy = true,
                        maxCombo = maxOf(it.maxCombo, finalCombo),
                        totalMerges = it.totalMerges + 1,
                    )
                }

                val chainMerge = if (_uiState.value.activePerk == Perk.CHAIN_MERGE) {
                    engine.calculateMerge(merge.targetX, merge.targetY, _uiState.value.grid)
                } else null

                if (chainMerge != null) {
                    delay(150) // Artificial delay for chain merge impact
                    val chainComboMultiplier = (finalCombo + 1).coerceAtMost(12)
                    val chainScore = chainMerge.baseScore * chainComboMultiplier

                    _uiState.update {
                        it.copy(
                            grid = it.grid.map { cell ->
                                if (chainMerge.steps.first().mergingCells.any { it.id == cell.id }) cell.copy(
                                    x = merge.targetX,
                                    y = merge.targetY,
                                ) else cell
                            },
                            pendingMerge = chainMerge,
                            activeMergeStepIndex = 0,
                            pendingMergeScore = chainScore,
                        )
                    }
                } else {
                    val activePerk = _uiState.value.activePerk
                    if (activePerk != null && activePerk != Perk.FUSION) {
                        consumePerk(activePerk)
                    }
                    _uiState.update {
                        it.copy(
                            activePerk = null,
                        )
                    }
                    if (activePerk == Perk.SKIP_SPAWN) {
                        consumePerk(Perk.SKIP_SPAWN)
                        _uiState.update { it.copy(isBusy = false) }
                        updateLevel()
                        checkValidMoves()
                        persistState(getCurrentGameState())
                    } else {
                        spawnFromQueue(_uiState.value.grid)
                    }
                }
            } else {
                delay(100) // Artificial delay between sequential group merges
                val nextStep = merge.steps[stepIndex + 1]
                _uiState.update {
                    val nextComboValue = it.combo + 1
                    it.copy(
                        combo = nextComboValue,
                        reachedComboTiers = if (nextComboValue == 0) emptySet() else it.reachedComboTiers,
                        grid = stateAfterStep.map { cell ->
                            if (nextStep.mergingCells.any { it.id == cell.id }) cell.copy(
                                x = merge.targetX,
                                y = merge.targetY,
                            ) else cell
                        },
                        activeMergeStepIndex = stepIndex + 1,
                    )
                }
            }
        }
    }

    private fun checkValidMoves() {
        val state = _uiState.value
        val isPossible = engine.isMovePossible(state.grid)
        val hasPerkOptions = state.perkOptions.isNotEmpty()
        
        val actionablePerks = state.collectedPerks.filter { perk ->
            perk.canSaveFromStuck && engine.canPerkResolveStuck(
                perk = perk,
                grid = state.grid,
                previews = state.preview,
                previousState = stateHistory.lastOrNull()
            )
        }

        _uiState.update {
            if (state.isDebugMode) {
                it.copy(isStuck = false, isGameOver = false)
            } else if (isPossible || hasPerkOptions) {
                it.copy(isStuck = false, isGameOver = false)
            } else if (actionablePerks.isNotEmpty()) {
                it.copy(isStuck = true, isGameOver = false)
            } else {
                it.copy(isStuck = false)
            }
        }

        if (!isPossible && hasPerkOptions.not() && actionablePerks.isEmpty() && !state.isDebugMode) {
            viewModelScope.launch {
                delay(1000)
                _uiState.update { it.copy(isGameOver = true) }
                settingsRepository.setGameState(null)
            }
        }
        recalculateHints()
    }

    private fun spawnFromQueue(currentState: List<HexagonCell>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            val gridWithoutTactical = engine.decrementTacticalFlags(currentState)
            val currentPerks = _uiState.value.onBoardPerks
            val (newState, newPreviews, perksAfterSpawn) = engine.spawnFromQueue(
                gridWithoutTactical,
                _uiState.value.preview,
                currentPerks,
            )
            val updatedPerks = engine.updateOnBoardPerks(perksAfterSpawn)
            val (nextPerks, nextCounter) = engine.trySpawnPerkOnBoard(
                newState,
                newPreviews,
                updatedPerks,
                _uiState.value.perkSpawnCounter,
            )

            _uiState.update {
                it.copy(
                    grid = newState,
                    preview = newPreviews,
                    onBoardPerks = nextPerks,
                    perkSpawnCounter = nextCounter,
                )
            }
            updateLevel()
            checkValidMoves()
            _uiState.update { it.copy(isBusy = false) }
            persistState(getCurrentGameState())
        }
    }

    fun getLevelProgress(): Float = _uiState.value.levelProgress

    fun getPotentialMerges(): Map<Pair<Int, Int>, PotentialMerge> {
        val state = _uiState.value
        val grid = state.grid
        val perk = state.activePerk
        val result = mutableMapOf<Pair<Int, Int>, PotentialMerge>()

        for (x in 0 until engine.columns) {
            for (y in 0 until engine.rows) {
                if (grid.none { it.x == x && it.y == y }) {
                    val merge = when (perk) {
                        Perk.FUSION -> engine.calculateFusion(x, y, grid)
                        Perk.PATH_MERGE -> engine.calculatePathMerge(x, y, grid)
                        else -> engine.calculateMerge(x, y, grid)
                    }

                    if (merge != null) {
                        result[x to y] = PotentialMerge(
                            targetX = x,
                            targetY = y,
                            finalValue = merge.finalValue,
                            baseScore = merge.baseScore,
                            participatingIds = merge.steps.flatMap { it.mergingCells }.map { it.id }
                                .toSet(),
                        )
                    }
                }
            }
        }
        return result
    }

    fun onViewBoardToggled() {
        _uiState.update { it.copy(showGameOverBoard = !it.showGameOverBoard) }
    }

    fun setDebugSelectedValue(value: Int?) {
        _uiState.update { it.copy(debugSelectedValue = value) }
    }

    fun toggleDebugMode() {
        _uiState.update { it.copy(
            isDebugMode = !it.isDebugMode,
            isStuck = false,
            isGameOver = false,
            selectedCellId = null,
            activePerk = null,
            isBusy = false
        ) }
        
        if (!_uiState.value.isDebugMode) {
            if (_uiState.value.preview.isEmpty()) {
                spawnFromQueue(_uiState.value.grid)
            } else {
                recalculateHints()
                checkValidMoves()
            }
            persistState(getCurrentGameState())
        } else {
            recalculateHints()
            checkValidMoves()
        }
    }

    fun toggleDebugAddAsGhost() {
        _uiState.update { it.copy(debugAddAsGhost = !it.debugAddAsGhost) }
    }

    fun onDebugCellClicked(x: Int, y: Int) {
        val state = _uiState.value
        val value = state.debugSelectedValue
        val asGhost = state.debugAddAsGhost

        _uiState.update { currentState ->
            var updatedGrid = currentState.grid
            var updatedPreview = currentState.preview

            if (value == null) {
                updatedGrid = updatedGrid.filterNot { it.x == x && it.y == y }
                updatedPreview = updatedPreview.filterNot { it.x == x && it.y == y }
            } else {
                val existingGrid = updatedGrid.find { it.x == x && it.y == y }
                val existingPreview = updatedPreview.find { it.x == x && it.y == y }

                if (asGhost) {
                    updatedGrid = updatedGrid.filterNot { it.x == x && it.y == y }
                    if (existingPreview != null) {
                        updatedPreview = updatedPreview.map { if (it.id == existingPreview.id) it.copy(value = value) else it }
                    } else {
                        updatedPreview = updatedPreview + engine.createPreviewCell(x, y, value)
                    }
                } else {
                    updatedPreview = updatedPreview.filterNot { it.x == x && it.y == y }
                    if (existingGrid != null) {
                        updatedGrid = updatedGrid.map { if (it.id == existingGrid.id) it.copy(value = value) else it }
                    } else {
                        updatedGrid = updatedGrid + engine.createCell(x, y, value)
                    }
                }
            }
            currentState.copy(grid = updatedGrid, preview = updatedPreview)
        }
        recalculateHints()
        checkValidMoves()
        persistState(getCurrentGameState())
    }

    fun addPerkManually(perk: Perk) {
        _uiState.update { it.copy(collectedPerks = it.collectedPerks + perk) }
        persistState(getCurrentGameState())
    }
}
