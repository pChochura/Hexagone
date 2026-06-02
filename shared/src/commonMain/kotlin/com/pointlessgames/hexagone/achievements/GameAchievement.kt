package com.pointlessgames.hexagone.achievements

enum class GameAchievement(
    val id: String,
    val title: String,
    val description: String,
    val androidId: String,
    val iosId: String,
    val isIncremental: Boolean = false
) {
    // Special Challenges
    CLEANSE("cleanse", "Cleanse", "Delete your last solid tile from the board", "CgkI_cleanse", "grp.cleanse"),
    THE_WHOLE_GANG("the_whole_gang", "The Whole Gang", "Merge all 6 neighbors with the same value", "CgkI_the_whole_gang", "grp.the_whole_gang"),
    TIME_MACHINE("time_machine", "Time Machine", "Use Undo 3 times in a row", "CgkI_time_machine", "grp.time_machine"),

    // Milestone Achievements
    BEGINNER_LUCK("beginner_luck", "Beginner's Luck", "Reach a score of 1,000", "CgkI_beginner_luck", "grp.beginner_luck"),
    HEX_ARCHITECT("hex_architect", "Hex Architect", "Reach a score of 10,000", "CgkI_hex_architect", "grp.hex_architect"),
    MILLIONAIRE_CLUB("millionaire_club", "The Millionaire Club", "Reach a score of 100,000", "CgkI_millionaire_club", "grp.millionaire_club"),
    A_NEW_BEGINNING("a_new_beginning", "A New Beginning", "Reach Level 5", "CgkI_a_new_beginning", "grp.a_new_beginning"),
    SEASONED_PLAYER("seasoned_player", "Seasoned Player", "Reach Level 15", "CgkI_seasoned_player", "grp.seasoned_player"),
    HEX_MASTER("hex_master", "Hex Master", "Reach Level 30", "CgkI_hex_master", "grp.hex_master"),
    DEEP_POCKETS("deep_pockets", "Deep Pockets", "Collect 10 perks in a single run", "CgkI_deep_pockets", "grp.deep_pockets"),

    // Mastery & Combo
    FEELING_THE_SURGE("feeling_the_surge", "Feeling the Surge", "Reach the Surge combo tier (11+)", "CgkI_feeling_the_surge", "grp.feeling_the_surge"),
    MAXIMUM_OVERDRIVE("maximum_overdrive", "Maximum Overdrive", "Reach the Overdrive combo tier (21+)", "CgkI_maximum_overdrive", "grp.maximum_overdrive"),
    ASCENSION("ascension", "Ascension", "Reach the Zenith combo tier (31+)", "CgkI_ascension", "grp.ascension"),
    CHAIN_REACTION("chain_reaction", "Chain Reaction", "Trigger a chain merge sequence of 5 or more steps", "CgkI_chain_reaction", "grp.chain_reaction"),
    THE_BIG_ONE("the_big_one", "The Big One", "Create a tile with a value of 15 or higher", "CgkI_the_big_one", "grp.the_big_one"),
    BEYOND_LIMITS("beyond_limits", "Beyond Limits", "Create a tile with a value of 20 or higher", "CgkI_beyond_limits", "grp.beyond_limits"),
    EFFICIENCY_EXPERT("efficiency_expert", "Efficiency Expert", "Perform 10 merges in a row without spawning new tiles", "CgkI_efficiency_expert", "grp.efficiency_expert"),

    // Tactical & Perk
    FIRST_AID("first_aid", "First Aid", "Use a perk for the first time", "CgkI_first_aid", "grp.first_aid"),
    THE_JANITOR("the_janitor", "The Janitor", "Use Remove Tile to raise the bar", "CgkI_the_janitor", "grp.the_janitor"),
    SACRIFICE("sacrifice", "Sacrifice", "Use Remove Tile on your highest-value tile", "CgkI_sacrifice", "grp.sacrifice"),
    TACTICAL_GENIUS("tactical_genius", "Tactical Genius", "Perform 5 Tactical Merges in a single game", "CgkI_tactical_genius", "grp.tactical_genius"),
    MASTER_OF_FATE("master_of_fate", "Master of Fate", "Use a perk to resolve a Stuck state", "CgkI_master_of_fate", "grp.master_of_fate"),
    PERK_COLLECTOR("perk_collector", "Perk Collector", "Use every type of perk at least once", "CgkI_perk_collector", "grp.perk_collector"),
    REDEMPTION("redemption", "Redemption", "Trigger the Redemption score bonus", "CgkI_redemption", "grp.redemption"),

    // Playstyle & Hidden
    LIVING_ON_THE_EDGE("living_on_the_edge", "Living on the Edge", "Perform a merge while the board has only 1 empty space", "CgkI_living_on_the_edge", "grp.living_on_the_edge"),
    CALCULATED_RISK("calculated_risk", "Calculated Risk", "Skip a spawn when the board is nearly full", "CgkI_calculated_risk", "grp.calculated_risk"),
    DOUBLE_VISION("double_vision", "Double Vision", "Duplicate a tile that is already part of a potential merge", "CgkI_double_vision", "grp.double_vision"),
    MARATHON("marathon", "Marathon", "Perform a total of 1,000 merges", "CgkI_marathon", "grp.marathon", isIncremental = true),

    // New Achievements
    PERK_HUNTER("perk_hunter", "Perk Hunter", "Collect a perk from the board", "CgkI_perk_hunter", "grp.perk_hunter", isIncremental = true),
    COMBO_BREAKER("combo_breaker", "Combo Breaker", "Break a combo higher than 7", "CgkI_combo_breaker", "grp.combo_breaker"),
    THE_JOURNEY_BEGINS("the_journey_begins", "The Journey Begins", "Finish your first game", "CgkI_the_journey_begins", "grp.the_journey_begins", isIncremental = true);
}
