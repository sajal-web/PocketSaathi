package com.sajalweb.pocketsaathi.domain

import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetCalculator @Inject constructor() {

    fun getDailyBudget(
        monthlyLimit: Double,
        percentage: Int,
        startDate: Long,
        endDate: Long
    ): Double {
        val effectiveMonthly = monthlyLimit * (percentage / 100.0)
        val daysInPeriod = getDaysBetween(startDate, endDate).coerceAtLeast(1)
        return effectiveMonthly / daysInPeriod
    }

    // Fallback for calls without dates (default to current month days)
    fun getDailyBudget(monthlyLimit: Double, percentage: Int): Double {
        val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        val effectiveMonthly = monthlyLimit * (percentage / 100.0)
        return effectiveMonthly / daysInMonth
    }

    private fun getDaysBetween(start: Long, end: Long): Int {
        if (start == 0L || end == 0L) {
            // If no dates stored, default to remaining days in current month
            val cal = Calendar.getInstance()
            val today = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            val endOfMonth = cal.timeInMillis
            val diff = endOfMonth - today
            return (TimeUnit.MILLISECONDS.toDays(diff) + 1).toInt()
        }
        val diff = end - start
        return (TimeUnit.MILLISECONDS.toDays(diff) + 1).toInt()
    }

    fun getRemainingToday(dailyBudget: Double, todaySpent: Double): Double {
        return (dailyBudget - todaySpent).coerceAtLeast(0.0)
    }

    fun getBudgetPercentUsed(dailyBudget: Double, todaySpent: Double): Float {
        if (dailyBudget <= 0) return 0f
        return (todaySpent / dailyBudget).toFloat().coerceIn(0f, 1f)
    }
}