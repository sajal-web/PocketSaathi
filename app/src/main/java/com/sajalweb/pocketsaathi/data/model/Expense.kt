package com.sajalweb.pocketsaathi.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val description: String,
    val category: Category,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)