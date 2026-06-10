package com.pointlessgames.hexagone.game.logic

import com.pointlessgames.hexagone.game.model.ChallengeGoal
import com.pointlessgames.hexagone.game.model.DailyChallenge
import com.pointlessgames.hexagone.game.model.Perk
import kotlinx.datetime.LocalDate
import kotlin.random.Random

object DailyChallengeProvider {
    fun getChallengesForDate(date: LocalDate, streak: Int = 0): List<DailyChallenge> {
        val seed = date.year * 10000L + (date.month.ordinal + 1) * 100L + date.day
        val random = Random(seed)

        val challenges = mutableListOf<DailyChallenge>()

        // Mission 1: Fundamental (Merges or Level or Score)
        val goal1 = listOf(
            ChallengeGoal.MERGE_COUNT,
            ChallengeGoal.LEVEL_REACHED,
            ChallengeGoal.SCORE_REACHED,
        ).random(random)
        challenges.add(createChallenge("m1", goal1, random, streak, difficulty = 1))

        // Mission 2: Skill/Tactics (Tactical Merges, Piece Value, Elite Sacrifice, Moves w/o perk)
        val goal2 = listOf(
            ChallengeGoal.TACTICAL_MERGES,
            ChallengeGoal.PIECE_VALUE_REACHED,
            ChallengeGoal.ELITE_SACRIFICE,
            ChallengeGoal.MOVES_WITHOUT_PERK,
            ChallengeGoal.LEGENDARY_GAMBLE,
        ).random(random)
        challenges.add(createChallenge("m2", goal2, random, streak, difficulty = 2))

        // Mission 3: Mastery (Pattern, Combo Maintenance, Ghost Horde, Diversity Streak, Frugal Survivor, Path Merge, Perk Restriction)
        val goal3 = listOf(
            ChallengeGoal.GEOMETRIC_PATTERN,
            ChallengeGoal.COMBO_MAINTENANCE,
            ChallengeGoal.GHOST_HORDE,
            ChallengeGoal.DIVERSITY_STREAK,
            ChallengeGoal.FRUGAL_SURVIVOR,
            ChallengeGoal.PATH_MERGE_COUNT,
            ChallengeGoal.PERK_RESTRICTED_LEVEL,
            ChallengeGoal.FROZEN_RECOVERY,
        ).random(random)
        challenges.add(createChallenge("m3", goal3, random, streak, difficulty = 3))

        return challenges
    }

    private fun createChallenge(
        id: String,
        goal: ChallengeGoal,
        random: Random,
        streak: Int,
        difficulty: Int,
    ): DailyChallenge {
        var restrictedPerk: Perk? = null
        var patternId: String? = null

        val target = when (goal) {
            ChallengeGoal.MERGE_COUNT -> random.nextInt(15, 31)
            ChallengeGoal.LEVEL_REACHED -> random.nextInt(5, 10)
            ChallengeGoal.SCORE_REACHED -> random.nextInt(2, 6) * 2500

            ChallengeGoal.TACTICAL_MERGES -> random.nextInt(3, 7)
            ChallengeGoal.PIECE_VALUE_REACHED -> random.nextInt(10, 15)
            ChallengeGoal.ELITE_SACRIFICE -> 1
            ChallengeGoal.MOVES_WITHOUT_PERK -> random.nextInt(15, 26)
            ChallengeGoal.LEGENDARY_GAMBLE -> 1

            ChallengeGoal.GEOMETRIC_PATTERN -> {
                patternId =
                    listOf("ring_of_fire", "great_wall", "twin_peaks", "the_prism").random(random)
                1
            }

            ChallengeGoal.COMBO_MAINTENANCE -> random.nextInt(5, 11) // Maintain x5+ for N turns
            ChallengeGoal.GHOST_HORDE -> random.nextInt(4, 5) // N ghosts of same value
            ChallengeGoal.PATH_MERGE_COUNT -> random.nextInt(4, 7) // Clear N tiles with Path Merge
            ChallengeGoal.DIVERSITY_STREAK -> 1 // 1-7 sequence
            ChallengeGoal.FRUGAL_SURVIVOR -> random.nextInt(5, 9) // Reach Level N with 8 perks
            ChallengeGoal.PERK_RESTRICTED_LEVEL -> {
                restrictedPerk = Perk.entries.filter { !it.isLegendary }.random(random)
                random.nextInt(8, 13)
            }

            ChallengeGoal.FROZEN_RECOVERY -> 1
            ChallengeGoal.COMBO_REACHED -> random.nextInt(5, 12)
        }

        // Rewards: 60% Score, 40% Perk
        val hasPerkReward = random.nextFloat() < 0.4f

        val baseScore = when (difficulty) {
            1 -> random.nextInt(2, 5) * 50 // 100-200
            2 -> random.nextInt(4, 9) * 50 // 200-400
            else -> random.nextInt(8, 17) * 50 // 400-800
        }
        val streakMultiplier = when (difficulty) {
            1 -> 25
            2 -> 50
            else -> 100
        }

        val rewardScore = if (!hasPerkReward) baseScore + (minOf(streak, 25) * streakMultiplier) else 0

        val rewardPerk = if (hasPerkReward) {
            val perks = Perk.entries.filter { perk ->
                // Don't reward the same perk you are restricted from using
                perk != restrictedPerk
            }
            perks.random(random)
        } else null

        return DailyChallenge(
            id = id,
            goal = goal,
            target = target,
            rewardScore = rewardScore,
            rewardPerk = rewardPerk,
            restrictedPerk = restrictedPerk,
            patternId = patternId,
        )
    }
}
