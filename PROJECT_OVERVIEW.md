# Project: Hexagone Overview

**Hexagone** is a high-polish, strategic hexagonal puzzle game built with **Compose Multiplatform**. It features a reactive, state-driven architecture, a deep strategic economy, and a premium "Solid UI" design language.

---

## 1. Project Structure

The project follows a modular Kotlin Multiplatform (KMP) architecture, utilizing a full-screen navigation system and strict design tokens.

### Key Directory Map
```text
shared/src/commonMain/kotlin/com/pointlessgames/hexagone/
├── auth/
│   ├── LoginViewModel.kt         # Onboarding & Auth logic
│   ├── SettingsViewModel.kt      # Account management logic
│   ├── ui/
│   │   ├── LoginScreen.kt        # Immersive landing page
│   │   ├── SettingsScreen.kt     # Full-screen account hub
│   │   └── components/
│   │       ├── PlayfulTitle.kt   # Animated brand component
│   │       ├── AuthButton.kt     # Standard high-fidelity buttons
│   │       └── NicknamePopup.kt  # Reusable identity dialog
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
│   │       ├── PerkBar.kt         # Strategic floating shelf: perks & vouchers
│   │       └── GameOverlays.kt    # Overlay orchestrator (Revive, Level Up, Game Over)
├── di/
│   └── GameModule.kt             # Koin DI & Navigation Routing
├── Navigator.kt                  # navigation3 Implementation & Routes
└── utils/
    ├── SoundManager.kt           # Audio engine & effect triggers
    └── BackHandler.kt            # Platform-agnostic system back handling
```

---

## 2. Navigation & Layout Architecture

### Full-Screen Navigation
The game has transitioned from local overlays to a formal navigation stack using **`navigation3`**. 
*   **Routes**: Defined as serializable objects (`Route.Shop`, `Route.Login`, etc.) in `Navigator.kt`.
*   **Terminal Transitions**: The `Navigator` includes a `replaceAll(Route)` method to clear the backstack, ensuring clean state resets during login/logout.
*   **Decoupled State**: Screens are independent destinations, drastically simplifying `GameScreen.kt` and improving performance.

### ScreenScaffold Organism
All secondary screens use a unified `ScreenScaffold` that provides:
*   **Translucent "Glass" Headers**: A vertical gradient top bar where content scrolls beautifully underneath.
*   **Hexagonal Navigation**: Standardized hexagonal back buttons.
*   **Dynamic Measurement**: Headers measure their own height to provide perfect top-padding for immersive content.

---

## 3. Authentication & Onboarding

### Mandatory Identity
Hexagone requires a player profile before gameplay begins. This ensures consistent leaderboard tracking and achievement synchronization.
*   **Initial Check**: `App.kt` checks for an existing `playerId` on startup; if missing, the user is directed to the `LoginScreen`.
*   **Anonymous Login**: Users can start as a guest, requiring only a nickname.
*   **Social Providers**: Placeholders for Google Play Games and Apple Game Center allow for pre-populating identity metadata.
*   **Nickname Popup**: A shared, high-fidelity `NicknamePopup` handles name entry and confirmation with smooth scale-in animations.

### Immersive Login
The `LoginScreen` serves as the game's visual introduction:
*   **Background Simulation**: A dimmed game board "plays itself" in the background, demonstrating mechanics to new players.
*   **Playful Title**: The "HEXAGONE" title features dynamic per-character animations, including a sequential "Wave" jump and random "Hexagon Pop" transformations.

---

## 4. Core Game Logic

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

### Save Me (Revive) Mechanic
When the board is full and no moves or perks are available, the game triggers a high-stakes **Revive Dialog**.
*   **One-Time Offer**: Only available once per game session.
*   **Inventory Integration**: Players can use an existing voucher or purchase one instantly with Diamonds.
*   **Loss Aversion**: The dialog highlights current score and level to emphasize what's at stake.

---

## 5. Achievement System

### UI Integration
*   **Card-Based UI**: Each achievement is presented as a high-fidelity card with a `WavyProgressBar`.
*   **Sequential Notifications**: Unlocked achievements are queued and displayed via a top-level popup with deep-linking support.

---

## 6. Daily Missions & Streak

### Immersive Progress
*   **Prominent Streak**: Current streak is the focal point with massive bold typography.
*   **Mission Log**: A custom month-view calendar highlighting daily completion.
*   **Persistent Rewards**: Completing all daily missions triggers server-side reward grants.

---

## 7. Interactive Tip System

### Component Architecture
*   **TipOverlay**: Full-screen overlay using a **Spotlight Effect** to highlight UI elements.
*   **Target Tracking**: Uses a custom `trackTipTarget` modifier and `onGloballyPositioned` to sync coordinates.

### Contextual Triggers
1.  **First Merge**: Guided instruction for new players.
2.  **Daily Challenge Intro**: Points out the system in the first session.
3.  **Perk Mastery**: Collection and usage education.
4.  **Post-Game Discovery**: Highlights Leaderboards and Achievements.

---

## 8. Strategic Features

### Prediction & Previews
*   **Interactive Previews**: Visually simulates Swaps, Moves, and Values before commitment.

### Perk & Voucher Economy
*   **Rarity Groups**: Common (VCMN), Rare (VRARE), Legendary (VLGD).
*   **Integrated Access**: Vouchers are accessible directly from the **PerkBar** in a dedicated floating pod.
*   **Blueprint Styling**: Vouchers use a distinct "Blueprint" visual language (dashed borders, rarity colors) to distinguish them from active, ready-to-use perks.
*   **Voucher Exchange**: Category-based vouchers can be exchanged for any perk within that rarity tier on-demand via the `VoucherSelectionDialog`.
*   **Persistence**: Inventory and currency balances are preserved across game restarts and app reloads.

---

## 9. "Solid UI" Design System

*   **Design Tokens**: Strict adherence to `MaterialTheme.spacing` and `cornerRadius`.
*   **High-Fidelity Components**: Reusable components like `AuthButton` and `HexagonIconButton` ensure visual consistency.
*   **Shallow UI Tree**: Optimized layouts using `Arrangement.spacedBy()` and modifier stacking.
*   **Consistent Immersive Depth**: Content visibility through translucent headers is standard.
