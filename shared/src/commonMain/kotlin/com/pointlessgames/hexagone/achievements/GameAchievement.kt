package com.pointlessgames.hexagone.achievements

import hexagone.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class AchievementCategory(val title: StringResource) {
    BASICS(Res.string.achievement_category_basics),
    MILESTONES(Res.string.achievement_category_milestones),
    MASTERY(Res.string.achievement_category_mastery),
    TACTICAL(Res.string.achievement_category_tactical),
    SPATIAL(Res.string.achievement_category_spatial),
    RESTRAINT(Res.string.achievement_category_restraint),
    GHOSTS(Res.string.achievement_category_ghosts),
    MISC(Res.string.achievement_category_misc),
    GRIND(Res.string.achievement_category_grind)
}

enum class GameAchievement(
    val id: String,
    val title: StringResource,
    val description: StringResource,
    val category: AchievementCategory,
    val androidId: String,
    val iosId: String,
    val isIncremental: Boolean = false
) {
    // --- BASICS ---
    THE_JOURNEY_BEGINS("the_journey_begins", Res.string.achievement_the_journey_begins_title, Res.string.achievement_the_journey_begins_desc, AchievementCategory.BASICS, "CgkI_the_journey_begins", "grp.the_journey_begins", isIncremental = true),
    FIRST_AID("first_aid", Res.string.achievement_first_aid_title, Res.string.achievement_first_aid_desc, AchievementCategory.BASICS, "CgkI_first_aid", "grp.first_aid"),
    BEGINNER_LUCK("beginner_luck", Res.string.achievement_beginner_luck_title, Res.string.achievement_beginner_luck_desc, AchievementCategory.BASICS, "CgkI_beginner_luck", "grp.beginner_luck"),
    PERK_HUNTER("perk_hunter", Res.string.achievement_perk_hunter_title, Res.string.achievement_perk_hunter_desc, AchievementCategory.BASICS, "CgkI_perk_hunter", "grp.perk_hunter", isIncremental = true),
    CHANCE_TAKEN("chance_taken", Res.string.achievement_chance_taken_title, Res.string.achievement_chance_taken_desc, AchievementCategory.BASICS, "CgkI_chance_taken", "grp.chance_taken"),

    // --- MILESTONES ---
    A_NEW_BEGINNING("a_new_beginning", Res.string.achievement_a_new_beginning_title, Res.string.achievement_a_new_beginning_desc, AchievementCategory.MILESTONES, "CgkI_a_new_beginning", "grp.a_new_beginning"),
    HEX_ARCHITECT("hex_architect", Res.string.achievement_hex_architect_title, Res.string.achievement_hex_architect_desc, AchievementCategory.MILESTONES, "CgkI_hex_architect", "grp.hex_architect"),
    SEASONED_PLAYER("seasoned_player", Res.string.achievement_seasoned_player_title, Res.string.achievement_seasoned_player_desc, AchievementCategory.MILESTONES, "CgkI_seasoned_player", "grp.seasoned_player"),
    MILLIONAIRE_CLUB("millionaire_club", Res.string.achievement_millionaire_club_title, Res.string.achievement_millionaire_club_desc, AchievementCategory.MILESTONES, "CgkI_millionaire_club", "grp.millionaire_club"),
    HEX_MASTER("hex_master", Res.string.achievement_hex_master_title, Res.string.achievement_hex_master_desc, AchievementCategory.MILESTONES, "CgkI_hex_master", "grp.hex_master"),
    DEEP_POCKETS("deep_pockets", Res.string.achievement_deep_pockets_title, Res.string.achievement_deep_pockets_desc, AchievementCategory.MILESTONES, "CgkI_deep_pockets", "grp.deep_pockets"),

    // --- MASTERY ---
    FEELING_THE_SURGE("feeling_the_surge", Res.string.achievement_feeling_the_surge_title, Res.string.achievement_feeling_the_surge_desc, AchievementCategory.MASTERY, "CgkI_feeling_the_surge", "grp.feeling_the_surge"),
    MAXIMUM_OVERDRIVE("maximum_overdrive", Res.string.achievement_maximum_overdrive_title, Res.string.achievement_maximum_overdrive_desc, AchievementCategory.MASTERY, "CgkI_maximum_overdrive", "grp.maximum_overdrive"),
    ASCENSION("ascension", Res.string.achievement_ascension_title, Res.string.achievement_ascension_desc, AchievementCategory.MASTERY, "CgkI_ascension", "grp.ascension"),
    EFFICIENCY_EXPERT("efficiency_expert", Res.string.achievement_efficiency_expert_title, Res.string.achievement_efficiency_expert_desc, AchievementCategory.MASTERY, "CgkI_efficiency_expert", "grp.efficiency_expert"),
    CHAIN_REACTION("chain_reaction", Res.string.achievement_chain_reaction_title, Res.string.achievement_chain_reaction_desc, AchievementCategory.MASTERY, "CgkI_chain_reaction", "grp.chain_reaction"),
    THE_BIG_ONE("the_big_one", Res.string.achievement_the_big_one_title, Res.string.achievement_the_big_one_desc, AchievementCategory.MASTERY, "CgkI_the_big_one", "grp.the_big_one"),
    BEYOND_LIMITS("beyond_limits", Res.string.achievement_beyond_limits_title, Res.string.achievement_beyond_limits_desc, AchievementCategory.MASTERY, "CgkI_beyond_limits", "grp.beyond_limits"),

    // --- TACTICAL ---
    REDEMPTION("redemption", Res.string.achievement_redemption_title, Res.string.achievement_redemption_desc, AchievementCategory.TACTICAL, "CgkI_redemption", "grp.redemption"),
    THE_JANITOR("the_janitor", Res.string.achievement_the_janitor_title, Res.string.achievement_the_janitor_desc, AchievementCategory.TACTICAL, "CgkI_the_janitor", "grp.the_janitor"),
    SACRIFICE("sacrifice", Res.string.achievement_sacrifice_title, Res.string.achievement_sacrifice_desc, AchievementCategory.TACTICAL, "CgkI_sacrifice", "grp.sacrifice"),
    ADVANCED_JANITOR("advanced_janitor", Res.string.achievement_advanced_janitor_title, Res.string.achievement_advanced_janitor_desc, AchievementCategory.TACTICAL, "CgkI_advanced_janitor", "grp.advanced_janitor"),
    ALL_AROUND("all_around", Res.string.achievement_all_around_title, Res.string.achievement_all_around_desc, AchievementCategory.TACTICAL, "CgkI_all_around", "grp.all_around"),
    TACTICAL_GENIUS("tactical_genius", Res.string.achievement_tactical_genius_title, Res.string.achievement_tactical_genius_desc, AchievementCategory.TACTICAL, "CgkI_tactical_genius", "grp.tactical_genius"),
    TACTICAL_GENIUS_ELITE("tactical_genius_elite", Res.string.achievement_tactical_genius_elite_title, Res.string.achievement_tactical_genius_elite_desc, AchievementCategory.TACTICAL, "CgkI_tactical_genius_elite", "grp.tactical_genius_elite"),
    MASTER_OF_FATE("master_of_fate", Res.string.achievement_master_of_fate_title, Res.string.achievement_master_of_fate_desc, AchievementCategory.TACTICAL, "CgkI_master_of_fate", "grp.master_of_fate"),
    DOUBLE_VISION("double_vision", Res.string.achievement_double_vision_title, Res.string.achievement_double_vision_desc, AchievementCategory.TACTICAL, "CgkI_double_vision", "grp.double_vision"),
    COMBO_BREAKER("combo_breaker", Res.string.achievement_combo_breaker_title, Res.string.achievement_combo_breaker_desc, AchievementCategory.TACTICAL, "CgkI_combo_breaker", "grp.combo_breaker"),

    // --- SPATIAL ---
    ARCHITECTS_DREAM("architects_dream", Res.string.achievement_architects_dream_title, Res.string.achievement_architects_dream_desc, AchievementCategory.SPATIAL, "CgkI_architects_dream", "grp.architects_dream"),
    TRIPLE_THREAT("triple_threat", Res.string.achievement_triple_threat_title, Res.string.achievement_triple_threat_desc, AchievementCategory.SPATIAL, "CgkI_triple_threat", "grp.triple_threat"),
    SNAKE_CHARMER("snake_charmer", Res.string.achievement_snake_charmer_title, Res.string.achievement_snake_charmer_desc, AchievementCategory.SPATIAL, "CgkI_snake_charmer", "grp.snake_charmer"),
    THE_WHOLE_GANG("the_whole_gang", Res.string.achievement_the_whole_gang_title, Res.string.achievement_the_whole_gang_desc, AchievementCategory.SPATIAL, "CgkI_the_whole_gang", "grp.the_whole_gang"),
    RING_OF_FIRE("ring_of_fire", Res.string.achievement_ring_of_fire_title, Res.string.achievement_ring_of_fire_desc, AchievementCategory.SPATIAL, "CgkI_ring_of_fire", "grp.ring_of_fire"),
    GREAT_WALL("great_wall", Res.string.achievement_great_wall_title, Res.string.achievement_great_wall_desc, AchievementCategory.SPATIAL, "CgkI_great_wall", "grp.great_wall"),
    TWIN_PEAKS("twin_peaks", Res.string.achievement_twin_peaks_title, Res.string.achievement_twin_peaks_desc, AchievementCategory.SPATIAL, "CgkI_twin_peaks", "grp.twin_peaks"),
    THE_PRISM("the_prism", Res.string.achievement_the_prism_title, Res.string.achievement_the_prism_desc, AchievementCategory.SPATIAL, "CgkI_the_prism", "grp.the_prism"),

    // --- RESTRAINT ---
    PACIFIST("pacifist", Res.string.achievement_pacifist_title, Res.string.achievement_pacifist_desc, AchievementCategory.RESTRAINT, "CgkI_pacifist", "grp.pacifist"),
    ASCETIC("ascetic", Res.string.achievement_ascetic_title, Res.string.achievement_ascetic_desc, AchievementCategory.RESTRAINT, "CgkI_ascetic", "grp.ascetic"),
    ZEN_MASTER("zen_master", Res.string.achievement_zen_master_title, Res.string.achievement_zen_master_desc, AchievementCategory.RESTRAINT, "CgkI_zen_master", "grp.zen_master"),

    // --- GHOSTS ---
    PHANTOM_MOVE("phantom_move", Res.string.achievement_phantom_move_title, Res.string.achievement_phantom_move_desc, AchievementCategory.GHOSTS, "CgkI_phantom_move", "grp.phantom_move"),
    SPECTRAL_ECHO("spectral_echo", Res.string.achievement_spectral_echo_title, Res.string.achievement_spectral_echo_desc, AchievementCategory.GHOSTS, "CgkI_spectral_echo", "grp.spectral_echo"),
    CLEAN_SWEEP("clean_sweep", Res.string.achievement_clean_sweep_title, Res.string.achievement_clean_sweep_desc, AchievementCategory.GHOSTS, "CgkI_clean_sweep", "grp.clean_sweep"),
    POLTERGEIST("poltergeist", Res.string.achievement_poltergeist_title, Res.string.achievement_poltergeist_desc, AchievementCategory.GHOSTS, "CgkI_poltergeist", "grp.poltergeist"),
    GHOSTLY_ENHANCEMENT("ghostly_enhancement", Res.string.achievement_ghostly_enhancement_title, Res.string.achievement_ghostly_enhancement_desc, AchievementCategory.GHOSTS, "CgkI_ghostly_enhancement", "grp.ghostly_enhancement"),
    QUADRUPLETS("quadruplets", Res.string.achievement_quadruplets_title, Res.string.achievement_quadruplets_desc, AchievementCategory.GHOSTS, "CgkI_quadruplets", "grp.quadruplets"),
    THE_MEDIUM("the_medium", Res.string.achievement_the_medium_title, Res.string.achievement_the_medium_desc, AchievementCategory.GHOSTS, "CgkI_the_medium", "grp.the_medium"),
    POSSESSION("possession", Res.string.achievement_possession_title, Res.string.achievement_possession_desc, AchievementCategory.GHOSTS, "CgkI_possession", "grp.possession"),
    GHOST_PROTOCOL("ghost_protocol", Res.string.achievement_ghost_protocol_title, Res.string.achievement_ghost_protocol_desc, AchievementCategory.GHOSTS, "CgkI_ghost_protocol", "grp.ghost_protocol"),
    SOLID_GROUND("solid_ground", Res.string.achievement_solid_ground_title, Res.string.achievement_solid_ground_desc, AchievementCategory.GHOSTS, "CgkI_solid_ground", "grp.solid_ground"),

    // --- MISC ---
    CLEANSE("cleanse", Res.string.achievement_cleanse_title, Res.string.achievement_cleanse_desc, AchievementCategory.MISC, "CgkI_cleanse", "grp.cleanse"),
    TIME_MACHINE("time_machine", Res.string.achievement_time_machine_title, Res.string.achievement_time_machine_desc, AchievementCategory.MISC, "CgkI_time_machine", "grp.time_machine"),
    LIVING_ON_THE_EDGE("living_on_the_edge", Res.string.achievement_living_on_the_edge_title, Res.string.achievement_living_on_the_edge_desc, AchievementCategory.MISC, "CgkI_living_on_the_edge", "grp.living_on_the_edge"),
    CALCULATED_RISK("calculated_risk", Res.string.achievement_calculated_risk_title, Res.string.achievement_calculated_risk_desc, AchievementCategory.MISC, "CgkI_calculated_risk", "grp.calculated_risk"),
    HIGH_ROLLER("high_roller", Res.string.achievement_high_roller_title, Res.string.achievement_high_roller_desc, AchievementCategory.MISC, "CgkI_high_roller", "grp.high_roller"),
    MISSED_OPPORTUNITY("missed_perk", Res.string.achievement_missed_perk_title, Res.string.achievement_missed_perk_desc, AchievementCategory.MISC, "CgkI_missed_perk", "grp.missed_perk"),

    // --- GRIND ---
    MARATHON("marathon", Res.string.achievement_marathon_title, Res.string.achievement_marathon_desc, AchievementCategory.GRIND, "CgkI_marathon", "grp.marathon", isIncremental = true),
    GAMBLER("gambler", Res.string.achievement_gambler_title, Res.string.achievement_gambler_desc, AchievementCategory.GRIND, "CgkI_gambler", "grp.gambler", isIncremental = true),
    PERK_COLLECTOR("perk_collector", Res.string.achievement_perk_collector_title, Res.string.achievement_perk_collector_desc, AchievementCategory.GRIND, "CgkI_perk_collector", "grp.perk_collector");
}

