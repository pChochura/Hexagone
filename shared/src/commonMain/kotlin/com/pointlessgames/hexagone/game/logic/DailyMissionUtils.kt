package com.pointlessgames.hexagone.game.logic

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.number

object DailyMissionUtils {
    fun calculateStreak(completedDates: Set<Long>, today: LocalDate): Int {
        var streak = 0
        val todaySeed = today.year * 10000L + today.month.number * 100L + today.day
        val yesterday = today.minus(1, DateTimeUnit.DAY)
        val yesterdaySeed = yesterday.year * 10000L + yesterday.month.number * 100L + yesterday.day

        var currentDate = if (completedDates.contains(todaySeed)) {
            today
        } else if (completedDates.contains(yesterdaySeed)) {
            yesterday
        } else {
            return 0
        }

        while (true) {
            val seed = currentDate.year * 10000L + currentDate.month.number * 100L + currentDate.day
            if (completedDates.contains(seed)) {
                streak++
                currentDate = currentDate.minus(1, DateTimeUnit.DAY)
            } else {
                break
            }
        }
        return streak
    }
}
