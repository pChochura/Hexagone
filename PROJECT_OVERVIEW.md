# Project: Hexagone Overview

**Hexagone** is a high-polish, strategic hexagonal puzzle game built with **Compose Multiplatform**. It features a reactive, state-driven architecture, a deep strategic economy, and a premium "Solid UI" design language.

---

## 1. Project Structure

The project follows a modular Kotlin Multiplatform (KMP) architecture, utilizing a full-screen navigation system and strict design tokens.

### Key Directory Map
```text
shared/src/commonMain/kotlin/com/pointlessgames/hexagone/
├── achievements/
│   ├── AchievementManager.kt     # Core Interface for unlocks & tracking
│   └── GameAchievement.kt        # Logical registry of all milestones
├── data/
│   ├── SettingsRepository.kt     # Game State & local persistence (DataStore)
│   ├── LeaderboardRepository.kt  # Supabase: Scores & Profile sync
│   └── MonetizationRepository.kt # Economy: RevenueCat & Supabase sync
├── billing/
│   ├── BillingManager.kt         # Economy interface
├── game/
│   ├── logic/
│   │   ├── GameEngine.kt         # Core math: neighbors, merges, hints
│   │   ├── Scoring.kt            # Centralized scoring formulas & multipliers
│   │   ├── PatternRecognitionEngine.kt # Geometric achievement scanning
│   │   └── DailyChallengeProvider.kt # Deterministic mission generator
│   ├── model/
│   │   ├── GameModels.kt         # Domain Models: Perk, GameUiState, HexDialogState
│   ├── ui/
│   │   ├── GameScreen.kt         # Main Gameplay Loop
│   │   ├── ShopScreen.kt         # Full-screen Store (Grids & Horizontal scroll)
│   │   ├── DailyMissionsScreen.kt # Full-screen Missions (Log & Rewards)
│   │   ├── LeaderboardScreen.kt  # Full-screen Rankings (Podium style)
│   │   ├── AchievementsScreen.kt # Full-screen Collection (Card-based)
│   │   └── components/
│   │       ├── ScreenScaffold.kt  # Organism: Translucent "Glass" headers
│   │       ├── HexDialogComponents.kt # Atoms: Premium Alert Dialogs & Cards
│   │       ├── GameGridOverlay.kt # Grid orchestrator & gesture handling
│   │       ├── ScoreSection.kt    # HUD: liquid progress & combo indicators
│   │       ├── PerkBar.kt         # Strategic tool selection shelf
│   │       └── GameOverlays.kt    # Dialog orchestration (Level Up, Game Over)
├── di/
│   └── GameModule.kt             # Koin DI & Navigation Routing
├── Navigator.kt                  # navigation3 Implementation & Routes
└── ui/
    └── theme/
        └── Theme.kt              # "Solid UI" Design System (Strict Design Tokens)
```

---

## 2. Navigation & Layout Architecture

### Full-Screen Navigation
The game has transitioned from local overlays to a formal navigation stack using **`navigation3`**. 
*   **Routes**: Defined as serializable objects (`Route.Shop`, `Route.Leaderboard`, etc.) in `Navigator.kt`.
*   **Decoupled State**: Screens are independent destinations, drastically simplifying `GameScreen.kt` and improving performance.

### ScreenScaffold Organism
All secondary screens use a unified `ScreenScaffold` that provides:
*   **Translucent "Glass" Headers**: A vertical gradient top bar where content scrolls beautifully underneath.
*   **Hexagonal Navigation**: Standardized hexagonal back buttons.
*   **Dynamic Measurement**: Headers measure their own height to provide perfect top-padding for immersive content.

---

## 3. Core Game Logic

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

## 4. Achievement System

### UI Integration
*   **Card-Based UI**: Each achievement is presented as a high-fidelity card with a `WavyProgressBar`.
*   **Dynamic Highlighting**: Clicking notifications deep-links directly to the relevant achievement within the full-screen view.
*   **Sequential Notifications**: Unlocked achievements are queued and displayed via a top-level popup.

---

## 5. Daily Missions & Streak

### Immersive Progress
*   **Prominent Streak**: Current streak is the focal point with massive bold typography.
*   **Next Reward Card**: High-fidelity card displaying upcoming milestone rewards (Diamonds/Perks).
*   **Mission Log**: A custom month-view calendar highlighting every day the player completed all objectives.
*   **Persistent Rewards**: Completing all 3 daily missions triggers a server-side reward grant (Diamonds & Vouchers).

---

## 6. Interactive Tip System

### Component Architecture
*   **TipOverlay**: Full-screen overlay using a **Spotlight Effect** to highlight UI elements.
*   **Target Tracking**: Uses a custom `trackTipTarget` modifier and `onGloballyPositioned` to sync coordinates.

### Contextual Triggers
1.  **First Merge**: Guided instruction for new players.
2.  **Daily Challenge Intro**: Points out the system in the first session.
3.  **Perk Mastery**: Collection and usage education.
4.  **Post-Game Discovery**: Highlights Leaderboards and Achievements.

---

## 7. Strategic Features

### Prediction & Previews
*   **Interactive Previews**: Visually simulates Swaps, Moves, and Values before commitment.
*   **Merge Isolation**: Previews at ghost positions do not trigger merges.

### Perk & Voucher Economy
*   **Rarity Groups**: Common (VCMN), Rare (VRARE), Legendary (VLGD).
*   **Voucher System**: Category-based vouchers can be exchanged for any perk within that rarity tier on-demand.
*   **Cloud-Synced Balances**: Inventory is stored server-side via RevenueCat.

---

## 8. "Solid UI" Design System

*   **Design Tokens**: Strict adherence to `MaterialTheme.spacing` and `cornerRadius`.
*   **Draw-Phase Animations**: High-frequency effects (like the podium glow) are optimized to run in the draw phase, eliminating unnecessary recompositions.
*   **Shallow UI Tree**: Minimal nesting through the use of `Arrangement.spacedBy()` and modifier stacking.
*   **Consistent Immersive Depth**: Content visibility through translucent headers is standard.
