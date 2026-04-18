package com.sajalweb.pocketsaathi.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPrefsDataStore(private val context: Context) {

    companion object {
        val MONTHLY_INCOME = doublePreferencesKey("monthly_income")
        val SETUP_DONE = booleanPreferencesKey("setup_done")
    }

    val monthlyIncome: Flow<Double> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[MONTHLY_INCOME] ?: 0.0 }

    val isSetupDone: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[SETUP_DONE] ?: false }

    suspend fun saveMonthlyIncome(income: Double) {
        context.dataStore.edit {
            it[MONTHLY_INCOME] = income
            it[SETUP_DONE] = true
        }
    }
}