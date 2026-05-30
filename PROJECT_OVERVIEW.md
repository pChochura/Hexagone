# Project: Hexagone Overview

**Hexagone** is a high-polish, strategic hexagonal puzzle game built with **Compose Multiplatform**. It features a reactive, state-driven architecture, deep strategic mechanics, and a vibrant, "glowing hardware" aesthetic.

---

## 1. Project Structure

The project follows a modular Kotlin Multiplatform (KMP) architecture, prioritizing separation of concerns and Compose best practices (State Hoisting, Slot-based APIs).

### Key Directory Map
```text
shared/src/commonMain/kotlin/com/pointlessgames/hexagone/
├── data/
│   └── SettingsRepository.kt     # Game State & DataStore persistence
├── di/
│   └── AppModule.kt              # Koin Dependency Injection
├── game/
│   ├── logic/
│   │   └── GameEngine.kt         # Core math: neighbors, merges, hints, scoring
│   ├── model/
│   │   ├── GameModels.kt         # Domain Models: Perk, GameUiState, MergeTransition
│   │   └── UIModels.kt           # UI-specific state: Particles, Animation states
│   ├── ui/
│   │   ├── GameScreen.kt         # Root UI Layout
│   │   └── components/
│   │       ├── PopupsLayer.kt     # Stable HUD notifications (sequential IDs)
│   │       ├── GridDrawing.kt     # Optimized DrawScope rendering logic
│   │       ├── GameGridOverlay.kt # Grid orchestrator & gesture handling
│   │       ├── ScoreSection.kt    # HUD: liquid progress & combo indicators
│   │       ├── PerkBar.kt         # Strategic tool selection shelf with item animations
│   │       └── GameOverlays.kt    # Dialog orchestration (Level Up, Game Over)
│   ├── GameViewModel.kt          # Orchestrator: Turn logic & animation loops
│   └── DebugDelegate.kt          # Isolated developer tools & state manipulation
├── ui/
│   └── theme/
│       └── Theme.kt              # Centralized "Glowing Hardware" Design System
└── utils/
    └── Animation.kt              # Idiomatic Compose animation helpers
```

---

## 2. Core Game Logic

### Hexagonal System
*   **Coordinate System**: Uses **axial coordinates** in a **staggered flat-top** layout (5 columns, 4 rows).
*   **Neighbor Logic**: Neighbors are calculated based on column parity (even/odd stagger).

### The Merge Formula
A merge occurs when 2+ tiles of the same value touch.
*   **Final Value**: $V_{max} + n - k$
    *   `Vmax`: Highest value in the group.
    *   `n`: Total number of tiles.
    *   `k`: Number of unique value groups involved.
*   **Base Score**: $\sum Values_{merging} + n^2 - n$.

### Scoring & Bonuses
*   **Scalable Bar Raised Bonus**: Granted whenever the smallest value tile is cleared from the game.
    *   **Formula**: `clearedValue * 50`.
*   **Scalable Sacrifice Bonus**: Granted when removing the only remaining highest-value tile.
    *   **Formula**: `(SacrificedValue * 75) + (Difference * 150)`, where *Difference* is the gap to the second highest tile.
*   **JANITOR+**: A rare combined achievement for clearing both the board's highest and lowest tiles simultaneously.
*   **Redemption Bonus**: If a move's score exceeds the previous turn's, a **50% bonus** is applied to the difference.
*   **Combo System**: Multipliers build with every merge, capped at **x12** for internal scoring.

---

## 3. Strategic Features

### Predictive Hint System & Hover Previews
The game calculates **Weighted Hints** and provides **Interactive Hover Previews**:
*   **Look-Ahead**: Simulates the current move + the next 3 pieces in the queue.
*   **Interactive Previews**: Visually simulates Swaps, Moves, and direct Value changes (Increment/Fusion) before commitment.
*   **Z-Index Prioritization**: Moving or selected tiles are dynamically layered on top.

### Perk Economy
*   **Rarity Weights**: Common (Undo, Move, Remove, Increment), Rare (Advance, Swap, Duplicate, Skip), Legendary (Fusion, Chain Merge, Path Merge).
*   **Behavioral Rules**:
    *   **Move & Duplicate**: These actions are "positional only." They do not trigger automatic merges, allowing for strategic setup and combo preparation.
    *   **Lifespan Stability**: Strategic perks like `Advance Queue` and `Skip Spawn` do not progress the "organic" board state. On-board perks only decrement their lifespan during standard turn progression (i.e., when a regular piece is placed from the queue).
    *   **Skip Spawn Versatility**: The `Skip Spawn` perk allows placing a tile on any valid empty cell, including those currently occupied by "ghost" previews from the queue.
*   **Stacked Storage**: Perks of the same type stack in the shelf, displaying a total count.
*   **On-Board Spawning (Pity System)**: Guaranteed spawning between 8 and 15 turns if the board has space, avoiding "ghost" positions.

---

## 4. UI & Visual Identity

*   **Design System**: Centralized in `HexagoneTheme`, mapping Material 3 roles to "Glowing Hardware" tokens.
*   **Stable Popups**: Notifications use **sequential IDs** and pre-calculated offsets for animation stability.
*   **Animated Perk Shelf**:
    *   **Popup Animation**: Items trigger a scale-up "pop" (150%) when added or counts increase.
    *   **Smart Scrolling**: Shelf automatically scrolls to updated perks during gameplay.
    *   **Suppressed Boot**: Initial state loads silently to avoid visual noise on game launch.
*   **Liquid HUD**: A progress bar with a dynamic wavy edge that "splashes" based on point intensity.
*   **Debug Layer**: `DebugDelegate` for real-time manipulation of tile values and perk spawning.

---

## 5. Development Guidelines
1.  **Modular UI**: Keep composables small. Use `Modifier` as the first parameter and prefer slot-based APIs.
2.  **State Atomicity**: Use `_uiState.update { ... }` for all gameplay changes.
3.  **Stability Guards**: Use stable keys (`key(id)`) for all dynamic items to prevent unnecessary recompositions.
4.  **Interaction Locking**: Check `isBusy` and `pendingMerge` flags during animations.
5.  **Persistence**: State is serialized via `kotlinx.serialization` and persisted to `DataStore` after every move.
