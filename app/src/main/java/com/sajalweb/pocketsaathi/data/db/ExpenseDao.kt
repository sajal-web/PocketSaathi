package com.sajalweb.pocketsaathi.data.db

import androidx.room.*
import com.sajalweb.pocketsaathi.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    fun getTodayExpenses(startOfDay: Long, endOfDay: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    fun getTodayTotal(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startOfWeek")
    fun getWeekTotal(startOfWeek: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startOfMonth")
    fun getMonthTotal(startOfMonth: Long): Flow<Double?>

    // For weekly chart — one sum per day
    @Query("""
        SELECT strftime('%w', timestamp/1000, 'unixepoch', 'localtime') as dayOfWeek,
               SUM(amount) as total
        FROM expenses
        WHERE timestamp >= :startOfWeek
        GROUP BY dayOfWeek
    """)
    fun getWeeklyBreakdown(startOfWeek: Long): Flow<List<DayTotal>>

    // Average daily spend over last 30 days (for "spent more than usual" insight)
    @Query("SELECT AVG(dailyTotal) FROM (SELECT SUM(amount) as dailyTotal FROM expenses WHERE timestamp >= :since GROUP BY date(timestamp/1000,'unixepoch','localtime'))")
    suspend fun getAvgDailySpend(since: Long): Double?

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentExpenses(limit: Int = 20): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startOfYear")
    fun getYearTotal(startOfYear: Long): Flow<Double?>

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
}

data class DayTotal(val dayOfWeek: String, val total: Double)