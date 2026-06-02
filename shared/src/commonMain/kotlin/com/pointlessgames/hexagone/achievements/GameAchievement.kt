package com.pointlessgames.hexagone.achievements

enum class GameAchievement(
    val id: String,
    val androidId: String,
    val iosId: String,
    val isIncremental: Boolean = false
) {
    // Special Challenges
    CLEANSE("cleanse", "CgkI_cleanse", "grp.cleanse"),
    THE_WHOLE_GANG("the_whole_gang", "CgkI_the_whole_gang", "grp.the_whole_gang"),
    TIME_MACHINE("time_machine", "CgkI_time_machine", "grp.time_machine"),

    // Milestone Achievements
    BEGINNER_LUCK("beginner_luck", "CgkI_beginner_luck", "grp.beginner_luck"),
    HEX_ARCHITECT("hex_architect", "CgkI_hex_architect", "grp.hex_architect"),
    MILLIONAIRE_CLUB("millionaire_club", "CgkI_millionaire_club", "grp.millionaire_club"),
    A_NEW_BEGINNING("a_new_beginning", "CgkI_a_new_beginning", "grp.a_new_beginning"),
    SEASONED_PLAYER("seasoned_player", "CgkI_seasoned_player", "grp.seasoned_player"),
    HEX_MASTER("hex_master", "CgkI_hex_master", "grp.hex_master"),
    DEEP_POCKETS("deep_pockets", "CgkI_deep_pockets", "grp.deep_pockets"),

    // Mastery & Combo
    FEELING_THE_SURGE("feeling_the_surge", "CgkI_feeling_the_surge", "grp.feeling_the_surge"),
    MAXIMUM_OVERDRIVE("maximum_overdrive", "CgkI_maximum_overdrive", "grp.maximum_overdrive"),
    ASCENSION("ascension", "CgkI_ascension", "grp.ascension"),
    CHAIN_REACTION("chain_reaction", "CgkI_chain_reaction", "grp.chain_reaction"),
    THE_BIG_ONE("the_big_one", "CgkI_the_big_one", "grp.the_big_one"),
    BEYOND_LIMITS("beyond_limits", "CgkI_beyond_limits", "grp.beyond_limits"),
    EFFICIENCY_EXPERT("efficiency_expert", "CgkI_efficiency_expert", "grp.efficiency_expert"),

    // Tactical & Perk
    FIRST_AID("first_aid", "CgkI_first_aid", "grp.first_aid"),
    THE_JANITOR("the_janitor", "CgkI_the_janitor", "grp.the_janitor"),
    SACRIFICE("sacrifice", "CgkI_sacrifice", "grp.sacrifice"),
    TACTICAL_GENIUS("tactical_genius", "CgkI_tactical_genius", "grp.tactical_genius"),
    MASTER_OF_FATE("master_of_fate", "CgkI_master_of_fate", "grp.master_of_fate"),
    PERK_COLLECTOR("perk_collector", "CgkI_perk_collector", "grp.perk_collector"),
    REDEMPTION("redemption", "CgkI_redemption", "grp.redemption"),

    // Playstyle & Hidden
    LIVING_ON_THE_EDGE("living_on_the_edge", "CgkI_living_on_the_edge", "grp.living_on_the_edge"),
    CALCULATED_RISK("calculated_risk", "CgkI_calculated_risk", "grp.calculated_risk"),
    DOUBLE_VISION("double_vision", "CgkI_double_vision", "grp.double_vision"),
    MARATHON("marathon", "CgkI_marathon", "grp.marathon", isIncremental = true),

    // New Achievements
    PERK_HUNTER("perk_hunter", "CgkI_perk_hunter", "grp.perk_hunter", isIncremental = true),
    COMBO_BREAKER("combo_breaker", "CgkI_combo_breaker", "grp.combo_breaker"),
    THE_JOURNEY_BEGINS("the_journey_begins", "CgkI_the_journey_begins", "grp.the_journey_begins", isIncremental = true);
}
