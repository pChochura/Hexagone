package com.pointlessgames.hexagone.achievements

enum class GameAchievement(
    val id: String,
    val title: String,
    val description: String,
    val androidId: String,
    val iosId: String,
    val isIncremental: Boolean = false
) {
    // --- Phase 1: The Basics ---
    THE_JOURNEY_BEGINS("the_journey_begins", "The Journey Begins", "Finish your first game", "CgkI_the_journey_begins", "grp.the_journey_begins", isIncremental = true),
    FIRST_AID("first_aid", "First Aid", "Use a perk for the first time", "CgkI_first_aid", "grp.first_aid"),
    BEGINNER_LUCK("beginner_luck", "Beginner's Luck", "Reach a score of 1,000", "CgkI_beginner_luck", "grp.beginner_luck"),
    PERK_HUNTER("perk_hunter", "Perk Hunter", "Collect a perk from the board", "CgkI_perk_hunter", "grp.perk_hunter", isIncremental = true),
    CHANCE_TAKEN("chance_taken", "Chance Taken", "Reroll perk rewards for the first time", "CgkI_chance_taken", "grp.chance_taken"),

    // --- Phase 2: Milestones ---
    A_NEW_BEGINNING("a_new_beginning", "A New Beginning", "Reach Level 5", "CgkI_a_new_beginning", "grp.a_new_beginning"),
    HEX_ARCHITECT("hex_architect", "Hex Architect", "Reach a score of 10,000", "CgkI_hex_architect", "grp.hex_architect"),
    SEASONED_PLAYER("seasoned_player", "Seasoned Player", "Reach Level 15", "CgkI_seasoned_player", "grp.seasoned_player"),
    MILLIONAIRE_CLUB("millionaire_club", "The Millionaire Club", "Reach a score of 100,000", "CgkI_millionaire_club", "grp.millionaire_club"),
    HEX_MASTER("hex_master", "Hex Master", "Reach Level 30", "CgkI_hex_master", "grp.hex_master"),
    DEEP_POCKETS("deep_pockets", "Deep Pockets", "Collect 10 perks in a single run", "CgkI_deep_pockets", "grp.deep_pockets"),

    // --- Phase 3: Mastery & Combos ---
    FEELING_THE_SURGE("feeling_the_surge", "Feeling the Surge", "Reach the Surge combo tier (11+)", "CgkI_feeling_the_surge", "grp.feeling_the_surge"),
    MAXIMUM_OVERDRIVE("maximum_overdrive", "Maximum Overdrive", "Reach the Overdrive combo tier (21+)", "CgkI_maximum_overdrive", "grp.maximum_overdrive"),
    ASCENSION("ascension", "Ascension", "Reach the Zenith combo tier (31+)", "CgkI_ascension", "grp.ascension"),
    EFFICIENCY_EXPERT("efficiency_expert", "Efficiency Expert", "Perform 10 merges in a row without spawning new tiles", "CgkI_efficiency_expert", "grp.efficiency_expert"),
    CHAIN_REACTION("chain_reaction", "Chain Reaction", "Trigger a chain merge sequence of 5 or more steps", "CgkI_chain_reaction", "grp.chain_reaction"),
    THE_BIG_ONE("the_big_one", "The Big One", "Create a tile with a value of 15 or higher", "CgkI_the_big_one", "grp.the_big_one"),
    BEYOND_LIMITS("beyond_limits", "Beyond Limits", "Create a tile with a value of 20 or higher", "CgkI_beyond_limits", "grp.beyond_limits"),

    // --- Phase 4: Tactical Prowess ---
    REDEMPTION("redemption", "Redemption", "Trigger the Redemption score bonus", "CgkI_redemption", "grp.redemption"),
    THE_JANITOR("the_janitor", "The Janitor", "Use Remove Tile to raise the bar", "CgkI_the_janitor", "grp.the_janitor"),
    SACRIFICE("sacrifice", "Sacrifice", "Use Remove Tile on your highest-value tile", "CgkI_sacrifice", "grp.sacrifice"),
    ADVANCED_JANITOR("advanced_janitor", "Advanced Janitor", "Raise the bar by using the Increment perk", "CgkI_advanced_janitor", "grp.advanced_janitor"),
    TACTICAL_GENIUS("tactical_genius", "Tactical Genius", "Perform 5 Tactical Merges in a single game", "CgkI_tactical_genius", "grp.tactical_genius"),
    TACTICAL_GENIUS_ELITE("tactical_genius_elite", "Tactical Expert", "A Tactical Merge with a base score over 1,000", "CgkI_tactical_genius_elite", "grp.tactical_genius_elite"),
    MASTER_OF_FATE("master_of_fate", "Master of Fate", "Use a perk to resolve a Stuck state", "CgkI_master_of_fate", "grp.master_of_fate"),
    DOUBLE_VISION("double_vision", "Double Vision", "Duplicate a tile that is already part of a potential merge", "CgkI_double_vision", "grp.double_vision"),
    COMBO_BREAKER("combo_breaker", "Combo Breaker", "Break a combo higher than 7", "CgkI_combo_breaker", "grp.combo_breaker"),

    // --- Phase 5: Spatial Architecture ---
    ARCHITECTS_DREAM("architects_dream", "The Architect’s Dream", "12+ tiles where every tile is part of a potential merge", "CgkI_architects_dream", "grp.architects_dream"),
    TRIPLE_THREAT("triple_threat", "Triple Threat", "Trigger 3 Bar Raised bonuses in a single turn", "CgkI_triple_threat", "grp.triple_threat"),
    SNAKE_CHARMER("snake_charmer", "Snake Charmer", "Perform a Path Merge with 10+ connected tiles", "CgkI_snake_charmer", "grp.snake_charmer"),
    THE_WHOLE_GANG("the_whole_gang", "The Whole Gang", "Merge all 6 neighbors with the same value", "CgkI_the_whole_gang", "grp.the_whole_gang"),
    RING_OF_FIRE("ring_of_fire", "The Ring of Fire", "Surround an empty cell with 6 tiles of the same value", "CgkI_ring_of_fire", "grp.ring_of_fire"),
    GREAT_WALL("great_wall", "The Great Wall", "Fill a vertical column with tiles of the same value", "CgkI_great_wall", "grp.great_wall"),
    TWIN_PEAKS("twin_peaks", "Twin Peaks", "Have two max-value tiles at opposite corners", "CgkI_twin_peaks", "grp.twin_peaks"),
    THE_PRISM("the_prism", "The Prism", "Have a sequence of 7 different tile values on the board", "CgkI_the_prism", "grp.the_prism"),

    // --- Phase 6: Purity & Restraint ---
    PACIFIST("pacifist", "Pacifist", "Reach Level 5 without ever triggering a combo", "CgkI_pacifist", "grp.pacifist"),
    ASCETIC("ascetic", "Ascetic", "Reach Level 15 without ever using a Perk", "CgkI_ascetic", "grp.ascetic"),
    ZEN_MASTER("zen_master", "Zen Master", "Reach Level 20 without ever using Undo", "CgkI_zen_master", "grp.zen_master"),

    // --- Phase 7: Ghost Mastery ---
    PHANTOM_MOVE("phantom_move", "Phantom Move", "Use Move on a ghost tile", "CgkI_phantom_move", "grp.phantom_move"),
    SPECTRAL_ECHO("spectral_echo", "Spectral Echo", "Use Duplicate on a ghost tile", "CgkI_spectral_echo", "grp.spectral_echo"),
    CLEAN_SWEEP("clean_sweep", "Clean Sweep", "Use Remove Tile on a ghost tile", "CgkI_clean_sweep", "grp.clean_sweep"),
    POLTERGEIST("poltergeist", "Poltergeist", "Swap a ghost tile with a solid tile", "CgkI_poltergeist", "grp.poltergeist"),
    GHOSTLY_ENHANCEMENT("ghostly_enhancement", "Ghostly Enhancement", "Use Increment on a ghost tile", "CgkI_ghostly_enhancement", "grp.ghostly_enhancement"),
    QUADRUPLETS("quadruplets", "Quadruplets", "Have 4 ghost tiles of the same value on the board", "CgkI_quadruplets", "grp.quadruplets"),
    THE_MEDIUM("the_medium", "The Medium", "Move a ghost into the center of a Ring of Fire", "CgkI_the_medium", "grp.the_medium"),
    POSSESSION("possession", "Possession", "Move a ghost tile onto an on-board perk", "CgkI_possession", "grp.possession"),
    GHOST_PROTOCOL("ghost_protocol", "Ghost Protocol", "Mark 3 ghost tiles as Tactical in one turn", "CgkI_ghost_protocol", "grp.ghost_protocol"),
    SOLID_GROUND("solid_ground", "Solid Ground", "Reach Level 30 without manipulating ghosts with perks", "CgkI_solid_ground", "grp.solid_ground"),

    // --- Phase 8: Miscellaneous ---
    CLEANSE("cleanse", "Cleanse", "Delete your last solid tile from the board", "CgkI_cleanse", "grp.cleanse"),
    TIME_MACHINE("time_machine", "Time Machine", "Use Undo 3 times in a row", "CgkI_time_machine", "grp.time_machine"),
    LIVING_ON_THE_EDGE("living_on_the_edge", "Living on the Edge", "Perform a merge while the board has only 1 empty space", "CgkI_living_on_the_edge", "grp.living_on_the_edge"),
    CALCULATED_RISK("calculated_risk", "Calculated Risk", "Skip a spawn when the board is nearly full", "CgkI_calculated_risk", "grp.calculated_risk"),
    HIGH_ROLLER("high_roller", "High Roller", "Reroll a legendary perk reward", "CgkI_high_roller", "grp.high_roller"),
    MISSED_OPPORTUNITY("missed_perk", "Missed Opportunity", "Miss a perk collection from the board", "CgkI_missed_perk", "grp.missed_perk"),

    // --- Phase 8: Long Term Grind ---
    MARATHON("marathon", "Marathon", "Perform a total of 1,000 merges", "CgkI_marathon", "grp.marathon", isIncremental = true),
    GAMBLER("gambler", "Gambler", "Reroll perk rewards 15 times", "CgkI_gambler", "grp.gambler", isIncremental = true),
    PERK_COLLECTOR("perk_collector", "Perk Collector", "Use every type of perk at least once", "CgkI_perk_collector", "grp.perk_collector");
}
