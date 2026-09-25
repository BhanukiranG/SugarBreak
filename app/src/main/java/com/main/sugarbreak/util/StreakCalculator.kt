package com.main.sugarbreak.util

import com.main.sugarbreak.domain.model.CheckInStatus
import com.main.sugarbreak.domain.model.DailyCheckIn
import com.main.sugarbreak.domain.model.StreakSummary
import java.time.LocalDate

object StreakCalculator {
    fun calculate(checkIns: List<DailyCheckIn>, referenceDate: LocalDate = LocalDate.now()): StreakSummary {
        val sortedCheckIns = checkIns.sortedBy { it.date }
        
        var currentStreak = 0
        var bestStreak = 0
        var successfulDays = 0
        var slipDays = 0
        
        var tempCurrent = 0
        var lastDate: LocalDate? = null

        for (checkIn in sortedCheckIns) {
            when (checkIn.status) {
                CheckInStatus.SUCCESS -> successfulDays++
                CheckInStatus.SLIP -> slipDays++
                else -> {}
            }

            // Check if there's a missing day gap that breaks the streak
            if (lastDate != null) {
                val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastDate, checkIn.date)
                if (daysBetween > 1L) {
                    // A missing day breaks the streak
                    tempCurrent = 0
                }
            }

            when (checkIn.status) {
                CheckInStatus.SUCCESS -> {
                    tempCurrent++
                    if (tempCurrent > bestStreak) {
                        bestStreak = tempCurrent
                    }
                }
                CheckInStatus.SLIP, CheckInStatus.SKIPPED -> {
                    tempCurrent = 0
                }
                CheckInStatus.PENDING -> {
                    // Pending does not add to streak or break it directly in the historical pass
                }
            }
            lastDate = checkIn.date
        }

        // Calculate current streak from today going backwards
        val checkInMap = sortedCheckIns.associateBy { it.date }
        currentStreak = 0
        
        // Start checking from today (or yesterday if today is pending/missing)
        var dateCursor = referenceDate
        val todayCheckIn = checkInMap[dateCursor]
        
        if (todayCheckIn == null || todayCheckIn.status == CheckInStatus.PENDING) {
            // We give them grace for today, so we check yesterday
            dateCursor = dateCursor.minusDays(1)
        }

        // Count consecutively backwards
        while (true) {
            val checkIn = checkInMap[dateCursor]
            if (checkIn != null && checkIn.status == CheckInStatus.SUCCESS) {
                currentStreak++
                dateCursor = dateCursor.minusDays(1)
            } else {
                break
            }
        }

        val totalResolved = successfulDays + slipDays
        val successRate = if (totalResolved > 0) {
            successfulDays.toFloat() / totalResolved.toFloat()
        } else {
            0f
        }

        return StreakSummary(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            successfulDays = successfulDays,
            slipDays = slipDays,
            successRate = successRate
        )
    }
}
