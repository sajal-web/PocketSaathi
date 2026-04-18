package com.sajalweb.pocketsaathi.data.repository

import com.sajalweb.pocketsaathi.data.db.DayTotal
import com.sajalweb.pocketsaathi.data.db.ExpenseDao
import com.sajalweb.pocketsaathi.data.model.Expense
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val dao: ExpenseDao
) {
    fun getAllExpenses(): Flow<List<Expense>> = dao.getAllExpenses()
    fun getRecentExpenses(): Flow<List<Expense>> = dao.getRecentExpenses()

    fun getTodayExpenses(): Flow<List<Expense>> {
        val (start, end) = todayRange()
        return dao.getTodayExpenses(start, end)
    }

    fun getTodayTotal(): Flow<Double?> {
        val (start, end) = todayRange()
        return dao.getTodayTotal(start, end)
    }

    fun getWeekTotal(): Flow<Double?> = dao.getWeekTotal(startOfWeek())
    fun getMonthTotal(): Flow<Double?> = dao.getMonthTotal(startOfMonth())
    fun getWeeklyBreakdown(): Flow<List<DayTotal>> = dao.getWeeklyBreakdown(startOfWeek())

    suspend fun getAvgDailySpend(): Double? {
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        return dao.getAvgDailySpend(thirtyDaysAgo)
    }

    suspend fun addExpense(expense: Expense) = dao.insert(expense)
    suspend fun updateExpense(expense: Expense) = dao.update(expense)
    suspend fun deleteExpense(expense: Expense) = dao.delete(expense)

    private fun todayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        return start to (start + 86_400_000L)
    }

    private fun startOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun startOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}