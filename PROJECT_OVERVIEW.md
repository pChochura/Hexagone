# Project: Hexagone Overview

**Hexagone** is a high-polish, strategic hexagonal puzzle game built with **Compose Multiplatform**. It features a reactive, state-driven architecture, deep strategic mechanics, and a vibrant, "glowing hardware" aesthetic.

---

## 1. Project Structure

The project is structured as a Kotlin Multiplatform (KMP) application with a shared logic module and platform-specific entry points.

### Key Directory Map
```text
shared/src/commonMain/kotlin/com/pointlessgames/hexagone/
├── data/
│   └── SettingsRepository.kt     # High Score & DataStore persistence
├── di/
│   └── AppModule.kt              # Koin Dependency Injection
├── game/
│   ├── logic/
│   │   └── GameEngine.kt         # Core math: neighbors, merges, hints, scoring
│   ├── model/
│   │   └── HexagonCell.kt        # Data Models: Perk, MergeStep, Hint, Particle
│   ├── ui/
│   │   ├── GameScreen.kt         # Root UI Layout and layering
│   │   └── components/
│   │       ├── GameGridOverlay.kt # The Grid: animations, gestures, rendering
│   │       ├── ScoreSection.kt    # The HUD: wavy progress, combo pops
│   │       ├── PerkBar.kt         # The Shelf: scrollable tool selection
│   │       └── GameOverlays.kt    # Overlays: Level Up, Stuck, Result Card
│   └── GameViewModel.kt          # Orchestrator: Turn logic, animation loops
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
*   **Sequential Merges**: Multi-group merges happen in steps (largest first) to build visual momentum.
*   **Combo System**: A multiplier that builds with every group merge in a move. It is maintained until a "weak" move (only 1 group) or a reset.
*   **Combo UI**: Features a high-impact "slam" animation that morphs from **Yellow to Red** as it approaches a x10 multiplier.

---

## 3. Strategic Features

### Predictive Hint System
The game calculates **Weighted Hints** for every empty tile:
*   **Look-Ahead**: Simulates the current move + the next 3 pieces in the queue.
*   **Weighting**: Assigns a favorability score (0.0 - 1.0) based on immediate points, combo building, and future board setup.
*   **Visuals**: Subtle dots that grow in size based on the strategic value of the move.

### Perk Economy
Perks are strategic tools collected via leveling up or on-board scavenge.
*   **Rarity Weights**: 
    *   **Common** (100-80 weight): UNDO, MOVE TILE, REMOVE TILE.
    *   **Rare** (50 weight): ADVANCE QUEUE, SWAP TILES.
    *   **Legendary** (20 weight): FUSION, CHAIN MERGE (features a distinct pulsating aura).
*   **Scavenge Mechanic**: Random 10% chance to spawn a perk on an empty hex. Must be captured via a merge at that position within 1-3 turns (depending on rarity).

---

## 4. UI & Visual Identity

*   **Atmospheric Dimming**: The board dims to focus on Level Up choices or "No Moves Left" scenarios.
*   **Neon Bloom**: Contoured, multi-layered "neon" outlines around active strategic areas (Perk Shelf, Result Card).
*   **Fluid HUD**: A liquid-like level progress bar that "splashes" with intensity proportional to the points earned.
*   **Interactive Result Card**: A frosted glass overlay featuring score count-ups and a "View Board" peek mode.

---

## 5. Development Guidelines
1.  **State Atomicitity**: Use `_uiState.update { ... }` for all gameplay changes.
2.  **Interaction Locking**: Always check `isBusy` or `pendingMerge` before processing gestures.
3.  **No Delays for UI**: Do not use `delay()` for animation sequencing; use the `finishedListener` callbacks in `GameGridOverlay`.
