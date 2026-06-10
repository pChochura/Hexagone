package com.pointlessgames.hexagone.game

import com.pointlessgames.hexagone.game.logic.GameEngine
import com.pointlessgames.hexagone.game.model.GameUiState
import com.pointlessgames.hexagone.game.model.Perk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class DebugDelegate(
    private val uiState: MutableStateFlow<GameUiState>,
    private val engine: GameEngine,
    private val onStateChanged: () -> Unit,
    private val onSpawnRequested: () -> Unit,
) {
    fun setDebugSelectedValue(value: Int?) {
        uiState.update { it.copy(debugSelectedValue = value) }
    }

    fun toggleDebugMode() {
        uiState.update { state ->
            val newDebugMode = !state.isDebugMode
            state.copy(
                isDebugMode = newDebugMode,
                debugUsed = state.debugUsed || newDebugMode,
                isStuck = if (newDebugMode) false else state.isStuck,
                isGameOver = if (newDebugMode) false else state.isGameOver,
                selectedCellId = null,
                activePerk = null,
                isBusy = if (newDebugMode) false else state.isBusy
            )
        }
        
        val state = uiState.value
        if (!state.isDebugMode) {
            if (state.preview.isEmpty()) {
                onSpawnRequested()
            } else {
                onStateChanged()
            }
        } else {
            onStateChanged()
        }
    }

    fun toggleDebugAddAsGhost() {
        uiState.update { it.copy(debugAddAsGhost = !it.debugAddAsGhost) }
    }

    fun onDebugCellClicked(x: Int, y: Int) {
        val state = uiState.value
        val value = state.debugSelectedValue
        val asGhost = state.debugAddAsGhost

        uiState.update { currentState ->
            var updatedGrid = currentState.grid
            var updatedPreview = currentState.preview
            var nextCellIdCounter = currentState.cellIdCounter
            var nextPreviewIdCounter = currentState.previewIdCounter

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
                        updatedPreview = updatedPreview + engine.createPreviewCell(x, y, value, id = "preview_${nextPreviewIdCounter++}")
                    }
                } else {
                    updatedPreview = updatedPreview.filterNot { it.x == x && it.y == y }
                    if (existingGrid != null) {
                        updatedGrid = updatedGrid.map { if (it.id == existingGrid.id) it.copy(value = value) else it }
                    } else {
                        updatedGrid = updatedGrid + engine.createCell(x, y, value, id = "cell_${nextCellIdCounter++}")
                    }
                }
            }
            currentState.copy(
                grid = updatedGrid,
                preview = updatedPreview,
                cellIdCounter = nextCellIdCounter,
                previewIdCounter = nextPreviewIdCounter
            )
        }
        onStateChanged()
    }

    fun addPerkManually(perk: Perk) {
        uiState.update { it.copy(collectedPerks = it.collectedPerks + perk, debugUsed = true) }
        onStateChanged()
    }
}
