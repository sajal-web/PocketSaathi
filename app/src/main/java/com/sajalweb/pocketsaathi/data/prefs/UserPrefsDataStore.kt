package com.sajalweb.pocketsaathi.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sajalweb.pocketsaathi.data.model.ReportType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPrefsDataStore(private val context: Context) {

    companion object {
        val MONTHLY_LIMIT = doublePreferencesKey("monthly_limit")
        val BUDGET_PERCENTAGE = intPreferencesKey("budget_percentage")
        val SETUP_DONE = booleanPreferencesKey("setup_done")
        val SELECTED_REPORT_TYPE = stringPreferencesKey("selected_report_type")
        val BUDGET_START_DATE = longPreferencesKey("budget_start_date")
        val BUDGET_END_DATE = longPreferencesKey("budget_end_date")
    }

    val monthlyLimit: Flow<Double> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[MONTHLY_LIMIT] ?: 0.0 }

    val budgetPercentage: Flow<Int> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[BUDGET_PERCENTAGE] ?: 70 }

    val isSetupDone: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[SETUP_DONE] ?: false }

    suspend fun saveBudgetConfig(
        monthlyLimit: Double,
        percentage: Int,
        startDate: Long,
        endDate: Long
    ) {
        context.dataStore.edit {
            it[MONTHLY_LIMIT] = monthlyLimit
            it[BUDGET_PERCENTAGE] = percentage
            it[SETUP_DONE] = true
            it[BUDGET_START_DATE] = startDate
            it[BUDGET_END_DATE] = endDate
        }
    }

    val selectedReportType: Flow<ReportType> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            when (prefs[SELECTED_REPORT_TYPE]) {
                "MONTHLY" -> ReportType.MONTHLY
                "YEARLY"  -> ReportType.YEARLY
                else      -> ReportType.WEEKLY
            }
        }

    suspend fun saveReportType(type: ReportType) {
        context.dataStore.edit { it[SELECTED_REPORT_TYPE] = type.name }
    }
    suspend fun resetAll() {
        context.dataStore.edit { it.clear() }
    }

    val budgetStartDate: Flow<Long> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[BUDGET_START_DATE] ?: 0L }

    val budgetEndDate: Flow<Long> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[BUDGET_END_DATE] ?: 0L }
    }