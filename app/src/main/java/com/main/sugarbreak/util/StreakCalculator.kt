package com.main.sugarbreak.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.CheckInStatus
import com.main.sugarbreak.domain.model.DailyCheckIn
import com.main.sugarbreak.domain.model.StreakSummary
import java.time.LocalDate

object StreakCalculator {
    @RequiresApi(Build.VERSION_CODES.O)
    fun calculate(
        checkIns: List<DailyCheckIn>, 
        referenceDate: LocalDate = LocalDate.now(),
        challengeBehavior: ChallengeBehavior = ChallengeBehavior.RESET_STREAK
    ): StreakSummary {
        val sortedCheckIns = checkIns.sortedBy { it.date }
        
        var currentStreak = 0
        var bestStreak = 0
        var successfulDays = 0
        var slipDays = 0
        
        var tempCurrent = 0
        var lastDate: LocalDate? = null
        var consecutiveSlips = 0

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
                    consecutiveSlips = 0
                }
            }

            when (checkIn.status) {
                CheckInStatus.SUCCESS -> {
                    consecutiveSlips = 0
                    tempCurrent++
                    if (tempCurrent > bestStreak) {
                        bestStreak = tempCurrent
                    }
                }
                CheckInStatus.SLIP -> {
                    consecutiveSlips++
                    when (challengeBehavior) {
                        ChallengeBehavior.CONTINUE -> {
                            tempCurrent++
                            if (tempCurrent > bestStreak) {
                                bestStreak = tempCurrent
                            }
                        }
                        ChallengeBehavior.ADD_RECOVERY_DAY -> {
                            if (consecutiveSlips > 1) {
                                tempCurrent = 0
                            }
                            // Else Pauses streak, doesn't increment or reset.
                        }
                        ChallengeBehavior.RESET_STREAK -> {
                            tempCurrent = 0
                        }
                    }
                }
                CheckInStatus.SKIPPED -> {
                    tempCurrent = 0
                    consecutiveSlips = 0
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
        var consecutiveSlipsBackward = 0
        while (true) {
            val checkIn = checkInMap[dateCursor]
            if (checkIn != null) {
                if (checkIn.status == CheckInStatus.SUCCESS) {
                    consecutiveSlipsBackward = 0
                    currentStreak++
                    dateCursor = dateCursor.minusDays(1)
                } else if (checkIn.status == CheckInStatus.SLIP) {
                    consecutiveSlipsBackward++
                    when (challengeBehavior) {
                        ChallengeBehavior.CONTINUE -> {
                            currentStreak++
                            dateCursor = dateCursor.minusDays(1)
                        }
                        ChallengeBehavior.ADD_RECOVERY_DAY -> {
                            if (consecutiveSlipsBackward > 1) {
                                break
                            } else {
                                dateCursor = dateCursor.minusDays(1)
                            }
                        }
                        ChallengeBehavior.RESET_STREAK -> {
                            break
                        }
                    }
                } else if (checkIn.status == CheckInStatus.SKIPPED) {
                    break
                } else {
                    break // PENDING
                }
            } else {
                break // Missing record breaks streak
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
