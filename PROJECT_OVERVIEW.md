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
│   │   ├── PatternRecognitionEngine.kt # Geometric achievement scanning
│   │   └── DailyChallengeProvider.kt # Deterministic daily mission generator
│   ├── model/
│   │   ├── GameModels.kt         # Domain Models: Perk, GameUiState, GameTip
│   │   └── UIModels.kt           # UI-specific state: Particles, Animation states
│   ├── ui/
│   │   ├── GameScreen.kt         # Root UI Layout
│   │   └── components/
│   │       ├── AchievementsDialog.kt # BottomSheet list of all rewards
│   │       ├── AchievementNotification.kt # Top-level clickable HUD popup
│   │       ├── DailyChallengeDialog.kt # 5-day streak tracker & mission list
│   │       ├── DailyChallengeRewardOverlay.kt # Cinematic completion effect
│   │       ├── TipOverlay.kt      # Contextual onboarding with spotlight effect
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
│   ├── ChallengeDelegate.kt      # Delegate: Real-time mission tracking
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
*   **Mimic Tiles (Wildcards)**: Mimic tiles adapt to any adjacent value to trigger a merge.
    *   **Solidification**: During previews or complex merges (Fusion/Chain), Mimics adopt the **highest possible value** from the participating group to maximize results.
    *   **Mimic-Only Merges**: If only Mimics merge, they adopt the **highest value currently on the board** (plus merge bonus). If the board is empty of regular tiles, they default to a base value of 1.
    *   **Value Range Exclusion**: Mimic tiles are excluded from board-wide value range calculations. This ensures that new tile spawns and level progression are governed strictly by regular tiles.
*   **Frozen Tiles**: Frozen tiles are excluded from all merge calculations and pathfinding. They are unfrozen automatically at the end of each turn.
*   **Removals**: Actions that remove tiles (e.g., *Remove Tile* perk) are excluded from merge-count statistics for challenge tracking.
*   **Path Merge (Legendary)**: Merges all connected tiles of the same value across the board into a single target.
*   **Final Value**: $V_{max} + n - k$
    *   `Vmax`: Highest value in the group.
    *   `n`: Total number of tiles.
    *   `k`: Number of unique value groups involved.
*   **Base Score**: $\sum Values_{merging} + n^2 - n$.
*   **Tactical Multiplier**: Any merge involving a tile marked as "tactical" (from a Move, Swap, or Perk action) receives a **1.5x base score multiplier**.

### Scoring & Bonuses
*   **Scalable Bar Raised Bonus**: Granted whenever the smallest regular value tile is cleared from the game. This can be triggered by a merge, removal, or by using the **Mimic perk** on the last remaining tile of the minimum value.
*   **Scalable Sacrifice Bonus**: Granted when removing the only remaining highest-value regular tile. Mimic tiles do not interfere with this detection.
*   **Execution Bonus**: Granted when removing a Mimic tile. The reward is calculated based on the highest value on the board: `(highestValue * 50) + 1000`.
*   **Redemption Bonus**: If a move's score exceeds the previous turn's baseline, a bonus is applied (`250 + 50% of the difference`).
*   **Combo System**: Multipliers build with every merge (capped at **x12**). 
    *   The combo resets when a tile is placed from the queue without triggering a merge.
    *   **Chain Merge Rule**: Using `CHAIN_MERGE` forces a combo reset to 0 if no chain reaction occurs, unless the initial merge was complex enough to maintain the combo naturally (multi-group or path merge).

---

## 3. Achievement System

The game features a multi-layered achievement system integrated via a platform-agnostic `AchievementManager`.

### Achievement Categories
1.  **Spatial Architecture**: Detected by `PatternRecognitionEngine` (e.g., *Ring of Fire*, *Great Wall*, *The Prism*). Patterns support **Mimic wildcards** for detection.
2.  **Strategic Mastery**: Turn-based milestones (e.g., *Triple Threat*, *Tactical Genius*, *Snake Charmer*).
3.  **Purity & Restraint**: Session-long tracking (e.g., *Pacifist*, *Ascetic*, *Zen Master*).
4.  **Lifetime Grind**: Incremental tracking in `DataStore` (e.g., *Marathon*, *Gambler*, *Perk Collector*).
5.  **Tactical Prowess**: Reward-based logic (e.g., *Redemption*, *Advanced Janitor*, *Double Vision*). Includes Mimic-specific triggers like *Perfect Fit* (using a mimic in a pattern).

### UI Integration
*   **Sequential Notifications**: Unlocked achievements are queued and displayed via a top-level `Popup` window.
*   **Multi-Tier Unlocks**: The logic supports triggering multiple achievement tiers (e.g., *Feeling the Surge* and *Maximum Overdrive*) in a single action.
*   **Interactive HUD**: Clicking an achievement notification immediately opens the full collection view.
*   **Dynamic Sorting**: The achievements list automatically prioritizes unlocked rewards while maintaining a logical progression order.

---

## 4. Daily Missions System

The game features a dynamic mission system that resets per session but tracks long-term commitment via a persistent streak.

### Deterministic Generation
*   **Seeded Variety**: Uses `DailyChallengeProvider` to generate 3 distinct missions daily, seeded by the current date (`yyyyMMdd`).
*   **Strategic Categories**:
    *   **Fundamental**: Direct interaction targets (e.g., *Merge 25 times*, *Reach Level 8*).
    *   **Skill & Tactics**: Advanced play requirements (e.g., *5 Tactical Merges*, *Create a Value 12 Tile*, *Frozen Recovery*).
    *   **Performance**: High-score or efficiency goals (e.g., *x10 Combo*, *10,000 Session Score*, *Combo Maintenance*).
    *   **Geometric & Pattern**: Board state layout goals (e.g., *Ring of Fire*, *The Prism*, *Ghost Horde*).
    *   **Restricted**: Conditional achievements (e.g., *Perk Restricted Level*, *Frugal Survivor*).

### Reward Mechanics
*   **Session-Specific Impact**: Completing a mission mid-game instantly rewards the player with either a **Score Boost** or a **Free Perk**, aiding the current run.
*   **Per-Game Collection**: Mission progress resets every time a new game starts, allowing players to collect rewards multiple times per day.
*   **Persistent Streak**: Completing the full set of 3 missions in any single session marks the day as "Done" in a persistent 5-day calendar view.
*   **Streak-Based Scaling**: Score rewards scale steadily with the current streak, providing higher bonuses for consistent daily play.

### UI Integration
*   **Cinematic Completion**: Completion triggers a high-impact `DailyChallengeRewardOverlay` with a star burst and glow effect.
*   **Wavy mission Cards**: The `DailyChallengeDialog` reuses the `WavyProgressBar` design from the achievement system for visual consistency.
*   **Reward Preview**: Mission cards explicitly state the reward (Score or Perk) to encourage completion.
*   **Post-Game Summary**: The Game Over screen provides a summary of all completed missions and their impact on the session.

---

## 5. Interactive Tip System

The game implements a contextual onboarding system to guide players through its deep mechanics without intrusive tutorials.

### Component Architecture
*   **TipOverlay**: A full-screen overlay that uses a **Spotlight Effect** to highlight specific UI elements while dimming the rest of the screen.
*   **Spotlight Mechanics**: Implemented using `ClipOp.Difference` on a `Path` containing the target element's bounding box.
*   **Target Tracking**: Uses a custom `trackTipTarget` modifier and `Modifier.onGloballyPositioned` to dynamically send screen coordinates of HUD elements back to the overlay coordinator.

### Contextual Triggers
1.  **First Merge**: Guided merging instruction for new players.
2.  **Daily Challenge Intro**: Points out the challenge system in the first session.
3.  **Perk Mastery**: Triggers when the first perk is collected, explaining the `PerkBar` mechanics.
4.  **Post-Game Discovery**: Highlights Leaderboards and Achievements on the first Game Over screen.

---

## 6. Strategic Features

### Prediction & Previews
*   **Interactive Previews**: Visually simulates Swaps, Moves, and direct Value changes before commitment.
*   **Merge Isolation**: Previews at ghost positions do not trigger merges; only solid tiles are calculated.
*   **Look-Ahead**: Simulations account for the current move plus the next 3 pieces in the queue.

### Offline Persistence & Sync
*   **Score Queue**: Failed leaderboard submissions are serialized and stored in `DataStore`.
*   **Background Processing**: Uses `WorkManager` (Android) and `BGTaskScheduler` (iOS) to automatically sync pending scores when connectivity is restored.
*   **Atomic State**: Full game state is persisted after every move to ensure no progress is lost.

### Perk Economy
*   **Rarity Weights**: 
    *   **Common**: Undo, Move, Remove, Increment (Upgrade).
    *   **Rare**: Advance, Swap, Duplicate, Skip (Pause), Freeze, Mimic.
    *   **Legendary**: Fusion, Chain Merge, Path Merge.
*   **Strategic Behavioral Rules**:
    *   **Target Restrictions**: Perks like *Upgrade* and *Mimic* are blocked from targeting existing Mimic tiles to maintain game balance.
    *   **Move & Duplicate**: Positional actions that preserve the "ghost" or "solid" status and allow for combo setup without forced merges. *Duplicate* correctly copies the Mimic attribute, displaying it as a star in the preview.
    *   **Freeze Strategy**: Allows isolating a **solid tile** to prevent accidental merges. Useful for preserving high-value clusters or setting up future complex moves. Restricted from targeting ghost tiles.
    *   **Lifespan Stability**: On-board perks only decrement lifespan during regular turn progression, not during strategic perk actions.
    *   **Pity System**: Guaranteed on-board perk spawning between 8 and 15 turns.

---

## 7. UI & Visual Identity

*   **Design System**: Centralized in `HexagoneTheme`, mapping Material 3 roles to "Glowing Hardware" tokens.
*   **Stable Popups**: Notifications use **sequential IDs** and pre-calculated offsets for animation stability.
*   **Liquid HUD**: A progress bar with a dynamic wavy edge that "splashes" based on point intensity.
*   **Frost Effect**: Frozen tiles are visually distinct with a themed blue border and a snowflake badge.
*   **Animated Perk Shelf**: Items trigger a scale-up "pop" (150%) when added, with smart scrolling to the latest updates.

---

## 8. Development Guidelines
1.  **ViewModel Delegation**: Extract complex logic into domain-specific delegates (State, Action, Effect, Merge, Achievement) to maintain a slim coordinator.
2.  **State Atomicity**: Use `_uiState.update { ... }` for all gameplay changes to ensure consistency.
3.  **Unique Merge IDs**: Always generate dynamic, unique IDs for merges (e.g., `cell_path_merge_N`) to prevent `MergeDelegate` from skipping animation steps due to ID collisions.
4.  **Stability Guards**: Use stable keys (`key(id)`) for all dynamic items to prevent unnecessary recompositions.
5.  **Interaction Locking**: Check `isBusy` and `pendingMerge` flags to prevent input during animations.
6.  **Persistence**: State is serialized via `kotlinx.serialization` and persisted after every successful move.
