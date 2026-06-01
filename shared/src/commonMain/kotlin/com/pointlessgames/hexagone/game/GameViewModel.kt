package com.pointlessgames.hexagone.game

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.hexagone.data.LeaderboardRepository
import com.pointlessgames.hexagone.data.SettingsRepository
import com.pointlessgames.hexagone.game.logic.GameEngine
import com.pointlessgames.hexagone.game.logic.Scoring
import com.pointlessgames.hexagone.game.logic.ScoreResult
import com.pointlessgames.hexagone.game.model.ComboTier
import com.pointlessgames.hexagone.game.model.DetailedGameResult
import com.pointlessgames.hexagone.game.model.GameEffect
import com.pointlessgames.hexagone.game.model.GameItem
import com.pointlessgames.hexagone.game.model.GameState
import com.pointlessgames.hexagone.game.model.GameUiState
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeStep
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.Particle
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PotentialMerge
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
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.StringResource

internal class GameViewModel(
    private val settingsRepository: SettingsRepository,
    private val leaderboardRepository: LeaderboardRepository,
) : ViewModel() {

    private val engine = GameEngine()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _hoveredMerge = MutableStateFlow<MergeTransition?>(null)
    val hoveredMerge: StateFlow<MergeTransition?> = _hoveredMerge.asStateFlow()

    private val _effects = MutableSharedFlow<GameEffect>(extraBufferCapacity = 64)
    val effects: SharedFlow<GameEffect> = _effects.asSharedFlow()

    private val debugDelegate = DebugDelegate(
        uiState = _uiState,
        engine = engine,
        onStateChanged = {
            recalculateHints()
            checkValidMoves()
            persistState(getCurrentGameState())
        },
        onSpawnRequested = {
            spawnFromQueue(_uiState.value.grid)
        },
    )

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
                            perksUsedTracking = savedState.perksUsedTracking,
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

    fun addParticles(newParticles: List<Particle>) = viewModelScope.launch {
        _effects.emit(GameEffect.Particles(newParticles))
    }

    fun addScorePopup(
        gridX: Int,
        gridY: Int,
        score: Int,
        color: Color,
        labelRes: StringResource? = null,
    ) = viewModelScope.launch {
        _effects.emit(GameEffect.ScorePopup(gridX, gridY, score, color, labelRes))
    }

    fun addPerkPopup(gridX: Int, gridY: Int, perk: Perk) = viewModelScope.launch {
        _effects.emit(GameEffect.PerkPopup(gridX, gridY, perk))
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
                        previousState = stateHistory.lastOrNull(),
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
            perksUsedTracking = state.perksUsedTracking,
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
                reachedComboTiers = previousState.reachedComboTiers,
                pendingLevelUps = previousState.pendingLevelUps,
                perkOptions = previousState.perkOptions,
                canReroll = previousState.canReroll,
                perkSpawnCounter = previousState.perkSpawnCounter,
                perksUsedTracking = previousState.perksUsedTracking,
                earnedRewardsThisTurn = emptyList(),
                pendingMerge = null,
                activePerk = null,
                selectedCellId = null,
                isBusy = false,
            )
        }

        lastProcessedMergeId = null
        lastProcessedStepIndex = -1
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
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || (state.isStuck && state.activePerk == null) || state.perkOptions.isNotEmpty()) return

        _hoveredMerge.value = null

        val perk = state.activePerk
        val selectedId = state.selectedCellId
        val previewAtPos = state.preview.find { it.x == x && it.y == y }

        if (perk == Perk.PATH_MERGE) return
        val isTileOnlyPerk =
            perk == Perk.REMOVE_TILE || perk == Perk.INCREMENT_TILE || perk == Perk.SWAP_TILES
        if (isTileOnlyPerk && previewAtPos == null) return

        if (perk == Perk.PATH_MERGE) return

        if (perk != null && perk != Perk.FUSION && perk != Perk.CHAIN_MERGE && perk != Perk.SKIP_SPAWN && previewAtPos != null) {
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
            saveState()
            _uiState.update { currentState ->
                val firstStep = merge.steps.first()
                val isPathMerge = merge.resultId == "preview_path_merge"
                val nextComboValue = Scoring.getNextStepCombo(currentState.combo, 0, isPathMerge)
                val stepScore = Scoring.getStepScore(firstStep.baseScore, nextComboValue)

                val stateAfterMergeStart = currentState.copy(
                    grid = currentState.grid.map { cell ->
                        if (firstStep.mergingCells.any { it.id == cell.id }) cell.copy(
                            x = x,
                            y = y,
                        ) else cell
                    },
                    preview = currentState.preview.filterNot { it.x == x && it.y == y },
                    pendingMerge = merge,
                    activeMergeStepIndex = 0,
                    pendingMergeScore = stepScore,
                    combo = nextComboValue,
                    isBusy = true,
                )
                if (perk == Perk.FUSION) {
                    stateAfterMergeStart.consumePerk(Perk.FUSION)
                } else {
                    stateAfterMergeStart
                }
            }
        } else {
            if (perk == Perk.CHAIN_MERGE) {
                saveState()
                _uiState.update { it.consumePerk(Perk.CHAIN_MERGE).copy(activePerk = null) }
                finalizeAction()
            }
        }
    }

    private fun getGameItem(id: String): GameItem? {
        val state = _uiState.value
        state.grid.find { it.id == id }?.let { return GameItem(it.id, it.x, it.y, it.value, false) }
        state.preview.find { it.id == id }
            ?.let { return GameItem(it.id, it.x, it.y, it.value, true) }
        return null
    }

    fun onEmptySpaceTouchDown(x: Int, y: Int) {
        val state = _uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || (state.isStuck && state.activePerk == null) || state.perkOptions.isNotEmpty()) return

        val perk = state.activePerk
        val selectedId = state.selectedCellId
        val ghostAtPos = state.preview.find { it.x == x && it.y == y }

        if (perk != null && selectedId != null && ghostAtPos?.id == selectedId) return

        if (perk == Perk.PATH_MERGE) return
        val isTileOnlyPerk =
            perk == Perk.REMOVE_TILE || perk == Perk.INCREMENT_TILE || perk == Perk.SWAP_TILES
        if (isTileOnlyPerk && ghostAtPos == null) return

        val merge = when (perk) {
            Perk.REMOVE_TILE -> {
                if (ghostAtPos != null) {
                    val merge = MergeTransition(
                        targetX = x,
                        targetY = y,
                        steps = emptyList(),
                        finalValue = 0,
                        totalCells = 1,
                        uniqueGroups = 0,
                        baseScore = ghostAtPos.value * 10,
                        resultId = "preview_remove_queue",
                        isRemoval = true,
                        participatingIds = setOf(ghostAtPos.id),
                    )
                    merge.copy(baseScore = calculatePotentialScore(merge, state))
                } else null
            }

            Perk.INCREMENT_TILE -> {
                if (ghostAtPos != null) {
                    val nextValue = ghostAtPos.value + 1
                    val merge = MergeTransition(
                        targetX = x,
                        targetY = y,
                        steps = emptyList(),
                        finalValue = 0,
                        totalCells = 1,
                        uniqueGroups = 0,
                        baseScore = 0,
                        resultId = "preview_increment_queue",
                        participatingIds = setOf(ghostAtPos.id),
                        previewValues = mapOf(ghostAtPos.id to nextValue),
                    )
                    merge.copy(baseScore = calculatePotentialScore(merge, state))
                } else null
            }

            Perk.PATH_MERGE -> {
                val merge = engine.calculatePathMerge(x, y, state.grid)
                merge?.let {
                    it.copy(
                        resultId = "preview_path_merge",
                        forceSolidIds = setOf("preview_path_merge"),
                        previewValues = ghostAtPos?.let { mapOf(it.id to merge.finalValue) }
                            ?: emptyMap(),
                        baseScore = calculatePotentialScore(it, state)
                    )
                }
            }

            Perk.MOVE_TILE, Perk.DUPLICATE_TILE -> {
                if (selectedId != null && (ghostAtPos == null || selectedId != ghostAtPos.id)) {
                    val source = getGameItem(selectedId)
                    if (source != null) {
                        val resultId =
                            if (perk == Perk.MOVE_TILE) "preview_move" else "preview_duplicate"
                        val swaps =
                            if (perk == Perk.MOVE_TILE) mapOf(selectedId to (x to y)) else null
                        val isSourceSolid = !source.isGhost
                        val forceSolidIds = if (isSourceSolid) setOf(resultId) else emptySet()
                        val forceGhostAtSource =
                            if (perk == Perk.MOVE_TILE && source.isGhost) setOf(selectedId) else emptySet()

                        MergeTransition(
                            targetX = x,
                            targetY = y,
                            steps = emptyList(),
                            finalValue = source.value,
                            totalCells = 1,
                            uniqueGroups = 0,
                            baseScore = 0,
                            resultId = resultId,
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
                            target.id to (source.x to source.y),
                        )

                        MergeTransition(
                            targetX = x,
                            targetY = y,
                            steps = emptyList(),
                            finalValue = 0,
                            totalCells = 1,
                            uniqueGroups = 0,
                            baseScore = 0,
                            resultId = "preview_swap",
                            previewSwaps = swaps,
                            participatingIds = setOf(source.id, target.id),
                        )
                    } else null
                } else null
            }

            Perk.FUSION -> {
                val merge = engine.calculateFusion(x, y, state.grid)
                merge?.let { it.copy(baseScore = calculatePotentialScore(it, state), resultId = "preview_fusion") }
            }
            null, Perk.CHAIN_MERGE, Perk.SKIP_SPAWN -> {
                val merge = if (perk == Perk.CHAIN_MERGE) {
                    engine.simulateChainMerge(x, y, state.grid, state.combo)
                } else {
                    engine.calculateMerge(x, y, state.grid)
                }
                merge?.let { it.copy(baseScore = calculatePotentialScore(it, state), resultId = "preview_merge") }
            }

            else -> null
        }
        _hoveredMerge.value = merge
    }

    fun onCellTouchDown(cell: HexagonCell) {
        val state = _uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || (state.isStuck && state.activePerk == null) || state.perkOptions.isNotEmpty()) return

        val perk = state.activePerk
        val selectedId = state.selectedCellId

        if (perk != null && selectedId == cell.id) return

        val merge = when (perk) {
            Perk.PATH_MERGE -> {
                val m = engine.calculatePathMerge(cell.x, cell.y, state.grid)
                m?.let {
                    it.copy(
                        resultId = "preview_path_merge",
                        forceSolidIds = setOf("preview_path_merge"),
                        previewValues = mapOf(cell.id to m.finalValue),
                        baseScore = calculatePotentialScore(it, state)
                    )
                }
            }

            Perk.REMOVE_TILE -> {
                val merge = MergeTransition(
                    targetX = cell.x,
                    targetY = cell.y,
                    steps = emptyList(),
                    finalValue = 0,
                    totalCells = 1,
                    uniqueGroups = 0,
                    baseScore = cell.value * 10,
                    resultId = "preview_remove",
                    isRemoval = true,
                    participatingIds = setOf(cell.id),
                )
                merge.copy(baseScore = calculatePotentialScore(merge, state))
            }

            Perk.INCREMENT_TILE -> {
                val nextValue = cell.value + 1
                val merge = MergeTransition(
                    targetX = cell.x,
                    targetY = cell.y,
                    steps = emptyList(),
                    finalValue = 0,
                    totalCells = 1,
                    uniqueGroups = 0,
                    baseScore = 0,
                    resultId = "preview_increment",
                    participatingIds = setOf(cell.id),
                    previewValues = mapOf(cell.id to nextValue),
                )
                merge.copy(baseScore = calculatePotentialScore(merge, state))
            }

            Perk.SWAP_TILES -> {
                if (selectedId != null && selectedId != cell.id) {
                    val source = getGameItem(selectedId)
                    val target = cell
                    if (source != null) {
                        val swaps = mapOf(
                            source.id to (target.x to target.y),
                            target.id to (source.x to source.y),
                        )

                        MergeTransition(
                            targetX = cell.x,
                            targetY = cell.y,
                            steps = emptyList(),
                            finalValue = 0,
                            totalCells = 1,
                            uniqueGroups = 0,
                            baseScore = 0,
                            resultId = "preview_swap",
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
        _uiState.update { currentState ->
            val cellToMove = currentState.grid.find { it.id == selectedId }
            val previewToMove = currentState.preview.find { it.id == selectedId }
            val value = cellToMove?.value ?: previewToMove?.value ?: return@update currentState

            val collectedPerk = currentState.onBoardPerks.find { it.x == x && it.y == y }?.perk
            if (collectedPerk != null) {
                viewModelScope.launch { addPerkPopup(x, y, collectedPerk) }
            }

            val updatedGrid = if (cellToMove != null) {
                currentState.grid.map {
                    if (it.id == selectedId) it.copy(x = x, y = y, isTactical = true) else it
                }
            } else {
                currentState.grid + engine.createCell(x, y, value, isTactical = true)
            }

            val updatedPreview =
                currentState.preview.filterNot { (it.id == selectedId || (it.x == x && it.y == y)) }

            currentState.copy(
                grid = updatedGrid,
                preview = updatedPreview,
                collectedPerks = currentState.collectedPerks + listOfNotNull(collectedPerk),
                onBoardPerks = currentState.onBoardPerks.filterNot { it.x == x && it.y == y },
            ).consumePerk(Perk.MOVE_TILE).copy(activePerk = null, selectedCellId = null)
        }
        finalizeAction()
    }

    private fun duplicateTile(selectedId: String, x: Int, y: Int) {
        saveState()
        _uiState.update { currentState ->
            val cellToCopy = currentState.grid.find { it.id == selectedId }
            val previewToCopy = currentState.preview.find { it.id == selectedId }
            val value = cellToCopy?.value ?: previewToCopy?.value ?: return@update currentState

            val collectedPerk = currentState.onBoardPerks.find { it.x == x && it.y == y }?.perk
            if (collectedPerk != null) {
                viewModelScope.launch { addPerkPopup(x, y, collectedPerk) }
            }

            val updatedGrid = currentState.grid + engine.createCell(x, y, value, isTactical = true)
            val updatedPreview = currentState.preview.filterNot { it.x == x && it.y == y }

            currentState.copy(
                grid = updatedGrid,
                preview = updatedPreview,
                collectedPerks = currentState.collectedPerks + listOfNotNull(collectedPerk),
                onBoardPerks = currentState.onBoardPerks.filterNot { it.x == x && it.y == y },
            ).consumePerk(Perk.DUPLICATE_TILE).copy(activePerk = null, selectedCellId = null)
        }
        finalizeAction()
    }

    private fun finalizeAction() {
        updateLevel()
        if (_uiState.value.preview.isEmpty()) {
            spawnFromQueue(_uiState.value.grid)
        } else {
            checkValidMoves()
        }
    }

    fun onPreviewClicked(preview: PreviewCell) {
        val state = _uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || (state.isStuck && state.activePerk == null) || state.perkOptions.isNotEmpty()) return

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
                _uiState.update { currentState ->
                    val previewToTarget = currentState.preview.find { it.id == preview.id }
                        ?: return@update currentState
                    val merge = MergeTransition(
                        targetX = previewToTarget.x,
                        targetY = previewToTarget.y,
                        steps = emptyList(),
                        finalValue = 0,
                        totalCells = 1,
                        uniqueGroups = 0,
                        baseScore = previewToTarget.value * 10,
                        resultId = "preview_remove_queue",
                        isRemoval = true,
                        participatingIds = setOf(previewToTarget.id),
                    )
                    val scoreResult = Scoring.calculateFinalScore(
                        merge = merge,
                        grid = currentState.grid,
                        preview = currentState.preview,
                        initialCombo = currentState.combo,
                        activePerk = currentState.activePerk,
                        lastMoveScore = lastMoveScore
                    )

                    val labelRes =
                        if (scoreResult.barRaisedBonus > 0) Res.string.label_bar_raised else Res.string.label_cleanup
                    val nextScore = currentState.score + scoreResult.totalScore

                    viewModelScope.launch {
                        persistBestScore(nextScore)
                        val popupColor =
                            if (scoreResult.barRaisedBonus > 0) Colors().skyBlue else Colors().greyBlue
                        addScorePopup(
                            previewToTarget.x,
                            previewToTarget.y,
                            scoreResult.totalScore,
                            popupColor,
                            labelRes
                        )
                    }

                    currentState.copy(
                        preview = currentState.preview.filter { it.id != previewToTarget.id },
                        score = nextScore,
                    ).consumePerk(Perk.REMOVE_TILE).copy(activePerk = null, selectedCellId = null)
                }
                finalizeAction()
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
                _uiState.update { currentState ->
                    val previewToUpdate = currentState.preview.find { it.id == preview.id }
                        ?: return@update currentState
                    val merge = MergeTransition(
                        targetX = previewToUpdate.x,
                        targetY = previewToUpdate.y,
                        steps = emptyList(),
                        finalValue = 0,
                        totalCells = 1,
                        uniqueGroups = 0,
                        baseScore = 0,
                        resultId = "preview_increment_queue",
                        participatingIds = setOf(previewToUpdate.id),
                        previewValues = mapOf(previewToUpdate.id to previewToUpdate.value + 1),
                    )
                    val scoreResult = Scoring.calculateFinalScore(
                        merge = merge,
                        grid = currentState.grid,
                        preview = currentState.preview,
                        initialCombo = currentState.combo,
                        activePerk = currentState.activePerk,
                        lastMoveScore = lastMoveScore
                    )

                    val nextPreview = currentState.preview.map {
                        if (it.id == previewToUpdate.id) it.copy(
                            value = it.value + 1,
                            isTactical = true,
                        ) else it
                    }

                    if (scoreResult.totalScore > 0) {
                        val nextScore = currentState.score + scoreResult.totalScore
                        viewModelScope.launch {
                            persistBestScore(nextScore)
                            addScorePopup(
                                previewToUpdate.x,
                                previewToUpdate.y,
                                scoreResult.totalScore,
                                Colors().skyBlue,
                                Res.string.label_bar_raised,
                            )
                        }
                        currentState.copy(
                            preview = nextPreview,
                            score = nextScore,
                        ).consumePerk(Perk.INCREMENT_TILE)
                            .copy(activePerk = null, selectedCellId = null)
                    } else {
                        currentState.copy(preview = nextPreview).consumePerk(Perk.INCREMENT_TILE)
                            .copy(activePerk = null, selectedCellId = null)
                    }
                }
                finalizeAction()
            }

            else -> {}
        }
    }

    fun onCellClicked(cell: HexagonCell) {
        val state = _uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || (state.isStuck && state.activePerk == null) || state.perkOptions.isNotEmpty()) return

        when (val perk = state.activePerk) {
            Perk.MOVE_TILE -> {
                _uiState.update { it.copy(selectedCellId = if (it.selectedCellId == cell.id) null else cell.id) }
            }

            Perk.DUPLICATE_TILE -> {
                _uiState.update { it.copy(selectedCellId = if (it.selectedCellId == cell.id) null else cell.id) }
            }

            Perk.INCREMENT_TILE -> {
                saveState()
                _uiState.update { currentState ->
                    val cellToUpdate =
                        currentState.grid.find { it.id == cell.id } ?: return@update currentState
                    val merge = MergeTransition(
                        targetX = cellToUpdate.x,
                        targetY = cellToUpdate.y,
                        steps = emptyList(),
                        finalValue = 0,
                        totalCells = 1,
                        uniqueGroups = 0,
                        baseScore = 0,
                        resultId = "preview_increment",
                        participatingIds = setOf(cellToUpdate.id),
                        previewValues = mapOf(cellToUpdate.id to cellToUpdate.value + 1),
                    )
                    val scoreResult = Scoring.calculateFinalScore(
                        merge = merge,
                        grid = currentState.grid,
                        preview = currentState.preview,
                        initialCombo = currentState.combo,
                        activePerk = currentState.activePerk,
                        lastMoveScore = lastMoveScore
                    )

                    val nextGrid = currentState.grid.map {
                        if (it.id == cellToUpdate.id) it.copy(
                            value = it.value + 1,
                            isTactical = true,
                        ) else it
                    }

                    if (scoreResult.totalScore > 0) {
                        val nextScore = currentState.score + scoreResult.totalScore
                        viewModelScope.launch {
                            persistBestScore(nextScore)
                            addScorePopup(
                                cellToUpdate.x,
                                cellToUpdate.y,
                                scoreResult.totalScore,
                                Colors().skyBlue,
                                Res.string.label_bar_raised,
                            )
                        }
                        currentState.copy(
                            grid = nextGrid,
                            score = nextScore,
                        ).consumePerk(Perk.INCREMENT_TILE)
                            .copy(activePerk = null, selectedCellId = null)
                    } else {
                        currentState.copy(grid = nextGrid).consumePerk(Perk.INCREMENT_TILE)
                            .copy(activePerk = null, selectedCellId = null)
                    }
                }
                finalizeAction()
            }

            Perk.PATH_MERGE -> {
                val merge = engine.calculatePathMerge(cell.x, cell.y, state.grid)
                if (merge != null) {
                    saveState()
                    _uiState.update { currentState ->
                        val firstStep = merge.steps.first()
                        val nextComboValue = Scoring.getNextStepCombo(currentState.combo, 0, true)
                        val stepScore = Scoring.getStepScore(firstStep.baseScore, nextComboValue)

                        currentState.copy(
                            grid = currentState.grid.map { c ->
                                if (firstStep.mergingCells.any { it.id == c.id }) c.copy(
                                    x = cell.x,
                                    y = cell.y,
                                ) else c
                            },
                            pendingMerge = merge.copy(resultId = "preview_path_merge"),
                            activeMergeStepIndex = 0,
                            pendingMergeScore = stepScore,
                            combo = nextComboValue,
                            isBusy = true,
                        )
                    }
                }
            }

            Perk.REMOVE_TILE -> {
                saveState()
                _uiState.update { currentState ->
                    val cellToRemove =
                        currentState.grid.find { it.id == cell.id } ?: return@update currentState
                    val merge = MergeTransition(
                        targetX = cellToRemove.x,
                        targetY = cellToRemove.y,
                        steps = emptyList(),
                        finalValue = 0,
                        totalCells = 1,
                        uniqueGroups = 0,
                        baseScore = cellToRemove.value * 10,
                        resultId = "preview_remove",
                        isRemoval = true,
                        participatingIds = setOf(cellToRemove.id),
                    )
                    val scoreResult = Scoring.calculateFinalScore(
                        merge = merge,
                        grid = currentState.grid,
                        preview = currentState.preview,
                        initialCombo = currentState.combo,
                        activePerk = currentState.activePerk,
                        lastMoveScore = lastMoveScore
                    )

                    val labelRes = when {
                        scoreResult.barRaisedBonus > 0 && scoreResult.sacrificeBonus > 0 -> Res.string.label_janitor_plus
                        scoreResult.barRaisedBonus > 0 -> Res.string.label_bar_raised
                        scoreResult.sacrificeBonus > 0 -> Res.string.label_sacrifice
                        else -> Res.string.label_cleanup
                    }

                    val nextScore = currentState.score + scoreResult.totalScore
                    viewModelScope.launch {
                        persistBestScore(nextScore)
                        val popupColor = when {
                            scoreResult.barRaisedBonus > 0 -> Colors().skyBlue
                            scoreResult.sacrificeBonus > 0 -> Colors().pink
                            else -> Colors().greyBlue
                        }
                        addScorePopup(
                            cellToRemove.x,
                            cellToRemove.y,
                            scoreResult.totalScore,
                            popupColor,
                            labelRes,
                        )
                    }

                    currentState.copy(
                        grid = currentState.grid.filter { it.id != cellToRemove.id },
                        score = nextScore,
                    ).consumePerk(Perk.REMOVE_TILE).copy(activePerk = null, selectedCellId = null)
                }
                finalizeAction()
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
        _uiState.update { currentState ->
            val item1 = currentState.grid.find { it.id == id1 }
                ?: currentState.preview.find { it.id == id1 }
            val item2 = currentState.grid.find { it.id == id2 }
                ?: currentState.preview.find { it.id == id2 }

            if (item1 == null || item2 == null) return@update currentState

            val x1 = if (item1 is HexagonCell) item1.x else (item1 as PreviewCell).x
            val y1 = if (item1 is HexagonCell) item1.y else (item1 as PreviewCell).y
            val x2 = if (item2 is HexagonCell) item2.x else (item2 as PreviewCell).x
            val y2 = if (item2 is HexagonCell) item2.y else (item2 as PreviewCell).y

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

            if (collectedPerkAt1 != null) {
                viewModelScope.launch { addPerkPopup(x1, y1, collectedPerkAt1) }
            }
            if (collectedPerkAt2 != null) {
                viewModelScope.launch { addPerkPopup(x2, y2, collectedPerkAt2) }
            }

            currentState.copy(
                grid = updatedGrid,
                preview = updatedPreview,
                collectedPerks = currentState.collectedPerks + listOfNotNull(
                    collectedPerkAt1,
                    collectedPerkAt2,
                ),
                onBoardPerks = currentState.onBoardPerks.filterNot { (it.x == x1 && it.y == y1) || (it.x == x2 && it.y == y2) },
            ).consumePerk(Perk.SWAP_TILES).copy(activePerk = null, selectedCellId = null)
        }
        finalizeAction()
    }

    fun onUsePerkClicked(perk: Perk) {
        if (_uiState.value.activePerk == perk) {
            _uiState.update { it.copy(activePerk = null, selectedCellId = null) }
            recalculateHints()
            checkValidMoves()
            return
        }

        when (perk) {
            Perk.ADVANCE_QUEUE -> {
                saveState()
                _uiState.update {
                    it.consumePerk(Perk.ADVANCE_QUEUE)
                        .copy(isGameOver = false, activePerk = null, selectedCellId = null)
                }
                spawnFromQueue(_uiState.value.grid, decrementLifespan = false)
            }

            Perk.UNDO -> {
                if (undoLastMove()) {
                    _uiState.update {
                        it.consumePerk(Perk.UNDO)
                            .copy(isGameOver = false, activePerk = null, selectedCellId = null)
                    }
                    persistState(getCurrentGameState())
                }
            }

            Perk.SKIP_SPAWN -> {
                _uiState.update {
                    it.copy(
                        isGameOver = false,
                        activePerk = Perk.SKIP_SPAWN,
                        selectedCellId = null,
                    )
                }
            }

            else -> {
                _uiState.update {
                    it.copy(
                        isGameOver = false,
                        activePerk = perk,
                        selectedCellId = null,
                    )
                }
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
        recalculateHints()
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
            earnedRewardsThisTurn = emptyList(),
        )
        updateLevel()
        checkValidMoves()
        persistState(getCurrentGameState())
    }

    fun onMergeAnimationFinished() {
        val state = _uiState.value
        val merge = state.pendingMerge ?: return
        val stepIndex = state.activeMergeStepIndex

        if (lastProcessedMergeId == merge.resultId && lastProcessedStepIndex == stepIndex) return
        lastProcessedMergeId = merge.resultId
        lastProcessedStepIndex = stepIndex

        val currentStep = merge.steps[stepIndex]

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }

            val currentState = _uiState.value
            val stateAfterStep = calculateStateAfterStep(currentState, merge, currentStep)

            if (stepIndex >= merge.steps.lastIndex) {
                processFinalStep(merge, currentState, stateAfterStep)
            } else {
                processNextStep(merge, stepIndex, stateAfterStep)
            }
        }
    }

    private fun calculateStateAfterStep(
        currentState: GameUiState,
        merge: MergeTransition,
        currentStep: MergeStep,
    ): List<HexagonCell> {
        return currentState.grid.filter { cell ->
            currentStep.mergingCells.none { it.id == cell.id } && (cell.x != merge.targetX || cell.y != merge.targetY)
        } + engine.createCell(
            merge.targetX,
            merge.targetY,
            currentStep.resultValue,
            merge.resultId,
        )
    }

    private suspend fun processFinalStep(
        merge: MergeTransition,
        currentState: GameUiState,
        stateAfterStep: List<HexagonCell>,
    ) {
        val barRaisedBonus = Scoring.calculateBarRaisedBonus(
            currentState.grid,
            currentState.preview,
            stateAfterStep,
            currentState.preview.filter { it.x != merge.targetX || it.y != merge.targetY },
        )

        val scoreResult = Scoring.calculateFinalScore(
            merge,
            currentState.grid,
            currentState.preview,
            currentState.combo,
            currentState.activePerk,
            lastMoveScore
        ).let {
            val multiplier = (it.finalCombo + 1).coerceAtMost(Scoring.MAX_COMBO_MULTIPLIER)
            val updatedBonus = (it.redemptionBonus + barRaisedBonus) * multiplier
            it.copy(
                totalScore = it.stepScore + updatedBonus + it.sacrificeBonus,
                bonusScore = updatedBonus,
                barRaisedBonus = barRaisedBonus
            )
        }

        lastMoveScore = null
        val finalCombo = scoreResult.finalCombo
        val redemptionBonus = scoreResult.redemptionBonus > 0
        val isBarRaised = barRaisedBonus > 0

        handlePopups(merge, scoreResult.totalScore, redemptionBonus, isBarRaised)

        val collectedOnBoard =
            currentState.onBoardPerks.find { it.x == merge.targetX && it.y == merge.targetY }?.perk

        if (collectedOnBoard != null) {
            addPerkPopup(merge.targetX, merge.targetY, collectedOnBoard)
        }

        val remainingOnBoard =
            currentState.onBoardPerks.filterNot { it.x == merge.targetX && it.y == merge.targetY }

        val finalScore = currentState.score + scoreResult.totalScore
        persistBestScore(finalScore)

        val tierRewards = handleTierRewards(finalCombo, currentState)
        val newlyEarnedPerks = tierRewards.map { it.second }
        val newReachedTiers = tierRewards.map { it.first }.toSet()
        val newRewardEffects = tierRewards.map { GameEffect.TierReward(it.first, it.second) }

        val finalGrid = if ((finalCombo + 1) >= ComboTier.ZENITH.threshold && 
            !currentState.reachedComboTiers.contains(ComboTier.ZENITH)) {
            stateAfterStep.map { it.copy(value = it.value + 1) }
        } else stateAfterStep

        _uiState.update {
            it.copy(
                score = finalScore,
                bestScore = maxOf(it.bestScore, finalScore),
                levelProgress = engine.getLevelProgress(finalScore, it.level),
                combo = finalCombo,
                grid = finalGrid,
                collectedPerks = it.collectedPerks + listOfNotNull(collectedOnBoard) + newlyEarnedPerks,
                onBoardPerks = remainingOnBoard,
                reachedComboTiers = if (finalCombo == 0) emptySet() else it.reachedComboTiers + newReachedTiers,
                earnedRewardsThisTurn = it.earnedRewardsThisTurn + newRewardEffects,
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

        handleChainMerge(merge, finalCombo)
    }

    private fun handlePopups(
        merge: MergeTransition,
        totalScore: Int,
        isRedemption: Boolean,
        isBarRaised: Boolean,
    ) {
        viewModelScope.launch {
            val labelRes = when {
                isRedemption && isBarRaised -> Res.string.label_tactical_redemption
                isRedemption -> Res.string.label_redemption
                isBarRaised -> Res.string.label_bar_raised
                else -> null
            }
            val color = when {
                isRedemption -> Colors().gold
                isBarRaised -> Colors().skyBlue
                else -> Color.White
            }
            addScorePopup(merge.targetX, merge.targetY, totalScore, color, labelRes)
        }
    }

    private fun handleTierRewards(
        finalCombo: Int,
        currentState: GameUiState,
    ): List<Pair<ComboTier, Perk>> {
        val rewards = mutableListOf<Pair<ComboTier, Perk>>()
        ComboTier.entries.forEach { tier ->
            if (finalCombo + 1 >= tier.threshold && !currentState.reachedComboTiers.contains(tier)) {
                val reward = when (tier) {
                    ComboTier.SURGE -> Perk.entries.filter { it.baseWeight in 50..80 }.random()
                    ComboTier.OVERDRIVE -> Perk.entries.filter { it.isLegendary }.random()
                    ComboTier.ZENITH -> Perk.entries.filter { it.isLegendary }.random()
                }
                rewards.add(tier to reward)
            }
        }
        return rewards
    }

    private suspend fun handleChainMerge(merge: MergeTransition, finalCombo: Int) {
        val chainMerge = if (_uiState.value.activePerk == Perk.CHAIN_MERGE) {
            engine.calculateMerge(merge.targetX, merge.targetY, _uiState.value.grid)
        } else null

        if (chainMerge != null) {
            delay(150)
            val nextCombo = finalCombo + 1
            val chainScore = Scoring.getStepScore(chainMerge.steps.first().baseScore, nextCombo)

            _uiState.update {
                it.copy(
                    combo = nextCombo,
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
            val activePerkBeforeClear = _uiState.value.activePerk
            _uiState.update { state ->
                val activePerk = state.activePerk
                val shouldConsume = activePerk == Perk.CHAIN_MERGE || 
                                    activePerk == Perk.PATH_MERGE || 
                                    activePerk == Perk.SKIP_SPAWN
                val nextState = if (shouldConsume) state.consumePerk(activePerk) else state
                nextState.copy(activePerk = null)
            }

            if (activePerkBeforeClear == Perk.SKIP_SPAWN) {
                spawnFromQueue(_uiState.value.grid, decrementLifespan = false, skipSpawn = true)
            } else {
                spawnFromQueue(_uiState.value.grid)
            }
        }
    }

    private suspend fun processNextStep(
        merge: MergeTransition,
        stepIndex: Int,
        stateAfterStep: List<HexagonCell>,
    ) {
        delay(100)
        val nextStep = merge.steps[stepIndex + 1]

        _uiState.update { state ->
            val isPathMerge = merge.resultId.contains("path_merge")
            val nextComboValue = Scoring.getNextStepCombo(state.combo, stepIndex + 1, isPathMerge)
            val stepScore = Scoring.getStepScore(nextStep.baseScore, nextComboValue)

            val tierRewards = handleTierRewards(nextComboValue, state)
            val newReachedTiers = tierRewards.map { it.first }.toSet()
            val newlyEarnedPerks = tierRewards.map { it.second }
            val newRewardEffects = tierRewards.map { GameEffect.TierReward(it.first, it.second) }

            state.copy(
                combo = nextComboValue,
                reachedComboTiers = state.reachedComboTiers + newReachedTiers,
                earnedRewardsThisTurn = state.earnedRewardsThisTurn + newRewardEffects,
                collectedPerks = state.collectedPerks + newlyEarnedPerks,
                grid = stateAfterStep.map { cell ->
                    if (nextStep.mergingCells.any { it.id == cell.id }) cell.copy(
                        x = merge.targetX,
                        y = merge.targetY,
                    ) else cell
                },
                activeMergeStepIndex = stepIndex + 1,
                pendingMergeScore = state.pendingMergeScore + stepScore,
            )
        }
    }

    private fun checkValidMoves() {
        val state = _uiState.value
        val isPossible = engine.isMovePossible(state.grid)
        val hasPerkOptions = state.perkOptions.isNotEmpty()

        val actionablePerks = state.collectedPerks.filter { perk ->
            engine.canPerkResolveStuck(
                perk = perk,
                grid = state.grid,
                previews = state.preview,
                previousState = stateHistory.lastOrNull(),
            )
        }.toSet()

        _uiState.update {
            if (state.isDebugMode) {
                it.copy(isStuck = false, isGameOver = false, stuckPerks = emptySet())
            } else if (isPossible || hasPerkOptions) {
                it.copy(isStuck = false, isGameOver = false, stuckPerks = emptySet())
            } else if (actionablePerks.isNotEmpty()) {
                it.copy(isStuck = true, isGameOver = false, stuckPerks = actionablePerks)
            } else {
                it.copy(isStuck = false, stuckPerks = emptySet())
            }
        }

        if (!isPossible && hasPerkOptions.not() && actionablePerks.isEmpty() && !state.isDebugMode) {
            viewModelScope.launch {
                delay(1000)
                val finalResult = DetailedGameResult(
                    score = state.score,
                    maxCombo = state.maxCombo,
                    maxPiece = state.highestValue,
                    totalMerges = state.totalMerges,
                    level = state.level,
                    perksUsed = state.perksUsedTracking,
                    perksAvailable = state.collectedPerks,
                    region = settingsRepository.getPlayerRegion() ?: "Global",
                )

                val playerName = settingsRepository.getPlayerName()
                _uiState.update { it.copy(isGameOver = true, pendingResult = if (playerName == null) finalResult else null) }
                settingsRepository.setGameState(null)

                if (playerName != null) {
                    val rankInfo = leaderboardRepository.submitResult(finalResult)
                    _uiState.update { it.copy(currentRank = rankInfo) }
                }
            }
        }
        recalculateHints()
    }

    private fun spawnFromQueue(
        currentState: List<HexagonCell>,
        decrementLifespan: Boolean = true,
        skipSpawn: Boolean = false,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            val gridWithoutTactical = engine.decrementTacticalFlags(currentState)
            val currentPerks = _uiState.value.onBoardPerks

            val (newState, newPreviews, perksAfterSpawn) = if (skipSpawn) {
                Triple(gridWithoutTactical, _uiState.value.preview, currentPerks)
            } else {
                engine.spawnFromQueue(
                    gridWithoutTactical,
                    _uiState.value.preview,
                    currentPerks,
                )
            }

            val updatedPerks = if (decrementLifespan) {
                engine.updateOnBoardPerks(perksAfterSpawn)
            } else {
                perksAfterSpawn
            }

            val (nextPerks, nextCounter) = engine.trySpawnPerkOnBoard(
                newState,
                newPreviews,
                updatedPerks,
                _uiState.value.perkSpawnCounter,
            )

            val rewardsToEmit = _uiState.value.earnedRewardsThisTurn

            _uiState.update {
                it.copy(
                    grid = newState,
                    preview = newPreviews,
                    onBoardPerks = nextPerks,
                    perkSpawnCounter = nextCounter,
                    earnedRewardsThisTurn = emptyList()
                )
            }

            rewardsToEmit.forEach { reward ->
                _effects.emit(reward)
            }

            updateLevel()
            checkValidMoves()
            _uiState.update { it.copy(isBusy = false) }
            persistState(getCurrentGameState())
        }
    }

    private fun calculatePotentialScore(merge: MergeTransition, state: GameUiState): Int {
        return Scoring.calculateFinalScore(
            merge = merge,
            grid = state.grid,
            preview = state.preview,
            initialCombo = state.combo,
            activePerk = state.activePerk,
            lastMoveScore = lastMoveScore
        ).totalScore
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
                        Perk.CHAIN_MERGE -> engine.simulateChainMerge(x, y, grid, state.combo)
                        else -> engine.calculateMerge(x, y, grid)
                    }

                    if (merge != null) {
                        result[x to y] = PotentialMerge(
                            targetX = x,
                            targetY = y,
                            finalValue = merge.finalValue,
                            baseScore = calculatePotentialScore(merge, state),
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

    fun setDebugSelectedValue(value: Int?) = debugDelegate.setDebugSelectedValue(value)

    fun toggleDebugMode() = debugDelegate.toggleDebugMode()

    fun toggleDebugAddAsGhost() = debugDelegate.toggleDebugAddAsGhost()

    fun onDebugCellClicked(x: Int, y: Int) = debugDelegate.onDebugCellClicked(x, y)

    fun addPerkManually(perk: Perk) = debugDelegate.addPerkManually(perk)
}
