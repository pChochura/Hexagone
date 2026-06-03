# Project: Hexagone Overview

**Hexagone** is a high-polish, strategic hexagonal puzzle game built with **Compose Multiplatform**. It features a reactive, state-driven architecture, deep strategic mechanics, and a vibrant, "glowing hardware" aesthetic.

---

## 1. Project Structure

The project follows a modular Kotlin Multiplatform (KMP) architecture, prioritizing separation of concerns and Compose best practices (State Hoisting, Slot-based APIs).

### Key Directory Map
```text
shared/src/commonMain/kotlin/com/pointlessgames/hexagone/
├── achievements/
│   ├── AchievementManager.kt     # Core Interface for unlocks & tracking
│   ├── LocalAchievementManager.kt # Persistence-backed implementation
│   └── GameAchievement.kt        # Logical registry of all milestones
├── data/
│   └── SettingsRepository.kt     # Game State & DataStore persistence
├── di/
│   └── AppModule.kt              # Koin Dependency Injection
├── game/
│   ├── logic/
│   │   ├── GameEngine.kt         # Core math: neighbors, merges, hints
│   │   ├── Scoring.kt            # Centralized scoring formulas & multipliers
│   │   └── PatternRecognitionEngine.kt # Geometric achievement scanning
│   ├── model/
│   │   ├── GameModels.kt         # Domain Models: Perk, GameUiState, MergeTransition
│   │   └── UIModels.kt           # UI-specific state: Particles, Animation states
│   ├── ui/
│   │   ├── GameScreen.kt         # Root UI Layout
│   │   └── components/
│   │       ├── AchievementsDialog.kt # BottomSheet list of all rewards
│   │       ├── AchievementNotification.kt # Top-level clickable HUD popup
│   │       ├── PopupsLayer.kt     # Stable HUD notifications (sequential IDs)
│   │       ├── GridDrawing.kt     # Optimized DrawScope rendering logic
│   │       ├── GameGridOverlay.kt # Grid orchestrator & gesture handling
│   │       ├── ScoreSection.kt    # HUD: liquid progress & combo indicators
│   │       ├── PerkBar.kt         # Strategic tool selection shelf
│   │       └── GameOverlays.kt    # Dialog orchestration (Level Up, Game Over)
│   ├── ActionDelegate.kt         # Delegate: User input & tile manipulation
│   ├── EffectDelegate.kt         # Delegate: Particles, Popups & HUD feedback
│   ├── MergeDelegate.kt          # Delegate: Merge lifecycle & animation logic
│   ├── StateDelegate.kt          # Delegate: History, Undo & Persistence
│   ├── AchievementDelegate.kt    # Delegate: Rule-based milestone triggering
│   ├── GameViewModel.kt          # Coordinator: Orchestrates delegates & state
│   └── DebugDelegate.kt          # Delegate: Developer tools & state manipulation
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
*   **Solid Only**: Only solid tiles participate in merges. Ghost tiles (previews) are ignored by the merge engine until they are solidified.
*   **Frozen Tiles**: Frozen tiles are excluded from all merge calculations and pathfinding. They are unfrozen automatically at the end of each turn.
*   **Path Merge (Legendary)**: Merges all connected tiles of the same value across the board into a single target.
*   **Final Value**: $V_{max} + n - k$
    *   `Vmax`: Highest value in the group.
    *   `n`: Total number of tiles.
    *   `k`: Number of unique value groups involved.
*   **Base Score**: $\sum Values_{merging} + n^2 - n$.
*   **Tactical Multiplier**: Any merge involving a tile marked as "tactical" (from a Move, Swap, or Perk action) receives a **1.5x base score multiplier**.

### Scoring & Bonuses
*   **Scalable Bar Raised Bonus**: Granted whenever the smallest value tile is cleared from the game.
*   **Scalable Sacrifice Bonus**: Granted when removing the only remaining highest-value tile.
*   **Redemption Bonus**: If a move's score exceeds the previous turn's baseline, a bonus is applied (`250 + 50% of the difference`).
*   **Combo System**: Multipliers build with every merge (capped at **x12**). 
    *   The combo resets when a tile is placed from the queue without triggering a merge.
    *   **Chain Merge Rule**: Using `CHAIN_MERGE` forces a combo reset to 0 if no chain reaction occurs, unless the initial merge was complex enough to maintain the combo naturally (multi-group or path merge).

---

## 3. Achievement System

The game features a multi-layered achievement system integrated via a platform-agnostic `AchievementManager`.

### Achievement Categories
1.  **Spatial Architecture**: Detected by `PatternRecognitionEngine` (e.g., *Ring of Fire*, *Great Wall*, *The Prism*).
2.  **Strategic Mastery**: Turn-based milestones (e.g., *Triple Threat*, *Tactical Genius*, *Snake Charmer*).
3.  **Purity & Restraint**: Session-long tracking (e.g., *Pacifist*, *Ascetic*, *Zen Master*).
4.  **Lifetime Grind**: Incremental tracking in `DataStore` (e.g., *Marathon*, *Gambler*, *Perk Collector*).
5.  **Tactical Prowess**: Reward-based logic (e.g., *Redemption*, *Advanced Janitor*, *Double Vision*).

### UI Integration
*   **Sequential Notifications**: Unlocked achievements are queued and displayed via a top-level `Popup` window.
*   **Multi-Tier Unlocks**: The logic supports triggering multiple achievement tiers (e.g., *Feeling the Surge* and *Maximum Overdrive*) in a single action.
*   **Interactive HUD**: Clicking an achievement notification immediately opens the full collection view.
*   **Dynamic Sorting**: The achievements list automatically prioritizes unlocked rewards while maintaining a logical progression order.

---

## 4. Strategic Features

### Predictive Hint System & Hover Previews
*   **Interactive Previews**: Visually simulates Swaps, Moves, and direct Value changes (Increment/Fusion) before commitment.
*   **Merge Isolation**: Previews at ghost positions do not trigger merges; only solid tiles are calculated.
*   **Look-Ahead**: Simulations account for the current move plus the next 3 pieces in the queue.

### Perk Economy
*   **Rarity Weights**: Common (Undo, Move, Remove, Increment), Rare (Advance, Swap, Duplicate, Skip, Freeze), Legendary (Fusion, Chain Merge, Path Merge).
*   **Strategic Behavioral Rules**:
    *   **Move & Duplicate**: Positional actions that preserve the "ghost" or "solid" status and allow for combo setup without forced merges.
    *   **Freeze Strategy**: Allows isolating a tile to prevent accidental merges, useful for preserving high-value clusters or setting up future complex moves.
    *   **Lifespan Stability**: On-board perks only decrement lifespan during regular turn progression, not during strategic perk actions.
    *   **Pity System**: Guaranteed on-board perk spawning between 8 and 15 turns.

---

## 5. UI & Visual Identity

*   **Design System**: Centralized in `HexagoneTheme`, mapping Material 3 roles to "Glowing Hardware" tokens.
*   **Stable Popups**: Notifications use **sequential IDs** and pre-calculated offsets for animation stability.
*   **Liquid HUD**: A progress bar with a dynamic wavy edge that "splashes" based on point intensity.
*   **Frost Effect**: Frozen tiles are visually distinct with a themed blue border and a snowflake badge.
*   **Animated Perk Shelf**: Items trigger a scale-up "pop" (150%) when added, with smart scrolling to the latest updates.

---

## 6. Development Guidelines
1.  **ViewModel Delegation**: Extract complex logic into domain-specific delegates (State, Action, Effect, Merge, Achievement) to maintain a slim coordinator.
2.  **State Atomicity**: Use `_uiState.update { ... }` for all gameplay changes to ensure consistency.
3.  **Unique Merge IDs**: Always generate dynamic, unique IDs for merges (e.g., `cell_path_merge_N`) to prevent `MergeDelegate` from skipping animation steps due to ID collisions.
4.  **Stability Guards**: Use stable keys (`key(id)`) for all dynamic items to prevent unnecessary recompositions.
5.  **Interaction Locking**: Check `isBusy` and `pendingMerge` flags to prevent input during animations.
6.  **Persistence**: State is serialized via `kotlinx.serialization` and persisted after every successful move.
