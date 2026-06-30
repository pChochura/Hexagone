# Hexagone: The Script System (Builds & Synergies)

## The Mechanics of Drafting (Forcing the Choice)
To ensure runs require intense strategic decision-making rather than just mindless hoarding of buffs, the game uses strict limitations and cursed choices to force the player's hand.

### 1. Limited Memory Slots (RAM)
The player (SPLICER) has only **3 Active Script Slots** for their build. 
*   When completing a node, you are presented with a "Draft Screen" offering 3 randomized Scripts. 
*   If your memory is full, drafting a new Script forces you to **Overwrite** (permanently delete) an existing Script in your inventory. This forces players to constantly pivot their strategy, abandoning early-game crutch Scripts to assemble late-game synergies.
*   **Rerolling:** If the 3 options are unfavorable, you can spend **Data Fragments** to reroll the selection. The cost increases exponentially with each reroll in a single node (e.g., 10, 20, 40, 80 fragments), making reckless rerolling incredibly punishing to your meta-progression economy.

### 2. Glitched Scripts (The Cursed Choice)
During specific events or after defeating Elite nodes, the player is forced to draft a **Glitched Script**. These Scripts offer a massive, game-breaking buff, but are inexorably paired with a severe, permanent debuff (often disabling core mechanics or perks). The player *must* choose one of three Glitched Scripts to proceed.
*   **Hyper-Threading Glitch:** You receive 2 extra Move Vouchers every node, BUT the turn timer decays 50% faster.
*   **Mimic Corruption:** Mimic tiles now spawn automatically on the grid, BUT your Combo Multiplier is permanently locked at x1.
*   **Read-Only Memory:** The Data Sink wirelessly absorbs tiles from anywhere on the board automatically, BUT the 'Move' perk is permanently disabled for the rest of the run.
*   **Data Bleed:** All base merge scores are permanently doubled, BUT you can no longer use the 'Swap' perk, and the board is shrunk by 1 hex.

### 3. The Boot Sequence (Node 1 Exclusive)
At the very beginning of a run, the first Draft Screen sometimes presents a unique, run-defining choice.
*   **State Persistence Protocol (Checkpoint):** Creates a hard save state at the start of every chapter. If you crash (die), you can restart from that chapter instead of the very beginning. **BUT** this script is so massive it consumes **2 Active Script Slots**, severely limiting your ability to build late-game synergies. You must choose it at Node 1 or it never appears again.

---

## The Script Directory

### Consumable Scripts (System Patches)
*Unlike standard scripts that occupy RAM, these are "select and use immediately" items. They do not take up a memory slot. Instead, they apply a permanent, one-time alteration to your game state or other scripts when drafted.*
*   **Overclock Patch:** Permanently increases the multiplier or effectiveness of a currently equipped Active Script (e.g., upgrades the 'Defrag Utility' from deleting 2 tiles to 3).
*   **Voucher Forge:** Instantly upgrades one of your existing Common Vouchers into its Rare equivalent.
*   **Base Value Injector:** Permanently increases the base score calculation of all tiles by a flat +50 points for the remainder of the run.
*   **Grid Expansion (Rare):** Permanently adds 1 extra random playable hex to the outer edge of the grid for the remainder of the run.

### Common Scripts (Routine Executables)
*Provide reliable, passive buffs to core mathematical mechanics and Data Sink interactions.*
*   **Overclock:** Base merge score is increased by a flat 15%.
*   **Predictive Algorithm:** The "Next Tile" HUD expands to show the next *two* upcoming tiles instead of just one, allowing for deeper planning.
*   **Defrag Utility:** At the start of every node, the two lowest-value tiles on the starting board are instantly deleted, cleaning up garbage data.
*   **Data Funnel:** Tiles deposited directly into the Data Sink gain a flat +250 value bonus, making it easier to hit quotas with lower-tier tiles.
*   **Voucher Scavenger:** There is a 5% chance to drop a Common Perk Voucher directly into your bank whenever a chain-merge of 4+ tiles occurs.

### Rare Scripts (Synergy Enablers & Boss Counters)
*Fundamentally alter how specific tiles interact, and provide hard counters to specific Chapter Boss mechanics.*
*   **Antivirus Payload (Counters 'The Replicator'):** Lowers the threshold to destroy Virus tiles. Any merge of Value 10 or higher adjacent to a Virus tile will now trigger a chain-reaction deletion (down from the standard Value 20 requirement).
*   **Decryption Key (Counters 'The Blackout Protocol'):** The highest-value encrypted tile on the board is always forcibly revealed to you, giving you an anchor point in the dark.
*   **Gravitational Anchor (Counters 'The Gravity Well'):** Any tile of Value 15 or higher becomes "Anchored" and is completely immune to the Gravity Well's pull.
*   **Breach Protocol (Counters 'The Iron Firewall'):** Your 'Remove Tile' perk can now be used to directly damage and break the impassable Firewall tiles, saving you from having to build combos against it.
*   **Thermal Bypass (Counters Frozen Tiles):** "Frozen" hazard tiles automatically thaw after 3 turns instead of remaining permanently frozen.
*   **Quota Siphon:** Every 5th tile absorbed by the Data Sink automatically deletes the lowest-value tile currently on the board, rewarding efficient extractions.
*   **Tactical Escalation:** Any merge involving a manually moved tile (via the 'Move' or 'Swap' perks) receives a 3.0x multiplier instead of the standard 1.5x.
*   **Overclocked Hotspots:** At the start of every node, 2 random empty hexes on the grid are permanently marked as "Overclocked." Any merges physically performed on these specific spots yield 3x the standard score and count double toward the Data Sink quota.
*   **Luminous Code:** *(Active - Once per node)* Manually convert any tile into a "Glowing" tile. The Glowing effect is contagious—if it merges with normal tiles, the resulting tile remains Glowing. Any merge involving a Glowing tile yields a massive flat point bonus and increases the final merged value by an additional +1 tier.

### Legendary Scripts (System Overrides)
*Game-breaking ultimate abilities that manipulate the physical grid and core logic. These are often "Active" scripts that the player can trigger via a dedicated UI button once per node or chapter.*

*   **Deep Extraction:** 
    *   *Effect (Active - Once per node):* Instantly teleport the single highest-value tile on your board directly into the Data Sink. Its deposited value is multiplied by 2x. This is the ultimate tool for brute-forcing a massive quota.
*   **Buffer Sectors (Memory Allocation):** 
    *   *Effect (Passive):* Permanently appends an extra row of 3 tiles to the bottom edge of the board for the rest of the run. Tiles cannot spawn here naturally, they must be dragged in. Any merges executed *inside* this buffer yield triple points.
*   **Sandbox Override (Unbounded Mode):** 
    *   *Effect (Active - Once per node):* Dissolve the hard 5x4 board borders into an infinite void. For exactly 3 turns, you can drag tiles anywhere into the empty space to build sprawling combos without space constraints. After 3 turns, the system "Defragments," snapping a new 5x4 boundary around the highest concentration of tiles. Anything left outside is instantly deleted.
*   **Core Fracture (Center Insertion):** 
    *   *Effect (Active - Once per node):* The ultimate panic button. Insert a cross of 5 completely empty hexes directly into the center of the board. This violently pushes all existing tiles outward. Any tiles pushed off the absolute edges of the board are permanently deleted, clearing out garbage data and instantly providing breathing room.
*   **The Omega Protocol:**
    *   *Effect (Active - Once per chapter):* Instantly upgrade every single tile currently on the board by +1 value (e.g., all 8s become 9s, all 15s become 16s). 
*   **Root Access (Admin Privileges):**
    *   *Effect (Passive):* You no longer need to physically push tiles *into* the Data Sink. The Data Sink can now wirelessly absorb tiles from *anywhere* on the board, provided they are destroyed using the "Path Merge" Legendary Perk.

---

## Optimization Nodes (Side Quests)
Occasionally, you will encounter standalone puzzle nodes (Side Quests) on the map. Instead of a standard survival challenge, these present you with a **pre-filled board** and a **strict move limit**. Your goal is to maximize your final score before you run out of moves. No new tiles spawn during these missions; it is a pure "mate-in-3" style logic puzzle.

To succeed and break the high score limits, you can draft specific scripts that *only* activate during these Optimization Nodes. To reward consistency, these scripts permanently **scale in power** every time you successfully complete an Optimization Node.

### Optimization Scripts (Scaling Math Multipliers)
*   **Even Parity Multiplier:** At the end of the mission, the final value of all "Even" numbered tiles is multiplied by **1.5x**. *(Scales: Multiplier increases by +0.5x after each successful mission).*
*   **The Delta Bonus:** At the end of the mission, the mathematical difference between your highest and lowest value tile is multiplied by **1,000** and added to your score. *(Scales: Bonus increases by +300 points after each successful mission).*
*   **Prime Escalation:** Any tile with a Prime Number value (2, 3, 5, 7, 11...) receives a flat **+5,000** point bonus to its final score calculation. *(Scales: Bonus increases by +1,000 points after each successful mission).*
