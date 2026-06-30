# Hexagone: The Cyber-Breach (Roguelite Design Vision)

> [!NOTE]
> This document summarizes the transition of Hexagone from an endless high-score puzzle game into a structured, run-based roguelite inspired by the sleek, cyber-aesthetic of *Data Wing*.

## 1. The Core Premise: Cyber-Espionage
The abstract hexagonal board is recontextualized as a highly secure, encrypted mainframe. The player acts as an elite "Splicer" (hacker) navigating through layers of network security. 
*   **The Goal:** Breach the Central Core and extract the data without the system crashing (the board filling up).
*   **The Enemies:** The game board actively fights back. Bosses and hazards are represented as security subroutines, firewalls, and corrupted sectors.

## 2. The Map and Routing
Progression is no longer linear. Between challenges, the player views a branching network map to plot their course to the Core.

### Node Types (Chapters)
Each node on the map represents a "Chapter" or "Floor" with its own specific completion objectives and rules.

**The "Data Sink" Mechanic (Extraction Tile):**
To complete many levels, players must interact with a special "Data Sink" (or "Bag") tile permanently affixed to the board. This acts as a physical extraction point.
*   Players must merge or move specific tiles *into* the Data Sink to deposit them.
*   The level is complete when the Data Sink absorbs its target quota (e.g., "Collect 10 tiles of Value 32+", or "Absorb a total collective value of 5,000").
*   This physicalizes the win condition directly onto the board, forcing players to build paths and strategically push high-value tiles toward a specific coordinate.

*   **Standard Protocol:** Standard rules apply. Fulfill the Data Sink quota to extract and advance.
*   **Encrypted Sector (Elite):** Harder constraints (e.g., "Combo resets twice as fast" or "3 tiles are permanently frozen"). High risk, but rewards a Legendary Script.
*   **The Black Market (Shop):** A safe node to spend accumulated run-currency on Vouchers or Script upgrades.
*   **Firewall (Boss):** The final node of a network sector. Introduces a massive, intrusive mechanic (e.g., a Replicator virus) that must be defeated to progress to the next sector.

## 3. Security Subroutines (Boss Encounters)
To complete a network sector (a major chapter in a run), the player must overcome a "Firewall" node. These boss encounters introduce intrusive, rule-breaking mechanics that disrupt normal puzzle strategy.

*   **The Replicator (Worm Virus):** A pulsing tile that duplicates itself every few turns. It cannot be moved or deleted by standard means. The player must build a high-value tile and merge it directly adjacent to the virus to trigger a chain-reaction deletion.
*   **The Blackout Protocol (Data Obfuscation):** Periodically encrypts random tiles on the board, hiding their numeric values behind glitch effects. The player must rely on short-term memory to feed the correct sequence of values into the Data Sink.
*   **The Gravity Well (Tractor Beam):** A massive 3-hex anomaly that slowly pulls all tiles towards it, destroying any that touch it. The player must deliberately feed it a massive sum of points to overload it, turning it into the extraction point.
*   **The Iron Firewall (Symmetrical Divide):** A literal wall of impassable hexes splits the board in half. New tiles only spawn on one side, but the Data Sink is on the other. The player must build combos against the wall to break individual tiles and open a breach.

## 4. The Script System (In-Run Builds & Grid Manipulation)
To survive the escalating security protocols, players must draft **Scripts** (passive modifiers) upon completing nodes. Scripts allow players to create broken, highly-specific builds that only last for the duration of the run.

*   **Synergy and Collection:** For example, combining **"Mimic's Blessing"** (Mimics give +500 points) with **"Corrupted Code"** (5% chance for any tile to spawn as a Mimic). Finding duplicate Scripts in a run upgrades them to Tier II or III, increasing their potency.
*   **Dynamic Grid Manipulation:** The physical geometry of the board can be rewritten on the fly via specific Scripts or ultimate abilities:
    *   **Buffer Sectors (Memory Allocation):** Permanently append an extra row of tiles (a "Buffer") to the top or bottom edge of the board. This creates a high-risk, high-reward crafting zone outside the main grid.
    *   **Sandbox Override (Unbounded Mode):** Temporarily dissolves the hard 5x4 borders into an infinite void. Players have 3 turns to build massive combos. Afterward, the system "Defragments," snapping a new 5x4 boundary around the highest concentration of tiles and deleting anything left outside.
    *   **Core Fracture (Center Insertion):** The ultimate panic button. A cross of completely empty hexes is violently inserted directly into the center of the board, pushing all existing tiles outward to create breathing room and push "garbage" tiles off the absolute edge.

## 5. The Relic Tree (Global Meta-Progression)
Outside of the active cyber-breach (the run), players have access to a permanent skill tree called the **Relic Tree**.
*   **Persistent Upgrades:** Even if a player fails a run, they earn "Data Fragments" (or Diamonds) that they can spend on the Relic Tree in the main menu.
*   **Relic Nodes:** Unlocking nodes on this tree grants permanent buffs that apply to all future runs. For example: "Start every run with 1 Common Move Perk," or "Elite Sectors have a 10% chance to yield double rewards."
*   **The Collection Aspect:** The Relic Tree provides a visual, satisfying collection mechanic that persists across the lifetime of the game, ensuring no run ever feels like a waste of time.

## 6. Session Persistence
Respecting the player's time and the mobile platform context:
*   **Chapter Checkpoints:** The game state is automatically saved at the start of every node/chapter.
*   **Drop-in / Drop-out:** If a player closes the app mid-run, they will resume exactly from the chapter they were currently attempting. If they fail a chapter, the run ends, but the overarching meta-progression (The Relic Tree) is retained.

## 7. The Core Loop
1.  **Deploy:** Start a run from the Main Menu, viewing the initial network map.
2.  **Infiltrate:** Select a node and play the Hexagone puzzle under that node's specific constraints.
3.  **Extract:** Complete the node's objective to clear the board.
4.  **Upgrade:** Choose 1 of 3 randomized Scripts to add to your build.
5.  **Advance:** Move to the next node on the map.
6.  **Victory/Defeat:** Reach the Core to win, or fill the board to lose. Either way, earn permanent currency to spend on the Relic Tree before starting the next run.
