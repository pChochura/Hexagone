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
*   **Sequential Merges**: Multi-group merges happen in steps (largest first) to build visual momentum and sold impact.
*   **Combo System**: A multiplier that builds with every group merge. Clamped at **x12** for internal scoring to prevent economy inflation.
*   **Tiered Milestones (Overdrive)**: Crossing specific thresholds grants permanent rewards:
    *   **SURGE (x11)**: +1 Random Rare Perk.
    *   **OVERDRIVE (x21)**: +1 Random Legendary Perk.
    *   **ZENITH (x31)**: +1 Legendary Perk & Board-wide +1 value upgrade.
*   **Combo UI**: A cinematic "slam" animation located to the **left of the score box**. Morphs from **Yellow to Red** to **Tier Colors** (Cyan/Pink/Gold) with a character-accurate glyph shadow and 3-5s slow-fade settle.

---

## 3. Strategic Features

### Predictive Hint System
The game calculates **Weighted Hints** for every empty tile:
*   **Look-Ahead**: Simulates the current move + the next 3 pieces in the queue.
*   **Evaluation**: Assigns favorability (0.0 - 1.0) based on score, combo potential, and future board setup.
*   **Perk Awareness**: Recalculates hints based on active perks (e.g., highlighting Fusion spots).
*   **Visuals**: Subtle, dynamic dots that scale in size based on strategic value.

### Perk Economy
Perks are strategic tools collected via leveling up or on-board scavenge.
*   **Rarity Weights**: 
    *   **Common** (Weight 100-80): UNDO, MOVE TILE, REMOVE TILE.
    *   **Rare** (Weight 50): ADVANCE QUEUE, SWAP TILES.
    *   **Legendary** (Weight 20): FUSION, CHAIN MERGE (features a distinct pulsating aura).
*   **On-Board Spawning (Pity System)**:
    *   **Fairness**: A perk is guaranteed not to spawn for 8 turns, and guaranteed to spawn by 15 turns if the board has space.
    *   **Clutter Control**: Max 1 perk on board at any time.
    *   **Ghost Priority**: Spawns prioritize empty positions not occupied by upcoming queue pieces (ghosts).
*   **Collection Logic**:
    *   **Player Collection**: Perks are captured by triggering a **merge**, **fusion**, or moving a tile onto the perk position.
    *   **Queue Solidification**: If a queue piece lands on a perk position, the perk is deleted (not collected).
*   **Weighted Lifespan**: Common perks last 3 turns, Rare 2 turns, and Legendary must be captured in exactly 1 turn.

---

## 4. UI & Visual Identity

*   **Atmospheric Dimming**: Seamless full-screen focus shift during choice-heavy states (Level Up, Stuck).
*   **Neon Bloom**: Contoured, multi-layered "neon" outlines around active strategic areas (Perk Shelf, Result Card).
*   **Fluid HUD**: A liquid level progress bar with a wavy edge. "Splashes" with intensity and speed proportional to the points earned relative to the current level.
*   **Stable Popup Stacking**: Popups (Score/Perks) group by **grid coordinates** and use **deterministic ID sorting** for stable horizontal offsets, preventing layout jitter during rapid merges.
*   **Dynamic Level Up**: 
    *   **Queue Indicator**: A badge showing the number of pending level-up rewards.
    *   **Perk Refresh**: Uses `AnimatedContent` to scale/fade perks when selecting multiple rewards in sequence.
*   **Emergency Mode**: When stuck, the HUD displays a floating, bouncing tooltip and faster shelf pulse to guide players toward using a perk.
*   **Interactive Result Card**: Floating frosted glass overlay with animated score count-ups, detailed stats (merges, max combo, highest tile), and a "View Board" peek mode.

---

## 5. Development Guidelines
1.  **State Atomicity**: Use `_uiState.update { ... }` for all gameplay changes to ensure consistency.
2.  **Interaction Locking**: Check `isBusy` and `pendingMerge` to block gestures during animations.
3.  **No Delays for UI**: Use `finishedListener` callbacks and `LaunchedEffect(state)` for reliable animation sequencing.
4.  **Level Choice Queueing**: Multiple level-ups are queued. Use `perkSpawnCounter` to track turns between on-board spawns.
5.  **Ghost/Perk Interactions**: Always clean up overlapping ghosts or perks when a tile occupies a hex to maintain board clarity.
