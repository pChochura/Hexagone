package com.pointlessgames.hexagone.game.model

import com.pointlessgames.hexagone.game.logic.GameEngine
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

sealed interface PerkActionResult {
    data class MoveTile(val sourceId: String, val targetX: Int, val targetY: Int) : PerkActionResult
    data class DuplicateTile(val sourceId: String, val targetX: Int, val targetY: Int) : PerkActionResult
    data class SwapTiles(val id1: String, val id2: String) : PerkActionResult
    data class TriggerMerge(val merge: MergeTransition) : PerkActionResult
    data class RemoveTile(val targetId: String, val targetX: Int, val targetY: Int) : PerkActionResult
    data class IncrementTile(val targetId: String, val targetX: Int, val targetY: Int) : PerkActionResult
    data class MimicTile(val targetId: String, val targetX: Int, val targetY: Int) : PerkActionResult
    data class FreezeTile(val targetId: String, val targetX: Int, val targetY: Int) : PerkActionResult
    data class SelectCell(val cellId: String?) : PerkActionResult
    data object None : PerkActionResult
}

@Serializable(with = PerkSerializer::class)
sealed class Perk(val baseWeight: Int, val id: String) {
    val isLegendary: Boolean get() = baseWeight <= 20
    val name: String get() = id
    val ordinal: Int get() = entries.indexOf(this)

    open internal fun canResolveStuck(grid: List<HexagonCell>, previews: List<PreviewCell>, engine: GameEngine, previousState: GameState?): Boolean = false

    open internal fun onEmptySpaceTouchDown(x: Int, y: Int, state: GameUiState, engine: GameEngine): MergeTransition? = null
    open internal fun onEmptySpaceClicked(x: Int, y: Int, state: GameUiState, engine: GameEngine): PerkActionResult = PerkActionResult.None
    
    open internal fun onCellTouchDown(cell: HexagonCell, state: GameUiState, engine: GameEngine): MergeTransition? = null
    open internal fun onCellClicked(cell: HexagonCell, state: GameUiState, engine: GameEngine): PerkActionResult = PerkActionResult.None

    open internal fun onPreviewClicked(preview: PreviewCell, state: GameUiState, engine: GameEngine): PerkActionResult = PerkActionResult.None

    open internal fun getMergeHints(grid: List<HexagonCell>, previews: List<PreviewCell>, currentCombo: Int, engine: GameEngine): List<MergeHint>? = null

    companion object {
        val entries: List<Perk> = listOf(
            UNDO, MOVE_TILE, REMOVE_TILE, ADVANCE_QUEUE, SWAP_TILES,
            FUSION, CHAIN_MERGE, DUPLICATE_TILE, SKIP_SPAWN,
            INCREMENT_TILE, FREEZE_TILE, PATH_MERGE, MIMIC
        )
    }
    data object UNDO : Perk(100, "UNDO") {
        override internal fun canResolveStuck(grid: List<HexagonCell>, previews: List<PreviewCell>, engine: GameEngine, previousState: GameState?): Boolean {
            return previousState != null && previousState.availableChoices > 1
        }
    }
    
    data object MOVE_TILE : Perk(80, "MOVE_TILE") {
        override internal fun canResolveStuck(grid: List<HexagonCell>, previews: List<PreviewCell>, engine: GameEngine, previousState: GameState?): Boolean {
            val occupied = grid.map { it.x to it.y }.toSet()
            if (occupied.size >= engine.columns * engine.rows) return false
            
            for (cell in grid) {
                val otherCells = grid.filter { it.id != cell.id }
                for (y in 0 until engine.rows) {
                    for (x in 0 until engine.columns) {
                        if (x to y !in occupied) {
                            val tempGrid = otherCells + cell.copy(x = x, y = y)
                            if (engine.isMovePossible(tempGrid) || hasAnyMergePotential(tempGrid, engine)) return true
                        }
                    }
                }
            }
            return false
        }
    
        override internal fun onEmptySpaceTouchDown(x: Int, y: Int, state: GameUiState, engine: GameEngine): MergeTransition? {
            val selectedId = state.selectedCellId
            val ghostAtPos = state.preview.find { it.x == x && it.y == y }
            if (selectedId != null && (ghostAtPos == null || selectedId != ghostAtPos.id)) {
                val source = getGameItem(selectedId, state)
                if (source != null) {
                    val forceGhostAtSource = if (source.isGhost) setOf(selectedId) else emptySet()
                    return MergeTransition(
                        targetX = x,
                        targetY = y,
                        steps = emptyList(),
                        finalValue = source.value,
                        totalCells = 1,
                        uniqueGroups = 0,
                        baseScore = 0,
                        resultId = "preview_move",
                        participatingIds = setOf(selectedId) + listOfNotNull(ghostAtPos?.id),
                        previewSwaps = mapOf(selectedId to (x to y)),
                        forceSolidIds = if (!source.isGhost) setOf("preview_move") + forceGhostAtSource else forceGhostAtSource
                    )
                }
            }
            return null
        }
    
        override internal fun onEmptySpaceClicked(x: Int, y: Int, state: GameUiState, engine: GameEngine): PerkActionResult {
            val selectedId = state.selectedCellId
            if (selectedId != null) {
                return PerkActionResult.MoveTile(selectedId, x, y)
            }
            return PerkActionResult.None
        }
    
        override internal fun onCellClicked(cell: HexagonCell, state: GameUiState, engine: GameEngine): PerkActionResult {
            val selectedId = state.selectedCellId
            return PerkActionResult.SelectCell(if (selectedId == cell.id) null else cell.id)
        }
    
        override internal fun onPreviewClicked(preview: PreviewCell, state: GameUiState, engine: GameEngine): PerkActionResult {
            val selectedId = state.selectedCellId
            if (selectedId != null && selectedId != preview.id) {
                return PerkActionResult.MoveTile(selectedId, preview.x, preview.y)
            } else {
                return PerkActionResult.SelectCell(preview.id)
            }
        }
    }
    
    data object REMOVE_TILE : Perk(80, "REMOVE_TILE") {
        override internal fun canResolveStuck(grid: List<HexagonCell>, previews: List<PreviewCell>, engine: GameEngine, previousState: GameState?): Boolean {
            return grid.isNotEmpty()
        }
    
        override internal fun onEmptySpaceTouchDown(x: Int, y: Int, state: GameUiState, engine: GameEngine): MergeTransition? {
            val ghostAtPos = state.preview.find { it.x == x && it.y == y }
            if (ghostAtPos != null) {
                return MergeTransition(
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
            }
            return null
        }
    
        override internal fun onCellTouchDown(cell: HexagonCell, state: GameUiState, engine: GameEngine): MergeTransition? {
            return MergeTransition(
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
        }
    
        override internal fun onEmptySpaceClicked(x: Int, y: Int, state: GameUiState, engine: GameEngine): PerkActionResult {
            val previewAtPos = state.preview.find { it.x == x && it.y == y }
            if (previewAtPos != null) {
                return PerkActionResult.RemoveTile(previewAtPos.id, previewAtPos.x, previewAtPos.y)
            }
            return PerkActionResult.None
        }
    
        override internal fun onCellClicked(cell: HexagonCell, state: GameUiState, engine: GameEngine): PerkActionResult {
            return PerkActionResult.RemoveTile(cell.id, cell.x, cell.y)
        }
    
        override internal fun onPreviewClicked(preview: PreviewCell, state: GameUiState, engine: GameEngine): PerkActionResult {
            return PerkActionResult.RemoveTile(preview.id, preview.x, preview.y)
        }
    }
    
    data object ADVANCE_QUEUE : Perk(50, "ADVANCE_QUEUE") {
        override internal fun canResolveStuck(grid: List<HexagonCell>, previews: List<PreviewCell>, engine: GameEngine, previousState: GameState?): Boolean {
            val tempGrid = grid.toMutableList()
            previews.forEach { p ->
                if (tempGrid.none { it.x == p.x && it.y == p.y }) {
                    tempGrid.add(engine.createCell(p.x, p.y, p.value))
                }
            }
            return engine.isMovePossible(tempGrid) || hasAnyMergePotential(tempGrid, engine) || (engine.columns * engine.rows - tempGrid.size) >= 3
        }
    }
    
    data object SWAP_TILES : Perk(50, "SWAP_TILES") {
        override internal fun canResolveStuck(grid: List<HexagonCell>, previews: List<PreviewCell>, engine: GameEngine, previousState: GameState?): Boolean {
            val allItems = grid.map { Triple(it.id, it.x, it.y) } + previews.map { Triple(it.id, it.x, it.y) }
            for (i in allItems.indices) {
                for (j in i + 1 until allItems.size) {
                    val (id1, x1, y1) = allItems[i]
                    val (id2, x2, y2) = allItems[j]
                    
                    val tempGrid = grid.map {
                        when (it.id) {
                            id1 -> it.copy(x = x2, y = y2)
                            id2 -> it.copy(x = x1, y = y1)
                            else -> it
                        }
                    }
                    if (engine.isMovePossible(tempGrid) || hasAnyMergePotential(tempGrid, engine)) return true
                }
            }
            return false
        }
    
        override internal fun onEmptySpaceTouchDown(x: Int, y: Int, state: GameUiState, engine: GameEngine): MergeTransition? {
            val selectedId = state.selectedCellId
            val ghostAtPos = state.preview.find { it.x == x && it.y == y }
            if (selectedId != null && ghostAtPos != null && selectedId != ghostAtPos.id) {
                val source = getGameItem(selectedId, state)
                val target = ghostAtPos
                if (source != null) {
                    val swaps = mapOf(
                        source.id to (target.x to target.y),
                        target.id to (source.x to source.y),
                    )
    
                    return MergeTransition(
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
                }
            }
            return null
        }
    
        override internal fun onCellTouchDown(cell: HexagonCell, state: GameUiState, engine: GameEngine): MergeTransition? {
            val selectedId = state.selectedCellId
            if (selectedId != null && selectedId != cell.id) {
                val source = getGameItem(selectedId, state)
                val target = cell
                if (source != null) {
                    val swaps = mapOf(
                        source.id to (target.x to target.y),
                        target.id to (source.x to source.y),
                    )
    
                    return MergeTransition(
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
                }
            }
            return null
        }
    
        override internal fun onEmptySpaceClicked(x: Int, y: Int, state: GameUiState, engine: GameEngine): PerkActionResult {
            val previewAtPos = state.preview.find { it.x == x && it.y == y }
            if (previewAtPos != null) {
                val selectedId = state.selectedCellId
                if (selectedId == null) {
                    return PerkActionResult.SelectCell(previewAtPos.id)
                } else if (selectedId != previewAtPos.id) {
                    return PerkActionResult.SwapTiles(selectedId, previewAtPos.id)
                }
            }
            return PerkActionResult.None
        }
    
        override internal fun onCellClicked(cell: HexagonCell, state: GameUiState, engine: GameEngine): PerkActionResult {
            val selectedId = state.selectedCellId
            if (selectedId == null) {
                return PerkActionResult.SelectCell(cell.id)
            } else if (selectedId == cell.id) {
                return PerkActionResult.SelectCell(null)
            } else {
                return PerkActionResult.SwapTiles(selectedId, cell.id)
            }
        }
    
        override internal fun onPreviewClicked(preview: PreviewCell, state: GameUiState, engine: GameEngine): PerkActionResult {
            val selectedId = state.selectedCellId
            if (selectedId == null) {
                return PerkActionResult.SelectCell(preview.id)
            } else if (selectedId != preview.id) {
                return PerkActionResult.SwapTiles(selectedId, preview.id)
            }
            return PerkActionResult.None
        }
    }
    
    data object FUSION : Perk(20, "FUSION") {
        override internal fun canResolveStuck(grid: List<HexagonCell>, previews: List<PreviewCell>, engine: GameEngine, previousState: GameState?): Boolean {
            val fromCells = grid.any { cell ->
                !cell.isFrozen &&
                        engine.getNeighbors(cell.x, cell.y).count { n ->
                            grid.any { it.x == n.first && it.y == n.second && !it.isFrozen }
                        } >= 2
            }
            if (fromCells) return true
            
            val occupied = grid.map { it.x to it.y }.toSet()
            for (y in 0 until engine.rows) {
                for (x in 0 until engine.columns) {
                    if (x to y !in occupied) {
                        val neighbors = engine.getNeighbors(x, y)
                        if (grid.count { it.x to it.y in neighbors.toSet() && !it.isFrozen } >= 2) return true
                    }
                }
            }
            return false
        }
    
        override internal fun onEmptySpaceTouchDown(x: Int, y: Int, state: GameUiState, engine: GameEngine): MergeTransition? {
            val (m, _) = engine.calculateFusion(x, y, state.grid, 0)
            return m?.copy(resultId = "preview_fusion")
        }
    
        override internal fun onEmptySpaceClicked(x: Int, y: Int, state: GameUiState, engine: GameEngine): PerkActionResult {
            if (state.grid.any { it.x == x && it.y == y }) return PerkActionResult.None
            val (merge, _) = engine.calculateFusion(x, y, state.grid, state.cellIdCounter)
            if (merge != null) {
                return PerkActionResult.TriggerMerge(merge)
            }
            return PerkActionResult.None
        }
    }
    
    data object CHAIN_MERGE : Perk(20, "CHAIN_MERGE") {
        override internal fun onEmptySpaceTouchDown(x: Int, y: Int, state: GameUiState, engine: GameEngine): MergeTransition? {
            val m = engine.simulateChainMerge(x, y, state.grid, state.combo)
            return m?.copy(resultId = "preview_merge")
        }
    
        override internal fun onEmptySpaceClicked(x: Int, y: Int, state: GameUiState, engine: GameEngine): PerkActionResult {
            if (state.grid.any { it.x == x && it.y == y }) return PerkActionResult.None
            val (merge, _) = engine.calculateMerge(x, y, state.grid, state.cellIdCounter)
            if (merge != null) {
                return PerkActionResult.TriggerMerge(merge)
            }
            return PerkActionResult.None
        }
    }
    
    data object DUPLICATE_TILE : Perk(50, "DUPLICATE_TILE") {
        override internal fun canResolveStuck(grid: List<HexagonCell>, previews: List<PreviewCell>, engine: GameEngine, previousState: GameState?): Boolean {
            val occupied = grid.map { it.x to it.y }.toSet()
            if (occupied.size >= engine.columns * engine.rows) return false
            for (cell in grid) {
                for (y in 0 until engine.rows) {
                    for (x in 0 until engine.columns) {
                        if (x to y !in occupied) {
                            val tempGrid = grid + cell.copy(id = "temp", x = x, y = y)
                            if (engine.isMovePossible(tempGrid) || hasAnyMergePotential(tempGrid, engine)) return true
                        }
                    }
                }
            }
            return false
        }
    
        override internal fun onEmptySpaceTouchDown(x: Int, y: Int, state: GameUiState, engine: GameEngine): MergeTransition? {
            val selectedId = state.selectedCellId
            val ghostAtPos = state.preview.find { it.x == x && it.y == y }
            if (selectedId != null && (ghostAtPos == null || selectedId != ghostAtPos.id)) {
                val source = getGameItem(selectedId, state)
                if (source != null) {
                    val resultId = if (source.isMimic) "preview_duplicate_mimic" else "preview_duplicate"
                    return MergeTransition(
                        targetX = x,
                        targetY = y,
                        steps = emptyList(),
                        finalValue = source.value,
                        totalCells = 1,
                        uniqueGroups = 0,
                        baseScore = 0,
                        resultId = resultId,
                        participatingIds = setOf(selectedId) + listOfNotNull(ghostAtPos?.id),
                        forceSolidIds = if (!source.isGhost) setOf(resultId) else emptySet(),
                    )
                }
            }
            return null
        }
    
        override internal fun onEmptySpaceClicked(x: Int, y: Int, state: GameUiState, engine: GameEngine): PerkActionResult {
            val selectedId = state.selectedCellId
            if (selectedId != null) {
                return PerkActionResult.DuplicateTile(selectedId, x, y)
            }
            return PerkActionResult.None
        }
    
        override internal fun onCellClicked(cell: HexagonCell, state: GameUiState, engine: GameEngine): PerkActionResult {
            val selectedId = state.selectedCellId
            return PerkActionResult.SelectCell(if (selectedId == cell.id) null else cell.id)
        }
    
        override internal fun onPreviewClicked(preview: PreviewCell, state: GameUiState, engine: GameEngine): PerkActionResult {
            return PerkActionResult.SelectCell(if (state.selectedCellId == preview.id) null else preview.id)
        }
    }
    
    data object SKIP_SPAWN : Perk(50, "SKIP_SPAWN") {
        override internal fun onEmptySpaceTouchDown(x: Int, y: Int, state: GameUiState, engine: GameEngine): MergeTransition? {
            val (m, _) = engine.calculateMerge(x, y, state.grid, 0)
            return m?.copy(resultId = "preview_merge")
        }
    
        override internal fun onEmptySpaceClicked(x: Int, y: Int, state: GameUiState, engine: GameEngine): PerkActionResult {
            if (state.grid.any { it.x == x && it.y == y }) return PerkActionResult.None
            val (merge, _) = engine.calculateMerge(x, y, state.grid, state.cellIdCounter)
            if (merge != null) {
                return PerkActionResult.TriggerMerge(merge)
            }
            return PerkActionResult.None
        }
    }
    
    data object INCREMENT_TILE : Perk(80, "INCREMENT_TILE") {
        override internal fun canResolveStuck(grid: List<HexagonCell>, previews: List<PreviewCell>, engine: GameEngine, previousState: GameState?): Boolean {
            return grid.any { cell ->
                val tempGridInc = grid.map { if (it.id == cell.id) it.copy(value = it.value + 1) else it }
                val tempGridDec = if (cell.value > 1) {
                    grid.map { if (it.id == cell.id) it.copy(value = it.value - 1) else it }
                } else null
                engine.isMovePossible(tempGridInc) || hasAnyMergePotential(tempGridInc, engine) || 
                        (tempGridDec != null && (engine.isMovePossible(tempGridDec) || hasAnyMergePotential(tempGridDec, engine)))
            }
        }
    
        override internal fun onEmptySpaceTouchDown(x: Int, y: Int, state: GameUiState, engine: GameEngine): MergeTransition? {
            val ghostAtPos = state.preview.find { it.x == x && it.y == y }
            if (ghostAtPos != null && !ghostAtPos.isMimic) {
                val nextValue = ghostAtPos.value + 1
                return MergeTransition(
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
            }
            return null
        }
    
        override internal fun onCellTouchDown(cell: HexagonCell, state: GameUiState, engine: GameEngine): MergeTransition? {
            if (!cell.isMimic) {
                val nextValue = cell.value + 1
                return MergeTransition(
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
            }
            return null
        }
    
        override internal fun onEmptySpaceClicked(x: Int, y: Int, state: GameUiState, engine: GameEngine): PerkActionResult {
            val previewAtPos = state.preview.find { it.x == x && it.y == y }
            if (previewAtPos != null && !previewAtPos.isMimic) {
                return PerkActionResult.IncrementTile(previewAtPos.id, previewAtPos.x, previewAtPos.y)
            }
            return PerkActionResult.None
        }
    
        override internal fun onCellClicked(cell: HexagonCell, state: GameUiState, engine: GameEngine): PerkActionResult {
            if (!cell.isMimic) {
                return PerkActionResult.IncrementTile(cell.id, cell.x, cell.y)
            }
            return PerkActionResult.None
        }
    
        override internal fun onPreviewClicked(preview: PreviewCell, state: GameUiState, engine: GameEngine): PerkActionResult {
            if (!preview.isMimic) {
                return PerkActionResult.IncrementTile(preview.id, preview.x, preview.y)
            }
            return PerkActionResult.None
        }
    }
    
    data object FREEZE_TILE : Perk(60, "FREEZE_TILE") {
        override internal fun onCellTouchDown(cell: HexagonCell, state: GameUiState, engine: GameEngine): MergeTransition? {
            return MergeTransition(
                targetX = cell.x,
                targetY = cell.y,
                steps = emptyList(),
                finalValue = 0,
                totalCells = 1,
                uniqueGroups = 0,
                baseScore = 0,
                resultId = "preview_freeze",
                participatingIds = setOf(cell.id),
                previewFrozenIds = setOf(cell.id)
            )
        }
    
        override internal fun onCellClicked(cell: HexagonCell, state: GameUiState, engine: GameEngine): PerkActionResult {
            return PerkActionResult.FreezeTile(cell.id, cell.x, cell.y)
        }
    }
    
    data object PATH_MERGE : Perk(10, "PATH_MERGE") {
        override internal fun canResolveStuck(grid: List<HexagonCell>, previews: List<PreviewCell>, engine: GameEngine, previousState: GameState?): Boolean {
            return hasAnyMergePotential(grid, engine)
        }
    
        override internal fun onEmptySpaceTouchDown(x: Int, y: Int, state: GameUiState, engine: GameEngine): MergeTransition? {
            val ghostAtPos = state.preview.find { it.x == x && it.y == y }
            if (ghostAtPos?.isMimic == true) return null
            val (m, _) = engine.calculatePathMerge(x, y, state.grid, 0)
            return m?.let {
                val nextId = "preview_path_merge"
                it.copy(
                    resultId = nextId,
                    forceSolidIds = setOf(nextId),
                    previewValues = ghostAtPos?.let { g -> mapOf(g.id to it.finalValue) } ?: emptyMap(),
                    isPerkAssisted = true
                )
            }
        }
    
        override internal fun onCellTouchDown(cell: HexagonCell, state: GameUiState, engine: GameEngine): MergeTransition? {
            if (cell.isMimic) return null
            val (m, _) = engine.calculatePathMerge(cell.x, cell.y, state.grid, 0)
            return m?.let {
                val nextId = "preview_path_merge"
                it.copy(
                    resultId = nextId,
                    forceSolidIds = setOf(nextId),
                    previewValues = mapOf(cell.id to it.finalValue),
                    isPerkAssisted = true
                )
            }
        }
    
        override internal fun onCellClicked(cell: HexagonCell, state: GameUiState, engine: GameEngine): PerkActionResult {
            val (merge, _) = engine.calculatePathMerge(cell.x, cell.y, state.grid, state.cellIdCounter)
            if (merge != null) {
                return PerkActionResult.TriggerMerge(merge)
            }
            return PerkActionResult.None
        }
    }
    
    data object MIMIC : Perk(25, "MIMIC") {
        override internal fun canResolveStuck(grid: List<HexagonCell>, previews: List<PreviewCell>, engine: GameEngine, previousState: GameState?): Boolean {
            return grid.isNotEmpty() || previews.isNotEmpty()
        }
    
        override internal fun onEmptySpaceTouchDown(x: Int, y: Int, state: GameUiState, engine: GameEngine): MergeTransition? {
            val ghostAtPos = state.preview.find { it.x == x && it.y == y }
            if (ghostAtPos != null && !ghostAtPos.isMimic) {
                return MergeTransition(
                    targetX = x,
                    targetY = y,
                    steps = emptyList(),
                    finalValue = ghostAtPos.value,
                    totalCells = 1,
                    uniqueGroups = 0,
                    baseScore = 0,
                    resultId = "preview_mimic",
                    participatingIds = setOf(ghostAtPos.id),
                )
            }
            return null
        }
    
        override internal fun onCellTouchDown(cell: HexagonCell, state: GameUiState, engine: GameEngine): MergeTransition? {
            if (!cell.isMimic) {
                return MergeTransition(
                    targetX = cell.x,
                    targetY = cell.y,
                    steps = emptyList(),
                    finalValue = cell.value,
                    totalCells = 1,
                    uniqueGroups = 0,
                    baseScore = 0,
                    resultId = "preview_mimic",
                    participatingIds = setOf(cell.id),
                )
            }
            return null
        }
    
        override internal fun onEmptySpaceClicked(x: Int, y: Int, state: GameUiState, engine: GameEngine): PerkActionResult {
            val previewAtPos = state.preview.find { it.x == x && it.y == y }
            if (previewAtPos != null && !previewAtPos.isMimic) {
                return PerkActionResult.MimicTile(previewAtPos.id, previewAtPos.x, previewAtPos.y)
            }
            return PerkActionResult.None
        }
    
        override internal fun onCellClicked(cell: HexagonCell, state: GameUiState, engine: GameEngine): PerkActionResult {
            if (!cell.isMimic) {
                return PerkActionResult.MimicTile(cell.id, cell.x, cell.y)
            }
            return PerkActionResult.None
        }
    
        override internal fun onPreviewClicked(preview: PreviewCell, state: GameUiState, engine: GameEngine): PerkActionResult {
            if (!preview.isMimic) {
                return PerkActionResult.MimicTile(preview.id, preview.x, preview.y)
            }
            return PerkActionResult.None
        }
    }
}

private fun hasAnyMergePotential(grid: List<HexagonCell>, engine: GameEngine): Boolean {
    return grid.any { cell ->
        !cell.isFrozen &&
                engine.getNeighbors(cell.x, cell.y).any { n ->
                    grid.any {
                        !it.isFrozen &&
                                it.x == n.first && it.y == n.second &&
                                (it.value == cell.value || it.isMimic || cell.isMimic)
                    }
                }
    }
}

private fun getGameItem(id: String, state: GameUiState): GameItem? {
    state.grid.find { it.id == id }?.let { return GameItem(it.id, it.x, it.y, it.value, false, it.isMimic) }
    state.preview.find { it.id == id }?.let { return GameItem(it.id, it.x, it.y, it.value, true, it.isMimic) }
    return null
}

object PerkSerializer : KSerializer<Perk> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Perk", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Perk) {
        encoder.encodeString(value.id)
    }

    override fun deserialize(decoder: Decoder): Perk {
        val id = decoder.decodeString()
        return Perk.entries.find { it.id == id } ?: Perk.UNDO
    }
}
