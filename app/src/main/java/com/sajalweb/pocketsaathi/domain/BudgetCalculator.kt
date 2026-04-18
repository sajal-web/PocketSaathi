package com.sajalweb.pocketsaathi.domain

import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetCalculator @Inject constructor() {

    fun getDailyBudget(monthlyLimit: Double, percentage: Int): Double {
        val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        val effectiveMonthly = monthlyLimit * (percentage / 100.0)
        return effectiveMonthly / daysInMonth
    }

    fun getRemainingToday(dailyBudget: Double, todaySpent: Double): Double {
        return (dailyBudget - todaySpent).coerceAtLeast(0.0)
    }

    fun getBudgetPercentUsed(dailyBudget: Double, todaySpent: Double): Float {
        if (dailyBudget <= 0) return 0f
        return (todaySpent / dailyBudget).toFloat().coerceIn(0f, 1f)
    }
}