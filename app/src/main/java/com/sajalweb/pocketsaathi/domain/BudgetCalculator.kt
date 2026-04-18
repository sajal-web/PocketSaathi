package com.sajalweb.pocketsaathi.domain

import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetCalculator @Inject constructor() {

    fun getDailyBudget(monthlyIncome: Double): Double {
        val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        // Reserve 30% for savings/rent
        val spendable = monthlyIncome * 0.70
        return spendable / daysInMonth
    }

    fun getRemainingToday(dailyBudget: Double, todaySpent: Double): Double {
        return (dailyBudget - todaySpent).coerceAtLeast(0.0)
    }

    fun getBudgetPercentUsed(dailyBudget: Double, todaySpent: Double): Float {
        if (dailyBudget <= 0) return 0f
        return (todaySpent / dailyBudget).toFloat().coerceIn(0f, 1f)
    }
}