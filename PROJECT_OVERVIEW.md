# Project: Hexagone Overview

**Hexagone** is a high-polish, strategic hexagonal puzzle game built with **Compose Multiplatform**. It features a reactive, state-driven architecture, a deep strategic economy, and a premium "Solid UI" design language.

---

## 1. Project Structure

The project follows a modular Kotlin Multiplatform (KMP) architecture, utilizing a full-screen navigation system and strict design tokens.

### Key Directory Map
```text
shared/src/commonMain/kotlin/com/pointlessgames/hexagone/
├── auth/
│   ├── ThemesViewModel.kt        # Theme selection & unlock logic
│   ├── SettingsViewModel.kt      # Account management logic
│   └── ui/
│       ├── ThemesScreen.kt       # Full-screen theme gallery
│       ├── SettingsScreen.kt     # Full-screen account hub
│       ├── ThemeUnlockedOverlay.kt # High-fidelity reward celebration
│       └── components/
│           ├── AuthButton.kt     # Standard high-fidelity buttons
│           └── NicknamePopup.kt  # Reusable identity dialog
├── achievements/
│   ├── AchievementManager.kt     # Core Interface for unlocks & tracking
│   └── GameAchievement.kt        # Logical registry of all milestones
├── leaderboard/
│   ├── LeaderboardViewModel.kt   # Global rankings & profile logic
│   └── ui/
│       └── LeaderboardScreen.kt  # Full-screen rankings (Podium style)
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
│   │   ├── AchievementsScreen.kt # Full-screen Collection (Card-based)
│   │   └── components/
│   │       ├── ScreenScaffold.kt  # Organism: Translucent "Glass" headers
│   │       ├── HexDialogComponents.kt # Atoms: Premium Alert Dialogs & Cards
│   │       ├── GameOverDialog.kt  # High-stakes session wrap-up
│   │       ├── ParticlesLayer.kt  # Visual flair & feedback
│   │       ├── PerkBar.kt         # Anchored strategic shelf
│   │       └── GameOverlays.kt    # Overlay orchestrator
│   ├── ActionDelegate.kt         # offloaded VM logic: Input handling
│   ├── StateDelegate.kt          # offloaded VM logic: UI State orchestration
│   ├── EffectDelegate.kt         # offloaded VM logic: Animation & SFX triggers
│   └── GameViewModel.kt          # Lean VM orchestrator (via Delegates)
├── share/
│   └── ShareManager.kt           # Platform-agnostic social sharing
├── di/
│   ├── AppModule.kt              # Main Koin entry point
│   ├── PlatformModule.kt         # Expected platform-specific bindings
│   └── GameModule.kt             # Feature-specific injections
├── Navigator.kt                  # navigation3 Implementation & Routes
└── App.kt                        # Application entry & theme root
```

---

## 2. Navigation & Layout Architecture

### Full-Screen Navigation
The game uses a formal navigation stack powered by **`navigation3`**. 
*   **Routes**: Defined as serializable objects in `Navigator.kt`:
    *   `Route.Game`: The main hexagonal grid.
    *   `Route.Shop`: Premium store and perk previews.
    *   `Route.Themes`: Visual customization gallery.
    *   *Also*: `Leaderboard`, `DailyMissions`, `Achievements`, `Settings`.
*   **Transitions**: Implements smooth `fadeIn`/`fadeOut` transitions for all screen changes.
*   **Navigator Singleton**: Provided via `LocalNavigator`, allowing any component to trigger deep-linked navigation (e.g., clicking an achievement notification to open the `Achievements` screen).

### ScreenScaffold Organism
All secondary screens use a unified `ScreenScaffold` that provides:
*   **Translucent "Glass" Headers**: A vertical gradient top bar where content scrolls beautifully underneath.
*   **Hexagonal Navigation**: Standardized hexagonal back buttons.
*   **Dynamic Measurement**: Headers measure their own height to provide perfect top-padding for immersive content.

---

## 3. Identity & Profile

### Anonymous First
Hexagone prioritizes immediate play. A unique `playerId` (UUID) is generated on first launch and persisted locally via `SettingsRepository`.
*   **Profile Creation**: Users can set a nickname at any time via the `SettingsScreen` or when submitting a high score.
*   **Supabase Sync**: Profiles and scores are synchronized with Supabase. The system includes a **sync-on-launch** mechanism to ensure local scores are uploaded if a connection was missing during gameplay.
*   **Nickname Popup**: A shared, high-fidelity `NicknamePopup` handles name entry with smooth scale-in animations and validation.

---

## 4. Visual Themes

Hexagone features a robust theming system that alters the entire visual atmosphere (colors, gradients, and hexagon styles).
*   **ThemeId Registry**: Includes `NEON_GLOW`, `OCEAN`, `MIDNIGHT`, `CYBER`, `BERRY`, etc.
*   **Unlock Mechanics**:
    *   **Progression**: Reach specific lifetime levels (e.g., Level 50 for `MIDNIGHT`).
    *   **Mastery**: Unlock all achievements (for `CYBER`).
    *   **Consistency**: Maintain a long Daily Mission streak (e.g., 10 days for `BERRY`).
*   **Premium Themes**: Some themes can be purchased directly using Diamonds.
*   **Celebration**: Unlocking a new theme triggers the `ThemeUnlockedOverlay`, showcasing the new visual style in a high-fidelity preview.

---

## 5. Core Game Logic

### Delegate-Based Architecture
To maintain a manageable `GameViewModel`, the logic is offloaded into specialized **Delegates**:
*   **ActionDelegate**: Handles user inputs (clicks, drags, perk usage).
*   **StateDelegate**: Manages the complex `GameUiState` and long-running timers.
*   **EffectDelegate**: Orchestrates one-off animations, particle triggers, and sound effects.
*   **MergeDelegate**: Pure logic for geometric merging and wildcard (Mimic) behavior.

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
*   **Balance Validation**: The dialog dynamically dims and disables "BUY" options if the current Diamond balance is insufficient, preventing invalid transaction attempts.
*   **Loss Aversion**: The dialog highlights current score and level to emphasize what's at stake.

---

## 5. Achievement System

### UI Integration
*   **Card-Based UI**: Each achievement is presented as a high-fidelity card with a `WavyProgressBar`.
*   **Sequential Notifications**: Unlocked achievements are queued and displayed via a top-level popup with deep-linking support.

---

## 6. Daily Missions & Streak

### Immersive Progress
*   **Prominent Streak**: Current streak is the focal point with massive bold typography and a dedicated Milestone Rewards section. The streak is calculated dynamically from the player's completion history to ensure accuracy.
*   **Mission Log**: A custom month-view calendar highlighting daily completion.
*   **Persistent Completion**: Completion status for the Daily Streak is persistent across match sessions and app reloads.
*   **Repeatable Session Rewards**: Missions reset to 0% progress at the start of every game session, allowing players to earn bonus rewards (Score/Perks) multiple times a day while working towards their persistent streak goal.

### Streak Protection Logic
*   **Cross-Day Grace**: If a player opens the app with unfinished missions from the previous day, an anchored **MissionRefreshPopup** appears.
*   **The Choice**: 
    *   **KEEP**: Finish yesterday's missions today to save the streak (completion attributes back to the original date).
    *   **REFRESH**: Reset the streak to 0 and get fresh missions for today.
*   **Automatic Reset**: If more than one full day is skipped, the system automatically resets the streak and refreshes missions to maintain the competitive integrity of the leaderboards.

---

## 7. Social Sharing

Players can share their achievements and final scores using the `ShareManager`.
*   **High-Fidelity Captures**: The game renders a dedicated `ShareableGameOverLayout` to an `ImageBitmap`.
*   **Contextual Meta**: Includes score, level, and a branded "Hexagone" overlay.
*   **Platform Integration**: Uses native share sheets via platform-specific `ShareManager` implementations.


---

## 8. Interactive Tip System

### Component Architecture
*   **TipOverlay**: Full-screen overlay using a **Spotlight Effect** to highlight UI elements.
*   **Target Tracking**: Uses a custom `trackTipTarget` modifier and `onGloballyPositioned` to sync coordinates.

### Contextual Triggers
1.  **First Merge**: Guided instruction for new players.
2.  **Daily Challenge Intro**: Points out the system in the first session.
3.  **Perk Mastery**: Collection and usage education.
4.  **Post-Game Discovery**: Highlights Leaderboards and Achievements.

---

## 9. Strategic Features

### Prediction & Previews
*   **Interactive Previews**: Visually simulates Swaps, Moves, and Values before commitment.

### Perks Bank Dialog
A central hub for strategic inventory management:
*   **Rarity-Based Grouping**: Perks are organized by Common, Rare, and Legendary tiers.
*   **Real-Time Counts**: Displays available vouchers per category with instant UI updates.
*   **Direct Restocking**: If a category is empty, a functional "BUY" button appears, allowing players to purchase vouchers without leaving their current context (e.g., during a Revive choice).
*   **Intuitive Navigation**: Horizontal lists feature adaptive scroll indicators (`ic_left`/`ic_right`) that appear only when more content is available to explore.

### Perk & Voucher Economy
*   **Unified Access**: Strategic actions are managed via an anchored **PerkBar shelf**. This shelf provides quick access to the Perks Bank (via the **ADD button**) and the Store (via the **SHOP button**).
    *   **Portrait**: Anchored to the bottom with top-rounded corners.
    *   **Landscape**: Anchored to the right with left-rounded corners.
*   **Optimistic UI & Sync**: Currency and voucher updates are applied instantly to the UI using an "Optimistic" pattern, with an **in-flight locking mechanism** that prevents flickering while background server synchronization (RevenueCat/Supabase) completes.
*   **Persistence**: Inventory and currency balances are preserved across game restarts and app reloads.
*   **Shop Transparency**: The main `ShopScreen` includes **Perk Previews** for every voucher tier, showing the specific icons of perks available in that category to inform purchase decisions.

---

## 10. Audio & Atmosphere

### Dynamic Soundscape
Hexagone features a multi-layered audio system built for immersive feedback:
*   **Background Music**: A looping, high-fidelity ambient track managed with lifecycle awareness (pauses/resumes automatically based on app visibility).
*   **UI Feedback**: Consistent click sounds for all buttons, dialogs, and notifications using `rememberPlayButtonSound`.
*   **Gameplay Stings**: Special audio triggers for merges, combo milestones, achievement unlocks, and game over states.

---

## 11. "Solid UI" Design System

*   **Design Tokens**: Strict adherence to `MaterialTheme.spacing` and `cornerRadius`.
*   **High-Fidelity Components**: Reusable components like `AuthButton` and `HexagonIconButton` ensure visual consistency.
*   **Shallow UI Tree**: Optimized layouts using `Arrangement.spacedBy()` and modifier stacking.
*   **Consistent Immersive Depth**: Content visibility through translucent headers is standard.

### Immersive Gestures
*   **Swipe-to-Pause/Restart**: Instead of relying on hardware back buttons, pausing the game uses a seamless pull-down gesture on the gameplay area.
    *   **Visual Feedback**: As the user drags down, the main game board and score sections smoothly animate downward and fade to translucent. This is implemented via `graphicsLayer` for GPU-optimized, zero-recomposition performance.
    *   **Threshold Trigger**: The "Restart?" menu only triggers if the drag distance passes a specific threshold before the finger is released, preventing accidental pauses.
    *   **Focused Intent**: The pause menu is stripped of generic "Quit" options. It only prompts the user to either Resume or Restart, explicitly detailing the progress at risk (score, perks, level, and daily missions) to maximize retention and focus.
