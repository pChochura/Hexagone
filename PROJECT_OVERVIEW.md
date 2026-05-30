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
│   │       ├── PerkBar.kt         # Strategic tool selection shelf
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
    *   `k`: Number of unique value groups involved (allows chain reactions).
*   **Base Score**: $\sum Values_{merging} + n^2 - n$. This formula ensures higher-value pieces are significantly more rewarding.

### Scoring & Combos
*   **Sequential Merges**: Multi-group merges happen in steps (largest first) to build visual momentum and sold impact.
*   **Combo System**: A multiplier that builds with every group merge. Clamped at **x12** for internal scoring.
*   **Tactical Multiplier**: Tiles marked as **Tactical** (generated via certain perks or chain reactions) grant a **1.5x Base Score bonus**.
*   **Redemption Bonus**: If a move's score exceeds the previous turn's score, a **50% Redemption Bonus** is applied to the difference.

---

## 3. Strategic Features

### Predictive Hint System & Hover Previews
The game calculates **Weighted Hints** and provides **Interactive Hover Previews**:
*   **Look-Ahead**: Simulates the current move + the next 3 pieces in the queue.
*   **Interactive Previews**: Visually simulates Swaps, Moves, and direct Value changes (Increment/Fusion) before the player commits.
*   **Z-Index Prioritization**: Moving or selected tiles are dynamically layered on top of static elements during interactions.

### Perk Economy
*   **Rarity Weights**: Common (Undo, Move, Remove, Increment), Rare (Advance, Swap, Duplicate, Skip), Legendary (Fusion, Chain Merge, Path Merge).
*   **On-Board Spawning (Pity System)**: Guaranteed spawning between 8 and 15 turns if the board has space. Spawns avoid "ghost" positions (where queue pieces will land).
*   **Cursed Reroll**: Once per level-up, the player can reroll the selection at the cost of removing Legendary perks from that specific pool.

---

## 4. UI & Visual Identity

*   **Design System**: Fully centralized in `HexagoneTheme`, mapping Material 3 roles to custom "Glowing Hardware" tokens (`Spacing`, `CornerRadius`, `IconSize`).
*   **Stable Popups**: Score and Perk notifications use **sequential IDs** and pre-calculated group offsets to prevent layout jitter and ensure animation stability.
*   **Liquid HUD**: A progress bar with a dynamic wavy edge that "splashes" based on point intensity.
*   **Emergency Mode**: When stuck, the system identifies specific perks that can resolve the state and enables only those "solution" perks in the shelf.
*   **Debug Layer**: A dedicated `DebugDelegate` allows real-time manipulation of tile values, perk spawning, and state forcing without polluting the main game logic.

---

## 5. Development Guidelines
1.  **Modular UI**: Keep composables small and focused. Use `Modifier` as the first parameter and prefer slot-based APIs for layouts.
2.  **State Atomicity**: Use `_uiState.update { ... }` for all gameplay changes to ensure consistency across observers.
3.  **Stability Guards**: Use stable keys (`key(id)`) for all dynamic list items (Popups, Particles, Grid Cells) to prevent unnecessary recompositions.
4.  **Interaction Locking**: Check `isBusy` and `pendingMerge` flags to prevent input conflicts during animations.
5.  **State Persistence**: Game state is serialized via `kotlinx.serialization` and persisted to `DataStore` after every significant move, supporting cross-session play.
