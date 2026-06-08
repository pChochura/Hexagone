package com.pointlessgames.hexagone.game.logic

import com.pointlessgames.hexagone.game.model.ChallengeGoal
import com.pointlessgames.hexagone.game.model.DailyChallenge
import com.pointlessgames.hexagone.game.model.Perk
import kotlinx.datetime.LocalDate
import kotlin.random.Random

object DailyChallengeProvider {
    fun getChallengesForDate(date: LocalDate): List<DailyChallenge> {
        val seed = date.year * 10000L + (date.month.ordinal + 1) * 100L + date.dayOfMonth
        val random = Random(seed)

        val challenges = mutableListOf<DailyChallenge>()
        
        // Mission 1: Fundamental (Merges or Level)
        val goal1 = if (random.nextBoolean()) ChallengeGoal.MERGE_COUNT else ChallengeGoal.LEVEL_REACHED
        challenges.add(createChallenge("m1", goal1, random))

        // Mission 2: Skill/Tactics (Tactical Merges or Piece Value)
        val goal2 = if (random.nextBoolean()) ChallengeGoal.TACTICAL_MERGES else ChallengeGoal.PIECE_VALUE_REACHED
        challenges.add(createChallenge("m2", goal2, random))

        // Mission 3: Performance (Combo or Score)
        val goal3 = if (random.nextBoolean()) ChallengeGoal.COMBO_REACHED else ChallengeGoal.SCORE_REACHED
        challenges.add(createChallenge("m3", goal3, random))

        return challenges
    }

    private fun createChallenge(id: String, goal: ChallengeGoal, random: Random): DailyChallenge {
        val target = when (goal) {
            ChallengeGoal.MERGE_COUNT -> random.nextInt(15, 31) // 15-30 merges
            ChallengeGoal.LEVEL_REACHED -> random.nextInt(5, 10)   // Level 5-9
            ChallengeGoal.COMBO_REACHED -> random.nextInt(5, 12)  // Combo 5-11
            ChallengeGoal.SCORE_REACHED -> random.nextInt(2, 6) * 2500 // 5000-12500 score
            ChallengeGoal.TACTICAL_MERGES -> random.nextInt(3, 7) // 3-6 tactical merges
            ChallengeGoal.PIECE_VALUE_REACHED -> random.nextInt(10, 15) // Value 10-14
        }

        // Rewards: 70% Score, 30% Perk
        val hasPerkReward = random.nextFloat() < 0.3f
        val rewardScore = if (!hasPerkReward) {
            (random.nextInt(5, 16) * 100) // 500-1500 score
        } else 0
        
        val rewardPerk = if (hasPerkReward) {
            val commonPerks = Perk.entries.filter { !it.isLegendary }
            commonPerks.random(random)
        } else null

        return DailyChallenge(
            id = id,
            goal = goal,
            target = target,
            rewardScore = rewardScore,
            rewardPerk = rewardPerk
        )
    }
}
