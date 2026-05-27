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
import com.pointlessgames.hexagone.game.model.ScorePopup
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

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
)

@Immutable
internal data class PotentialMerge(
    val targetX: Int,
    val targetY: Int,
    val finalValue: Int,
    val baseScore: Int,
    val participatingIds: Set<String>
)

internal sealed interface GameEffect {
    data class Particles(val particles: List<Particle>) : GameEffect
    data class ScorePopup(
        val gridX: Int,
        val gridY: Int,
        val score: Int,
        val color: Color,
        val label: String? = null
    ) : GameEffect
    data class PerkPopup(
        val gridX: Int,
        val gridY: Int,
        val perk: Perk
    ) : GameEffect
}

@Serializable
internal enum class ComboTier(val label: String, val threshold: Int) {
    SURGE("SURGE", 11),
    OVERDRIVE("OVERDRIVE", 21),
    ZENITH("ZENITH", 31)
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
                            bestScore = best,
                            mergeHintsEnabled = hintsEnabled
                        )
                    }
                    lastLevel = savedState.level
                    updateLevel()
                    checkValidMoves()
                } catch (e: Exception) {
                    _uiState.update { it.copy(bestScore = best, mergeHintsEnabled = hintsEnabled) }
                    restartGame()
                }
            } else {
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

    fun addScorePopup(gridX: Int, gridY: Int, score: Int, color: Color, label: String? = null) {
        viewModelScope.launch {
            _effects.emit(GameEffect.ScorePopup(gridX, gridY, score, color, label))
        }
    }

    fun addPerkPopup(gridX: Int, gridY: Int, perk: Perk) {
        viewModelScope.launch {
            _effects.emit(GameEffect.PerkPopup(gridX, gridY, perk))
        }
    }

    private fun saveState() {
        val currentState = getCurrentGameState()
        stateHistory.add(currentState)
        if (stateHistory.size > 10) stateHistory.removeAt(0)
        persistState(currentState)
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

    fun onEmptySpaceTouchDown(x: Int, y: Int) {
        val state = _uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || state.isStuck || state.perkOptions.isNotEmpty()) return

        val perk = state.activePerk
        if (perk != null && perk != Perk.CHAIN_MERGE && perk != Perk.FUSION) return

        if (state.grid.any { it.x == x && it.y == y }) return

        val merge = if (perk == Perk.FUSION) {
            engine.calculateFusion(x, y, state.grid)
        } else {
            engine.calculateMerge(x, y, state.grid)
        }
        _hoveredMerge.value = merge
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
                            isTactical = true
                        ) else it
                    },
                    preview = state.preview.filterNot { it.x == x && it.y == y },
                    collectedPerks = state.collectedPerks + listOfNotNull(collectedPerk),
                    onBoardPerks = state.onBoardPerks.filterNot { it.x == x && it.y == y }
                )
            } else {
                val previewToMove = state.preview.find { it.id == selectedId }
                if (previewToMove != null) {
                    state.copy(
                        preview = state.preview.map {
                            if (it.id == selectedId) it.copy(
                                x = x,
                                y = y,
                                isTactical = true
                            ) else it
                        }.filterNot { it.id != selectedId && it.x == x && it.y == y },
                        collectedPerks = state.collectedPerks + listOfNotNull(collectedPerk),
                        onBoardPerks = state.onBoardPerks.filterNot { it.x == x && it.y == y }
                    )
                } else state
            }
        }

        finishPerkAction(Perk.MOVE_TILE)
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

            Perk.REMOVE_TILE -> {
                saveState()
                val baseCleanupScore = preview.value * 10
                
                val currentMinOnBoard = state.grid.minOfOrNull { it.value } ?: Int.MAX_VALUE
                val isLowestValue = preview.value <= currentMinOnBoard
                val isLastInQueue = state.preview.count { it.value == preview.value } == 1
                val noneOnBoard = state.grid.none { it.value == preview.value }
                
                val barRaised = isLowestValue && isLastInQueue && noneOnBoard
                val finalBonus = if (barRaised) baseCleanupScore + 250 else baseCleanupScore
                val label = if (barRaised) "BAR RAISED" else "CLEANUP"

                _uiState.update { s ->
                    s.copy(
                        preview = s.preview.filter { it.id != preview.id },
                        score = s.score + finalBonus
                    )
                }
                addScorePopup(preview.x, preview.y, finalBonus, Color(0xFF90A4AE), label)
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

            Perk.REMOVE_TILE -> {
                saveState()
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
                
                val label = if (barRaised && isOnlyHighest) "JANITOR+" else if (barRaised) "BAR RAISED" else if (isOnlyHighest) "SACRIFICE" else "CLEANUP"

                _uiState.update { it.copy(
                    grid = it.grid.filter { it.id != cell.id },
                    score = it.score + finalBonus
                ) }
                val popupColor = if (barRaised) Color(0xFF4FC3F7) else if (isOnlyHighest) Color(0xFFF06292) else Color(0xFF90A4AE)
                addScorePopup(cell.x, cell.y, finalBonus, popupColor, label)
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
        val cell1 = state.grid.find { it.id == id1 }
        val cell2 = state.grid.find { it.id == id2 }
        val preview1 = state.preview.find { it.id == id1 }
        val preview2 = state.preview.find { it.id == id2 }

        val x1 = cell1?.x ?: preview1?.x ?: return
        val y1 = cell1?.y ?: preview1?.y ?: return
        val x2 = cell2?.x ?: preview2?.x ?: return
        val y2 = cell2?.y ?: preview2?.y ?: return

        val collectedPerkAt1 = state.onBoardPerks.find { it.x == x1 && it.y == y1 }?.perk
        val collectedPerkAt2 = state.onBoardPerks.find { it.x == x2 && it.y == y2 }?.perk
        
        if (collectedPerkAt1 != null) addPerkPopup(x1, y1, collectedPerkAt1)
        if (collectedPerkAt2 != null) addPerkPopup(x2, y2, collectedPerkAt2)

        _uiState.update { currentState ->
            currentState.copy(
                grid = currentState.grid.map {
                    when (it.id) {
                        id1 -> it.copy(x = x2, y = y2, isTactical = true)
                        id2 -> it.copy(x = x1, y = y1, isTactical = true)
                        else -> it
                    }
                },
                preview = currentState.preview.map {
                    when (it.id) {
                        id1 -> it.copy(x = x2, y = y2, isTactical = true)
                        id2 -> it.copy(x = x1, y = y1, isTactical = true)
                        else -> it
                    }
                },
                collectedPerks = currentState.collectedPerks + listOfNotNull(collectedPerkAt1, collectedPerkAt2),
                onBoardPerks = currentState.onBoardPerks.filterNot { (it.x == x1 && it.y == y1) || (it.x == x2 && it.y == y2) }
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

        _uiState.update { it.copy(isStuck = false, isGameOver = false) }
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
            )
        }
        checkValidMoves()
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
            bestScore = _uiState.value.bestScore,
            collectedPerks = listOf(),
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

                val redemptionBonus = if (lastMoveScore != null && totalAddedScore > lastMoveScore!!) {
                    val bonus = 250 + ((totalAddedScore - lastMoveScore!!) * 0.5).toInt()
                    lastMoveScore = null
                    bonus
                } else {
                    lastMoveScore = null
                    0
                }

                val combinedScore = totalAddedScore + redemptionBonus
                val label = when {
                    redemptionBonus > 0 && merge.isTactical -> "TACTICAL REDEMPTION"
                    redemptionBonus > 0 -> "REDEMPTION"
                    merge.isTactical -> "TACTICIAN"
                    else -> null
                }

                val popupColor = when (label) {
                    "TACTICAL REDEMPTION", "REDEMPTION" -> Color(0xFFFFD54F)
                    "TACTICIAN" -> Color(0xFFBB86FC)
                    else -> Color.White
                }

                addScorePopup(
                    merge.targetX,
                    merge.targetY,
                    combinedScore,
                    popupColor,
                    label
                )

                val collectedOnBoard =
                    currentState.onBoardPerks.find { it.x == merge.targetX && it.y == merge.targetY }?.perk
                
                if (collectedOnBoard != null) {
                    addPerkPopup(
                        merge.targetX,
                        merge.targetY,
                        collectedOnBoard
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
                val nextBestScore =
                    if (finalScore > currentState.bestScore) finalScore else currentState.bestScore
                if (nextBestScore > currentState.bestScore) {
                    settingsRepository.setBestScore(nextBestScore)
                }

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
                        bestScore = nextBestScore,
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
                    spawnFromQueue(_uiState.value.grid)
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
        val actionablePerks = state.collectedPerks.filter { it.canSaveFromStuck }

        _uiState.update {
            if (isPossible || hasPerkOptions) {
                it.copy(isStuck = false, isGameOver = false)
            } else if (actionablePerks.isNotEmpty()) {
                it.copy(isStuck = true, isGameOver = false)
            } else {
                it.copy(isStuck = false)
            }
        }

        if (!isPossible && hasPerkOptions.not() && actionablePerks.isEmpty()) {
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
                currentPerks
            )
            val updatedPerks = engine.updateOnBoardPerks(perksAfterSpawn)
            val (nextPerks, nextCounter) = engine.trySpawnPerkOnBoard(
                newState,
                newPreviews,
                updatedPerks,
                _uiState.value.perkSpawnCounter
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
                    val merge = if (perk == Perk.FUSION) {
                        engine.calculateFusion(x, y, grid)
                    } else {
                        engine.calculateMerge(x, y, grid)
                    }
                    
                    if (merge != null) {
                        result[x to y] = PotentialMerge(
                            targetX = x,
                            targetY = y,
                            finalValue = merge.finalValue,
                            baseScore = merge.baseScore,
                            participatingIds = merge.steps.flatMap { it.mergingCells }.map { it.id }.toSet()
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
}
