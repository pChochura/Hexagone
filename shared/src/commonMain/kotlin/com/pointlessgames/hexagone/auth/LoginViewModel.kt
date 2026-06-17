package com.pointlessgames.hexagone.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.hexagone.data.LeaderboardRepository
import com.pointlessgames.hexagone.game.logic.GameEngine
import com.pointlessgames.hexagone.game.model.GameEffect
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.PreviewCell
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

internal class LoginViewModel(
    private val leaderboardRepository: LeaderboardRepository,
) : ViewModel() {

    private val engine = GameEngine()
    private val random = Random(0)
    private var cellIdCounter = 0

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<GameEffect>()
    val effects: SharedFlow<GameEffect> = _effects.asSharedFlow()

    data class UiState(
        val name: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
        val isSuccess: Boolean = false,
        val showNicknamePopup: Boolean = false,
        // Background Simulation State
        val backgroundGrid: List<HexagonCell> = emptyList(),
        val backgroundPreviews: List<PreviewCell> = emptyList(),
        val pendingMerge: MergeTransition? = null,
        val activeMergeStepIndex: Int = 0,
    )

    init {
        startBackgroundSimulation()
    }

    private fun startBackgroundSimulation() {
        viewModelScope.launch {
            val (initialGrid, initialId) = engine.generateInitialGrid(random)
            cellIdCounter = initialId
            var currentGrid = initialGrid
            var currentPreviews = engine.pickRandomPreviews(currentGrid, emptyList(), emptyList(),
                random, 0).first
            
            _uiState.value = _uiState.value.copy(
                backgroundGrid = currentGrid,
                backgroundPreviews = currentPreviews
            )

            while (true) {
                delay(2000)
                
                // Try to find a merge
                val emptySpaces = mutableListOf<Pair<Int, Int>>()
                for (x in 0 until engine.columns) {
                    for (y in 0 until engine.rows) {
                        if (currentGrid.none { it.x == x && it.y == y }) {
                            emptySpaces.add(x to y)
                        }
                    }
                }

                if (emptySpaces.isEmpty()) {
                    // Reset grid if full
                    val (resetGrid, resetId) = engine.generateInitialGrid(random)
                    cellIdCounter = resetId
                    currentGrid = resetGrid
                    currentPreviews = engine.pickRandomPreviews(currentGrid, emptyList(), emptyList(),
                        random, 0).first
                } else {
                    val (tx, ty) = emptySpaces.random(random)
                    val spawnValue = random.nextInt(1, 4)
                    val (transition, nextId) = engine.calculateMerge(tx, ty, currentGrid + engine.createCell(tx, ty, spawnValue), cellIdCounter)
                    cellIdCounter = nextId

                    if (transition != null) {
                        _uiState.value = _uiState.value.copy(pendingMerge = transition, activeMergeStepIndex = 0)
                        
                        // Simulate animation steps
                        transition.steps.forEachIndexed { index, _ ->
                            delay(400)
                            _uiState.value = _uiState.value.copy(activeMergeStepIndex = index + 1)
                        }
                        
                        delay(300)
                        currentGrid = currentGrid.filter { cell -> transition.steps.none { it.mergingCells.any { mc -> mc.id == cell.id } } }
                        currentGrid = currentGrid + engine.createCell(tx, ty, transition.finalValue, id = transition.resultId)
                    } else {
                        currentGrid = currentGrid + engine.createCell(tx, ty, spawnValue, id = "bg_${cellIdCounter++}")
                    }
                }

                _uiState.value = _uiState.value.copy(
                    backgroundGrid = currentGrid,
                    pendingMerge = null,
                    activeMergeStepIndex = 0
                )
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun onSignInAnonymously() {
        _uiState.value = _uiState.value.copy(showNicknamePopup = true)
    }

    fun onSignInWithGoogle() {
        // Placeholder: Pre-populate name and show popup
        _uiState.value = _uiState.value.copy(name = "Google Player", showNicknamePopup = true)
    }

    fun onSignInWithApple() {
        // Placeholder: Pre-populate name and show popup
        _uiState.value = _uiState.value.copy(name = "Apple Player", showNicknamePopup = true)
    }

    fun onDismissNicknamePopup() {
        _uiState.value = _uiState.value.copy(showNicknamePopup = false)
    }

    fun consumeSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }

    fun onCreateProfile() {
        val name = _uiState.value.name.trim()
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Username cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                leaderboardRepository.createProfile(name)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, showNicknamePopup = false)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }

    // Required for GameGridOverlay callback
    fun onMergeAnimationFinished() {
        // Handled via state delays in startBackgroundSimulation for background
    }
}
