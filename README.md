# Hexagone ⬡

<img width="1024" height="500" alt="Frame 16 (1)" src="https://github.com/user-attachments/assets/f134398c-b80c-4098-b577-282e29381fdc" />

---

**Hexagone** is a high-polish, strategic hexagonal puzzle game built with **Compose Multiplatform**. It combines deep strategic mechanics, a reactive state-driven architecture, and a vibrant "glowing hardware" aesthetic to deliver a premium puzzle experience.


<a href="https://play.google.com/store/apps/details?id=com.pointlessgames.hexagone"><img width="195" height="58" alt="GetItOnGooglePlay_Badge_Web_color_English 1" src="https://github.com/user-attachments/assets/10e2a000-2d60-43d8-88c7-dd210237c18c"/></a>
<a href="https://apps.apple.com/us/app/hexagone-merge/id6782570490"><img width="175" height="58" alt="Download_on_the_App_Store_Badge_US-UK_RGB_blk_092917 1" src="https://github.com/user-attachments/assets/932a8504-9a48-44d1-b253-83165fbe4200" /></a>


## ✨ Features

- **Strategic Hexagonal Gameplay**: A unique merge system on a staggered flat-top hexagonal grid.
- **Deep Perk Economy**: 15+ unique perks ranging from common "Undo" to legendary "Path Merge" that shift the tide of the game.
- **Dynamic Achievement System**: Over 30 milestones across categories like Spatial Architecture, Strategic Mastery, and Purity.
- **Daily Missions**: Deterministically generated daily challenges with session-specific rewards and persistent streaks.
- **Contextual Onboarding**: An interactive tip system with spotlight effects that guides you through advanced mechanics without friction.
- **Compose Multiplatform**: Shared logic and UI across Android and iOS using Kotlin Multiplatform.
- **Glowing Hardware Aesthetic**: A custom design system leveraging Material 3 roles into a cohesive, neon-inspired visual identity.

## 📸 Screenshots

<img width="19%" height="1600" alt="Frame 39" src="https://github.com/user-attachments/assets/20ede7f9-0347-4b0d-a57c-df356bf14f9e" />
<img width="19%" height="1600" alt="Frame 40" src="https://github.com/user-attachments/assets/51157bdd-09b2-4ba7-a487-8cc3f8158fbe" />
<img width="19%" height="1600" alt="Frame 41" src="https://github.com/user-attachments/assets/6e8a1db9-c06b-4ba9-bdc7-af6bf9a74471" />
<img width="19%" height="1600" alt="Frame 42" src="https://github.com/user-attachments/assets/71b7cfa9-bb20-431c-8946-38e5767843a1" />
<img width="19%" height="1600" alt="Frame 43" src="https://github.com/user-attachments/assets/aceb37d2-12af-486b-9c08-e554f37a1d5e" />


## 🕹️ Core Mechanics

### The Merge Formula
A merge occurs when 2+ tiles of the same value touch. The final value of the merged tile is calculated as:
$$V_{final} = V_{max} + n - k$$
*   `Vmax`: Highest value in the group.
*   `n`: Total number of tiles.
*   `k`: Number of unique value groups involved.

### Tactical Multipliers
Moves involving strategic tile manipulations (Moves, Swaps, or Perk actions) receive a **1.5x base score multiplier**, encouraging advanced planning over simple placement.

### Combo System
Chain merges together to build multipliers up to **x12**. Strategic use of "Chain Merge" and "Fusion" perks can sustain high-scoring streaks.

## 🎨 Visual Identity

- **Liquid HUD**: A progress bar with a dynamic wavy edge that "splashes" based on point intensity.
- **Frost Effect**: Frozen tiles are visually distinct with a themed blue border and a custom snowflake badge.
- **Stable Popups**: HUD notifications use sequential IDs and pre-calculated offsets to ensure animation stability during high-intensity play.
- **Neon-Glow Design**: Mapping Material 3 roles to a custom "Glowing Hardware" palette.

## 🛠️ Tech Stack

- **UI**: Compose Multiplatform (Android/iOS)
- **Architecture**: MVI-inspired with specialized delegates (Merge, Action, Effect, State)
- **DI**: [Koin](https://insert-koin.io/)
- **Persistence**: DataStore with Kotlinx Serialization
- **Concurrency**: Kotlin Coroutines & Flow
- **Animations**: Custom idiomatic Compose animation helpers

## 📁 Project Structure

```text
shared/src/commonMain/kotlin/...
├── achievements/   # Achievement tracking & persistence
├── game/
│   ├── logic/      # Math, scoring, & pattern recognition engine
│   ├── ui/         # Composable screens & "Glowing Hardware" components
│   └── delegates/  # Specialized game state controllers
├── di/             # Koin modules
└── theme/          # Centralized Design System
```

## 🚀 Getting Started

1.  Clone the repository.
2.  Open in Android Studio (Ladybug or newer).
3.  Run the `:androidApp` or `:iosApp` configuration.

---

*Developed by Pointless Games.*
